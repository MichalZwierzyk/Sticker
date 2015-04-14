package com.mz.sticker.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.mz.sticker.R;
import com.mz.sticker.application.StickerApplication;
import com.mz.sticker.bitmap.BitmapUtil;
import com.mz.sticker.util.CursorUtil;
import com.mz.sticker.toast.ToastManager;

/**
 * NOTE: If you use intent to start Camera Activity without passing MediaStore.EXTRA_OUTPUT parameter you will get not null data
 * parameter in Activity.onActivityResult method from which you can extract thumbnail of the image.
 * If you use MediaStore.EXTRA_OUTPUT parameter you will get null data parameter in Activity.onActivityResult} method.
 */
public class CameraUtil {

    public static final int CAPTURE_MEDIA_ACTIVITY_REQUEST_CODE = 100;
    public enum CameraFacing { FRONT, BACK };
    public enum MediaType { MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO, MEDIA_TYPE_AUDIO };

    private static final String TAG = "CameraUtil";
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String VIDEO_EXTENSION = ".mp4";
    private static final String IMAGE_PREFIX = "IMG_";
    private static final String VIDEO_PREFIX = "VID_";
    private static final String IMAGES_DIRECTORY = "StickerAppImages";

    private static Uri lastCapturedMediaUri;
    private static long cameraAppInvokeTime;

    /**
     * @param activity Activity which will receive operation callback in Activity.onActivityResult
     * @return Path to file in which image was saved or null if something went wrong
     */
    public static Uri captureImage(Activity activity) {
        cameraAppInvokeTime = System.currentTimeMillis();
        lastCapturedMediaUri = captureMediaContent(activity, MediaType.MEDIA_TYPE_IMAGE, null, null, null);
        return lastCapturedMediaUri;
    }

    /**
     * @param activity Activity which will receive operation callback in Activity.onActivityResult
     * @return Path to file in which video was saved or null if something went wrong
     */
    public static Uri captureVideo(Activity activity, Float videoQuality, Integer videoDuration, Integer videoSize) {
        cameraAppInvokeTime = System.currentTimeMillis();
        lastCapturedMediaUri = captureMediaContent(activity, MediaType.MEDIA_TYPE_VIDEO, videoQuality, videoDuration, videoSize);
        return lastCapturedMediaUri;
    }

