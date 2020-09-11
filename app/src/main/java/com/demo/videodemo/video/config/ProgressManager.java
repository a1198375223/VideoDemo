package com.demo.videodemo.video.config;

public abstract class ProgressManager {
    public abstract void saveProgress(String url, long progress);

    public abstract long getSavedProgress(String url);
}
