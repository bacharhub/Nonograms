package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.BoardInputValue;
import com.white.black.nonogram.ColoredNumber;
import com.white.black.nonogram.ColoringProgress;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleFactory;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class BoardView {

    private static final int FAST_CLICK_IN_MILLISECONDS = 350;
    private final ViewListener viewListener;
    private final Puzzle puzzle;
    private int boardMaxHeight;
    private final int boardTop;
    private final float slotSizeToFontSizeMultiplicationFactor;
    private final float zoomedSlotSizeToFontSizeMultiplicationFactor;

    private int maxNumbersRowSize;
    private int numberOfSlotsInARow;
    private int maxNumbersColumnSize;
    private int numberOfSlotsInAColumn;
    private float slotSize;
    private int curve;
    private int fontSize;
    private float zoomedSlotSize;
    private int zoomedFontSize;
    private float completionSlotLength;
    private int miniPicSlotSize;
    private int slotTouchDownX = -1; // not initialized;
    private int slotTouchDownY = -1; // not initialized;
    private int slotTouchUpX = -1; // not initialized;
    private int slotTouchUpY = -1; // not initialized;
    private int clueX = -1; // not initialized;
    private int clueY = -1; // not initialized;
    private int slotMarkClueColor;

    private RectF boardBackgroundBounds;
    private RectF rowBounds;
    private RectF columnBounds;
    private RectF miniPicBounds;
    private RectF gridBounds;

    private Bitmap question;
    private Bitmap disqualify;
    private Bitmap permanentDisqualify;
    private Bitmap bulb;
    private Bitmap handCursor;

    private ClickIntention clickIntention = ClickIntention.NONE;
    private boolean vibrating;
    private SparseArray<Bitmap> colorBitmapMap;
    private SparseIntArray colorToTextColorMap;
    private Map<Point, Bitmap> numbersBitmapMap;
    private Map<Point, Bitmap> zoomedNumbersBitmapMap;

    // CAMERA
    private float cameraX;
    private float cameraPointerX = -1;
    private float cameraPointerY = -1;
    private float addToCameraX;
    private float cameraY;
    private float addToCameraY;

    private float scale = 1f;
    private float addToScale = 0f;
    private float scaleDownPointer1X = -1;
    private float scaleDownPointer1Y = -1;
    private float scaleDownPointer2X = -1;
    private float scaleDownPointer2Y = -1;

    private boolean isTutorial;

    private boolean isVibrating() {
        return vibrating;
    }

    public int getBoardTop() {
        return this.boardTop;
    }

    public int getBoardBottom() {
        return this.boardTop + boardMaxHeight;
    }

    private double calculateAccumulatedDistanceFromCenter(float x1, float y1, float x2, float y2, float x3, float y3) {
        float centerX = x3;
        float centerY = y3;
        double firstPointerDistanceFromStartCenter = Math.sqrt((x1 - centerX) * (x1 - centerX) + (y1 - centerY) * (y1 - centerY));
        double secondPointerDistanceFromStartCenter = Math.sqrt((x2 - centerX) * (x2 - centerX) + (y2 - centerY) * (y2 - centerY));
        double fingersAccumulatedDistanceFromStartCenter = firstPointerDistanceFromStartCenter + secondPointerDistanceFromStartCenter;
        return fingersAccumulatedDistanceFromStartCenter;
    }

    private enum CameraState {
        ZOOM,
        MOVEMENT,
        NONE
    }

    private enum ClickIntention {
        FILL_SLOT,
        MOVE_BOARD,
        NONE
    }

    private CameraState cameraState = CameraState.NONE;

    public void handleCamera() {
        if (TouchMonitor.INSTANCE.touchDown(0)) {
            if (TouchMonitor.INSTANCE.touchDown(1)) {
                if (cameraState != CameraState.ZOOM) {
                    if (TouchMonitor.INSTANCE.getDownCoordinates().y >= boardTop && TouchMonitor.INSTANCE.getDownCoordinates().y <= getBoardBottom() && TouchMonitor.INSTANCE.getDownCoordinates(1).y >= boardTop && TouchMonitor.INSTANCE.getDownCoordinates(1).y <= getBoardBottom()) {
                        cameraState = CameraState.ZOOM;
                        clickIntention = ClickIntention.MOVE_BOARD;
                        slotTouchDownX = -1;
                        slotTouchDownY = -1;
                        slotTouchUpX = -1;
                        slotTouchUpY = -1;

                        scaleDownPointer1X = (TouchMonitor.INSTANCE.getDownCoordinates(0).x);
                        scaleDownPointer1Y = (TouchMonitor.INSTANCE.getDownCoordinates(0).y);
                        scaleDownPointer2X = (TouchMonitor.INSTANCE.getDownCoordinates(1).x);
                        scaleDownPointer2Y = (TouchMonitor.INSTANCE.getDownCoordinates(1).y);

                        if (scale != 1f) {
                            double startAccumulatedDistance = calculateAccumulatedDistanceFromCenter(scaleDownPointer1X, scaleDownPointer1Y, scaleDownPointer2X, scaleDownPointer2Y, (scaleDownPointer1X + scaleDownPointer2X) / 2, (scaleDownPointer1Y + scaleDownPointer2Y) / 2);
                            double endAccumulatedDistance = calculateAccumulatedDistanceFromCenter((TouchMonitor.INSTANCE.getMove(0).x), (TouchMonitor.INSTANCE.getMove(0).y), (TouchMonitor.INSTANCE.getMove(1).x), (TouchMonitor.INSTANCE.getMove(1).y), (scaleDownPointer1X + scaleDownPointer2X) / 2, (scaleDownPointer1Y + scaleDownPointer2Y) / 2);
                            float newScale = (float) (endAccumulatedDistance - startAccumulatedDistance) / 200;
                            addToCameraX = -(((scaleDownPointer2X + scaleDownPointer1X) / 2) - ApplicationSettings.INSTANCE.getScreenWidth() / 2) * ((scale + newScale) - 1f);
                            addToCameraY = -(((scaleDownPointer2Y + scaleDownPointer1Y) / 2) - boardTop) * ((scale + newScale) - 1f);
                            cameraX -= addToCameraX;
                            cameraY -= addToCameraY;
                        }

                        initMovementPointers();
                    }
                } else {
                    double startAccumulatedDistance = calculateAccumulatedDistanceFromCenter(scaleDownPointer1X, scaleDownPointer1Y, scaleDownPointer2X, scaleDownPointer2Y, (scaleDownPointer1X + scaleDownPointer2X) / 2, (scaleDownPointer1Y + scaleDownPointer2Y) / 2);
                    double endAccumulatedDistance = calculateAccumulatedDistanceFromCenter((TouchMonitor.INSTANCE.getMove(0).x), (TouchMonitor.INSTANCE.getMove(0).y), (TouchMonitor.INSTANCE.getMove(1).x), (TouchMonitor.INSTANCE.getMove(1).y), (scaleDownPointer1X + scaleDownPointer2X) / 2, (scaleDownPointer1Y + scaleDownPointer2Y) / 2);

                    float newScale = (float) (endAccumulatedDistance - startAccumulatedDistance) / 200;
                    addToCameraX = -(((scaleDownPointer2X + scaleDownPointer1X) / 2) - ApplicationSettings.INSTANCE.getScreenWidth() / 2) * ((scale + newScale) - 1f);
                    addToCameraY = -(((scaleDownPointer2Y + scaleDownPointer1Y) / 2) - boardTop) * ((scale + newScale) - 1f);

                    if (newScale < 0) {
                        newScale = newScale * 2;
                    }

                    if (scale + newScale >= 1f) {
                        if (newScale != addToScale) {
                            addToScale = newScale;
                            initDimensions();
                        }
                    } else {
                        cameraState = CameraState.NONE;
                        initMovement();
                        initZoom();
                        initDimensions();
                    }
                }
            } else {
                if (!isAttemptingToFillSlots()) {
                    if (cameraState == CameraState.NONE) {
                        if (TouchMonitor.INSTANCE.getDownCoordinates().y >= boardTop && TouchMonitor.INSTANCE.getDownCoordinates().y <= getBoardBottom()) {
                            cameraState = CameraState.MOVEMENT;
                            cameraPointerX = TouchMonitor.INSTANCE.getDownCoordinates().x;
                            cameraPointerY = TouchMonitor.INSTANCE.getDownCoordinates().y;
                        }
                    } else if (cameraState == CameraState.MOVEMENT) {
                        if (scale > 1f) {
                            if (clickIntention.equals(ClickIntention.NONE) && !shortDistanceHasBeenMade()) {
                                clickIntention = ClickIntention.MOVE_BOARD;
                            }

                            if (boardBackgroundBounds.width() > ApplicationSettings.INSTANCE.getScreenWidth()) {
                                addToCameraX = 3 * (TouchMonitor.INSTANCE.getMove().x - cameraPointerX) / scale;
                            }

                            if (boardBackgroundBounds.height() > boardMaxHeight) {
                                addToCameraY = 3 * (TouchMonitor.INSTANCE.getMove().y - cameraPointerY) / scale;
                            }

                            if (addToCameraY != 0 || addToCameraX != 0) {
                                initDimensions();
                            }
                        }
                    }
                }
            }
        } else {
            if (cameraState != CameraState.ZOOM || (TouchMonitor.INSTANCE.touchUp(1))) {
                releaseZoomAndMovement();
            }
        }
    }

    public BoardView(
            ViewListener viewListener,
            Puzzle puzzle,
            int boardTop,
            int boardMaxHeight,
            float slotSizeToFontSizeMultiplicationFactor,
            float zoomedSlotSizeToFontSizeMultiplicationFactor
    ) {
        this.viewListener = viewListener;
        this.puzzle = puzzle;
        this.boardTop = boardTop;
        this.boardMaxHeight = boardMaxHeight;
        this.slotSizeToFontSizeMultiplicationFactor = slotSizeToFontSizeMultiplicationFactor;
        this.zoomedSlotSizeToFontSizeMultiplicationFactor = zoomedSlotSizeToFontSizeMultiplicationFactor;
    }

    public void setBoardMaxHeight(int boardMaxHeight) {
        this.boardMaxHeight = boardMaxHeight;
    }

    private boolean legalSlot(int x, int y) {
        Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
        if (isTutorial && solutionStep != null) {
            return x >= solutionStep.getXLeft() && x <= solutionStep.getXRight() && y >= solutionStep.getYTop() && y <= solutionStep.getYBottom();
        }

        return (x >= 0 && x < puzzle.getWidth() && y >= 0 && y < puzzle.getHeight());
    }

    private boolean legalSlotX(int x) {
        Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
        if (isTutorial && solutionStep != null) {
            return legalSlot(x, solutionStep.getYTop());
        }

        return legalSlot(x, 0);
    }

    private boolean legalSlotY(int y) {
        Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
        if (isTutorial && solutionStep != null) {
            return legalSlot(solutionStep.getXLeft(), y);
        }

        return legalSlot(0, y);
    }

    private void fillGridSlotsOnMovementRouter(Canvas canvas, Paint paint, BoardInputValue boardInputValue, int color) {
        if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.JOYSTICK)) {
            if (slotTouchUpX != -1 && slotTouchUpY != -1) {
                fillGridSlotsOnMovement(canvas, paint, boardInputValue, color);
            }
        } else {
            if (isAttemptingToFillSlots()) {
                fillGridSlotsOnMovementTouch(canvas, paint, boardInputValue, color);
            }
        }
    }

    public void moveTouchDownSlotUsingJoystick(int xd, int yd) {
        this.slotTouchDownX = Math.max(this.slotTouchDownX, 0);
        this.slotTouchDownY = Math.max(this.slotTouchDownY, 0);

        if (xd != 0 || yd != 0) {
            MyMediaPlayer.play("select");
        }

        if (this.slotTouchDownX + xd >= 0 && this.slotTouchDownX + xd < puzzle.getWidth()) {
            this.slotTouchDownX += xd;
        }

        if (this.slotTouchDownY + yd >= 0 && this.slotTouchDownY + yd < puzzle.getHeight()) {
            this.slotTouchDownY += yd;
        }

        this.slotTouchUpX = this.slotTouchDownX;
        this.slotTouchUpY = this.slotTouchDownY;
    }

    public void moveTouchUpSlotToStartIfOutOfBounds() {
        this.slotTouchUpX = Math.max(this.slotTouchUpX, 0);
        this.slotTouchUpY = Math.max(this.slotTouchUpY, 0);
    }

    public void moveTouchUpSlotUsingJoystick(int xd, int yd) {
        moveTouchUpSlotToStartIfOutOfBounds();

        if (xd != 0 || yd != 0) {
            MyMediaPlayer.play("select");
        }

        if (this.slotTouchUpX + xd >= 0 && this.slotTouchUpX + xd < puzzle.getWidth()) {
            this.slotTouchUpX += xd;
        }

        if (this.slotTouchUpY + yd >= 0 && this.slotTouchUpY + yd < puzzle.getHeight()) {
            this.slotTouchUpY += yd;
        }
    }

    private void fillGridSlotsOnMovement(Canvas canvas, Paint paint, BoardInputValue boardInputValue, int color) {
        int minDownUpX = Math.max(0, Math.min(slotTouchUpX, slotTouchDownX));
        int maxDownUpX = Math.max(slotTouchUpX, slotTouchDownX);
        int minDownUpY = Math.max(0, Math.min(slotTouchUpY, slotTouchDownY));
        int maxDownUpY = Math.max(slotTouchUpY, slotTouchDownY);

        RectF slotBounds = new RectF();

        for (int x = minDownUpX; x <= maxDownUpX; x++) {
            for (int y = minDownUpY; y <= maxDownUpY; y++) {
                if (boardInputValue != null) {
                    float slotCenterX = rowBounds.right + x * slotSize + slotSize / 2;
                    float slotCenterY = columnBounds.bottom + y * slotSize + slotSize / 2;
                    slotBounds.set(slotCenterX - slotSize * 4 / 10,
                            slotCenterY - slotSize * 4 / 10,
                            slotCenterX + slotSize * 4 / 10,
                            slotCenterY + slotSize * 4 / 10);

                    if (puzzle.getBoardInputValue(x, y) != BoardInputValue.PERMANENT_DISQUALIFY) {
                        paint.setColor(Color.WHITE);
                        canvas.drawRect(slotBounds, paint);

                        if (boardInputValue.equals(BoardInputValue.BRUSH)) {
                            slotBounds.set(slotCenterX - slotSize * 5 / 10,
                                    slotCenterY - slotSize * 5 / 10,
                                    slotCenterX + slotSize * 5 / 10,
                                    slotCenterY + slotSize * 5 / 10);
                            paint.setAlpha(100);
                            canvas.drawBitmap(colorBitmapMap.get(color), null, slotBounds, paint);
                            paint.setAlpha(255);
                        } else if (boardInputValue.equals(BoardInputValue.DISQUALIFY)) {
                            canvas.drawBitmap(disqualify, null, slotBounds, paint);
                        } else if (boardInputValue.equals(BoardInputValue.QUESTION_MARK)) {
                            canvas.drawBitmap(question, null, slotBounds, paint);
                        }
                    }
                }
            }
        }

        drawSlotCounter(canvas, paint, maxDownUpX, minDownUpX, maxDownUpY, minDownUpY);
    }

    private void fillGridSlotsOnMovementForTutorial(Canvas canvas, Paint paint) {
        Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
        if (solutionStep == null) {
            return;
        }

        int color = puzzle.getColorSet().get(0);
        RectF slotBounds = new RectF();

        int interval = 500;
        int xMaxInterval = (solutionStep.getXRight() - solutionStep.getXLeft() + 1) * interval;
        int yMaxInterval = (solutionStep.getYBottom() - solutionStep.getYTop() + 1) * interval;
        long actualXInterval = System.currentTimeMillis() % ((long) xMaxInterval);
        long actualYInterval = System.currentTimeMillis() % ((long) yMaxInterval);
        long progressFromXLeftToXRight = actualXInterval / interval;
        long progressFromYTopToYBottom = actualYInterval / interval;

        for (int x = solutionStep.getXLeft(); x <= solutionStep.getXLeft() + progressFromXLeftToXRight; x++) {
            for (int y = solutionStep.getYTop(); y <= solutionStep.getYTop() + progressFromYTopToYBottom; y++) {
                float slotCenterX = rowBounds.right + x * slotSize + slotSize / 2;
                float slotCenterY = columnBounds.bottom + y * slotSize + slotSize / 2;
                slotBounds.set(slotCenterX - slotSize * 4 / 10,
                        slotCenterY - slotSize * 4 / 10,
                        slotCenterX + slotSize * 4 / 10,
                        slotCenterY + slotSize * 4 / 10);

                paint.setColor(Color.WHITE);
                canvas.drawRect(slotBounds, paint);

                slotBounds.set(slotCenterX - slotSize * 5 / 10,
                        slotCenterY - slotSize * 5 / 10,
                        slotCenterX + slotSize * 5 / 10,
                        slotCenterY + slotSize * 5 / 10);
                paint.setAlpha(100);
                canvas.drawBitmap(colorBitmapMap.get(color), null, slotBounds, paint);
                paint.setAlpha(255);
            }
        }

        float handCursorXLeft = rowBounds.right + slotSize / 2 + solutionStep.getXLeft() * slotSize + (solutionStep.getXRight() - solutionStep.getXLeft()) * slotSize * (actualXInterval / (float) xMaxInterval);
        float handCursorYTop = columnBounds.bottom + slotSize / 2 + solutionStep.getYTop() * slotSize + (solutionStep.getYBottom() - solutionStep.getYTop()) * slotSize * (actualYInterval / (float) yMaxInterval);
        RectF handCursorBounds = new RectF(
                handCursorXLeft - slotSize * 4 / 10,
                handCursorYTop,
                handCursorXLeft + slotSize * 4 / 10,
                handCursorYTop + slotSize * 8 / 10
        );

        canvas.drawBitmap(handCursor, null, handCursorBounds, paint);
    }

    private void fillGridSlotsOnMovementTouch(Canvas canvas, Paint paint, BoardInputValue boardInputValue, int color) {
        if (TouchMonitor.INSTANCE.touchDown() && !TouchMonitor.INSTANCE.touchUp() && legalSlot(slotTouchDownX, slotTouchDownY)) {
            float boardTouchX = TouchMonitor.INSTANCE.getMove().x - gridBounds.left;
            float boardTouchY = TouchMonitor.INSTANCE.getMove().y - gridBounds.top;
            int slotX = Math.min(numberOfSlotsInARow - maxNumbersRowSize - 1, Math.max(0, (int) (boardTouchX / slotSize)));
            int slotY = Math.min(numberOfSlotsInAColumn - maxNumbersColumnSize - 1, Math.max(0, (int) (boardTouchY / slotSize)));

            if (legalSlotX(slotX)) {
                if (slotTouchUpX != slotX && GameSettings.INSTANCE.getAppearance().equals(Appearance.MINIMIZED)) {
                    MyMediaPlayer.play("select");
                }

                slotTouchUpX = slotX;
            }

            if (legalSlotY(slotY)) {
                if (slotTouchUpY != slotY && GameSettings.INSTANCE.getAppearance().equals(Appearance.MINIMIZED)) {
                    MyMediaPlayer.play("select");
                }

                slotTouchUpY = slotY;
            }

            if (legalSlot(slotTouchUpX, slotTouchUpY)) {
                fillGridSlotsOnMovement(canvas, paint, boardInputValue, color);
            }
        }
    }

    private void drawSlotCounter(Canvas canvas, Paint paint, int maxDownUpX, int minDownUpX, int maxDownUpY, int minDownUpY) {
        if (maxDownUpX - minDownUpX > 1 || maxDownUpY - minDownUpY > 1) {
            float slotCounterX;
            float slotCounterY;

            Rect textBounds = new Rect();
            String text = (maxDownUpX - minDownUpX + 1) + " x " + (maxDownUpY - minDownUpY + 1);
            paint.setTextSize(fontSize);
            paint.getTextBounds(text, 0, text.length(), textBounds);

            if (slotTouchUpX > slotTouchDownX) {
                slotCounterX = slotTouchDownX + 0.5f;
            } else {
                slotCounterX = slotTouchDownX - 0.5f - textBounds.width() / slotSize;
            }

            if (slotTouchUpY > slotTouchDownY) {
                slotCounterY = slotTouchDownY + 0.5f;
            } else {
                slotCounterY = slotTouchDownY - 0.5f;
            }

            RectF slotCounterBounds = new RectF(
                    rowBounds.right + slotCounterX * slotSize,
                    columnBounds.bottom + slotCounterY * slotSize,
                    rowBounds.right + (slotCounterX + 1) * slotSize + textBounds.width(),
                    columnBounds.bottom + (slotCounterY + 1) * slotSize
            );

            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(slotCounterBounds, curve * 3, curve * 3, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(slotCounterBounds, curve * 3, curve * 3, paint);
            paint.setTextSize(fontSize);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, slotCounterBounds.centerX(), slotCounterBounds.centerY() + textBounds.height() / 2, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }

    private void editSlot(BoardInputValue boardInputValue, int color) {
        cameraState = CameraState.NONE;
        clueX = -1;
        clueY = -1;
        int minDownUpX = Math.max(0, Math.min(slotTouchUpX, slotTouchDownX));
        int maxDownUpX = Math.max(slotTouchUpX, slotTouchDownX);
        int minDownUpY = Math.max(0, Math.min(slotTouchUpY, slotTouchDownY));
        int maxDownUpY = Math.max(slotTouchUpY, slotTouchDownY);

        ColoringProgress coloringProgress = puzzle.copyColoringProgress();

        for (int x = minDownUpX; x <= maxDownUpX; x++) {
            for (int y = minDownUpY; y <= maxDownUpY; y++) {
                if (puzzle.getBoardInputValue(x, y) != BoardInputValue.PERMANENT_DISQUALIFY) {
                    puzzle.putBoardInputValue(x, y, boardInputValue);
                    if (boardInputValue.equals(BoardInputValue.BRUSH)) {
                        puzzle.colorAtPoint(x, y, color);
                    } else {
                        puzzle.colorAtPoint(x, y, 0);
                    }

                    fillWithDisqualify(puzzle, x, y);
                }
            }
        }

        if (!puzzle.copyColoringProgress().equals(coloringProgress)) {
            puzzle.addColoringProgressToUndo(coloringProgress);
        }

        if (isTutorial) {
            Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
            if (solutionStep != null) {
                boolean stepComplete = true;
                for (int x = solutionStep.getXLeft(); x <= solutionStep.getXRight(); x++) {
                    for (int y = solutionStep.getYTop(); y <= solutionStep.getYBottom(); y++) {
                        if (puzzle.copyColoringProgress().getColoringProgress()[x][y] != color) {
                            stepComplete = false;
                        }
                    }
                }

                if (stepComplete) {
                    puzzle.nextSolutionStep();
                }
            }
        }

        TouchMonitor.INSTANCE.setTouchUp(false);
    }

    private void fillWithDisqualify(Puzzle puzzle, int x, int y) {
        if (puzzle.isRowComplete(y)) {
            puzzle.fillRowBoardInputValueIfEmpty(y, BoardInputValue.DISQUALIFY);
        }

        if (puzzle.isColumnComplete(x)) {
            puzzle.fillColumnBoardInputValueIfEmpty(x, BoardInputValue.DISQUALIFY);
        }
    }

    private void editSlotUsingTouch(BoardInputValue boardInputValue, int color) {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (legalSlot(slotTouchDownX, slotTouchDownY) || (clickedFast() && shortDistanceHasBeenMade())) {
                float boardTouchX = TouchMonitor.INSTANCE.getUpCoordinates().x - gridBounds.left;
                float boardTouchY = TouchMonitor.INSTANCE.getUpCoordinates().y - gridBounds.top;
                int slotX = (int) (boardTouchX / slotSize);
                int slotY = (int) (boardTouchY / slotSize);


                if (clickedFast() && shortDistanceHasBeenMade()) {
                    boardTouchX = TouchMonitor.INSTANCE.getDownCoordinates().x - gridBounds.left;
                    boardTouchY = TouchMonitor.INSTANCE.getDownCoordinates().y - gridBounds.top;
                    if (legalSlot((int) (boardTouchX / slotSize), (int) (boardTouchY / slotSize))) {
                        if (legalSlotX(slotX)) {
                            slotTouchUpX = slotX;
                        }

                        if (legalSlotY(slotY)) {
                            slotTouchUpY = slotY;
                        }

                        if (legalSlot(slotTouchUpX, slotTouchUpY)) {
                            MyMediaPlayer.play("select");
                            slotTouchDownX = slotTouchUpX;
                            slotTouchDownY = slotTouchUpY;
                            editSlot(boardInputValue, color);
                        }
                    }
                } else {
                    if (legalSlotX(slotX)) {
                        slotTouchUpX = slotX;
                    }

                    if (legalSlotY(slotY)) {
                        slotTouchUpY = slotY;
                    }

                    if (legalSlot(slotTouchUpX, slotTouchUpY)) {
                        editSlot(boardInputValue, color);
                    }
                }
            }

            clickIntention = ClickIntention.NONE;
            slotTouchDownX = -1;
            slotTouchDownY = -1;
            vibrating = false;
        } else if (TouchMonitor.INSTANCE.touchDown()) {
            if (TouchMonitor.INSTANCE.getDownCoordinates().y < boardTop + boardMaxHeight && TouchMonitor.INSTANCE.getDownCoordinates().y > boardTop) {
                if (isAttemptingToFillSlots()) {
                    float boardTouchX;
                    float boardTouchY;

                    if (clickIntention == ClickIntention.NONE) {
                        clickIntention = ClickIntention.FILL_SLOT;
                        if (!zoomed() && !shortDistanceHasBeenMade()) {
                            boardTouchX = TouchMonitor.INSTANCE.getDownCoordinates().x - gridBounds.left;
                            boardTouchY = TouchMonitor.INSTANCE.getDownCoordinates().y - gridBounds.top;
                        } else {
                            boardTouchX = TouchMonitor.INSTANCE.getMove().x - gridBounds.left;
                            boardTouchY = TouchMonitor.INSTANCE.getMove().y - gridBounds.top;
                        }

                        slotTouchDownX = (int) (boardTouchX / slotSize);
                        slotTouchDownY = (int) (boardTouchY / slotSize);
                    }
                } else {
                    ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
                    ses.schedule(vibrationRunnable, FAST_CLICK_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                    ses.shutdown();
                }
            } else {
                slotTouchDownX = -1;
                slotTouchDownY = -1;
            }
        }
    }

    private final Runnable vibrationRunnable = new Runnable() {
        @Override
        public void run() {
            if (scale > 1f && isAttemptingToFillSlots() && !isVibrating() && !cameraState.equals(CameraState.ZOOM) && TouchMonitor.INSTANCE.touchDown()) {
                ((GameViewListener) viewListener).onZoomedSlotSelected();
                vibrating = true;
            }
        }
    };

    public void editSlotRouter(BoardInputValue boardInputValue, int color) {
        if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.JOYSTICK)) {
            MyMediaPlayer.play("blop");
            this.slotTouchDownX = Math.max(this.slotTouchDownX, 0);
            this.slotTouchDownY = Math.max(this.slotTouchDownY, 0);
            this.slotTouchUpX = Math.max(this.slotTouchUpX, 0);
            this.slotTouchUpY = Math.max(this.slotTouchUpY, 0);
            editSlot(boardInputValue, color);
            this.slotTouchDownX = this.slotTouchUpX;
            this.slotTouchDownY = this.slotTouchUpY;
        } else {
            editSlotUsingTouch(boardInputValue, color);
        }
    }

    private boolean shortDistanceHasBeenMade() {
        double distBetweenMoveAndDown = Math.sqrt((TouchMonitor.INSTANCE.getMove().x - TouchMonitor.INSTANCE.getDownCoordinates().x) * (TouchMonitor.INSTANCE.getMove().x - TouchMonitor.INSTANCE.getDownCoordinates().x) + (TouchMonitor.INSTANCE.getMove().y - TouchMonitor.INSTANCE.getDownCoordinates().y) * (TouchMonitor.INSTANCE.getMove().y - TouchMonitor.INSTANCE.getDownCoordinates().y));
        return distBetweenMoveAndDown < ApplicationSettings.INSTANCE.getScreenWidth() / 15;
    }

    private boolean clickedFast() {
        return (TouchMonitor.INSTANCE.touchUp() && TouchMonitor.INSTANCE.getUpTime() - TouchMonitor.INSTANCE.getDownTime() < FAST_CLICK_IN_MILLISECONDS);
    }

    private boolean zoomed() {
        return scale + addToScale != 1f;
    }

    private boolean isAttemptingToFillSlots() {
        boolean clickLastsLongEnoughToFillASlot = System.currentTimeMillis() - TouchMonitor.INSTANCE.getDownTime() > FAST_CLICK_IN_MILLISECONDS;
        boolean clickedFast = clickedFast();
        boolean shortDistanceHasBeenMade = shortDistanceHasBeenMade();
        boolean usingTouch = GameSettings.INSTANCE.getInput().equals(GameSettings.Input.TOUCH);

        return usingTouch && ((!zoomed() && !shortDistanceHasBeenMade) || (clickedFast && shortDistanceHasBeenMade) || (clickLastsLongEnoughToFillASlot && shortDistanceHasBeenMade) || clickIntention.equals(ClickIntention.FILL_SLOT));
    }

    private void fixBoardBackgroundBounds() {
        if (scale > 1f) {
            if (boardBackgroundBounds.left >= 0 && boardBackgroundBounds.right > ApplicationSettings.INSTANCE.getScreenWidth()) {
                boardBackgroundBounds.set(
                        0,
                        boardBackgroundBounds.top,
                        boardBackgroundBounds.right - boardBackgroundBounds.left,
                        boardBackgroundBounds.bottom
                );


                if (TouchMonitor.INSTANCE.touchUp()) {
                    cameraX = -(ApplicationSettings.INSTANCE.getScreenWidth() / 2 - boardBackgroundBounds.width() / 2);
                }
            }

            if (boardBackgroundBounds.right <= ApplicationSettings.INSTANCE.getScreenWidth() && boardBackgroundBounds.left < 0) {
                boardBackgroundBounds.set(
                        boardBackgroundBounds.left + (ApplicationSettings.INSTANCE.getScreenWidth() - boardBackgroundBounds.right),
                        boardBackgroundBounds.top,
                        ApplicationSettings.INSTANCE.getScreenWidth(),
                        boardBackgroundBounds.bottom
                );


                if (TouchMonitor.INSTANCE.touchUp()) {
                    cameraX = (ApplicationSettings.INSTANCE.getScreenWidth() / 2 - boardBackgroundBounds.width() / 2);
                }
            }

            if (boardBackgroundBounds.top >= boardTop && boardBackgroundBounds.bottom > getBoardBottom()) {
                boardBackgroundBounds.set(
                        boardBackgroundBounds.left,
                        boardTop,
                        boardBackgroundBounds.right,
                        boardBackgroundBounds.bottom - (boardBackgroundBounds.top - boardTop)
                );

                if (TouchMonitor.INSTANCE.touchUp()) {
                    cameraY = 0;
                }
            }

            if (boardBackgroundBounds.bottom < getBoardBottom()) {
                boardBackgroundBounds.set(
                        boardBackgroundBounds.left,
                        boardBackgroundBounds.top + (getBoardBottom() - boardBackgroundBounds.bottom),
                        boardBackgroundBounds.right,
                        getBoardBottom()
                );

                if (TouchMonitor.INSTANCE.touchUp()) {
                    cameraY = boardBackgroundBounds.top - boardTop;
                }
            }
        }
    }

    private void initDimensions() {
        int backgroundWidthToUse = ApplicationSettings.INSTANCE.getScreenWidth() * 98 / 100;

        int backgroundHeightToUse;
        if (boardMaxHeight * numberOfSlotsInARow / numberOfSlotsInAColumn > backgroundWidthToUse) {
            backgroundHeightToUse = backgroundWidthToUse * numberOfSlotsInAColumn / numberOfSlotsInARow;
        } else {
            backgroundHeightToUse = boardMaxHeight;
            backgroundWidthToUse = boardMaxHeight * numberOfSlotsInARow / numberOfSlotsInAColumn;
        }

        backgroundWidthToUse *= (scale + addToScale);
        backgroundHeightToUse *= (scale + addToScale);
        completionSlotLength = (scale + addToScale) * ApplicationSettings.INSTANCE.getScreenHeight() / 100;

        slotSize = (int) Math.min((double) (backgroundWidthToUse - completionSlotLength) / numberOfSlotsInARow, (double) (backgroundHeightToUse - completionSlotLength) / numberOfSlotsInAColumn);

        float pushCameraLeftBy = cameraX + addToCameraX;
        float pushCameraUpBy = cameraY + addToCameraY;

        boardBackgroundBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() / 2 - numberOfSlotsInARow * slotSize / 2 - completionSlotLength / 2 + pushCameraLeftBy,
                boardTop + pushCameraUpBy,
                ApplicationSettings.INSTANCE.getScreenWidth() / 2 + numberOfSlotsInARow * slotSize / 2 + completionSlotLength / 2 + pushCameraLeftBy,
                boardTop + numberOfSlotsInAColumn * slotSize + completionSlotLength + pushCameraUpBy
        );

        fixBoardBackgroundBounds();

        curve = ApplicationSettings.INSTANCE.getScreenWidth() / 80;

        rowBounds = new RectF(
                boardBackgroundBounds.left,
                boardBackgroundBounds.top + maxNumbersColumnSize * slotSize + completionSlotLength,
                boardBackgroundBounds.left + maxNumbersRowSize * slotSize + completionSlotLength,
                boardBackgroundBounds.bottom
        );

        columnBounds = new RectF(
                boardBackgroundBounds.left + maxNumbersRowSize * slotSize + completionSlotLength,
                boardBackgroundBounds.top,
                boardBackgroundBounds.right,
                boardBackgroundBounds.top + maxNumbersColumnSize * slotSize + completionSlotLength
        );

        fontSize = (int) (slotSize * slotSizeToFontSizeMultiplicationFactor);
        zoomedSlotSize = slotSize * 100 / 96;
        zoomedFontSize = (int) (zoomedSlotSize * zoomedSlotSizeToFontSizeMultiplicationFactor);

        miniPicBounds = new RectF(
                rowBounds.left,
                columnBounds.top,
                rowBounds.right,
                columnBounds.bottom
        );

        miniPicSlotSize = (int) Math.min(miniPicBounds.width() / puzzle.getWidth(), miniPicBounds.height() / puzzle.getHeight());

        gridBounds = new RectF(
                rowBounds.right,
                columnBounds.bottom,
                boardBackgroundBounds.right,
                boardBackgroundBounds.bottom
        );
    }

    public void init(Context context) {
        isTutorial = puzzle.isTutorial();
        slotMarkClueColor = Color.CYAN;
        bulb = BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512);
        handCursor = BitmapLoader.INSTANCE.getImage(context, R.drawable.hand_cursor_512);
        question = BitmapLoader.INSTANCE.getImage(context, R.drawable.question_mark_100);
        disqualify = BitmapLoader.INSTANCE.getImage(context, R.drawable.disqualify_100);
        permanentDisqualify = BitmapLoader.INSTANCE.getImage(context, R.drawable.permanent_disqualify_100);
        maxNumbersRowSize = getMaxNumbersRowSize(puzzle);
        numberOfSlotsInARow = puzzle.getWidth() + maxNumbersRowSize;
        maxNumbersColumnSize = getMaxNumbersColumnSize(puzzle);
        numberOfSlotsInAColumn = puzzle.getHeight() + maxNumbersColumnSize;

        initDimensions();

        this.colorBitmapMap = new SparseArray<>();
        this.colorToTextColorMap = new SparseIntArray();
        Paint paint = PaintManager.INSTANCE.createPaint();
        for (Integer color : puzzle.getColorSet()) {
            this.colorBitmapMap.put(color, createColorBitMap(paint, color));
            this.colorToTextColorMap.put(color, (PuzzleFactory.INSTANCE.getDistanceBetweenColors(color, Color.WHITE) > PuzzleFactory.INSTANCE.getDistanceBetweenColors(color, Color.BLACK)) ? Color.WHITE : Color.BLACK);
        }

        numbersBitmapMap = new HashMap<>();
        zoomedNumbersBitmapMap = new HashMap<>();

        clear();
    }

    private Bitmap createColorBitMap(Paint paint, Integer color) {
        Bitmap imageBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        imageBitmap.setDensity(Bitmap.DENSITY_NONE);
        Canvas canvas = new Canvas(imageBitmap);
        drawGridColor(canvas, paint, color);
        return imageBitmap;
    }

    private void drawBackgroundAndBackgroundOutline(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        canvas.drawRoundRect(boardBackgroundBounds, curve, curve, paint);
        paint.setAlpha(255);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(boardBackgroundBounds, curve, curve, paint);
    }

    private void drawRowAndColumnBackgroundAndBackgroundOutline(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rowBounds, curve, curve, paint);
        canvas.drawRoundRect(columnBounds, curve, curve, paint);
    }

    private void drawRowAndColumnNumbers(Canvas canvas, Paint paint, Integer colorToZoomFor) {
        paint.setTextAlign(Paint.Align.CENTER);
        drawRowNumbers(canvas, paint, puzzle, rowBounds, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor);
        drawColumnNumbers(canvas, paint, puzzle, columnBounds, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawRowAndColumnCompletion(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);

        int y = (int) rowBounds.top;
        int rowCounter = 0;
        for (List<ColoredNumber> row : puzzle.getNumbers().getRows()) {
            if (puzzle.hasColoringProgress()) {
                if (row.equals(PuzzleFactory.INSTANCE.getRowColors(rowCounter, puzzle.getColoringProgressColors()))) {
                    paint.setColor(Color.GREEN);
                } else {
                    paint.setColor(Color.RED);
                }
            } else {
                if (row.size() == 0) {
                    paint.setColor(Color.GREEN);
                } else {
                    paint.setColor(Color.RED);
                }
            }

            rowCounter++;
            canvas.drawRoundRect(rowBounds.right - completionSlotLength, y, rowBounds.right, y + slotSize, curve, curve, paint);
            y += slotSize;
        }

        int x = (int) columnBounds.left;
        int columnCounter = 0;
        for (List<ColoredNumber> column : puzzle.getNumbers().getColumns()) {
            if (puzzle.hasColoringProgress()) {
                if (column.equals(PuzzleFactory.INSTANCE.getColumnColors(columnCounter, puzzle.getColoringProgressColors()))) {
                    paint.setColor(Color.GREEN);
                } else {
                    paint.setColor(Color.RED);
                }
            } else {
                if (column.size() == 0) {
                    paint.setColor(Color.GREEN);
                } else {
                    paint.setColor(Color.RED);
                }
            }

            columnCounter++;
            canvas.drawRoundRect(x, columnBounds.bottom - completionSlotLength, x + slotSize, columnBounds.bottom, curve, curve, paint);
            x += slotSize;
        }
    }

    private void drawRowAndColumn(Canvas canvas, Paint paint, Integer colorToZoomFor) {
        drawRowAndColumnBackgroundAndBackgroundOutline(canvas, paint);
        drawRowAndColumnCompletion(canvas, paint);
        drawRowAndColumnNumbers(canvas, paint, colorToZoomFor);
    }

    public void draw(Canvas canvas, Paint paint, BoardInputValue boardInputValue, Integer colorToZoomFor) {
        drawBackgroundAndBackgroundOutline(canvas, paint);
        drawSlotMark(canvas, paint);
        drawRowAndColumn(canvas, paint, colorToZoomFor);
        drawGrid(canvas, paint);
        drawSolutionStep(canvas, paint);
        fillGridSlots(canvas, paint, colorToZoomFor);

        if (isTutorial && !TouchMonitor.INSTANCE.touchDown()) {
            fillGridSlotsOnMovementForTutorial(canvas, paint);
        } else {
            fillGridSlotsOnMovementRouter(canvas, paint, boardInputValue, colorToZoomFor);
        }

        drawSlotNumbers(canvas, paint);
        drawMiniPic(canvas, paint);
    }

    private void drawSlotNumbers(Canvas canvas, Paint paint) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(fontSize);
        for (int y = 4; y < numberOfSlotsInAColumn - maxNumbersColumnSize - 1; y += 5) {
            float slotCenterX = rowBounds.right + (numberOfSlotsInARow - maxNumbersRowSize - 1) * slotSize + slotSize / 2;
            float slotCenterY = columnBounds.bottom + y * slotSize + slotSize / 2;
            Rect numBounds = new Rect();
            String num = String.valueOf(y + 1);
            paint.getTextBounds(num, 0, num.length(), numBounds);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawText(num, slotCenterX, slotCenterY + numBounds.height() / 2, paint);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(num, slotCenterX, slotCenterY + numBounds.height() / 2, paint);
        }

        for (int x = 4; x < numberOfSlotsInARow - maxNumbersRowSize - 1; x += 5) {
            float slotCenterX = rowBounds.right + x * slotSize + slotSize / 2;
            float slotCenterY = columnBounds.bottom + (numberOfSlotsInAColumn - maxNumbersColumnSize - 1) * slotSize + slotSize / 2;
            Rect numBounds = new Rect();
            String num = String.valueOf(x + 1);
            paint.getTextBounds(num, 0, num.length(), numBounds);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawText(num, slotCenterX, slotCenterY + numBounds.height() / 2, paint);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(num, slotCenterX, slotCenterY + numBounds.height() / 2, paint);
        }
    }

    private void drawSlotMark(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);

        if (legalSlot(clueX, clueY)) {
            paint.setColor(slotMarkClueColor);
            paint.setAlpha(50);
            canvas.drawRect(boardBackgroundBounds.left, rowBounds.top + clueY * slotSize, boardBackgroundBounds.right, rowBounds.top + (clueY + 1) * slotSize, paint);
            canvas.drawRect(columnBounds.left + clueX * slotSize, boardBackgroundBounds.top, columnBounds.left + (clueX + 1) * slotSize, boardBackgroundBounds.bottom, paint);
        }

        if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.JOYSTICK)) {
            paint.setColor(Color.MAGENTA);
            paint.setAlpha(50);
            float currentStrokeWidth = paint.getStrokeWidth();
            if (legalSlot(slotTouchUpX, slotTouchUpY)) {
                canvas.drawRect(boardBackgroundBounds.left, rowBounds.top + slotTouchUpY * slotSize, boardBackgroundBounds.right, rowBounds.top + (slotTouchUpY + 1) * slotSize, paint);
                canvas.drawRect(columnBounds.left + slotTouchUpX * slotSize, boardBackgroundBounds.top, columnBounds.left + (slotTouchUpX + 1) * slotSize, boardBackgroundBounds.bottom, paint);
                paint.setAlpha(255);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(slotSize / 10);
                canvas.drawRect(rowBounds.right + slotTouchUpX * slotSize + slotSize / 20, columnBounds.bottom + slotTouchUpY * slotSize + slotSize / 20, rowBounds.right + (slotTouchUpX + 1) * slotSize - slotSize / 20, columnBounds.bottom + (slotTouchUpY + 1) * slotSize - slotSize / 20, paint);
            } else {
                canvas.drawRect(boardBackgroundBounds.left, rowBounds.top, boardBackgroundBounds.right, rowBounds.top + slotSize, paint);
                canvas.drawRect(columnBounds.left, boardBackgroundBounds.top, columnBounds.left + slotSize, boardBackgroundBounds.bottom, paint);
                paint.setAlpha(255);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(slotSize / 10);
                canvas.drawRect(rowBounds.right + slotSize / 20, columnBounds.bottom + slotSize / 20, rowBounds.right + slotSize - slotSize / 20, columnBounds.bottom + slotSize - slotSize / 20, paint);
            }

            paint.setStrokeWidth(currentStrokeWidth);
        } else if (!legalSlot(clueX, clueY) && legalSlot(slotTouchUpX, slotTouchUpY)) {
            paint.setColor(Color.MAGENTA);
            paint.setAlpha(50);
            canvas.drawRect(boardBackgroundBounds.left, rowBounds.top + slotTouchUpY * slotSize, boardBackgroundBounds.right, rowBounds.top + (slotTouchUpY + 1) * slotSize, paint);
            canvas.drawRect(columnBounds.left + slotTouchUpX * slotSize, boardBackgroundBounds.top, columnBounds.left + (slotTouchUpX + 1) * slotSize, boardBackgroundBounds.bottom, paint);
        }

        paint.setAlpha(255);
    }

    private void drawMiniPic(Canvas canvas, Paint paint) {
        if (puzzle.hasColoringProgress()) {
            float xOffset = (miniPicBounds.width() - puzzle.getWidth() * miniPicSlotSize) / 2;
            float yOffset = (miniPicBounds.height() - puzzle.getHeight() * miniPicSlotSize) / 2;
            int[][] coloringProgress = puzzle.getColoringProgressColors();
            for (int x = 0; x < puzzle.getWidth(); x++) {
                for (int y = 0; y < puzzle.getHeight(); y++) {
                    paint.setColor(coloringProgress[x][y]);
                    canvas.drawRect(
                            miniPicBounds.left + x * miniPicSlotSize + xOffset,
                            miniPicBounds.top + y * miniPicSlotSize + yOffset,
                            miniPicBounds.left + (x + 1) * miniPicSlotSize + xOffset,
                            miniPicBounds.top + (y + 1) * miniPicSlotSize + yOffset,
                            paint
                    );
                }
            }
        }
    }

    private void drawGridColor(Canvas canvas, Paint paint, int color) {
        Rect slotBounds = new Rect(1, 1, 9, 9);

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float lgt = hsv[2];
        if (lgt > 0.85f) {
            lgt = 0.85f;
        } else if (lgt < 0.15f) {
            lgt = 0.15f;
        }

        float moreLgt = Math.min(1f, lgt + 0.15f);
        float lessLgt = Math.max(0f, lgt - 0.15f);

        Rect moreLgtBounds = new Rect(
                2,
                2,
                8,
                3
        );

        Rect lessLgtBounds = new Rect(
                2,
                2,
                3,
                8
        );

        paint.setColor(Color.HSVToColor(new float[]{hsv[0], hsv[1], lgt}));
        canvas.drawRect(slotBounds, paint);
        paint.setColor(Color.HSVToColor(new float[]{hsv[0], hsv[1], moreLgt}));
        canvas.drawRect(moreLgtBounds, paint);
        paint.setColor(Color.HSVToColor(new float[]{hsv[0], hsv[1], lessLgt}));
        canvas.drawRect(lessLgtBounds, paint);
    }

    private void fillGridSlots(Canvas canvas, Paint paint, Integer brushColor) {
        RectF slotBounds = new RectF();
        if (puzzle.hasColoringProgress()) {
            int[][] coloringProgress = puzzle.getColoringProgressColors();
            for (int x = 0; x < numberOfSlotsInARow - maxNumbersRowSize; x++) {
                for (int y = 0; y < numberOfSlotsInAColumn - maxNumbersColumnSize; y++) {
                    float slotCenterX = rowBounds.right + x * slotSize + slotSize / 2;
                    float slotCenterY = columnBounds.bottom + y * slotSize + slotSize / 2;
                    if (puzzle.getBoardInputValue(x, y) != null) {
                        if (puzzle.getBoardInputValue(x, y).equals(BoardInputValue.BRUSH)) {
                            float colorSizeFactor = (brushColor.intValue() == coloringProgress[x][y]) ? 1f : 0.85f;
                            slotBounds.set((int) (slotCenterX - colorSizeFactor * slotSize * 5 / 10),
                                    (int) (slotCenterY - colorSizeFactor * slotSize * 5 / 10),
                                    (int) (slotCenterX + colorSizeFactor * slotSize * 5 / 10),
                                    (int) (slotCenterY + colorSizeFactor * slotSize * 5 / 10));
                            canvas.drawBitmap(colorBitmapMap.get(coloringProgress[x][y]), null, slotBounds, paint);
                        } else {
                            slotBounds.set(slotCenterX - slotSize * 4 / 10,
                                    slotCenterY - slotSize * 4 / 10,
                                    slotCenterX + slotSize * 4 / 10,
                                    slotCenterY + slotSize * 4 / 10);
                            if (puzzle.getBoardInputValue(x, y).equals(BoardInputValue.DISQUALIFY)) {
                                canvas.drawBitmap(disqualify, null, slotBounds, paint);
                            } else if (puzzle.getBoardInputValue(x, y).equals(BoardInputValue.QUESTION_MARK)) {
                                canvas.drawBitmap(question, null, slotBounds, paint);
                            } else if (puzzle.getBoardInputValue(x, y).equals(BoardInputValue.PERMANENT_DISQUALIFY)) {
                                canvas.drawBitmap(permanentDisqualify, null, slotBounds, paint);
                            }
                        }
                    }
                }
            }

            if (legalSlot(clueX, clueY)) {
                slotBounds.set(
                        rowBounds.right + clueX * slotSize, columnBounds.bottom + clueY * slotSize - slotSize * 2 / 5, rowBounds.right + clueX * slotSize + slotSize * 4 / 5, columnBounds.bottom + clueY * slotSize + slotSize * 2 / 5
                );

                canvas.drawBitmap(bulb, null, slotBounds, paint);
            }
        }
    }

    private void drawGrid(Canvas canvas, Paint paint) {
        paint.setColor(Color.BLACK);
        float GRID_OUTLINE_WIDTH = 1;
        paint.setStrokeWidth(GRID_OUTLINE_WIDTH);
        for (int y = 1; y < numberOfSlotsInAColumn - maxNumbersColumnSize; y++) {
            canvas.drawLine(rowBounds.right, columnBounds.bottom + y * slotSize, boardBackgroundBounds.right, columnBounds.bottom + y * slotSize, paint);
        }

        float GRID_BOLD_OUTLINE_WIDTH = 5;
        paint.setStrokeWidth(GRID_BOLD_OUTLINE_WIDTH);
        for (int y = 5; y < numberOfSlotsInAColumn - maxNumbersColumnSize; y += 5) {
            canvas.drawLine(rowBounds.right, columnBounds.bottom + y * slotSize, boardBackgroundBounds.right, columnBounds.bottom + y * slotSize, paint);
        }

        paint.setStrokeWidth(GRID_OUTLINE_WIDTH);
        for (int x = 1; x < numberOfSlotsInARow - maxNumbersRowSize; x++) {
            canvas.drawLine(rowBounds.right + x * slotSize, columnBounds.bottom, rowBounds.right + x * slotSize, boardBackgroundBounds.bottom, paint);
        }

        paint.setStrokeWidth(GRID_BOLD_OUTLINE_WIDTH);
        for (int x = 5; x < numberOfSlotsInARow - maxNumbersRowSize; x += 5) {
            canvas.drawLine(rowBounds.right + x * slotSize, columnBounds.bottom, rowBounds.right + x * slotSize, boardBackgroundBounds.bottom, paint);
        }
    }

    private void drawSolutionStep(Canvas canvas, Paint paint) {
        if (isTutorial) {
            paint.setStyle(Paint.Style.STROKE);
            Puzzle.SolutionStep solutionStep = puzzle.getSolutionStep();
            if (solutionStep != null) {
                int strokeWidth = 10;

                paint.setStrokeWidth(strokeWidth);
                paint.setColor(Color.CYAN);
                canvas.drawRect(
                        rowBounds.right + solutionStep.getXLeft() * slotSize + strokeWidth / 2,
                        columnBounds.bottom + solutionStep.getYTop() * slotSize + strokeWidth / 2,
                        rowBounds.right + (solutionStep.getXRight() + 1) * slotSize - strokeWidth / 2,
                        columnBounds.bottom + (solutionStep.getYBottom() + 1) * slotSize - strokeWidth / 2,
                        paint
                );
            }

            paint.setStyle(Paint.Style.FILL);
        }
    }

    private int getMaxNumbersColumnSize(Puzzle puzzle) {
        int maxNumbersColumnSize = 0;
        for (List<ColoredNumber> column : puzzle.getNumbers().getColumns()) {
            if (column.size() > maxNumbersColumnSize) {
                maxNumbersColumnSize = column.size();
            }
        }

        return maxNumbersColumnSize;
    }

    private int getMaxNumbersRowSize(Puzzle puzzle) {
        int maxNumbersRowSize = 0;
        for (List<ColoredNumber> row : puzzle.getNumbers().getRows()) {
            if (row.size() > maxNumbersRowSize) {
                maxNumbersRowSize = row.size();
            }
        }

        return maxNumbersRowSize;
    }

    private void drawRowNumbers(Canvas canvas, Paint paint, Puzzle puzzle, RectF rowBounds, float slotSize, int fontSize, float zoomedSlotSize, int zoomedFontSize, int curve, Integer colorToZoomFor) {
        RectF numberBounds = new RectF();
        int y = (int) rowBounds.top;
        for (List<ColoredNumber> row : puzzle.getNumbers().getRows()) {
            int x = (int) (rowBounds.left + slotSize * (maxNumbersRowSize - row.size()));
            for (ColoredNumber coloredNumber : row) {
                drawNumber(canvas, paint, puzzle, coloredNumber, x, y, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor, numberBounds);
                x += slotSize;
            }

            y += slotSize;
        }
    }

    private void drawColumnNumbers(Canvas canvas, Paint paint, Puzzle puzzle, RectF columnBounds, float slotSize, int fontSize, float zoomedSlotSize, int zoomedFontSize, int curve, Integer colorToZoomFor) {
        RectF numberBounds = new RectF();
        int x = (int) columnBounds.left;
        for (List<ColoredNumber> column : puzzle.getNumbers().getColumns()) {
            int y = (int) (columnBounds.top + slotSize * (maxNumbersColumnSize - column.size()));
            for (ColoredNumber coloredNumber : column) {
                drawNumber(canvas, paint, puzzle, coloredNumber, x, y, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor, numberBounds);
                y += slotSize;
            }

            x += slotSize;
        }
    }

    private void drawNumberOnImageNotAvailable(Canvas canvas, Paint paint, Puzzle puzzle, ColoredNumber coloredNumber, int x, int y, float slotSize, int fontSize, float zoomedSlotSize, int zoomedFontSize, int curve, Integer colorToZoomFor, RectF numberBounds) {
        String num;
        Rect numBounds = new Rect();
        boolean multiColor = puzzle.getColorSet().size() != 1;
        boolean shouldZoomIn = colorToZoomFor.equals(coloredNumber.getColor()) && multiColor;

        float currentSlotSize = (shouldZoomIn) ? zoomedSlotSize : slotSize;
        float currentFontSize = (shouldZoomIn) ? zoomedFontSize : fontSize;

        numberBounds.set(
                x + slotSize / 2 - currentSlotSize * 48 / 100,
                y + slotSize / 2 - currentSlotSize * 48 / 100,
                x + slotSize / 2 + currentSlotSize * 48 / 100,
                y + slotSize / 2 + currentSlotSize * 48 / 100
        );

        paint.setColor(coloredNumber.getColor());
        canvas.drawRoundRect(numberBounds, curve, curve, paint);

        int numValColor = colorToTextColorMap.get(coloredNumber.getColor());

        if (shouldZoomIn) {
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawRoundRect(numberBounds, curve, curve, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setColor(numValColor);
        paint.setTextSize(currentFontSize);
        num = String.valueOf(coloredNumber.getVal());
        paint.getTextBounds(num, 0, num.length(), numBounds);
        canvas.drawText(String.valueOf(coloredNumber.getVal()), numberBounds.centerX(), numberBounds.centerY() + numBounds.height() / 2, paint);
    }

    private void drawNumber(Canvas canvas, Paint paint, Puzzle puzzle, ColoredNumber coloredNumber, int x, int y, float slotSize, int fontSize, float zoomedSlotSize, int zoomedFontSize, int curve, Integer colorToZoomFor, RectF numberBounds) {
        if (zoomed()) {
            drawNumberOnImageNotAvailable(canvas, paint, puzzle, coloredNumber, x, y, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor, numberBounds);
        } else {
            boolean multiColor = puzzle.getColorSet().size() != 1;
            boolean shouldZoomIn = colorToZoomFor.equals(coloredNumber.getColor()) && multiColor;
            Map<Point, Bitmap> relevantNumbersBitmapMap = (shouldZoomIn) ? zoomedNumbersBitmapMap : numbersBitmapMap;

            Point slot = new Point(x, y);
            Bitmap numberImage = relevantNumbersBitmapMap.get(slot);
            if (numberImage == null) {
                numberImage = Bitmap.createBitmap((int) slotSize + 6 /* for bold stroke */, (int) slotSize + 6 /* for bold stroke */, Bitmap.Config.ARGB_8888);
                numberImage.setDensity(Bitmap.DENSITY_NONE);
                Canvas tmpCanvas = new Canvas(numberImage);
                drawNumberOnImageNotAvailable(tmpCanvas, paint, puzzle, coloredNumber, 3, 3, slotSize, fontSize, zoomedSlotSize, zoomedFontSize, curve, colorToZoomFor, numberBounds);
                relevantNumbersBitmapMap.put(slot, numberImage);
            }

            canvas.drawBitmap(numberImage, x - 3 /* for bold stroke */, y - 3 /* for bold stroke */, paint);
        }
    }

    public void useClue() {
        if (puzzle.getFixedUndo().empty()) {
            puzzle.setUsingHintOnFirstStep(true);
        }

        ColoringProgress coloringProgress = puzzle.copyColoringProgress();

        // fix color mistakes if there are any
        for (int x = 0; x < puzzle.getColoringProgressColors().length; x++) {
            for (int y = 0; y < puzzle.getColoringProgressColors()[0].length; y++) {
                if (puzzle.getColoringProgressColors()[x][y] != 0 && puzzle.getColoringProgressColors()[x][y] != puzzle.getFilteredColors()[x][y]) {
                    puzzle.colorAtPoint(x, y, puzzle.getFilteredColors()[x][y]);
                    if (puzzle.getFilteredColors()[x][y] == 0) {
                        puzzle.putBoardInputValue(x, y, BoardInputValue.DISQUALIFY);
                    } else {
                        puzzle.putBoardInputValue(x, y, BoardInputValue.BRUSH);
                    }

                    clueX = x;
                    clueY = y;
                    puzzle.addColoringProgressToUndo(coloringProgress);
                    return;
                }
            }
        }

        // fix disqualify mistakes if there are any
        for (int x = 0; x < puzzle.getColoringProgressColors().length; x++) {
            for (int y = 0; y < puzzle.getColoringProgressColors()[0].length; y++) {
                if (puzzle.getBoardInputValue(x, y) != null && puzzle.getBoardInputValue(x, y).equals(BoardInputValue.DISQUALIFY) && puzzle.getFilteredColors()[x][y] != 0) {
                    puzzle.colorAtPoint(x, y, puzzle.getFilteredColors()[x][y]);
                    puzzle.putBoardInputValue(x, y, BoardInputValue.BRUSH);

                    clueX = x;
                    clueY = y;
                    puzzle.addColoringProgressToUndo(coloringProgress);
                    return;
                }
            }
        }

        // fill question marks if there are any
        for (int x = 0; x < puzzle.getColoringProgressColors().length; x++) {
            for (int y = 0; y < puzzle.getColoringProgressColors()[0].length; y++) {
                if (puzzle.getBoardInputValue(x, y) != null && puzzle.getBoardInputValue(x, y).equals(BoardInputValue.QUESTION_MARK)) {
                    puzzle.colorAtPoint(x, y, puzzle.getFilteredColors()[x][y]);
                    if (puzzle.getFilteredColors()[x][y] == 0) {
                        puzzle.putBoardInputValue(x, y, BoardInputValue.DISQUALIFY);
                    } else {
                        puzzle.putBoardInputValue(x, y, BoardInputValue.BRUSH);
                    }

                    clueX = x;
                    clueY = y;
                    puzzle.addColoringProgressToUndo(coloringProgress);
                    return;
                }
            }
        }

        //fill some slot if there are no mistakes
        for (int x = 0; x < puzzle.getColoringProgressColors().length; x++) {
            for (int y = 0; y < puzzle.getColoringProgressColors()[0].length; y++) {
                if (puzzle.getColoringProgressColors()[x][y] == 0 && puzzle.getColoringProgressColors()[x][y] != puzzle.getFilteredColors()[x][y]) {
                    puzzle.colorAtPoint(x, y, puzzle.getFilteredColors()[x][y]);
                    puzzle.putBoardInputValue(x, y, BoardInputValue.BRUSH);
                    clueX = x;
                    clueY = y;
                    puzzle.addColoringProgressToUndo(coloringProgress);
                    return;
                }
            }
        }
    }

    public void clear() {
        slotTouchDownX = -1; // not initialized;
        slotTouchDownY = -1; // not initialized;
        slotTouchUpX = -1; // not initialized;
        slotTouchUpY = -1; // not initialized;
        clueX = -1; // not initialized;
        clueY = -1; // not initialized;

        initZoom();
        initMovement();
        initDimensions();
        cameraState = CameraState.NONE;
        clickIntention = ClickIntention.NONE;
    }

    private void releaseZoomAndMovement() {
        cameraState = CameraState.NONE;
        clickIntention = ClickIntention.NONE;
        releaseZoom();
        releaseMovement();
        initDimensions();
        initZoomPointers();
        initMovementPointers();
    }

    private void releaseZoom() {
        scale += addToScale;
        addToScale = 0f;
    }

    private void initZoom() {
        initZoomPointers();
        addToScale = 0f;
        scale = 1f;
    }

    private void initZoomPointers() {
        scaleDownPointer1X = -1;
        scaleDownPointer1Y = -1;
        scaleDownPointer2X = -1;
        scaleDownPointer2Y = -1;
    }

    private void releaseMovement() {
        cameraX += addToCameraX;
        cameraY += addToCameraY;
        addToCameraX = 0;
        addToCameraY = 0;
    }

    private void initMovementPointers() {
        cameraPointerX = -1;
        cameraPointerY = -1;
    }

    private void initMovement() {
        initMovementPointers();
        cameraX = 0;
        cameraY = 0;
        addToCameraX = 0;
        addToCameraY = 0;
    }
}
