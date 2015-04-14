package com.mz.sticker.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.mz.sticker.camera.CameraUtil;
import com.mz.sticker.image.ImageLoader;

import java.lang.ref.WeakReference;

public class LoadBitmapTask extends AsyncTask<Void, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewRef;
    private String imageFileName = null;
    private int imageWidth, imageHeight;
    private boolean centerImageInside;

    public LoadBitmapTask(ImageView imageView, String imageFileName, int imageWidth, int imageHeight, boolean centerImageInside) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewRef = new WeakReference<ImageView>(imageView);
        this.imageFileName = imageFileName;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.centerImageInside = centerImageInside;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    // Decode image in background
    @Override
    protected Bitmap doInBackground(Void... v) {
        return CameraUtil.loadImageFromFile(imageFileName, imageWidth, imageHeight, centerImageInside, 0L);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(isCancelled()) {
            bitmap = null;
        }
        if((imageViewRef != null) && (bitmap != null)) {
            final ImageView imageView = imageViewRef.get();
            final LoadBitmapTask loadBitmapTask = ImageLoader.getLoadBitmapTask(imageView);
            if((this == loadBitmapTask) && (imageView != null) && !bitmap.isRecycled()) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
