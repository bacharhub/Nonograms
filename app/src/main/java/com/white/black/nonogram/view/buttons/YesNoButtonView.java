package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.view.listeners.ViewListener;

public class YesNoButtonView extends LabeledHorizontalButtonView {

    private String description;

    public YesNoButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
        this.description = super.description;
    }

    @Override
    protected void drawState(Canvas canvas, Paint paint) {
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        paint.setColor(backgroundColor);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected void drawText(Canvas canvas, Paint paint, RectF superBounds) {
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 24);
        paint.setTextAlign(Paint.Align.CENTER);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawText(this.description, bounds.width() * 27 / 100 + superBounds.centerX(), superBounds.centerY() + descriptionHeight / 2, paint);
    }

    @Override
    public void onButtonPressed() {
    }
}
