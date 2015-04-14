package com.mz.sticker.bitmap;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;

public interface BitmapOperationCallback {
    void onBitmapToByteArray(ArrayList<byte[]> byteArrays);
    void onByteArrayToBitmap(ArrayList<Bitmap> bitmaps);
    void onNamedBitmapToNamedByteArray(HashMap<String, byte[]> namedByteArrays);
    void onNamedByteArrayToNamedBitmap(HashMap<String, Bitmap> namedBitmaps);
    void onSquareBitmapToCircleBitmap(Bitmap circleBitmap);
    void onDecodeSampledBitmap(Bitmap sampledBitmap);
    void onScaleBitmapPreserveAspectRatio(Bitmap scaledBitmap);
    void onScaleBitmap(Bitmap scaledBitmap);
}
