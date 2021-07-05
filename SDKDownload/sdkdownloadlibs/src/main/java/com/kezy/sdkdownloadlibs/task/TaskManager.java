package com.kezy.sdkdownloadlibs.task;

import android.content.Context;
import android.util.Log;

import com.kezy.sdkdownloadlibs.impls.TaskImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Kezy
 * @Time 2021/6/30
 * @Description
 */
public class TaskManager {

    private Context mContext;
    private Map<String, DownloadTask> mKeyTaskMap = new HashMap<>();



    private TaskManager() {
        loadLocalData();
    }

    private static class INSTANCE {
        private static TaskManager instance = new TaskManager();
    }

    public static TaskManager getInstance() {
        return INSTANCE.instance;
    }

    public TaskImpl createDownloadTask(Context context, DownloadInfo info) {
        if (context == null || info == null) {
            return null;
        }
        this.mContext = context.getApplicationContext();
        DownloadTask task = getTaskByOnlyKey(info.onlyKey());
        if (task == null) {
            task = new DownloadTask(context, info);
            updateTask(task);
        }
        return task;
    }


    public void updateTask(DownloadTask task) {
        if (task == null || task.mDownloadInfo == null) {
            return;
        }
        mKeyTaskMap.put(task.createDownloadKey(), task);
        saveLocalData();
    }

    public void removeTask(DownloadTask task) {
        if (task == null || task.mDownloadInfo == null) {
            return;
        }
        mKeyTaskMap.remove(task.createDownloadKey());
    }

    public DownloadTask getTaskByOnlyKey(String onlyKey) {
        for (Map.Entry<String, DownloadTask> entry : mKeyTaskMap.entrySet()) {
            if (entry != null && entry.getKey().endsWith(onlyKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void startTask(Context context, DownloadTask task) {
        if (task != null) {
            task.start(context);
        }
    }

    public void pauseTask(Context context, DownloadTask task) {
        if (task != null) {
            task.pause(context);
        }
    }

    public void deleteTask(Context context, DownloadTask task) {
        if (task != null) {
            task.remove(context);
        }
    }

    private void loadLocalData() {
        Log.e("--------msg", "--------1 load local data ");
    }

    private void saveLocalData() {
        Log.e("--------msg", "--------2 save local data ");
    }


    public void destroy(){
    }
}
