package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.R;

public class ShopView {

    private int backgroundColor;
    private RectF backgroundBounds;
    private BankView bankView;
    private Bitmap door;
    private RectF doorBounds;

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
        doorBounds = new RectF(
                100,
                ApplicationSettings.INSTANCE.getScreenHeight() - 292,
                292,
                ApplicationSettings.INSTANCE.getScreenHeight() - 100
        );
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(backgroundColor);
        canvas.drawRect(backgroundBounds, paint);
        bankView.draw(canvas, paint);
        //paint.setColor(Color.WHITE);
        //canvas.drawCircle(doorBounds.centerX(), doorBounds.centerY(), 130, paint);
        canvas.drawBitmap(door, null, doorBounds, paint);
    }
}
