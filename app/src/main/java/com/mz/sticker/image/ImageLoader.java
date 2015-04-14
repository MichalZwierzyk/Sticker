package com.mz.sticker.image;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import com.mz.sticker.drawable.AsyncDrawable;
import com.mz.sticker.task.LoadBitmapTask;

public abstract class ImageLoader {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void loadBitmap(String imageFileName, int imageWidth, int imageHeight, boolean centerImageInside, ImageView imageView) {
        if (cancelPotentialWork(imageFileName, imageView)) {
            final LoadBitmapTask task = new LoadBitmapTask(imageView, imageFileName, imageWidth, imageHeight, centerImageInside);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(imageView.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else {
                task.execute();
            }
        }
    }

    private static boolean cancelPotentialWork(String imageFileName, ImageView imageView) {
        final LoadBitmapTask loadBitmapTask = getLoadBitmapTask(imageView);

        if(loadBitmapTask != null) {
            String fileName = loadBitmapTask.getImageFileName();
            // If bitmapData is not yet set or it differs from the new data
            if((fileName == null) || !TextUtils.equals(fileName, imageFileName)) {
                // Cancel previous task
                loadBitmapTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    public static LoadBitmapTask getLoadBitmapTask(ImageView imageView) {
        if(imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getLoadBitmapTask();
            }
        }
        return null;
    }

}
