package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.buttons.LeaveShopButton;
import com.white.black.nonogram.view.listeners.ViewListener;

public class ShopView {

    private int backgroundColor;
    private RectF backgroundBounds;
    private BankView bankView;
    private Bitmap door;
    private boolean show;
    private LeaveShopButton leaveShopButton;

    public void show() {
        this.show = true;
    }

    public boolean isShown() {
        return this.show;
    }

    public void hide() {
        this.show = false;
    }

    public void init(Context context) {
        backgroundColor = ContextCompat.getColor(context, R.color.shopBackground);
        backgroundBounds = new RectF(
                0,
                0,
                ApplicationSettings.INSTANCE.getScreenWidth(),
                ApplicationSettings.INSTANCE.getScreenHeight()
        );

        bankView = new BankView();
        bankView.init(context);

        door = BitmapLoader.INSTANCE.getImage(context, R.drawable.open_door_with_stroke_64);

        leaveShopButton = new LeaveShopButton(
                (ViewListener) context,
                new RectF(
                        100,
                        ApplicationSettings.INSTANCE.getScreenHeight() - 292,
                        292,
                        ApplicationSettings.INSTANCE.getScreenHeight() - 100
                ),
                Color.LTGRAY,
                Color.GRAY,
                Color.DKGRAY,
                new Bitmap[]{door},
                context,
                new Paint());
    }

    public void draw(Canvas canvas, Paint paint) {
        if (show) {
            paint.setColor(backgroundColor);
            canvas.drawRect(backgroundBounds, paint);
            bankView.draw(canvas, paint);
            leaveShopButton.draw(canvas, paint);

            drawClues(canvas, paint);
        }
    }

    private void drawClues(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        int gap = ApplicationSettings.INSTANCE.getScreenWidth() * 10 / 100;
        int verticalItemWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 35 / 100;
        int verticalItemHeight = ApplicationSettings.INSTANCE.getScreenHeight() * 25 / 100;

        canvas.drawRoundRect(
                new RectF(
                        gap,
                        280,
                        gap + verticalItemWidth,
                        280 + verticalItemHeight
                ), 45, 45, paint);

        canvas.drawRoundRect(
                new RectF(
                        2 * gap + verticalItemWidth,
                        280,
                        2 * gap + 2 * verticalItemWidth,
                        280 + verticalItemHeight
                ), 45, 45, paint);

        canvas.drawRoundRect(
                new RectF(
                        gap,
                        280 + verticalItemHeight + gap,
                        2 * gap + 2 * verticalItemWidth,
                        280 + verticalItemHeight + gap + verticalItemWidth
                ), 45, 45, paint);
    }

    public void onTouch() {
        if (leaveShopButton.wasPressed()) {
            leaveShopButton.onButtonPressed();
        }
    }
}
