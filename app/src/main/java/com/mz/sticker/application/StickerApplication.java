package com.mz.sticker.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mz.sticker.exception.AppUncaughtExceptionHandler;

public class StickerApplication extends Application {

    private static StickerApplication application;
    private static Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        Thread.currentThread().setUncaughtExceptionHandler(new AppUncaughtExceptionHandler(getApplicationInfo().dataDir));
    }

    public static Context getAppContext() {
        return application.getApplicationContext();
    }

    public static void postOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static void postOnUiThreadWithDelay(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

}