    private static Uri captureMediaContent(Activity activity, MediaType mediaType, Float videoQuality, Integer videoDuration,
                                           Integer videoSize) {
        PackageManager packageManager = activity.getPackageManager();
        if(getNumberOfCameras(activity) == 0) {
            ToastManager.showToast(activity.getString(R.string.device_has_no_camera), Toast.LENGTH_LONG);
            return null;
        }
        Intent captureMediaContentIntent = new Intent((mediaType == MediaType.MEDIA_TYPE_IMAGE) ? MediaStore.ACTION_IMAGE_CAPTURE :
                MediaStore.ACTION_VIDEO_CAPTURE);
        ComponentName componentName;
        if((componentName = captureMediaContentIntent.resolveActivity(packageManager)) == null) {
            ToastManager.showToast(activity.getString(R.string.device_has_no_camera_app), Toast.LENGTH_LONG);
            return null;
        }
        else {
            captureMediaContentIntent.setComponent(componentName);
            Uri mediaContentUri = getOutputMediaFileUri(mediaType, activity);
            if(mediaContentUri != null) {
                captureMediaContentIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaContentUri);
                if(videoQuality != null) {
                    captureMediaContentIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality);
                }
                if(videoDuration != null) {
                    captureMediaContentIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoDuration);
                }
                if(videoSize != null) {
                    captureMediaContentIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, videoSize);
                }
                activity.startActivityForResult(captureMediaContentIntent, CAPTURE_MEDIA_ACTIVITY_REQUEST_CODE);
                return mediaContentUri;
            }
            else {
                return null;
            }
        }
    }

    private static Uri getOutputMediaFileUri(MediaType mediaType, Context context) {
        File mediaStorageDir = getOutputMediaDir(context);
        if(mediaStorageDir != null) {
            return getMediaFilePath(mediaStorageDir.getAbsolutePath(), mediaType);
        }
        else {
            return null;
        }
    }

    public static File getOutputMediaDir(Context context) {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return getExternalOutputDir(context);
        }
        else {
            return getInternalOutputDir(context);
        }
    }

    private static File getExternalOutputDir(Context context) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGES_DIRECTORY);
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.e(TAG, String.format("Failed to create directory: %s", mediaStorageDir.getAbsolutePath()));
                return getInternalOutputDir(context);
            }
        }
        return mediaStorageDir;
    }

    private static File getInternalOutputDir(Context context) {
        File mediaStorageDir = new File(context.getFilesDir(), IMAGES_DIRECTORY);
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.e(TAG, String.format("Failed to create directory: %s", mediaStorageDir.getAbsolutePath()));
                ToastManager.showToast(context.getString(R.string.cannot_create_photo_dir), Toast.LENGTH_LONG);
                return null;
            }
        }
        return mediaStorageDir;
    }

    private static Uri getMediaFilePath(String mediaDirectory, MediaType mediaType) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile = null;
        switch(mediaType) {
            case MEDIA_TYPE_IMAGE:
                mediaFile = new File(mediaDirectory + File.separator + IMAGE_PREFIX + timeStamp + IMAGE_EXTENSION);
                break;
            case MEDIA_TYPE_VIDEO:
                mediaFile = new File(mediaDirectory + File.separator + VIDEO_PREFIX + timeStamp + VIDEO_EXTENSION);
                break;
            default:
                break;
        }
        return Uri.fromFile(mediaFile);
    }

    /**
     * @param filename		Path to image file
     * @param creationTime	Time when image file was created
     */
    public static int extractExifOrientationTagFromImageFile(String filename, long creationTime) {
		/* The issue is on some devices, there's a bug that makes the picture taken saved in your app folder without proper 
		   exif tags while a properly rotated image is saved in the android default folder (even though it shouldn't be).
		   Now what we do is recording the time when we're starting the camera app. Then in {@link onActivityResult}, we query
		   the Media Provider to see if any pictures were saved after this timestamp we've saved. That means most likely Android OS 
		   saved the properly rotated picture in the default folder and of course put an entry in the media store and we can use the 
		   rotation information from this row.  */
        int imageRotation = -1;
        long imageFileSize = new File(filename).length();

        Cursor mediaFileCursor = StickerApplication.getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE},
                MediaStore.MediaColumns.DATE_ADDED + " >= ?", new String[]{String.valueOf(creationTime / 1000 - 1)},
                MediaStore.MediaColumns.DATE_ADDED + " DESC");
        if((mediaFileCursor != null) && (mediaFileCursor.getCount() != 0)) {
            while(mediaFileCursor.moveToNext()) {
                long mediaFileSize = CursorUtil.getLongFromCursor(mediaFileCursor, MediaStore.MediaColumns.SIZE);
                // Extra check to make sure that we are getting the orientation from the proper file
                if(mediaFileSize == imageFileSize) {
                    imageRotation = CursorUtil.getIntFromCursor(mediaFileCursor, MediaStore.Images.ImageColumns.ORIENTATION);
                    break;
                }
            }
        }
	    
	    /* Now if the rotation at this point is still -1, then that means this is one of the devices with proper rotation information. */
        if(imageRotation == -1) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filename);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            imageRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        }

        return imageRotation;
    }

    public static void loadLastCapturedImageFromFile(final int imageWidth, final int imageHeight, final CameraOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap loadedBitmap = loadImageFromFile(lastCapturedMediaUri.getPath(), imageWidth, imageHeight, true, cameraAppInvokeTime);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoadCapturedImageFromFile(loadedBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap loadImageFromFile(String imageFile, int imageWidth, int imageHeight, boolean centerInside, long cameraAppInvokeTime) {
        Bitmap bitmap = BitmapUtil.decodeSampledBitmapFromFile(imageFile, imageWidth, imageHeight, Config.ARGB_8888);
        if(bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            float maxSize;
            if (centerInside) {
                maxSize = (imageWidth > imageHeight) ? imageWidth : imageHeight;
            } else {
                float scale = 1.0f;
                int maxDimension = Math.max(imageWidth, imageHeight);
                while (((bitmapWidth * scale) >= maxDimension) && ((bitmapHeight * scale) >= maxDimension)) {
                    scale -= 0.05f;
                }
                maxSize = (bitmapWidth > bitmapHeight) ? bitmapWidth * (scale + 0.05f) : bitmapHeight * (scale + 0.05f);
            }
            Bitmap scaledBitmap = BitmapUtil.scaleBitmapPreserveAspectRatio(bitmap, maxSize);
            bitmap.recycle();
            int imageRotation = extractExifOrientationTagFromImageFile(imageFile, cameraAppInvokeTime);
            Matrix matrix = new Matrix();
            switch (imageRotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                case 90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                case 180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                case 270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
            if(scaledBitmap.isRecycled()) {
                return null;
            }
            else {
                final Bitmap loadedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                scaledBitmap.recycle();
                return loadedBitmap;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Get number of cameras attached to device.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getNumberOfCameras(Context context) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // On API level 17 and above we can use packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) to check
            // if device has camera
            return android.hardware.Camera.getNumberOfCameras();
        }
        else {
            CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                return cm.getCameraIdList().length;
            }
            catch (CameraAccessException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    /**
     * The orientation of the camera image. The value is the angle that the camera image needs to be rotated clockwise so it shows
     * correctly on the display in its natural orientation. It should be 0, 90, 180, or 270.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    public static Integer getCameraOrientation(Context context, CameraFacing cameraFacing) {
        int numberOfCameras = getNumberOfCameras(context);
        if(numberOfCameras == 0) {
            Log.e(TAG, "Device has no camera");
            return null;
        }
        Integer cameraOrientation = null;
        int facing;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
            facing = (cameraFacing == CameraFacing.FRONT) ?
                    android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT :
                    android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
            for(int cameraId = 0; cameraId < numberOfCameras; ++cameraId) {
                android.hardware.Camera.getCameraInfo(cameraId, cameraInfo);
                if(facing == cameraInfo.facing) {
                    cameraOrientation = cameraInfo.orientation;
                    break;
                }
            }
        }
        else {
            CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            facing = (cameraFacing == CameraFacing.FRONT) ? CameraCharacteristics.LENS_FACING_FRONT :
                    CameraCharacteristics.LENS_FACING_BACK;
            for(int cameraId = 0; cameraId < numberOfCameras; ++cameraId) {
                try {
                    CameraCharacteristics charact = cm.getCameraCharacteristics(Integer.toString(cameraId));
                    if(facing == charact.get(CameraCharacteristics.LENS_FACING)) {
                        cameraOrientation = charact.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        break;
                    }
                }
                catch (CameraAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        if(cameraOrientation == null) {
            Log.e(TAG, "Device has no specified camera");
        }
        return cameraOrientation;
    }

    public static Uri getLastCapturedMediaUri() {
        return lastCapturedMediaUri;
    }

}
