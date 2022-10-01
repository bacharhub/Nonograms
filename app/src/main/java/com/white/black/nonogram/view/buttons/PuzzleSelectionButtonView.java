package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleFactory;
import com.white.black.nonogram.PuzzleReference;
import com.white.black.nonogram.R;
import com.white.black.nonogram.SubPuzzle;
import com.white.black.nonogram.view.listeners.ViewListener;

public class PuzzleSelectionButtonView extends PicButtonView  {

    private final int TEXT_SIZE = ApplicationSettings.INSTANCE.getScreenWidth() / 26;
    private final RectF innerImageBounds;
    private final RectF pressedInnerImageBounds;
    private final RectF puzzleImageBounds;
    private final RectF pressedPuzzleImageBounds;
    private final PuzzleReference puzzleReference;
    private final String puzzleSize;
    private final String puzzleName;
    private final int puzzleBorder;
    private final int textHeight;
    private final Bitmap puzzleImageWhite;

    private final int color1;
    private final int color2;
    private final int color3;
    private Bitmap pressedPuzzleSelectionButtonView;
    private Bitmap puzzleSelectionButtonView;

    private static Bitmap vip;
    private static Bitmap lock;

    public PuzzleReference getPuzzleReference() {
        return puzzleReference;
    }

    public PuzzleSelectionButtonView(ViewListener viewListener, PuzzleReference puzzleReference, RectF bounds, int color1, int color2, int color3, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3,null, bounds, context, paint);

        vip = BitmapLoader.INSTANCE.getImage(context, R.drawable.vip_100);
        lock = BitmapLoader.INSTANCE.getImage(context, R.drawable.lock_512);

        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;

        float imageEdgeLength = (super.innerImageBounds.right - super.innerImageBounds.left);
        innerImageBounds = new RectF(
                super.innerImageBounds.left,
                super.innerImageBounds.top + super.innerImageBounds.height() / 5,
                super.innerImageBounds.left + imageEdgeLength,
                super.innerImageBounds.top + super.innerImageBounds.height() / 5 + imageEdgeLength);

        pressedInnerImageBounds = new RectF(
                super.pressedInnerImageBounds.left,
                super.pressedInnerImageBounds.top + super.pressedInnerImageBounds.height() / 5,
                super.pressedInnerImageBounds.left + imageEdgeLength,
                super.pressedInnerImageBounds.top + super.pressedInnerImageBounds.height() / 5 + imageEdgeLength);

