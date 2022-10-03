package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.LabeledPicButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class LeaderboardButtonView extends LabeledPicButtonView {

    public LeaderboardButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, "#1", color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 2 / 10,
                bounds.top + bounds.height() * 2 / 10,
                bounds.right - bounds.width() * 2 / 10,
                bounds.bottom - bounds.height() * 2 / 10), context, paint);
    }


    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onLeaderboardButtonPressed();
    }
}
