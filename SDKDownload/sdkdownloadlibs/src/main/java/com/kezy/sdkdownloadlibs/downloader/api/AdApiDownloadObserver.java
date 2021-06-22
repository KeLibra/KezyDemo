package com.kezy.sdkdownloadlibs.downloader.api;

import android.app.DownloadManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description ContentObserver监听下载的进度
 */
public class AdApiDownloadObserver extends ContentObserver {

    private final String TAG = "-------msg" + getClass().getCanonicalName();

    private final Handler handler;

    /**
     * 记录成功或者失败的状态，主要用来只发送一次成功或者失败
     */
    private boolean isEnd = false;

    private final DownloadManager downloadManager;

    private final DownloadManager.Query query;

    private long mDownloadId;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public AdApiDownloadObserver(Handler handler, DownloadManager downloadManager, long downloadId) {
        super(handler);
        this.handler = handler;
        this.downloadManager = downloadManager;
        this.mDownloadId = downloadId;
        query = new DownloadManager.Query().setFilterById(downloadId);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        queryDownloadStatus();
    }

    /**
     * 检查下载的状态
     */
    private void queryDownloadStatus() {
        // Java 7 新的 try-with-resources ，凡是实现了AutoCloseable接口的可自动close()，所以此处不需要手动cursor.close()
        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor != null && cursor.moveToNext()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                long totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                long currentSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                // 当前进度
                int mProgress;
                if (totalSize != 0) {
                    mProgress = (int) ((currentSize * 100) / totalSize);
                } else {
                    mProgress = 0;
                }
                Message message = Message.obtain();
                DownloadInfo task = new DownloadInfo("");
                // 下载暂停

                task.taskId = mDownloadId;
                task.progress = mProgress;
                task.totalSize = totalSize;
                task.tempSize = currentSize;


                Log.d(TAG, String.valueOf(mProgress));
                switch (status) {
                    case DownloadManager.STATUS_PAUSED:
                        task.isRunning = false;
                        task.status = EngineImpl.Status.STOPPED;
                        message.obj = task;
                        Log.d(TAG, "STATUS_PAUSED");
                        break;
                    case DownloadManager.STATUS_PENDING:
                        // 开始下载
                        task.isRunning = true;
                        task.status = EngineImpl.Status.STARTED;
                        message.obj = task;
                        Log.d(TAG, "STATUS_PENDING");
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        task.isRunning = true;
                        task.status = EngineImpl.Status.DOWNLOADING;
                        message.obj = task;

                        Log.d(TAG, "STATUS_RUNNING");
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        if (!isEnd) {
                            // 完成
                            task.isRunning = false;
                            task.status = EngineImpl.Status.FINISHED;
                            message.obj = task;
                            Log.d(TAG, "STATUS_SUCCESSFUL");
                        }
                        isEnd = true;
                        break;
                    case DownloadManager.STATUS_FAILED:
                        if (!isEnd) {
                            task.isRunning = false;
                            task.status = EngineImpl.Status.ERROR;
                            message.obj = task;
                            Log.d(TAG, "STATUS_FAILED");
                        }
                        isEnd = true;
                        break;
                    default:
                        break;
                }
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
