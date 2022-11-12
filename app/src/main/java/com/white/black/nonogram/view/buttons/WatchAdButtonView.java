package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class WatchAdButtonView extends LabeledHorizontalButtonView {

    private final RectF coinBounds;
    private final RectF pressedCoinBounds;
    private final int descriptionHeight;

    public WatchAdButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, description, bounds, color1, color2, color3, innerImages, context, paint);
        coinBounds = new RectF(
                bounds.left + bounds.width() * 65 / 100,
                bounds.centerY() - 64,
                bounds.left + bounds.width() * 65 / 100 + 128,
                bounds.centerY() + 64
        );

        pressedCoinBounds = new RectF(
                coinBounds.left + bounds.width() * 4 / 100,
                coinBounds.top + bounds.height() * 10 / 100,
                coinBounds.left + bounds.width() * 4 / 100 + 128,
                coinBounds.top + bounds.height() * 10 / 100 + 128
        );

        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 17);
        Rect descriptionBounds = new Rect();
        paint.getTextBounds(description, 0, description.length(), descriptionBounds);
        descriptionHeight = descriptionBounds.height();
    }

    @Override
    protected void drawState(Canvas canvas, Paint paint) {
        long interval = 2_000L;
        float progress = (float)(Math.abs(interval / 2 - (System.currentTimeMillis() % interval))) / (interval / 2f);
        float red = 255f;
        float green = 209f + (255f - 209f) * progress;
        float blue = 220f + (255f - 220f) * progress;
        innerBackgroundColor = Color.rgb((int)red, (int)green, (int)blue);

        RectF bounds0 = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds0, paint);
        RectF bounds1 = (isPressed())? pressedCoinBounds : coinBounds;
        canvas.drawBitmap(innerImages[1], null, bounds1, paint);
        paint.setColor(backgroundColor);
    }

    protected void drawText(Canvas canvas, Paint paint, RectF superBounds) {
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 17);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(description, superBounds.left + superBounds.width() * 43 / 100, superBounds.centerY() + descriptionHeight / 2, paint);
    }

    @Override
    public void onButtonPressed() {
        ((GameViewListener)viewListener).onWatchVideo();
    }
}
