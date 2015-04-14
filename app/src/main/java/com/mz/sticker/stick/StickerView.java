package com.mz.sticker.stick;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import com.mz.sticker.crop.HighlightView;

public class StickerView extends HighlightView {

    private Bitmap stickerBitmap;
    private int stickerBitmapWidth;
    private int stickerBitmapHeight;
    private float rotate = 0.0f;

    public StickerView(View context, Bitmap bitmap) {
        super(context);
        stickerBitmap = bitmap;
        stickerBitmapWidth = bitmap.getWidth();
        stickerBitmapHeight = bitmap.getHeight();
    }

    public Bitmap getStickerViewBitmap() {
        return stickerBitmap;
    }

    public float getStickerRotate() {
        return rotate;
    }

    public void setStickerRotate(float rotate) {
        this.rotate = rotate;
    }

    protected void draw(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        outlinePaint.setStrokeWidth(outlineWidth);

        int px = drawRect.left + ((drawRect.right - drawRect.left) / 2);
        int py = drawRect.top + ((drawRect.bottom - drawRect.top) / 2);
        canvas.rotate(rotate, px, py);
        canvas.drawBitmap(stickerBitmap, null, drawRect, null);
        canvas.rotate(-rotate, px, py);
        if (!hasFocus()) {
            outlinePaint.setColor(Color.BLACK);
            canvas.drawRect(drawRect, outlinePaint);
        } else {
            Rect viewDrawingRect = new Rect();
            viewContext.getDrawingRect(viewDrawingRect);

            path.addRect(new RectF(drawRect), Path.Direction.CW);
            outlinePaint.setColor(highlightColor);

            canvas.restore();
            canvas.drawPath(path, outlinePaint);

            if (showThirds) {
                drawThirds(canvas);
            }

            if (handleMode == HandleMode.Always ||
                    (handleMode == HandleMode.Changing && modifyMode == ModifyMode.Grow)) {
                drawHandles(canvas);
            }
        }
    }

}
