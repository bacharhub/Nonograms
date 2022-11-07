package com.white.black.nonogram.view.buttons.shop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.view.buttons.LabeledHorizontalButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.function.Supplier;

public class BuyShopItemButtonView extends LabeledHorizontalButtonView {

    private final Runnable purchase;
    private final Supplier<Boolean> enoughCoins;
    private final int buttonInnerBackgroundColor;
    private final int costColor;
    private final int descriptionHeight;
    private final float fontSize;

    public BuyShopItemButtonView(
            ViewListener viewListener,
            String description,
            RectF bounds,
            int color1,
            int color2,
            int color3,
            Bitmap[] innerImages,
            Context context,
            Paint paint,
            Runnable purchase,
            Supplier<Boolean> enoughCoins
    ) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
        this.purchase = purchase;
        this.enoughCoins = enoughCoins;
        this.buttonInnerBackgroundColor = color1;
        this.costColor = color3;
        this.fontSize = labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 18;
        paint.setTextSize(fontSize);
        Rect descriptionBounds = new Rect();
        paint.getTextBounds(description, 0, description.length(), descriptionBounds);
        descriptionHeight = descriptionBounds.height();
    }

    @Override
    protected void drawState(Canvas canvas, Paint paint) {
        RectF superBounds = (isPressed()) ? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        RectF bounds = (isPressed()) ? pressedInnerImageBounds : innerImageBounds;

        if (enoughCoins.get()) {
            paint.setColor(buttonInnerBackgroundColor);
            canvas.drawRoundRect(superBounds, curve, curve, paint);
            canvas.drawBitmap(innerImages[0], null, bounds, paint);
        } else {
            canvas.drawBitmap(innerImages[1], null, bounds, paint);
        }
    }

    protected void drawText(Canvas canvas, Paint paint, RectF superBounds) {
        if (enoughCoins.get()) {
            paint.setColor(costColor);
        } else {
            paint.setColor(Color.LTGRAY);
        }

        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(description, superBounds.left + superBounds.width() * 60 / 100, superBounds.centerY() + descriptionHeight / 2, paint);
    }

    @Override
    public void onButtonPressed() {
        if (enoughCoins.get()) {
            purchase.run();
        }
    }
}