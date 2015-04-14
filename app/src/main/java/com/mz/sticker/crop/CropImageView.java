package com.mz.sticker.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.mz.sticker.screen.CropImageActivity;
import com.mz.sticker.screen.StickerActivity;

import java.util.ArrayList;

public class CropImageView extends ImageViewTouchBase {

    private ArrayList<HighlightView> highlightViews = new ArrayList<HighlightView>();
    private HighlightView motionHighlightView;
    private Context context;

    private float lastX;
    private float lastY;
    private int motionEdge;

    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context) {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ArrayList<HighlightView> getHighlightViews() {
        return highlightViews;
    }

    public HighlightView getHighlightView() {
        return motionHighlightView;
    }

    public Context getImageViewContext() {
        return context;
    }

    public void setImageViewContext(Context context) {
        this.context = context;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (bitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : highlightViews) {

                hv.matrix.set(getUnrotatedMatrix());
                hv.invalidate();
                if (hv.hasFocus()) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : highlightViews) {
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (HighlightView hv : highlightViews) {
            hv.matrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(context instanceof CropImageActivity) {
            CropImageActivity cropImageActivity = (CropImageActivity) context;
            if (cropImageActivity.isSaving()) {
                return false;
            }
        }
        else if(context instanceof StickerActivity) {
            StickerActivity stickerActivity = (StickerActivity) context;
            if (stickerActivity.isSaving()) {
                return false;
            }
        }


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (HighlightView hv : highlightViews) {
                    int edge = hv.getHit(event.getX(), event.getY());
                    if (edge != HighlightView.GROW_NONE) {
                        motionEdge = edge;
                        motionHighlightView = hv;
                        lastX = event.getX();
                        lastY = event.getY();
                        motionHighlightView.setMode((edge == HighlightView.MOVE)
                                ? HighlightView.ModifyMode.Move
                                : HighlightView.ModifyMode.Grow);
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (motionHighlightView != null) {
                    centerBasedOnHighlightView(motionHighlightView);
                    motionHighlightView.setMode(HighlightView.ModifyMode.None);
                }
                motionHighlightView = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (motionHighlightView != null) {
                    motionHighlightView.handleMotion(motionEdge, event.getX()
                            - lastX, event.getY() - lastY);
                    lastX = event.getX();
                    lastY = event.getY();
                    ensureVisible(motionHighlightView);
                }
                break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                center(true, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around. This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    center(true, true);
                }
                break;
        }

        return true;
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.drawRect;

        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);

        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    public void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.drawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);

        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[] { hv.cropRect.centerX(), hv.cropRect.centerY() };
            getUnrotatedMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (HighlightView mHighlightView : highlightViews) {
            mHighlightView.draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        highlightViews.add(hv);
        invalidate();
    }

    public void clearHighlightViews() {
        highlightViews.clear();
        invalidate();
    }

    public void setHighlightViewToImageOriginalSize() {
        HighlightView hv = highlightViews.get(0);
        hv.setCropRect(new Rect((int) hv.imageRect.left, (int) hv.imageRect.top, (int) hv.imageRect.right, (int) hv.imageRect.bottom));
        centerBasedOnHighlightView(hv);
    }

    public void setHighlightViewToImageSquareSize() {
        HighlightView hv = highlightViews.get(0);
        int left, top, right, bottom;
        if(hv.imageRect.right < hv.imageRect.bottom) {
            left = 0;
            right = (int) hv.imageRect.right;
            top = (int) ((hv.imageRect.bottom / 2) - (hv.imageRect.right / 2));
            bottom = (int) ((hv.imageRect.bottom / 2) + (hv.imageRect.right / 2));
        }
        else {
            top = 0;
            bottom = (int) hv.imageRect.bottom;
            left = (int) ((hv.imageRect.right / 2) - (hv.imageRect.bottom / 2));
            right = (int) ((hv.imageRect.right / 2) + (hv.imageRect.bottom / 2));
        }
        hv.setCropRect(new Rect(left, top, right, bottom));
        centerBasedOnHighlightView(hv);
    }

    public void setHighlightViewToImageVerticalSize() {
        HighlightView hv = highlightViews.get(0);
        int left = (int) ((hv.imageRect.right / 2) - ((hv.imageRect.bottom * 0.66) / 2));
        int right = (int) ((hv.imageRect.right / 2) + ((hv.imageRect.bottom * 0.66) / 2));
        hv.setCropRect(new Rect(Math.max(left, 0), 0, (int) Math.min(right, hv.imageRect.right), (int) hv.imageRect.bottom));
        centerBasedOnHighlightView(hv);
    }

    public void setHighlightViewToImageHorizontalSize() {
        HighlightView hv = highlightViews.get(0);
        int top = (int) ((hv.imageRect.bottom / 2) - ((hv.imageRect.right * (9.0 / 16.0)) / 2));
        int bottom = (int) ((hv.imageRect.bottom / 2) + ((hv.imageRect.right * (9.0 / 16.0)) / 2));
        hv.setCropRect(new Rect(0, Math.max(top, 0), (int) hv.imageRect.right, (int) Math.min(bottom, hv.imageRect.bottom)));
        centerBasedOnHighlightView(hv);
    }
}