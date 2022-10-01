package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.listeners.PuzzleSelectionViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class SwitchCategoryButtonView extends TitlePicButtonView {

    public SwitchCategoryButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
    }

    @Override
    protected void drawInputState(Canvas canvas, Paint paint) {
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        paint.setColor(backgroundColor);
    }

    @Override
    public void onButtonPressed() {
        ((PuzzleSelectionViewListener)viewListener).onSwitchCategoryPressed();
    }
}
