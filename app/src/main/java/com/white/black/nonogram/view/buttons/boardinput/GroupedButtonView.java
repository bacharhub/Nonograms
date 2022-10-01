package com.white.black.nonogram.view.buttons.boardinput;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public abstract class GroupedButtonView<V> extends PicButtonView {

    protected GroupedButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

    public abstract <V> V getPressedButtonValue();

    public abstract <V> void draw(Canvas canvas, Paint paint, V currentPressedVal);
}
