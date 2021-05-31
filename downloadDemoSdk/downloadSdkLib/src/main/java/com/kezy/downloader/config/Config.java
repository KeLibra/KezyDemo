package com.kezy.downloader.config;

import java.util.concurrent.ExecutorService;

import okhttp3.OkHttpClient;

/**
 * com.kezy.downloader.config
 * Time: 2019/4/12 9:55
 * Description:
 */
public class Config {
    public ExecutorService threadPool;

    public String targetDir;

    public OkHttpClient client;
}
