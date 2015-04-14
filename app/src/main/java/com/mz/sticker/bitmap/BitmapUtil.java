package com.mz.sticker.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mz.sticker.application.StickerApplication;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;

public abstract class BitmapUtil {

    public static final int HIGHEST_COMPRESS_QUALITY = 100;
    public static final int LOWEST_COMPRESS_QUALITY = 0;
    public static final int STANDARD_COMPRESS_QUALITY = 85;

    private static final int THREADS_NUMBER = 10;

    public static Bitmap squareBitmapToCircleBitmap(Bitmap sourceBitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawCircle(sourceBitmap.getWidth() / 2, sourceBitmap.getHeight() / 2, sourceBitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        final Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
        canvas.drawBitmap(sourceBitmap, rect, rect, paint);
        return outputBitmap;
    }

    public static void squareBitmapToCircleBitmapInBackground(final Bitmap sourceBitmap, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap circleBitmap = squareBitmapToCircleBitmap(sourceBitmap);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSquareBitmapToCircleBitmap(circleBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap viewBitmap = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);
        return viewBitmap;
    }

    /**
     * @param bitmap Bitmap which will be scaled preserving aspect ratio
     * @param size   Maximal size in pixels of both width and height after scaling
     * @return Scaled bitmap
     */
    public static Bitmap scaleBitmapPreserveAspectRatio(Bitmap bitmap, float size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleFactor = (width > height) ? (size / width) : (size / height);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return scaledBitmap;
    }

    public static void scaleBitmapPreserveAspectRatioInBackground(final Bitmap bitmap, final int size,
                                                                  final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap scaledBitmap = scaleBitmapPreserveAspectRatio(bitmap, size);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onScaleBitmapPreserveAspectRatio(scaledBitmap);
                    }
                });
            }
        }).start();
    }

    public static void scaleBitmapInBackground(final Bitmap bitmap, final int newWidth, final int newHeight,
                                               final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onScaleBitmap(scaledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, resId, null, null, null, null, null, null, null, null);
    }

    public static void decodeSampledBitmapFromResourceInBackground(final int resId, final int reqWidth, final int reqHeight,
                                                                   final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromResource(resId, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int offset, int length, int reqWidth, int reqHeight,
                                                          Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, null, null, data, offset, length, null, null, null, null);
    }

    public static void decodeSampledBitmapFromByteArrayInBackground(final byte[] data, final int offset, final int length,
                                                                    final int reqWidth, final int reqHeight, final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromByteArray(data, offset, length, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromFile(String fileName, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, null, fileName, null, null, null, null, null, null, null);
    }

    public static void decodeSampledBitmapFromFileInBackground(final String fileName, final int reqWidth, final int reqHeight,
                                                               final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromFile(fileName, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, Rect outPadding, int reqWidth, int reqHeight,
                                                               Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, null, null, null, null, null, fd, outPadding, null, null);
    }

    public static void decodeSampledBitmapFromFileDescriptorInBackground(final FileDescriptor fd, final Rect outPadding, final int reqWidth,
                                                                         final int reqHeight, final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromFileDescriptor(fd, outPadding, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromResourceStream(TypedValue value, InputStream is, Rect pad, int reqWidth, int reqHeight,
                                                               Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, null, null, null, null, null, null, pad, value, is);
    }

    public static void decodeSampledBitmapFromResourceStreamInBackground(final TypedValue value, final InputStream is, final Rect pad,
                                                                         final int reqWidth, final int reqHeight, final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromResourceStream(value, is, pad, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream is, Rect outPadding, int reqWidth, int reqHeight,
                                                       Bitmap.Config bitmapConfig) {
        return decodeSampledBitmap(reqWidth, reqHeight, bitmapConfig, null, null, null, null, null, null, outPadding, null, is);
    }

    public static void decodeSampledBitmapFromStreamInBackground(final InputStream is, final Rect outPadding, final int reqWidth,
                                                                 final int reqHeight, final Bitmap.Config bitmapConfig, final BitmapOperationCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap sampledBitmap = decodeSampledBitmapFromStream(is, outPadding, reqWidth, reqHeight, bitmapConfig);
                StickerApplication.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDecodeSampledBitmap(sampledBitmap);
                    }
                });
            }
        }).start();
    }

    private static Bitmap decodeSampledBitmap(int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, Integer resId, String fileName,
                                              byte[] data, Integer offset, Integer length, FileDescriptor fd, Rect rect, TypedValue value, InputStream is) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeBitmap(options, resId, fileName, data, offset, length, fd, rect, value, is);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = bitmapConfig;
        return decodeBitmap(options, resId, fileName, data, offset, length, fd, rect, value, is);
    }

    private static Bitmap decodeBitmap(BitmapFactory.Options options, Integer resId, String fileName, byte[] data, Integer offset,
                                       Integer length, FileDescriptor fd, Rect rect, TypedValue value, InputStream is) {
        Resources res = StickerApplication.getAppContext().getResources();
        Bitmap sampledBitmap = null;
        if(resId != null) {
            sampledBitmap = BitmapFactory.decodeResource(res, resId, options);
        }
        else if(fileName != null) {
            sampledBitmap = BitmapFactory.decodeFile(fileName, options);
        }
        else if(data != null) {
            sampledBitmap = BitmapFactory.decodeByteArray(data, offset, length, options);
        }
        else if(fd != null) {
            sampledBitmap = BitmapFactory.decodeFileDescriptor(fd, rect, options);
        }
        else if(value != null) {
            sampledBitmap = BitmapFactory.decodeResourceStream(res, value, is, rect, options);
        }
        else if(is != null) {
            sampledBitmap = BitmapFactory.decodeStream(is, rect, options);
        }
        return sampledBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while(((halfHeight / inSampleSize) > reqHeight) && ((halfWidth / inSampleSize) > reqWidth)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning compress for max quality.
     * 						  Some formats, like PNG which is lossless, will ignore the quality setting.
     */
    public static byte[] bitmapToCompressedByteArray(Bitmap bitmap, CompressFormat compressFormat, int compressQuality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean result = bitmap.compress(compressFormat, compressQuality, stream);
        if(result) {
            return stream.toByteArray();
        }
        else {
            return null;
        }
    }

    public static Bitmap compressedByteArrayToBitmap(byte[] byteArray, Bitmap.Config bitmapConfig, Set<SoftReference<Bitmap>> reusableBitmaps) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapOptions);
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inScaled = false;
        bitmapOptions.inPreferredConfig = bitmapConfig;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            addInBitmapOptions(bitmapOptions, reusableBitmaps);
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapOptions);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, Set<SoftReference<Bitmap>> reusableBitmaps) {
        if(reusableBitmaps != null) {
            // Try to find a bitmap to use for inBitmap
            Bitmap inBitmap = getBitmapFromReusableSet(options, reusableBitmaps);
            if(inBitmap != null) {
                // inBitmap only works with mutable bitmaps, so force the decoder to return mutable bitmaps
                options.inMutable = true;
                // If a suitable bitmap has been found, set it as the value of inBitmap
                options.inBitmap = inBitmap;
            }
        }
    }

    /* This method iterates through the reusable bitmaps, looking for one to use for inBitmap */
    private static Bitmap getBitmapFromReusableSet(BitmapFactory.Options options, Set<SoftReference<Bitmap>> reusableBitmaps) {
        Bitmap bitmap = null;
        if((reusableBitmaps != null) && !reusableBitmaps.isEmpty()) {
            synchronized(reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                Bitmap item;
                while(iterator.hasNext()) {
                    item = iterator.next().get();
                    if((item != null) && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap
                        if(canUseForInBitmap(item, options)) {
                            bitmap = item;
                            // Remove from reusable set so it can't be used again
                            iterator.remove();
                            break;
                        }
                    }
                    else {
                        // Remove from the set if the reference has been cleared
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate allocation byte count
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }
        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        int allocationSize = getBitmapByteSize(bitmap);
        ByteBuffer buffer = ByteBuffer.allocate(allocationSize);
        bitmap.copyPixelsToBuffer(buffer);
        bitmap.recycle();
        return buffer.array();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapByteSize(Bitmap bitmap) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    /**
     * @return Squared bitmap
     */
    public static Bitmap byteArrayToBitmap(byte[] byteArray, Bitmap.Config bitmapConfig) {
        int[] intArray = new int[byteArray.length];
        for(int i = 0; i < byteArray.length; ++i) {
            intArray[i] = byteArray[i];
        }
        int bytesPerPixel = getBytesPerPixel(bitmapConfig);
        int bitmapSize = (int) Math.sqrt(byteArray.length / bytesPerPixel);
        return Bitmap.createBitmap(intArray, 0, bitmapSize, bitmapSize, bitmapSize, bitmapConfig);
    }

    public static int getBytesPerPixel(Config bitmapConfig) {
        switch(bitmapConfig) {
            case ARGB_8888:
                return 4;
            case RGB_565:
                return 2;
            case ARGB_4444:
                return 2;
            case ALPHA_8:
                return 1;
            default:
                return 1;
        }
    }

    /**
     * @return Future object which can be used to cancel background task
     */
    public static Future<?> byteArraysToBitmaps(List<byte[]> byteArrays, CompressFormat compressFormat, Bitmap.Config bitmapConfig,
                                                Set<SoftReference<Bitmap>> reusableBitmaps, WeakReference<BitmapOperationCallback> callbackRef) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        return singleThreadExecutor.submit(new BitmapByteArrayConverter<byte[], Bitmap>(byteArrays, compressFormat, LOWEST_COMPRESS_QUALITY,
                bitmapConfig, reusableBitmaps, callbackRef, Bitmap.class, singleThreadExecutor));
    }

    /**
     * @return Future object which can be used to cancel background task
     */
    public static Future<?> bitmapsToByteArrays(List<Bitmap> bitmaps, CompressFormat compressFormat, int compressQuality,
                                                WeakReference<BitmapOperationCallback> callbackRef) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        return singleThreadExecutor.submit(new BitmapByteArrayConverter<Bitmap, byte[]>(bitmaps, compressFormat, compressQuality,
                null, null, callbackRef, byte[].class, singleThreadExecutor));
    }

    /**
     * @return Future object which can be used to cancel background task
     */
    public static Future<?> namedByteArraysToBitmaps(HashMap<String, byte[]> namedByteArrays, CompressFormat compressFormat,
                                                     Bitmap.Config bitmapConfig, Set<SoftReference<Bitmap>> reusableBitmaps,
                                                     WeakReference<BitmapOperationCallback> callbackRef) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        return singleThreadExecutor.submit(new NamedBitmapNamedByteArrayConverter<String, byte[], Bitmap>(namedByteArrays, compressFormat,
                LOWEST_COMPRESS_QUALITY, bitmapConfig, reusableBitmaps, callbackRef, Bitmap.class, singleThreadExecutor));
    }

    /**
     * @return Future object which can be used to cancel background task
     */
    public static Future<?> namedBitmapsToByteArrays(HashMap<String, Bitmap> namedBitmaps, CompressFormat compressFormat, int compressQuality,
                                                     WeakReference<BitmapOperationCallback> callbackRef) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        return singleThreadExecutor.submit(new NamedBitmapNamedByteArrayConverter<String, Bitmap, byte[]>(namedBitmaps, compressFormat,
                compressQuality, null, null, callbackRef, byte[].class, singleThreadExecutor));
    }

    private static class BitmapByteArrayConverter<T, U> implements Runnable {
        private List<T> srcList;
        private CompressFormat compressFormat;
        private int compressQuality;
        private Bitmap.Config bitmapConfig;
        private WeakReference<BitmapOperationCallback> callbackRef;
        private Class<?> destType;
        private ExecutorService converterExecutor;
        private Set<SoftReference<Bitmap>> reusableBitmaps;

        public BitmapByteArrayConverter(List<T> srcList, CompressFormat compressFormat, int compressQuality, Bitmap.Config bitmapConfig,
                                        Set<SoftReference<Bitmap>> reusableBitmaps, WeakReference<BitmapOperationCallback> callbackRef, Class<?> destType,
                                        ExecutorService converterExecutor) {
            this.srcList = srcList;
            this.compressFormat = compressFormat;
            this.compressQuality = compressQuality;
            this.bitmapConfig = bitmapConfig;
            this.reusableBitmaps = reusableBitmaps;
            this.callbackRef = callbackRef;
            this.destType = destType;
            this.converterExecutor = converterExecutor;
        }

        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMBER);
            ArrayList<Future<U>> tasks = new ArrayList<>();
            for(T listEntry : srcList) {
                if(Thread.currentThread().isInterrupted()) {
                    executor.shutdownNow();
                    converterExecutor.shutdown();
                    return;
                }
                tasks.add(executor.submit(new BitmapByteArrayCallable<T, U>(listEntry, compressFormat, compressQuality, bitmapConfig,
                        reusableBitmaps, destType)));
            }
            final ArrayList<U> destList = new ArrayList<>();
            for(Future<U> task : tasks) {
                if(Thread.currentThread().isInterrupted()) {
                    executor.shutdownNow();
                    converterExecutor.shutdown();
                    return;
                }
                try {
                    U taskResult = task.get();
                    destList.add(taskResult);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            if(Thread.currentThread().isInterrupted()) {
                converterExecutor.shutdown();
                return;
            }
            final BitmapOperationCallback callback;
            if((callbackRef != null) && ((callback = callbackRef.get()) != null)) {
                StickerApplication.postOnUiThread(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        if(destType.equals(Bitmap.class)) {
                            callback.onByteArrayToBitmap((ArrayList<Bitmap>) destList);
                        }
                        else {
                            callback.onBitmapToByteArray((ArrayList<byte[]>) destList);
                        }
                    }
                });
            }
            converterExecutor.shutdown();
        }
    }

    private static class NamedBitmapNamedByteArrayConverter<T, U, V> implements Runnable {
        private HashMap<T, U> srcMap;
        private CompressFormat compressFormat;
        private int compressQuality;
        private Bitmap.Config bitmapConfig;
        private Set<SoftReference<Bitmap>> reusableBitmaps;
        private WeakReference<BitmapOperationCallback> callbackRef;
        private Class<?> destType;
        private ExecutorService converterExecutor;

        public NamedBitmapNamedByteArrayConverter(HashMap<T, U> srcMap, CompressFormat compressFormat, int compressQuality,
                                                  Bitmap.Config bitmapConfig, Set<SoftReference<Bitmap>> reusableBitmaps,
                                                  WeakReference<BitmapOperationCallback> callbackRef, Class<?> destType, ExecutorService converterExecutor) {
            this.srcMap = srcMap;
            this.compressFormat = compressFormat;
            this.compressQuality = compressQuality;
            this.bitmapConfig = bitmapConfig;
            this.reusableBitmaps = reusableBitmaps;
            this.callbackRef = callbackRef;
            this.destType = destType;
            this.converterExecutor = converterExecutor;
        }

        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMBER);
            ArrayList<Future<Pair<T, V>>> tasks = new ArrayList<>();
            for(Entry<T, U> mapEntry : srcMap.entrySet()) {
                if(Thread.currentThread().isInterrupted()) {
                    executor.shutdownNow();
                    converterExecutor.shutdown();
                    return;
                }
                tasks.add(executor.submit(new NamedBitmapNamedByteArrayCallable<T, U ,V>(Pair.create(mapEntry.getKey(), mapEntry.getValue()),
                        compressFormat, compressQuality, bitmapConfig, reusableBitmaps, destType)));
            }
            final HashMap<T, V> destMap = new HashMap<>();
            for(Future<Pair<T, V>> task : tasks) {
                if(Thread.currentThread().isInterrupted()) {
                    executor.shutdownNow();
                    converterExecutor.shutdown();
                    return;
                }
                try {
                    Pair<T, V> taskResult = task.get();
                    destMap.put(taskResult.first, taskResult.second);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            if(Thread.currentThread().isInterrupted()) {
                converterExecutor.shutdown();
                return;
            }
            final BitmapOperationCallback callback;
            if((callbackRef != null) && ((callback = callbackRef.get()) != null)) {
                StickerApplication.postOnUiThread(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        if(destType.equals(Bitmap.class)) {
                            callback.onNamedByteArrayToNamedBitmap((HashMap<String, Bitmap>) destMap);
                        }
                        else {
                            callback.onNamedBitmapToNamedByteArray((HashMap<String, byte[]>) destMap);
                        }
                    }
                });
            }
            converterExecutor.shutdown();
        }
    }

    private static class BitmapByteArrayCallable<T, U> implements Callable<U> {
        private T objectToConvert;
        private CompressFormat compressFormat;
        private int compressQuality;
        private Bitmap.Config bitmapConfig;
        private Set<SoftReference<Bitmap>> reusableBitmaps;
        private Class<?> typeToConvert;

        public BitmapByteArrayCallable(T objectToConvert, CompressFormat compressFormat, int compressQuality, Bitmap.Config bitmapConfig,
                                       Set<SoftReference<Bitmap>> reusableBitmaps, Class<?> typeToConvert) {
            this.objectToConvert = objectToConvert;
            this.compressFormat = compressFormat;
            this.compressQuality = compressQuality;
            this.bitmapConfig = bitmapConfig;
            this.reusableBitmaps = reusableBitmaps;
            this.typeToConvert = typeToConvert;
        }

        @SuppressWarnings("unchecked")
        @Override
        public U call() throws Exception {
            U convertedObject;
            if(typeToConvert.equals(Bitmap.class)) {
                convertedObject = (U) compressedByteArrayToBitmap((byte[])objectToConvert, bitmapConfig, reusableBitmaps);
            }
            else {
                convertedObject = (U) bitmapToCompressedByteArray((Bitmap)objectToConvert, compressFormat, compressQuality);
            }
            return convertedObject;
        }
    }

    private static class NamedBitmapNamedByteArrayCallable<T, U, V> implements Callable<Pair<T, V>> {
        private Pair<T, U> objectToConvert;
        private CompressFormat compressFormat;
        private int compressQuality;
        private Bitmap.Config bitmapConfig;
        private Set<SoftReference<Bitmap>> reusableBitmaps;
        private Class<?> typeToConvert;

        public NamedBitmapNamedByteArrayCallable(Pair<T, U> objectToConvert, CompressFormat compressFormat, int compressQuality,
                                                 Bitmap.Config bitmapConfig, Set<SoftReference<Bitmap>> reusableBitmaps, Class<?> typeToConvert) {
            this.objectToConvert = objectToConvert;
            this.compressFormat = compressFormat;
            this.compressQuality = compressQuality;
            this.bitmapConfig = bitmapConfig;
            this.reusableBitmaps = reusableBitmaps;
            this.typeToConvert = typeToConvert;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Pair<T, V> call() throws Exception {
            V convertedObject;
            if(typeToConvert.equals(Bitmap.class)) {
                convertedObject = (V) compressedByteArrayToBitmap((byte[])objectToConvert.second, bitmapConfig, reusableBitmaps);
            }
            else {
                convertedObject = (V) bitmapToCompressedByteArray((Bitmap)objectToConvert.second, compressFormat, compressQuality);
            }
            return Pair.create(objectToConvert.first, convertedObject);
        }
    }

}
