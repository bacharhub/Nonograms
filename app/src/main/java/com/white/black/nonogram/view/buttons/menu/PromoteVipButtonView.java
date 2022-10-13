package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;
import com.white.black.nonogram.view.listeners.VipPromoter;

public class PromoteVipButtonView extends PicButtonView {

    public PromoteVipButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF superBounds = (isPressed())? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        if (AdManager.isRemoveAds()) {
            paint.setColor(Color.YELLOW);
            canvas.drawRoundRect(superBounds, curve, curve, paint);
        }

        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        paint.setColor(backgroundColor);
    }

    @Override
    public void onButtonPressed() {
        ((VipPromoter)viewListener).promote();
    }
}