        Puzzle puzzle = puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext());

        this.puzzleReference = puzzleReference;
        this.puzzleName = (puzzle.isDone())? puzzle.getName() : context.getString(R.string.unknown_puzzle_name);
        this.puzzleSize = context.getString(R.string.puzzle_size_format, puzzle.getWidth(), puzzle.getHeight());

        puzzleBorder = Color.rgb((Color.red(color2) + Color.red(color3)) / 2, (Color.green(color2) + Color.green(color3)) / 2, (Color.blue(color2) + Color.blue(color3)) / 2);

        paint.setTextSize(TEXT_SIZE);
        Rect textBounds = new Rect();
        paint.getTextBounds(context.getString(R.string.letter), 0, 1, textBounds);
        this.textHeight = textBounds.height();

        puzzleImageWhite = BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_white_512);

        float idealImageHorizontalGapFromCenter = innerImageBounds.width() * 45 / 100;
        float idealImageVerticalGapFromCenter = innerImageBounds.height() * 45 / 100;
        float widthHeightMax = Math.max(puzzle.getWidth(), puzzle.getHeight());
        float widthMultiplyFactor = idealImageHorizontalGapFromCenter / widthHeightMax;
        float heightMultiplyFactor = idealImageVerticalGapFromCenter / widthHeightMax;
        float puzzleImageWidth = puzzle.getWidth() * widthMultiplyFactor;
        float puzzleImageHeight = puzzle.getHeight() * heightMultiplyFactor;

        puzzleImageBounds = new RectF(
                innerImageBounds.centerX() - puzzleImageWidth,
                innerImageBounds.centerY() - puzzleImageHeight,
                innerImageBounds.centerX() + puzzleImageWidth,
                innerImageBounds.centerY() + puzzleImageHeight
        );

        pressedPuzzleImageBounds = new RectF(
                pressedInnerImageBounds.centerX() - puzzleImageWidth,
                pressedInnerImageBounds.centerY() - puzzleImageHeight,
                pressedInnerImageBounds.centerX() + puzzleImageWidth,
                pressedInnerImageBounds.centerY() + puzzleImageHeight
        );
    }

    public Puzzle getPuzzle() {
        return this.puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext());
    }

    public void recycleBitmap() {
        if (pressedPuzzleSelectionButtonView != null && !pressedPuzzleSelectionButtonView.isRecycled()) {
            pressedPuzzleSelectionButtonView.recycle();
        }

        if (puzzleSelectionButtonView != null && !puzzleSelectionButtonView.isRecycled()) {
            puzzleSelectionButtonView.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (isPressed()) {
            if (pressedPuzzleSelectionButtonView == null || pressedPuzzleSelectionButtonView.isRecycled()) {
                synchronized (this) {
                    if (pressedPuzzleSelectionButtonView == null || pressedPuzzleSelectionButtonView.isRecycled()) {
                        pressedPuzzleSelectionButtonView = new PuzzleSelectionButtonView(viewListener, puzzleReference, new RectF(0, 0, bounds.width(), bounds.height()), color1, color2, color3, (Context)viewListener, paint).drawToBitmap(paint,true);
                    }
                }
            }

            canvas.drawBitmap(pressedPuzzleSelectionButtonView, null, super.backgroundBounds, paint);
        } else {
            if (puzzleSelectionButtonView == null || puzzleSelectionButtonView.isRecycled()) {
                synchronized (this) {
                    if (puzzleSelectionButtonView == null || puzzleSelectionButtonView.isRecycled()) {
                        puzzleSelectionButtonView = new PuzzleSelectionButtonView(viewListener, puzzleReference, new RectF(0, 0, bounds.width(), bounds.height()), color1, color2, color3, (Context)viewListener, paint).drawToBitmap(paint,false);
                    }
                }
            }

            canvas.drawBitmap(puzzleSelectionButtonView, null, super.backgroundBounds, paint);
        }
    }

    private Bitmap drawToBitmap(Paint paint, boolean isPressed) {
        Bitmap imageBitmap;
        RectF backgroundBounds = (isPressed)? super.pressedBounds : super.backgroundBounds;
        imageBitmap = Bitmap.createBitmap((int)backgroundBounds.right, (int)backgroundBounds.bottom, Bitmap.Config.ARGB_8888);
        imageBitmap.setDensity(Bitmap.DENSITY_NONE);
        Canvas canvas = new Canvas(imageBitmap);

        super.draw(canvas, paint, isPressed);
        RectF innerBackgroundBounds = (isPressed) ? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        RectF innerImageBounds = (isPressed) ? this.pressedInnerImageBounds : this.innerImageBounds;
        RectF bounds = (isPressed) ? super.pressedBounds : super.bounds;
        RectF puzzleImageBounds = (isPressed) ? this.pressedPuzzleImageBounds : this.puzzleImageBounds;

        paint.setColor(backgroundColor);
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        String puzzleName = (this.puzzleName.length() >= 9)? this.puzzleName.substring(0, 7) + ".." : this.puzzleName;
        canvas.drawText(puzzleName, innerImageBounds.centerX(), (innerImageBounds.top + innerBackgroundBounds.top) / 2 + textHeight / 2, paint);
        canvas.drawText(puzzleSize, innerImageBounds.centerX(), (innerImageBounds.bottom + innerBackgroundBounds.bottom) / 2 + textHeight / 2, paint);

        Puzzle puzzle = puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext());

        if (puzzle.getPuzzleClass() != null && puzzle.getPuzzleClass().equals(Puzzle.PuzzleClass.VIP)  && !AdManager.isRemoveAds()) {
            paint.setColor(puzzleBorder);
            canvas.drawRect(bounds.left, innerImageBounds.top, bounds.right, innerImageBounds.bottom, paint);
            paint.setShader(gradient);
            canvas.drawRect(innerBackgroundBounds.left, innerImageBounds.top, innerBackgroundBounds.right, innerImageBounds.bottom, paint);
            paint.setShader(null);

            canvas.drawBitmap(vip, null, new RectF(
                    innerImageBounds.left + innerImageBounds.width() / 30 + (innerBackgroundBounds.left - bounds.left),
                    innerImageBounds.top + innerImageBounds.width() / 30,
                    innerImageBounds.left + innerImageBounds.width() / 30 + (innerImageBounds.width() / 4) * 94 / 70 + (innerBackgroundBounds.left - bounds.left),
                    innerImageBounds.top + innerImageBounds.width() / 30 + innerImageBounds.width() / 4
            ), paint);

            canvas.drawBitmap(lock, null, new RectF(
                    puzzleImageBounds.centerX() - innerBackgroundBounds.width() / 5,
                    puzzleImageBounds.centerY() - innerBackgroundBounds.width() / 5,
                    puzzleImageBounds.centerX() + innerBackgroundBounds.width() / 5,
                    puzzleImageBounds.centerY() + innerBackgroundBounds.width() / 5
            ), paint);
            paint.setAlpha(255);
        } else {
            if (puzzle.isDone()) {
                canvas.drawBitmap(puzzle.getFilteredBitmap(), null, puzzleImageBounds, paint);
            } else {
                if (puzzle.getSubPuzzles().size() == 0) {
                    paint.setColor(puzzleBorder);
                    canvas.drawRect(bounds.left, innerImageBounds.top, bounds.right, innerImageBounds.bottom, paint);

                    if (puzzle.isPartiallyDone()) {
                        paint.setColor(Color.WHITE);
                        canvas.drawRect(innerBackgroundBounds.left, innerImageBounds.top, innerBackgroundBounds.right, innerImageBounds.bottom, paint);
                        Bitmap partiallySolvedImage = PuzzleFactory.INSTANCE.createBitmapOfFilteredColors(puzzle.getColoringProgressColors());
                        float fitPartialImageIntoBoundsMultiplyingFactor = Math.min(innerImageBounds.height() / partiallySolvedImage.getHeight(), innerBackgroundBounds.width() / partiallySolvedImage.getWidth());
                        RectF partiallySolvedImageBounds = new RectF(
                                innerBackgroundBounds.centerX() - fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getWidth() / 2,
                                innerImageBounds.centerY() - fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getHeight() / 2,
                                innerBackgroundBounds.centerX() + fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getWidth() / 2,
                                innerImageBounds.centerY() + fitPartialImageIntoBoundsMultiplyingFactor * partiallySolvedImage.getHeight() / 2
                        );
                        canvas.drawBitmap(partiallySolvedImage, null, partiallySolvedImageBounds, paint);
                        paint.setAlpha(120);
                    }

                    paint.setShader(gradient);
                    canvas.drawRect(innerBackgroundBounds.left, innerImageBounds.top, innerBackgroundBounds.right, innerImageBounds.bottom, paint);
                    paint.setShader(null);
                    canvas.drawBitmap(puzzleImageWhite, null, new RectF(
                            puzzleImageBounds.centerX() - innerBackgroundBounds.width() / 5,
                            puzzleImageBounds.centerY() - innerBackgroundBounds.width() / 5,
                            puzzleImageBounds.centerX() + innerBackgroundBounds.width() / 5,
                            puzzleImageBounds.centerY() + innerBackgroundBounds.width() / 5
                    ), paint);
                    paint.setAlpha(255);
                } else {
                    int minWidthHeight = Math.min((int)(puzzle.getSubPuzzles().get(puzzle.getSubPuzzles().size() - 1).getPuzzle().getWidth() * puzzleImageBounds.width() / puzzle.getWidth()), (int)(puzzle.getSubPuzzles().get(puzzle.getSubPuzzles().size() - 1).getPuzzle().getHeight() * puzzleImageBounds.height() / puzzle.getHeight()));
                    for (SubPuzzle subPuzzle : puzzle.getSubPuzzles()) {
                        RectF subPuzzleImageBounds = new RectF(
                                puzzleImageBounds.left + subPuzzle.getX() * puzzleImageBounds.width() / puzzle.getWidth(),
                                puzzleImageBounds.top + subPuzzle.getY() * puzzleImageBounds.height() / puzzle.getHeight(),
                                puzzleImageBounds.left + (subPuzzle.getX() + subPuzzle.getPuzzle().getWidth()) * puzzleImageBounds.width() / puzzle.getWidth(),
                                puzzleImageBounds.top + (subPuzzle.getY() + subPuzzle.getPuzzle().getHeight()) * puzzleImageBounds.height() / puzzle.getHeight()
                        );

                        new RectPuzzleSelectionButtonView(viewListener, subPuzzleImageBounds, subPuzzle.getPuzzle(), color1, color2, color3, puzzleImageWhite, minWidthHeight).draw(canvas, paint);
                    }
                }
            }
        }

        return imageBitmap;
    }

    @Override
    public void onButtonPressed() {

    }
}
