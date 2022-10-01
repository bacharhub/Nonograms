package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleReference;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.SubPuzzle;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
import com.white.black.nonogram.view.buttons.RectPuzzleSelectionButtonView;
import com.white.black.nonogram.view.buttons.TrashCanButtonView;
import com.white.black.nonogram.view.listeners.PuzzleSelectionViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.HashMap;
import java.util.Map;

public enum PuzzleSelectionView {

    INSTANCE;

    private static final int PUZZLE_SOLVING_TIME_DESC_FONT_SIZE = ApplicationSettings.INSTANCE.getScreenWidth() / 18;
    private static final int PUZZLE_DIMENSIONS_FONT_SIZE = ApplicationSettings.INSTANCE.getScreenWidth() / 23;
    private static final int PUZZLE_NAME_FONT_SIZE = ApplicationSettings.INSTANCE.getScreenWidth() / 15;
    private int backgroundColor;
    private RectF windowBounds;
    private RectF windowBackgroundBounds;
    private RectF windowInnerBackgroundBounds;
    private RectF puzzleImageBounds;
    private RectF puzzleImageBackgroundBounds;
    private RectF clockBounds;
    private String puzzleDimensionsDesc;
    private int puzzleDimensionsDescHeight;
    private int puzzleSolvingTimeDescHeight;
    private int puzzleNameDescHeight;
    private LinearGradient gradient;
    private int windowBackgroundColor;
    private int windowInnerBackgroundColor;
    private int windowInnerBackgroundGradientTo;
    private int curve;
    private CloseWindowButtonView closeWindowButtonView;
    private TrashCanButtonView trashCanButtonView;
    private PuzzleReference puzzleReference;
    private Puzzle selectedPuzzle;
    private Appearance appearance;
    private Bitmap puzzleImageWhite;
    private Bitmap clock;
    private ViewListener viewListener;
    private String incompletePuzzleNameDescription;
    private Map<String, RectPuzzleSelectionButtonView> subPuzzleButtons;

    public String getPuzzleDimensionsDesc() {
        return puzzleDimensionsDesc;
    }

    public int getPuzzleDimensionsDescHeight() {
        return puzzleDimensionsDescHeight;
    }

    public int getPuzzleSolvingTimeDescHeight() {
        return puzzleSolvingTimeDescHeight;
    }

    public int getPuzzleNameDescHeight() {
        return puzzleNameDescHeight;
    }

    public static int getPuzzleSolvingTimeDescFontSize() {
        return PUZZLE_SOLVING_TIME_DESC_FONT_SIZE;
    }

    public PuzzleReference getPuzzleReference() {
        return this.puzzleReference;
    }

    public static int getPuzzleDimensionsFontSize() {
        return PUZZLE_DIMENSIONS_FONT_SIZE;
    }

    public static int getPuzzleNameFontSize() {
        return PUZZLE_NAME_FONT_SIZE;
    }

