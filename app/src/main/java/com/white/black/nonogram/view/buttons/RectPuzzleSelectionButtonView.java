package com.white.black.nonogram.view.buttons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleFactory;
import com.white.black.nonogram.view.listeners.PuzzleSelectionViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

public class RectPuzzleSelectionButtonView extends ButtonView {

    private final Puzzle puzzle;
    private final int color1;
    private final int color2;
    private final int color3;
    private final Bitmap puzzleImageWhite;
    private final int minWidthHeight;

    public RectPuzzleSelectionButtonView(ViewListener viewListener, RectF bounds, Puzzle puzzle, int color1, int color2, int color3, Bitmap puzzleImageWhite, int minWidthHeight) {
        super(viewListener, bounds);
        this.puzzle = puzzle;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.puzzleImageWhite = puzzleImageWhite;
        this.minWidthHeight = minWidthHeight;
    }

    @Override
    public void onButtonPressed() {
        if (!puzzle.isDone()) {
            ((PuzzleSelectionViewListener)viewListener).onPuzzleButtonPressed();
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        if (puzzle.isDone()) {
            canvas.drawBitmap(puzzle.getFilteredBitmap(), null, bounds , paint);
        } else {
            paint.setColor(color3);
            float dis = (bounds.height() * 5 / 100);
            float disToAddIfPressed = (isPressed())? dis : 0;

            RectF topLeftBounds = new RectF(
                    bounds.centerX() - bounds.width() * 50 / 100 + disToAddIfPressed,
                    bounds.centerY() - bounds.height() * 50 / 100 + disToAddIfPressed,
                    bounds.centerX() + bounds.width() * 45 / 100 + disToAddIfPressed,
                    bounds.centerY() + bounds.height() * 45 / 100 + disToAddIfPressed
            );

            RectF paddedSubPuzzleImageBounds = new RectF(
                    topLeftBounds.centerX() - topLeftBounds.width() * 45 / 100,
                    topLeftBounds.centerY() - topLeftBounds.height() * 45 / 100,
                    topLeftBounds.centerX() + topLeftBounds.width() * 45 / 100,
                    topLeftBounds.centerY() + topLeftBounds.height() * 45 / 100
            );

            if (!isPressed()) {
                Path path = new Path();
                path.moveTo(bounds.left, bounds.top);
                path.lineTo(topLeftBounds.left, topLeftBounds.bottom);
                path.lineTo(topLeftBounds.left + dis, bounds.bottom);
                path.lineTo(bounds.right, bounds.bottom);
                path.lineTo(bounds.right, bounds.top + dis);
                path.lineTo(bounds.right - dis, bounds.top);
                path.lineTo(bounds.left, bounds.top);

                canvas.drawPath(path, paint);
            }

            if (puzzle.isPartiallyDone()) {
                paint.setColor(Color.WHITE);
                canvas.drawRect(topLeftBounds, paint);
                Bitmap partiallySolvedImage = PuzzleFactory.INSTANCE.createBitmapOfFilteredColors(puzzle.getColoringProgressColors());
                float fitPartialImageIntoBoundsMultiplyingFactor = Math.min(topLeftBounds.height() / partiallySolvedImage.getHeight(), topLeftBounds.width() / partiallySolvedImage.getWidth());
                RectF partiallySolvedImageBounds = new RectF(
                        topLeftBounds.centerX() - fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getWidth() / 2,
                        topLeftBounds.centerY() - fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getHeight() / 2,
                        topLeftBounds.centerX() + fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getWidth() / 2,
                        topLeftBounds.centerY() + fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getHeight() / 2
                );
                canvas.drawBitmap(partiallySolvedImage, null, partiallySolvedImageBounds, paint);
                paint.setAlpha(120);
            }

            int gradient1 = (isPressed())? color2 : color1;
            int gradient2 = (isPressed())? color1 : color2;

            paint.setShader(new LinearGradient(
                    topLeftBounds.left,
                    topLeftBounds.top,
                    topLeftBounds.right,
                    topLeftBounds.bottom,
                    new int[]{gradient1, gradient2},
                    new float[]{0f, 1f},
                    Shader.TileMode.MIRROR));
            canvas.drawRect(topLeftBounds, paint);
            paint.setShader(null);

            canvas.drawBitmap(puzzleImageWhite, null, new RectF(
                    paddedSubPuzzleImageBounds.centerX() - minWidthHeight / 5,
                    paddedSubPuzzleImageBounds.centerY() - minWidthHeight / 5,
                    paddedSubPuzzleImageBounds.centerX() + minWidthHeight / 5,
                    paddedSubPuzzleImageBounds.centerY() + minWidthHeight / 5
            ), paint);

            paint.setAlpha(255);
        }
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }
}
