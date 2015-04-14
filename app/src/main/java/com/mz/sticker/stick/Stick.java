package com.mz.sticker.stick;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.mz.sticker.screen.StickerActivity;

/**
 * Builder for sticker Intents and utils for handling result
 */
public class Stick {

    public static final int REQUEST_STICK = 6800;
    public static final int RESULT_ERROR = 404;

    public interface Extra {
        String ASPECT_X = "aspect_x";
        String ASPECT_Y = "aspect_y";
        String ENTER_FROM_GALLERY = "enter_from_gallery";
        String ERROR = "error";
    }

    private Intent stickIntent;

    /**
     * Create a Stick Intent builder with source image
     *
     * @param source Source image URI
     */
    public Stick(Uri source) {
        stickIntent = new Intent();
        stickIntent.setData(source);
    }

    public Stick asSquare() {
        stickIntent.putExtra(Extra.ASPECT_X, 1);
        stickIntent.putExtra(Extra.ASPECT_Y, 1);
        return this;
    }

    /**
     * Set output URI where the Stickped image will be saved
     *
     * @param output Output image URI
     */
    public Stick output(Uri output) {
        stickIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        return this;
    }

    /**
     * Send the Stick Intent!
     *
     * @param activity Activity to receive result
     * @param enterFromGallery Was previous activity a gallery?
     */
    public void start(Activity activity, boolean enterFromGallery) {
        stickIntent.putExtra(Extra.ENTER_FROM_GALLERY, enterFromGallery);
        start(activity, REQUEST_STICK);
    }

    /**
     * Send the Stick Intent with a custom requestCode
     *
     * @param activity Activity to receive result
     * @param requestCode requestCode for result
     */
    public void start(Activity activity, int requestCode) {
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    /**
     * Send the Stick Intent!
     *
     * @param context Context
     * @param fragment Fragment to receive result
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start(Context context, Fragment fragment) {
        start(context, fragment, REQUEST_STICK);
    }

    /**
     * Send the Stick Intent with a custom requestCode
     *
     * @param context Context
     * @param fragment Fragment to receive result
     * @param requestCode requestCode for result
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void start(Context context, Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getIntent(context), requestCode);
    }

    /**
     * Get Intent to start Stick Activity
     *
     * @param context Context
     * @return Intent for StickImageActivity
     */
    public Intent getIntent(Context context) {
        stickIntent.setClass(context, StickerActivity.class);
        return stickIntent;
    }

    /**
     * Retrieve URI for Stickped image, as set in the Intent builder
     *
     * @param result Output Image URI
     */
    public static Uri getOutput(Intent result) {
        return result.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
    }

    /**
     * Retrieve error that caused Stick to fail
     *
     * @param result Result Intent
     * @return Throwable handled in StickImageActivity
     */
    public static Throwable getError(Intent result) {
        return (Throwable) result.getSerializableExtra(Extra.ERROR);
    }

}

