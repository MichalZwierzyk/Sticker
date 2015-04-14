package com.mz.sticker.camera;

import android.graphics.Bitmap;

public interface CameraOperationCallback {
    void onLoadCapturedImageFromFile(Bitmap bitmap);
}
