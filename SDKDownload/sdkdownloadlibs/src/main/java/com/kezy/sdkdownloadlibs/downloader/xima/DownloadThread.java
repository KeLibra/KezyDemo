package com.kezy.sdkdownloadlibs.downloader.xima;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;

import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWNLOAD_ING;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWN_ERROR;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWN_OK;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWN_START;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.HANDLER_PAUSE;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.HANDLER_REMOVE;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.REQUEST_TIME_OUT;



/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadThread extends Thread{

    private DownloadInfo mTask;
    private final WeakReference<DownloadService.UpdateHandler> weakHandler;
    private final WeakReference<Context> weakContext;


    public DownloadThread(Context context, DownloadInfo task, DownloadService.UpdateHandler handler) {
        this.mTask = task;
        weakHandler = new WeakReference<>(handler);;
        weakContext = new WeakReference<>(context);;
    }

    @Override
    public void run() {
        Log.d("mydownload", "start: " + mTask.name + ", @ " + mTask.retryCount);
        Message message = null;
        if (weakHandler == null ||  weakHandler.get() == null) {
            return;
        }
        Handler handler = weakHandler.get();
        try {
            if (!DownloadUtils.checkSdcardMounted()) {
                return;
            }

            long downloadSize = downloadUpdateFile(handler, mTask);

            if (downloadSize == Integer.MAX_VALUE) {
                message = Message.obtain();
                if (mTask.status == DownloadInfo.Status.DELETE) {
                    message.what = HANDLER_REMOVE;
                } else {
                    message.what = HANDLER_PAUSE;
                    mTask.status = DownloadInfo.Status.STOPPED;
                }
                message.obj = mTask;

            } else if (downloadSize > 0) {
                // ????????????
                message = Message.obtain();
                message.what = DOWN_OK;
                mTask.status = DownloadInfo.Status.FINISHED;
                mTask.path = mTask.getFilePath();
                message.obj = mTask;

                Log.e("----------msg", " ------- ???????????? ---- downloadSize " + downloadSize);
            } else {
                mTask.status = DownloadInfo.Status.ERROR;
                message = Message.obtain();
                message.what = DOWN_ERROR;
                message.obj = mTask;
                Log.d("mydownload", "downloadCoutn" + downloadSize);
            }
        } catch (SocketTimeoutException e) {
            message = Message.obtain();
            message.what =  REQUEST_TIME_OUT;
            mTask.status = DownloadInfo.Status.ERROR;
            message.obj = mTask;
        } catch (IOException e) {
            message = Message.obtain();
            message.what = DOWN_ERROR;
            mTask.status = DownloadInfo.Status.ERROR;
            message.obj = mTask;
        } finally {
            Log.d("-----msg mydownload", mTask.retryCount + " --- :finally -- " + (message == null ? "null" : message.what));
            handler.sendMessage(message);
        }
    }


    /***
     * ????????????
     *
     * @throws IOException
     */
    public long downloadUpdateFile(Handler handler,final DownloadInfo task) throws IOException {

        double downloadSpeed;
        long speedTemp = task.tempSize;
        long mUpDateTimerMillis = 0;
        int down_step = 1;// ??????step
        long downloadedLength = 0;// ????????????????????????
        int updateCount = 0;// ?????????

        Log.v("--------msg", "??????task????????? ---  task.isRunning " + task.isRunning);
        if (!task.isRunning) // ??????task?????????
        {
            return Integer.MAX_VALUE;
        }

        task.status = DownloadInfo.Status.DOWNLOADING;

        boolean isRestart = task.tempSize != 0;
        if (task.retryCount == 0) {
            if (handler != null) {
               Message message = Message.obtain();
               message.what = DOWN_START;
               task.status = DownloadInfo.Status.STARTED;
               message.obj = task;
               handler.sendMessage(message);
            }
        }
        Log.v("--------msg v2", " ------ isRestart = " + isRestart);

        long curSize = task.tempSize;
        HttpURLConnection connection = null;
        InputStream in = null;
        RandomAccessFile out = null;
        try {
            URL url = new URL(task.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36");
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Charset", "UTF-8");

            connection.setInstanceFollowRedirects(true);// ?????????????????????
            connection.setConnectTimeout(20000);
            if (task.tempSize > 0) // ???????????????????????????
            {
                // ?????????????????????????????????,???????????????
                if (task.tempSize > 0 && task.tempSize < task.totalSize) {
                    String range = String.format("bytes=%d-%d", curSize, task.totalSize - 1);
                    connection.setRequestProperty("Range", range);
                } else {
                    task.tempSize = 0;
                }
            }
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_PARTIAL) {
                // ??????????????????
                String filelen = connection.getHeaderField("Content-length");
                String filerange = connection.getHeaderField("Content-Range");
                if (!TextUtils.isEmpty(filerange)) {
                    int index = filerange.lastIndexOf('/');
                    filerange = filerange.substring(index + 1);
                    filelen = filerange;
                }

                // ???????????????????????????????????????
                task.retryCount = 0;

                long file_len = 0;
                try {
                    file_len = Long.valueOf(filelen);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (task.tempSize > 0) {
                    if (file_len != task.totalSize) {
                        throw new Exception("file size error!try!");
                    }
                } else {
                    task.totalSize = file_len;
                }
            } else if (HttpURLConnection.HTTP_MOVED_PERM == status || HttpURLConnection.HTTP_SEE_OTHER == status || HttpURLConnection.HTTP_MOVED_TEMP == status) {
                throw new Exception("download url change!");
            } else {
                throw new Exception("network error!");
            }

            // ??????????????????
            if (TextUtils.isEmpty(task.name)) {
                String connUrl = connection.getURL().toString();
                task.name = URLDecoder.decode(connUrl.substring(connUrl.lastIndexOf("/") + 1), "utf-8");
                task.name = task.name.substring(task.name.lastIndexOf("/") + 1);
            }
            // ??????????????????
            if (TextUtils.isEmpty(task.path)) {
                // ??????apk????????????
                String path = getDiskCachePath();
                if (TextUtils.isEmpty(path)) {
                    return 0;
                }

                task.path = path + "/" + DownloadService.DOWNLOAD_APK_PATH;
                Log.d("-------msg", "??????????????????   " + task.path);
            } else if (!new File(task.path).canWrite()) {
                return 0;
            }

            File dlPath = new File(task.path);
            // ?????????????????????????????????????????????
            if (!dlPath.exists() && !dlPath.mkdirs())
                return 0;

            // ??????????????????????????????apk??????
            if (dlPath.isDirectory()) {
                File[] childFiles = dlPath.listFiles();
                if (childFiles != null && childFiles.length > 0) {
                    int size = childFiles.length;
                    for (int i = 0; i < size; i++) {
                        if (task.name.equals(childFiles[i].getName())) {
                            childFiles[i].delete();
                        }
                    }
                }
            }

            File file = getTempDownloadPath(task);
            Log.v("---------msg", " ----- download save   getTempDownloadPath.length()  = " + file.length() + " ---- path = " + file.getAbsolutePath());
            if (file != null && file.length() <= 0) {
                Log.e("---------msg", " ----- download save   ????????????????????? ??????????????????  = ");
                task.tempSize = 0;
                task.progress = 0;
                curSize = 0;
            }
            int nread;
            byte[] buffer = new byte[4096];
            in = new BufferedInputStream(connection.getInputStream());
            out = new RandomAccessFile(file, "rw"); // ??????????????????????????????????????????????????????????????????seek
            out.seek(curSize); // ?????????????????????
            try {
                while (task.isRunning && (nread = in.read(buffer, 0, buffer.length)) > 0) {
                    out.write(buffer, 0, nread);
                    // ????????????????????????
                    curSize += nread;
                    task.tempSize = curSize;

                    long l = 0;
                    if (task.totalSize > 0) {
                        l = curSize * 100 / task.totalSize;
                    }
                    if (updateCount == 0 || (l - down_step) >= updateCount) {
                        updateCount += down_step;
                        if (updateCount > task.progress) {
                            task.progress = updateCount;
//                            handleDownloadProgressUpdate(task.url, updateCount);
                            Log.v("-------msg", "handler = " + handler +" ------ progress = " + updateCount );
                            if (handler != null) {
                                Message message = Message.obtain();
                                message.what = DOWNLOAD_ING;
                                task.status = DownloadInfo.Status.DOWNLOADING;
                                message.obj = task;
                                handler.sendMessage(message);
                            }
                        }
                    }
                    if (System.currentTimeMillis() - mUpDateTimerMillis > 1000) {
                        downloadSpeed = (curSize - speedTemp) * 1000 / (System.currentTimeMillis() - mUpDateTimerMillis);  //??????????????????
                        task.speed = downloadSpeed;         //??????????????????
                        speedTemp = curSize;
                        mUpDateTimerMillis = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                return -1;
            }

            if (!task.isRunning) {
                if (task.status != DownloadInfo.Status.DELETE) {
                    task.status = 0;
                }
                return Integer.MAX_VALUE;
            }
            // ??????????????????
            if (task.totalSize == task.tempSize || task.totalSize == 0) {
                if (!TextUtils.isEmpty(task.name) && (task.name.endsWith(".apk") || task.name.endsWith(".APK"))) {
                    file.renameTo(new File(task.path, task.name));
                } else {
                    file.renameTo(new File(task.path, task.name + ".apk"));
                }
                Log.e("---------msg", " ---- ???????????? file ------ " + file.getPath());
                downloadedLength = task.tempSize;
                task.tempSize = 0;
                task.totalSize = 0;
                task.isRunning = false;
            } else {
                task.tempSize = 0;
                task.totalSize = 0;
                task.isRunning = false;
                File[] childFiles = dlPath.listFiles();
                if (childFiles != null && childFiles.length > 0) {
                    int size = childFiles.length;
                    for (int i = 0; i < size; i++) {
                        String filenames = task.name + ".temp";
                        if (filenames.equals(childFiles[i].getName())) {
                            childFiles[i].delete();
                        }
                    }
                }
                return Integer.MAX_VALUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    // ???????????????
                    in.close();
                }
                if (null != out) {
                    // ???????????????
                    out.close();
                }
                if (null != connection) {
                    // ??????????????????
                    connection.disconnect();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return downloadedLength;
    }

    @Nullable
    public String getDiskCachePath() {
        if (weakContext == null|| weakContext.get() == null) {
            return null;
        }
        Context context = weakContext.get();

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() == null) {
                if (context.getCacheDir() == null) {
                    return null;
                }
                return context.getCacheDir().getPath();
            }
            return context.getExternalCacheDir().getPath();
        } else {
            if (context.getCacheDir() != null) {
                return context.getCacheDir().getPath();
            }
            return null;
        }
    }

    private File getTempDownloadPath(DownloadInfo task) {
        if (task != null) {
            return new File(task.path, task.name + ".temp");
        }
        return null;
    }
}
