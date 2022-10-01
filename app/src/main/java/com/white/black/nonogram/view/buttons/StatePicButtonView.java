package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.listeners.ViewListener;

public abstract class StatePicButtonView extends PicButtonView {

    protected final RectF innerImageBounds;
    protected final RectF pressedInnerImageBounds;

    StatePicButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);

        float imageEdgeLength = (super.innerImageBounds.bottom - super.innerImageBounds.top) * 7 / 10;
        innerImageBounds = new RectF(
                super.innerImageBounds.right - imageEdgeLength,
                super.innerImageBounds.bottom - imageEdgeLength,
                super.innerImageBounds.right,
                super.innerImageBounds.bottom);

        pressedInnerImageBounds = new RectF(
                super.pressedInnerImageBounds.right - imageEdgeLength,
                super.pressedInnerImageBounds.bottom - imageEdgeLength,
                super.pressedInnerImageBounds.right,
                super.pressedInnerImageBounds.bottom);
    }

    protected abstract void drawInputState(Canvas canvas, Paint paint);

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        drawInputState(canvas, paint);
    }
}
