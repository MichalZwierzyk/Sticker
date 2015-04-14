package com.mz.sticker.toast;

import com.mz.sticker.application.StickerApplication;

import android.widget.Toast;

public class ToastManager {

    /**
     * @param toastLength Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void showToast(String text, int toastLength) {
        Toast.makeText(StickerApplication.getAppContext(), text, toastLength).show();
    }

    /**
     * @param toastLength Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void showToast(int textResId, int toastLength) {
        Toast.makeText(StickerApplication.getAppContext(), textResId, toastLength).show();
    }

}
