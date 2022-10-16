package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.listeners.ViewListener;

public abstract class LabeledHorizontalButtonView extends PicButtonView {

    protected final RectF innerImageBounds;
    protected final RectF pressedInnerImageBounds;
    protected final String description;
    protected final int descriptionHeight;

    protected LabeledHorizontalButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);

        float imageEdgeLength = (super.innerImageBounds.bottom - super.innerImageBounds.top);
        innerImageBounds = new RectF(
                super.innerImageBounds.left,
                super.innerImageBounds.top,
                super.innerImageBounds.left + imageEdgeLength,
                super.innerImageBounds.top + imageEdgeLength);

        pressedInnerImageBounds = new RectF(
                super.pressedInnerImageBounds.left,
                super.pressedInnerImageBounds.top,
                super.pressedInnerImageBounds.left + imageEdgeLength,
                super.pressedInnerImageBounds.top + imageEdgeLength);

        this.description = description;

        labeledPicFontSizeFactor = Float.valueOf(((Context)viewListener).getString(R.string.labeledHorizontalButtonDescriptionHeightFontSizeFactor));
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 24);
        Rect descriptionBounds = new Rect();
        String letter = ((Context)viewListener).getString(R.string.letter);
        paint.getTextBounds(letter, 0, letter.length(), descriptionBounds);
        descriptionHeight = descriptionBounds.height();
    }

    protected abstract void drawState(Canvas canvas, Paint paint);

    protected void drawText(Canvas canvas, Paint paint, RectF superBounds) {
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 24);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(description, superBounds.left + superBounds.width() * 4 / 10, superBounds.centerY() + descriptionHeight / 2, paint);
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF superBounds = (isPressed())? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        drawState(canvas, paint);
        drawText(canvas, paint, superBounds);
    }
}
