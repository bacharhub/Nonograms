package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.MenuOptionsViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class MenuSettingsButtonView extends PicButtonView {

    public MenuSettingsButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
    }

    @Override
    public void onButtonPressed() {
        ((MenuOptionsViewListener)viewListener).onOptionsButtonPressed();
    }
}
