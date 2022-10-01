package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.view.buttons.LabeledHorizontalButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class PrivacyPolicyButtonView extends LabeledHorizontalButtonView {

    public PrivacyPolicyButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
    }

    protected void drawState(Canvas canvas, Paint paint) {
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        paint.setColor(backgroundColor);
    }

    protected void drawText(Canvas canvas, Paint paint, RectF superBounds) {
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenWidth() / 30);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(description, superBounds.left + superBounds.width() * 60 / 100, superBounds.centerY() + descriptionHeight / 2, paint);
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onPrivacyPolicyButtonPressed();
    }
}
