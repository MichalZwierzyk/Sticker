package com.mz.sticker.screen;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.mz.sticker.R;
import com.mz.sticker.crop.Crop;
import com.mz.sticker.crop.CropImageView;
import com.mz.sticker.crop.CropUtil;
import com.mz.sticker.crop.HighlightView;
import com.mz.sticker.crop.ImageViewTouchBase;
import com.mz.sticker.crop.RotateBitmap;
import com.mz.sticker.stick.StickUtil;
import com.mz.sticker.stick.StickerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StickerActivity extends MonitoredActivityWithActionBar {

    private static final String TAG = StickerActivity.class.getSimpleName();

    private static final boolean IN_MEMORY_CROP = Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1;
    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;
    private static final float ROTATION_STEP = 45.0f;

    private final Handler handler = new Handler();

    private int aspectX;
    private int aspectY;

    // Output image
    private int maxX;
    private int maxY;
    private int exifRotation;

    private Uri sourceUri;
    private Uri saveUri;

    private boolean enterFromGallery;
    private boolean isSaving;

    private int sampleSize;
    RotateBitmap rotateBitmap;

    @InjectView(R.id.crop_image)
    CropImageView imageView;

    @InjectView(R.id.stickers_palette_one)
    ImageView stickersPalatteOne;

    @InjectView(R.id.stickers_palette_two)
    ImageView stickersPalatteTwo;

    @InjectView(R.id.stickers_palette_three)
    ImageView stickersPalatteThree;

    @InjectView(R.id.stickers_palette_four)
    ImageView stickersPalatteFour;

    @InjectView(R.id.stickers_palette_five)
    ImageView stickersPalatteFive;

    @InjectView(R.id.stickers_palette_six)
    ImageView stickersPalatteSix;

    View.OnClickListener stickersPaletteOnClickListener;

    HighlightView cropView;

    @Override
    public void onCreate(Bundle icicle) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(icicle);
        setContentView(R.layout.activity_sticker);
        ButterKnife.inject(this);
        initViews();

        setupFromIntent();
        if (rotateBitmap == null) {
            finish();
            return;
        }
        startCrop();
    }

    private void initViews() {
        imageView.setImageViewContext(this);
        imageView.setRecycler(new ImageViewTouchBase.Recycler() {
            @Override
            public void recycle(Bitmap b) {
                b.recycle();
                System.gc();
            }
        });
        stickersPaletteOnClickListener = new StickersPaletteOnClickListener();
        stickersPalatteOne.setOnClickListener(stickersPaletteOnClickListener);
        stickersPalatteTwo.setOnClickListener(stickersPaletteOnClickListener);
        stickersPalatteThree.setOnClickListener(stickersPaletteOnClickListener);
        stickersPalatteFour.setOnClickListener(stickersPaletteOnClickListener);
        stickersPalatteFive.setOnClickListener(stickersPaletteOnClickListener);
        stickersPalatteSix.setOnClickListener(stickersPaletteOnClickListener);
    }

    @OnClick(R.id.btn_cancel)
    public void cancelCrop() {
        if(!enterFromGallery) {
            new File(sourceUri.getPath()).delete();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.stickers_store_button)
    public void openStickersStore() {
        startActivity(new Intent(this, StickersStoreActivity.class));
    }

    @OnClick(R.id.btn_save)
    public void saveImage() {
        onSaveClicked(true);
    }

    private class StickersPaletteOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.stickers_palette_one:
                    openStickersPalette(0);
                    break;
                case R.id.stickers_palette_two:
                    openStickersPalette(1);
                    break;
                case R.id.stickers_palette_three:
                    openStickersPalette(2);
                    break;
                case R.id.stickers_palette_four:
                    openStickersPalette(3);
                    break;
                case R.id.stickers_palette_five:
                    openStickersPalette(4);
                    break;
                case R.id.stickers_palette_six:
                    openStickersPalette(5);
                    break;
            }
        }
    }

    private void openStickersPalette(int paletteNum) {
        Intent stickersPaletteIntent = new Intent(this, StickersPaletteActivity.class);
        stickersPaletteIntent.putExtra(StickersPaletteActivity.REQUEST_STICKERS_PALETTE_NUM, paletteNum);
        startActivityForResult(stickersPaletteIntent, StickersPaletteActivity.REQUEST_STICKERS_PALETTE);
    }

    private void setupFromIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            aspectX = extras.getInt(Crop.Extra.ASPECT_X);
            aspectY = extras.getInt(Crop.Extra.ASPECT_Y);
            maxX = extras.getInt(Crop.Extra.MAX_X);
            maxY = extras.getInt(Crop.Extra.MAX_Y);
            saveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);
            enterFromGallery = extras.getBoolean(Crop.Extra.ENTER_FROM_GALLERY);
        }

        sourceUri = intent.getData();
        if (sourceUri != null) {
            exifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, getContentResolver(), sourceUri));

            InputStream is = null;
            try {
                sampleSize = calculateBitmapSampleSize(sourceUri);
                is = getContentResolver().openInputStream(sourceUri);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = sampleSize;
                rotateBitmap = new RotateBitmap(BitmapFactory.decodeStream(is, null, option), exifRotation);
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage());
                setResultException(e);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OOM reading image: " + e.getMessage());
                setResultException(e);
            } finally {
                CropUtil.closeSilently(is);
            }
        }
    }

    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtil.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    private void startCrop() {
        if (isFinishing()) {
            return;
        }
        imageView.setImageRotateBitmapResetBase(rotateBitmap, true);
        StickUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop_wait),
                new Runnable() {
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(1);
                        handler.post(new Runnable() {
                            public void run() {
                                if (imageView.getScale() == 1F) {
                                    imageView.center(true, true);
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        new StickerApplier().crop(false, null);
                    }
                }, handler
        );
    }

    private class StickerApplier {

        private void makeDefault(boolean showCropArea, Bitmap stickerBitmap) {
            if (rotateBitmap == null) {
                return;
            }

            if(showCropArea) {
                StickerView sticker = new StickerView(imageView, stickerBitmap);
                final int width = rotateBitmap.getWidth();
                final int height = rotateBitmap.getHeight();

                Rect imageRect = new Rect(0, 0, width, height);

                int left = (int) ((imageRect.right / 2.0) - (stickerBitmap.getWidth()));
                int right = (int) ((imageRect.right / 2.0) + (stickerBitmap.getWidth()));
                int top = (int) ((imageRect.bottom / 2.0) - (stickerBitmap.getHeight()));
                int bottom = (int) ((imageRect.bottom / 2.0) + (stickerBitmap.getHeight()));

                RectF cropRect = new RectF(left, top, right, bottom);
                sticker.setup(imageView.getUnrotatedMatrix(), imageRect, cropRect, aspectX != 0 && aspectY != 0);

                imageView.clearHighlightViews();
                imageView.add(sticker);
                imageView.centerBasedOnHighlightView(sticker);
            }
        }

        public void crop(final boolean showCropArea, final Bitmap stickerBitmap) {
            handler.post(new Runnable() {
                public void run() {
                    makeDefault(showCropArea, stickerBitmap);
                    imageView.invalidate();
                    if (imageView.getHighlightViews().size() == 1) {
                        cropView = imageView.getHighlightViews().get(0);
                        cropView.setFocus(true);
                    }
                }
            });
        }
    }

    private void onSaveClicked(boolean finish) {
        if(cropView == null || isSaving) {
            if(finish) {
                finish();
            }
            return;
        }
        isSaving = true;

        Bitmap croppedImage;
        RectF imgViewRect = cropView.getImageRect();
        cropView.setCropRect(new Rect((int) imgViewRect.left, (int) imgViewRect.top, (int) imgViewRect.right, (int) imgViewRect.bottom));
        Rect r = cropView.getScaledCropRect(sampleSize);
        int width = r.width();
        int height = r.height();

        int outWidth = width;
        int outHeight = height;
        if (maxX > 0 && maxY > 0 && (width > maxX || height > maxY)) {
            float ratio = (float) width / (float) height;
            if ((float) maxX / (float) maxY > ratio) {
                outHeight = maxY;
                outWidth = (int) ((float) maxY * ratio + .5f);
            } else {
                outWidth = maxX;
                outHeight = (int) ((float) maxX / ratio + .5f);
            }
        }

        if (IN_MEMORY_CROP && rotateBitmap != null) {
            croppedImage = inMemoryCrop(rotateBitmap, r, outWidth, outHeight);
            if (croppedImage != null) {
                imageView.setImageBitmapResetBase(croppedImage, true);
                imageView.center(true, true);
                imageView.getHighlightViews().clear();
            }
        } else {
            try {
                croppedImage = imageView.getImageBitmap();
            } catch (IllegalArgumentException e) {
                setResultException(e);
                finish();
                return;
            }

            if (croppedImage != null) {
                imageView.setImageRotateBitmapResetBase(new RotateBitmap(croppedImage, exifRotation), true);
                imageView.center(true, true);
                imageView.getHighlightViews().clear();
            }
        }
        saveImage(croppedImage, finish);
    }

    private void saveImage(Bitmap croppedImage, final boolean finish) {
        if (croppedImage != null) {
            final Bitmap b = croppedImage;
            StickUtil.startBackgroundJob(this, null, getResources().getString(R.string.sticker_saving),
                    new Runnable() {
                        public void run() {
                            saveOutput(b, finish);
                        }
                    }, handler
            );
        } else {
            finish();
        }
    }

    @TargetApi(10)
    private Bitmap decodeRegionCrop(Rect rect, int outWidth, int outHeight) {
        // Release memory now
        clearImageView();

        InputStream is = null;
        Bitmap croppedImage = null;
        try {
            is = getContentResolver().openInputStream(sourceUri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();

            if (exifRotation != 0) {
                // Adjust crop area to account for image rotation
                Matrix matrix = new Matrix();
                matrix.setRotate(-exifRotation);

                RectF adjusted = new RectF();
                matrix.mapRect(adjusted, new RectF(rect));

                // Adjust to account for origin at 0,0
                adjusted.offset(adjusted.left < 0 ? width : 0, adjusted.top < 0 ? height : 0);
                rect = new Rect((int) adjusted.left, (int) adjusted.top, (int) adjusted.right, (int) adjusted.bottom);
            }

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
                if (rect.width() > outWidth || rect.height() > outHeight) {
                    Matrix matrix = new Matrix();
                    matrix.postScale((float) outWidth / rect.width(), (float) outHeight / rect.height());
                    croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix, true);
                }
            } catch (IllegalArgumentException e) {
                // Rethrow with some extra information
                throw new IllegalArgumentException("Rectangle " + rect + " is outside of the image ("
                        + width + "," + height + "," + exifRotation + ")", e);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error cropping image: " + e.getMessage());
            finish();
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OOM cropping image: " + e.getMessage());
            setResultException(e);
        } finally {
            CropUtil.closeSilently(is);
        }
        return croppedImage;
    }

    private Bitmap inMemoryCrop(RotateBitmap rotateBitmap, Rect rect, int outWidth, int outHeight) {
        // In-memory crop means potential OOM errors,
        // but we have no choice as we can't selectively decode a bitmap with this API level
        System.gc();

        Bitmap croppedImage = null;
        try {
            croppedImage = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(croppedImage);
            RectF dstRect = new RectF(0, 0, rect.width(), rect.height());

            Matrix m = new Matrix();
            m.setRectToRect(new RectF(rect), dstRect, Matrix.ScaleToFit.FILL);
            m.preConcat(rotateBitmap.getRotateMatrix());
            canvas.drawBitmap(rotateBitmap.getBitmap(), m, null);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OOM cropping image: " + e.getMessage());
            setResultException(e);
            System.gc();
        }

        // Release Bitmap memory as soon as possible
        clearImageView();
        return croppedImage;
    }

    private void clearImageView() {
        imageView.clear();
        if (rotateBitmap != null) {
            rotateBitmap.recycle();
        }
        System.gc();
    }

    private void saveOutput(Bitmap croppedImage, boolean finish) {
        if (saveUri != null) {
            int exifRotation = CropUtil.getExifRotation(new File(sourceUri.getPath()));

            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
            } catch (IOException e) {
                setResultException(e);
                Log.e(TAG, "Cannot open file: " + saveUri);
            } finally {
                CropUtil.closeSilently(outputStream);
            }

            CropUtil.setExifRotation(new File(saveUri.getPath()), exifRotation);

            setResultUri(saveUri);
        }

        final Bitmap b = croppedImage;
        handler.post(new Runnable() {
            public void run() {
                imageView.clear();
                b.recycle();
            }
        });

        if(finish) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rotateBitmap != null) {
            rotateBitmap.recycle();
        }
    }

    @Override
    public void onBackPressed() {
        if(!enterFromGallery) {
            new File(sourceUri.getPath()).delete();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    public boolean isSaving() {
        return isSaving;
    }

    private void setResultUri(Uri uri) {
        setResult(RESULT_OK, new Intent().putExtra(MediaStore.EXTRA_OUTPUT, uri));
    }

    private void setResultException(Throwable throwable) {
        setResult(Crop.RESULT_ERROR, new Intent().putExtra(Crop.Extra.ERROR, throwable));
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.sticker_activity_menu, menu);
        menu.findItem(R.id.action_apply_sticker).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!imageView.getHighlightViews().isEmpty()) {
                    Bitmap oldBitmap = imageView.getImageBitmap();
                    Bitmap displayedBitmap = Bitmap.createBitmap(oldBitmap.getWidth(), oldBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(displayedBitmap);
                    canvas.drawBitmap(oldBitmap, null, new Rect(0, 0, oldBitmap.getWidth(), oldBitmap.getHeight()), null);
                    StickerView stickerView = (StickerView) imageView.getHighlightViews().get(0);
                    Rect cropRect = stickerView.getScaledCropRect(1.0f);
                    int px = cropRect.left + ((cropRect.right - cropRect.left) / 2);
                    int py = cropRect.top + ((cropRect.bottom - cropRect.top) / 2);
                    canvas.rotate(stickerView.getStickerRotate() - rotateBitmap.getRotation(), px, py);
                    canvas.drawBitmap(stickerView.getStickerViewBitmap(), null, cropRect, null);
                    imageView.setImageBitmap(displayedBitmap);
                    imageView.clearHighlightViews();
                }
                return true;
            }
        });
        menu.findItem(R.id.action_remove_sticker).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                imageView.clearHighlightViews();
                return true;
            }
        });
        menu.findItem(R.id.action_rotate).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                StickerView stickerView = (StickerView) imageView.getHighlightViews().get(0);
                if(stickerView != null) {
                    stickerView.setStickerRotate(stickerView.getStickerRotate() + ROTATION_STEP);
                    imageView.invalidate();
                }
                return true;
            }
        });
        menu.findItem(R.id.action_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onSaveClicked(false);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, sourceUri.toString());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_picture)));
                return true;
            }
        });
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case StickersPaletteActivity.REQUEST_STICKERS_PALETTE:
                if(resultCode == RESULT_OK) {
                    int stickerId = data.getIntExtra(StickersPaletteActivity.REQUEST_STICKER_ID, -1);
                    // put sticker on picture
                    new StickerApplier().crop(true, BitmapFactory.decodeResource(getResources(), stickerId, null));
                }
                break;
            default:
                break;
        }
    }

}