    public Puzzle getOverallPuzzle() { return  puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext()); }

    public Puzzle getSelectedPuzzle() {
        return selectedPuzzle;
    }

    PuzzleSelectionView() {
        this.appearance = Appearance.MINIMIZED;
    }

    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public void setPuzzleReference(PuzzleReference puzzleReference) {
        this.puzzleReference = puzzleReference;
    }

    public void init(Context context, PuzzleReference puzzleReference, ViewListener viewListener, Paint paint) {
        subPuzzleButtons = new HashMap<>();

        Puzzle puzzle = puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext());
        if (puzzle.getSubPuzzles().size() > 0) {
            for (SubPuzzle subPuzzle : puzzle.getSubPuzzles()) {
                if (!subPuzzle.getPuzzle().isDone()) {
                    this.selectedPuzzle = subPuzzle.getPuzzle();
                    break;
                }
            }
        } else {
            this.selectedPuzzle = puzzle;
        }

        this.viewListener = viewListener;
        this.puzzleReference = puzzleReference;
        int color1 = Puzzles.getCurrent().getColorPack().getColor1();
        int color2 = Puzzles.getCurrent().getColorPack().getColor2();
        int color3 = Puzzles.getCurrent().getColorPack().getColor3();

        backgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);
        windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 25 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 85 / 100
        );

        windowInnerBackgroundColor = color1;
        windowInnerBackgroundGradientTo = color2;
        windowBackgroundColor = color3;

        this.gradient = new LinearGradient(
                windowBounds.left,
                windowBounds.top,
                windowBounds.right,
                windowBounds.bottom,
                new int[]{windowInnerBackgroundColor, windowInnerBackgroundGradientTo},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        windowBackgroundBounds = new RectF(windowBounds.left, windowBounds.top, windowBounds.right + windowBounds.width() * 2 / 100, windowBounds.bottom + windowBounds.height() * 2 / 100);
        this.curve = ApplicationSettings.INSTANCE.getScreenWidth() / 30;
        float padding = windowBounds.width() / 60;
        this.windowInnerBackgroundBounds = new RectF(windowBounds.left + padding, windowBounds.top + padding, windowBounds.right - padding, windowBounds.bottom - padding);

        float onTopButtonEdgeLength = windowBounds.height() / 9;
        RectF closeButtonBounds = new RectF(
                windowBackgroundBounds.right - onTopButtonEdgeLength,
                windowBounds.top - windowBounds.height() / 20 - onTopButtonEdgeLength,
                windowBackgroundBounds.right,
                windowBounds.top - windowBounds.height() / 20
        );

        closeWindowButtonView = new CloseWindowButtonView(
                (ViewListener)context,
                closeButtonBounds,
                windowInnerBackgroundColor, windowInnerBackgroundGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF trashCanButtonBounds = new RectF(
                windowBackgroundBounds.left,
                windowBounds.top - windowBounds.height() / 20 - onTopButtonEdgeLength,
                windowBackgroundBounds.left + onTopButtonEdgeLength,
                windowBounds.top - windowBounds.height() / 20
        );

        trashCanButtonView = new TrashCanButtonView(
                (ViewListener)context,
                trashCanButtonBounds,
                windowInnerBackgroundColor, windowInnerBackgroundGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.restart_512)}, context, paint);

        RectF extendedPuzzleImageBackgroundBounds = new RectF(
                windowBounds.left,
                windowBounds.centerY() - (windowBounds.right - windowBounds.left) / 2,
                windowBounds.right,
                windowBounds.centerY() + (windowBounds.right - windowBounds.left) / 2
        );

        puzzleImageBackgroundBounds = new RectF(
                windowInnerBackgroundBounds.left,
                extendedPuzzleImageBackgroundBounds.top,
                windowInnerBackgroundBounds.right,
                extendedPuzzleImageBackgroundBounds.bottom
                );

        float idealImageHorizontalGapFromCenter = puzzleImageBackgroundBounds.width() * 45 / 100;
        float idealImageVerticalGapFromCenter = puzzleImageBackgroundBounds.height() * 45 / 100;
        float widthHeightMax = Math.max(puzzle.getWidth(), puzzle.getHeight());
        float widthMultiplyFactor = idealImageHorizontalGapFromCenter / widthHeightMax;
        float heightMultiplyFactor = idealImageVerticalGapFromCenter / widthHeightMax;
        float puzzleImageWidth = puzzle.getWidth() * widthMultiplyFactor;
        float puzzleImageHeight = puzzle.getHeight() * heightMultiplyFactor;

        puzzleImageBounds = new RectF(
                puzzleImageBackgroundBounds.centerX() - puzzleImageWidth,
                puzzleImageBackgroundBounds.centerY() - puzzleImageHeight,
                puzzleImageBackgroundBounds.centerX() + puzzleImageWidth,
                puzzleImageBackgroundBounds.centerY() + puzzleImageHeight
        );

        float clockSize = windowBounds.height() / 12;

        clockBounds = new RectF(
                windowInnerBackgroundBounds.left + clockSize / 2,
                windowInnerBackgroundBounds.bottom - clockSize * 3 / 2,
                windowInnerBackgroundBounds.left + clockSize * 3 / 2,
                windowInnerBackgroundBounds.bottom - clockSize / 2
        );

        paint.setTextSize(PUZZLE_DIMENSIONS_FONT_SIZE);
        Rect textBounds = new Rect();
        this.puzzleDimensionsDesc = context.getString(R.string.puzzle_size_format, puzzle.getWidth(), puzzle.getHeight());
        paint.getTextBounds(puzzleDimensionsDesc, 0, puzzleDimensionsDesc.length(), textBounds);
        this.puzzleDimensionsDescHeight = textBounds.height();
        paint.setTextSize(PUZZLE_SOLVING_TIME_DESC_FONT_SIZE);
        textBounds = new Rect();
        paint.getTextBounds(context.getString(R.string.letter), 0, 1, textBounds);
        this.puzzleSolvingTimeDescHeight = textBounds.height();
        paint.setTextSize(PUZZLE_NAME_FONT_SIZE);
        textBounds = new Rect();
        paint.getTextBounds(puzzle.getName(), 0, puzzle.getName().length(), textBounds);
        this.puzzleNameDescHeight = textBounds.height();

        incompletePuzzleNameDescription = context.getString(R.string.unknown_puzzle_name);

        puzzleImageWhite = BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_white_512);
        clock = BitmapLoader.INSTANCE.getImage(context, R.drawable.alarm_clock_100);
    }

    private void selectPuzzle() {
        if (subPuzzleButtons != null) {
            for (RectPuzzleSelectionButtonView rectPuzzleSelectionButtonView : subPuzzleButtons.values()) {
                if (rectPuzzleSelectionButtonView.wasPressed()) {
                    TouchMonitor.INSTANCE.setTouchUp(false);
                    this.selectedPuzzle = rectPuzzleSelectionButtonView.getPuzzle();
                    rectPuzzleSelectionButtonView.onButtonPressed();
                    break;
                }
            }
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);

        paint.setAlpha(255);

        paint.setColor(windowBackgroundColor);
        canvas.drawRoundRect(windowBackgroundBounds, curve, curve, paint);
        paint.setShader(gradient);
        canvas.drawRoundRect(windowBounds, curve, curve, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(windowInnerBackgroundBounds, curve, curve, paint);

        closeWindowButtonView.draw(canvas, paint);
        trashCanButtonView.draw(canvas, paint);

        Puzzle puzzle = puzzleReference.getPuzzle(((Context) viewListener).getApplicationContext());

        canvas.drawBitmap(clock, null, clockBounds, paint);
        paint.setTextSize(PUZZLE_SOLVING_TIME_DESC_FONT_SIZE);
        paint.setColor(Color.BLACK);
        canvas.drawText(puzzle.getSolvingTimeHumanFormat(), clockBounds.right + clockBounds.width() / 4, clockBounds.centerY() + puzzleSolvingTimeDescHeight / 2, paint);
        paint.setTextSize(PUZZLE_DIMENSIONS_FONT_SIZE);
        float distFromEdge = windowInnerBackgroundBounds.width() / 30;
        float puzzleDimensionsY = windowInnerBackgroundBounds.top + distFromEdge + puzzleDimensionsDescHeight;
        canvas.drawText(puzzleDimensionsDesc, windowInnerBackgroundBounds.left + distFromEdge, puzzleDimensionsY, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        String puzzleNameDesc = (puzzle.isDone())? puzzle.getName() : incompletePuzzleNameDescription;
        paint.setTextSize(PUZZLE_NAME_FONT_SIZE);
        if (puzzleNameDesc.length() > 15) {
            if (puzzleNameDesc.length() > 20) {
                paint.setTextSize(PUZZLE_NAME_FONT_SIZE * 6 / 10);
            } else {
                paint.setTextSize(PUZZLE_NAME_FONT_SIZE * 8 / 10);
            }
        }

        canvas.drawText(puzzleNameDesc, windowInnerBackgroundBounds.centerX(), puzzleDimensionsY / 2 + puzzleImageBackgroundBounds.top / 2 + puzzleNameDescHeight / 2, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        if (puzzle.isDone()) {
            canvas.drawBitmap(puzzle.getFilteredBitmap(), null, puzzleImageBounds, paint);
        } else {
            if (puzzle.getSubPuzzles().size() == 0) {
                RectPuzzleSelectionButtonView rectPuzzleSelectionButtonView = subPuzzleButtons.get(puzzle.getName());
                if (rectPuzzleSelectionButtonView == null) {
                    RectF singlePuzzleBounds = new RectF(
                            puzzleImageBackgroundBounds.centerX() - puzzleImageBackgroundBounds.width() * 45 / 100,
                            puzzleImageBackgroundBounds.centerY() - puzzleImageBackgroundBounds.width() * 45 / 100,
                            puzzleImageBackgroundBounds.centerX() + puzzleImageBackgroundBounds.width() * 45 / 100,
                            puzzleImageBackgroundBounds.centerY() + puzzleImageBackgroundBounds.width() * 45 / 100
                    );

                    rectPuzzleSelectionButtonView = new RectPuzzleSelectionButtonView(viewListener, singlePuzzleBounds, puzzle, windowInnerBackgroundColor, windowInnerBackgroundGradientTo, windowBackgroundColor, puzzleImageWhite, (int)puzzleImageBackgroundBounds.width());
                    subPuzzleButtons.put(puzzle.getName(), rectPuzzleSelectionButtonView);
                }

                rectPuzzleSelectionButtonView.draw(canvas, paint);
            } else {
                int minWidthHeight = Math.min((int)(puzzle.getSubPuzzles().get(puzzle.getSubPuzzles().size() - 1).getPuzzle().getWidth() * puzzleImageBounds.width() / puzzle.getWidth()), (int)(puzzle.getSubPuzzles().get(puzzle.getSubPuzzles().size() - 1).getPuzzle().getHeight() * puzzleImageBounds.height() / puzzle.getHeight()));
                for (SubPuzzle subPuzzle : puzzle.getSubPuzzles()) {
                    RectPuzzleSelectionButtonView rectPuzzleSelectionButtonView = subPuzzleButtons.get(puzzle.getName() + subPuzzle.getX() + subPuzzle.getY());
                    if (rectPuzzleSelectionButtonView == null) {
                        RectF subPuzzleImageBounds = new RectF(
                                puzzleImageBounds.left + subPuzzle.getX() * puzzleImageBounds.width() / puzzle.getWidth(),
                                puzzleImageBounds.top + subPuzzle.getY() * puzzleImageBounds.height() / puzzle.getHeight(),
                                puzzleImageBounds.left + (subPuzzle.getX() + subPuzzle.getPuzzle().getWidth()) * puzzleImageBounds.width() / puzzle.getWidth(),
                                puzzleImageBounds.top + (subPuzzle.getY() + subPuzzle.getPuzzle().getHeight()) * puzzleImageBounds.height() / puzzle.getHeight()
                        );

                        rectPuzzleSelectionButtonView = new RectPuzzleSelectionButtonView(viewListener, subPuzzleImageBounds, subPuzzle.getPuzzle(), windowInnerBackgroundColor, windowInnerBackgroundGradientTo, windowBackgroundColor, puzzleImageWhite, minWidthHeight);
                        subPuzzleButtons.put(puzzle.getName() + subPuzzle.getX() + subPuzzle.getY(), rectPuzzleSelectionButtonView);
                    }

                    rectPuzzleSelectionButtonView.draw(canvas, paint);
                }
            }
        }

        if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            YesNoQuestion.INSTANCE.draw(canvas, paint);
        }
    }


    public void onTouchEvent() {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                YesNoQuestion.INSTANCE.onTouchEvent();
            } else {
                if (!windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y) &&
                        !windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)) {
                    if (trashCanButtonView.wasPressed()) {
                        ((PuzzleSelectionViewListener)viewListener).onStartOverButtonPressed();
                        MyMediaPlayer.play("blop");
                    } else {
                        appearance = Appearance.MINIMIZED;
                        MyMediaPlayer.play("blop");
                    }
                }

                selectPuzzle();

                TouchMonitor.INSTANCE.setTouchUp(false);
            }
        }
    }
}
