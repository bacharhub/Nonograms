package com.white.black.nonogram.view.buttons.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.LabeledHorizontalButtonView;
import com.white.black.nonogram.view.listeners.GameOptionsViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class StartOverButtonView extends LabeledHorizontalButtonView {

    public StartOverButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
    }

    @Override
    protected void drawState(Canvas canvas, Paint paint) {
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        paint.setColor(backgroundColor);
    }

    @Override
    public void onButtonPressed() {
        ((GameOptionsViewListener)viewListener).onStartOverButtonPressed();
    }
}
