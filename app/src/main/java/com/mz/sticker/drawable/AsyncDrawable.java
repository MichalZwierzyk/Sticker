package com.mz.sticker.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.mz.sticker.task.LoadBitmapTask;

import java.lang.ref.WeakReference;

public class AsyncDrawable extends BitmapDrawable {

    private final WeakReference<LoadBitmapTask> loadBitmapTaskRef;

    public AsyncDrawable(Resources res, Bitmap bitmap, LoadBitmapTask loadBitmapTask) {
        super(res, bitmap);
        loadBitmapTaskRef = new WeakReference<LoadBitmapTask>(loadBitmapTask);
    }

    public LoadBitmapTask getLoadBitmapTask() {
        return loadBitmapTaskRef.get();
    }

}
