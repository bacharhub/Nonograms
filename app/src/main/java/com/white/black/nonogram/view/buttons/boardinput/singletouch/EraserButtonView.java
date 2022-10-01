package com.white.black.nonogram.view.buttons.boardinput.singletouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.BoardInputValue;
import com.white.black.nonogram.view.buttons.boardinput.GroupedButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class EraserButtonView extends GroupedButtonView<BoardInputValue> {

    public EraserButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, context, paint);
    }

    @Override
    public <V> void draw(Canvas canvas, Paint paint, V currentPressedVal) {
        super.draw(canvas, paint);
        RectF superBounds = (isPressed())? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        if ((currentPressedVal).equals(BoardInputValue.ERASER)) {
            paint.setColor(Color.YELLOW);
            canvas.drawRoundRect(superBounds, curve, curve, paint);
        }

        canvas.drawBitmap(innerImages[0], null, bounds, paint);
    }

    @Override
    public void onButtonPressed() {

    }

    @Override
    public BoardInputValue getPressedButtonValue() {
        return BoardInputValue.ERASER;
    }
}
