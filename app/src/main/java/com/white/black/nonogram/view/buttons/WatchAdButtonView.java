package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class WatchAdButtonView extends PicButtonView {

    public WatchAdButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, bounds, context, paint);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {}

    @Override
    public void onButtonPressed() {
        ((GameViewListener)viewListener).onWatchVideo();
    }
}
