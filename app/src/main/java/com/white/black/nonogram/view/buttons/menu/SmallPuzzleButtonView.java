package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.LabeledPicButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;

public class SmallPuzzleButtonView extends LabeledPicButtonView {

    public SmallPuzzleButtonView(MenuViewListener menuViewListener, RectF bounds, String description, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(menuViewListener, bounds, description, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 3 / 10,
                bounds.top + bounds.height() * 3 / 10,
                bounds.right - bounds.width() * 3 / 10,
                bounds.bottom - bounds.height() * 3 / 10), context, paint);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onSmallPuzzleButtonPressed();
    }
}
