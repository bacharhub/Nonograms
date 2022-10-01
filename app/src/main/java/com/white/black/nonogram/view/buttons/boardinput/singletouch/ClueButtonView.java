package com.white.black.nonogram.view.buttons.boardinput.singletouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class ClueButtonView extends PicButtonView {

    private final Puzzle puzzle;

    public ClueButtonView(ViewListener viewListener, Puzzle puzzle, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.top - bounds.height() * 1 / 10 + (bounds.right - bounds.left)), context, paint);
        this.puzzle = puzzle;
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        if (puzzle.isClueAvailable()) {
            canvas.drawBitmap(innerImages[0], null, bounds, paint);
        } else {
            RectF rechargingClueBounds = new RectF(
                    bounds.centerX() - bounds.width() * 15 / 100,
                    bounds.centerY() - bounds.height() * 15 / 100,
                    bounds.centerX() + bounds.width() * 5 / 10,
                    bounds.centerY() + bounds.height() * 5 / 10
            );
            canvas.drawBitmap(innerImages[1], null, rechargingClueBounds, paint);
            paint.setTextSize(bounds.width() * 45 / 100);
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.LEFT);
            String timeLeft = String.valueOf(((puzzle.getLastClueUsedTime() + Puzzle.getIntervalBetweenCluesMilliseconds()) - System.currentTimeMillis()) / 1000);
            Rect timeLeftTextBounds = new Rect();
            paint.getTextBounds(timeLeft, 0, timeLeft.length(), timeLeftTextBounds);
            canvas.drawText(timeLeft, bounds.left + bounds.width() / 10, bounds.top + timeLeftTextBounds.height(), paint);
        }
    }

    @Override
    public void onButtonPressed() {
        puzzle.setClueNotAvailable();
    }
}
