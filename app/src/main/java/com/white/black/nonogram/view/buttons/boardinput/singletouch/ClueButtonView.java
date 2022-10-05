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

import java.util.function.Supplier;

public class ClueButtonView extends PicButtonView {

    private final Puzzle puzzle;
    private final Supplier<Integer> numOfAvailableClues;
    private final Runnable onClueButtonPressed;

    public ClueButtonView(
            ViewListener viewListener,
            Puzzle puzzle,
            RectF bounds,
            int color1,
            int color2,
            int color3,
            Bitmap[] innerImages,
            Context context,
            Paint paint,
            Supplier<Integer> numOfAvailableClues,
            Runnable onClueButtonPressed
    ) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.top - bounds.height() * 1 / 10 + (bounds.right - bounds.left)), context, paint);
        this.puzzle = puzzle;
        this.numOfAvailableClues = numOfAvailableClues;
        this.onClueButtonPressed = onClueButtonPressed;
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed()) ? pressedInnerImageBounds : innerImageBounds;
        int numOfAvailableClues = this.numOfAvailableClues.get();

        //if (puzzle.isClueAvailable()) {
        //    canvas.drawBitmap(innerImages[0], null, bounds, paint);
        //} else {
        RectF rechargingClueBounds = new RectF(
                bounds.centerX() - bounds.width() * 15 / 100,
                bounds.centerY() - bounds.height() * 15 / 100,
                bounds.centerX() + bounds.width() * 5 / 10,
                bounds.centerY() + bounds.height() * 5 / 10
        );
        Bitmap image = numOfAvailableClues > 0 ? innerImages[0] : innerImages[1];
        canvas.drawBitmap(image, null, rechargingClueBounds, paint);
        paint.setTextSize(bounds.width() * 45 / 100);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        /*String timeLeft = String.valueOf(((puzzle.getLastClueUsedTime() + Puzzle.getIntervalBetweenCluesMilliseconds()) - System.currentTimeMillis()) / 1000);
        Rect timeLeftTextBounds = new Rect();
        paint.getTextBounds(timeLeft, 0, timeLeft.length(), timeLeftTextBounds);
        canvas.drawText(timeLeft, bounds.left + bounds.width() / 10, bounds.top + timeLeftTextBounds.height(), paint);
        }*/

        String numOfAvailableCluesAsString = String.valueOf(this.numOfAvailableClues.get());
        Rect clueCountRect = new Rect();
        paint.getTextBounds(numOfAvailableCluesAsString, 0, numOfAvailableCluesAsString.length(), clueCountRect);
        canvas.drawText(numOfAvailableCluesAsString, bounds.left + bounds.width() / 10, bounds.top + clueCountRect.height(), paint);
    }

    @Override
    public void onButtonPressed() {
        // puzzle.setClueNotAvailable();
        onClueButtonPressed.run();
    }
}
