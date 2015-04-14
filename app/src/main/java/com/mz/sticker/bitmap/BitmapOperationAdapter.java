package com.mz.sticker.bitmap;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;

public class BitmapOperationAdapter implements BitmapOperationCallback {

    @Override
    public void onSquareBitmapToCircleBitmap(Bitmap circleBitmap) { }

    @Override
    public void onBitmapToByteArray(ArrayList<byte[]> byteArrays) { }

    @Override
    public void onByteArrayToBitmap(ArrayList<Bitmap> bitmaps) { }

    @Override
    public void onNamedBitmapToNamedByteArray(HashMap<String, byte[]> namedByteArrays) { }

    @Override
    public void onNamedByteArrayToNamedBitmap(HashMap<String, Bitmap> namedBitmaps) { }

    @Override
    public void onDecodeSampledBitmap(Bitmap sampledBitmap) { }

    @Override
    public void onScaleBitmapPreserveAspectRatio(Bitmap scaledBitmap) { }

    @Override
    public void onScaleBitmap(Bitmap scaledBitmap) { }

}
