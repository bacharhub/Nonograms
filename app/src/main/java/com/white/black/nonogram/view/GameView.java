package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdSize;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.BoardInputValue;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.ButtonState;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.MultiTouchBrushButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.MultiTouchQuestionButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.MultiTouchDisqualifyButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.MultiTouchEraserButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.SlideDownButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.SlideLeftButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.SlideRightButtonView;
import com.white.black.nonogram.view.buttons.boardinput.multitouch.SlideUpButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.BrushButtonView;
import com.white.black.nonogram.view.buttons.boardinput.ButtonGroup;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.ClueButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.ColorButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.DisqualifyButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.EraserButtonView;
import com.white.black.nonogram.view.buttons.PuzzleSelectionSettingsButtonView;
import com.white.black.nonogram.view.buttons.boardinput.GroupedButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.QuestionButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.RedoButtonView;
import com.white.black.nonogram.view.buttons.ReturnButtonView;
import com.white.black.nonogram.view.buttons.boardinput.singletouch.UndoButtonView;
import com.white.black.nonogram.view.listeners.GameMonitoringListener;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private volatile boolean initDone;

    private final GameViewListener gameViewListener;
    private int backgroundColor;
    private List<Bitmap> animals;
    private ReturnButtonView returnButtonView;
    private PuzzleSelectionSettingsButtonView puzzleSelectionSettingsButtonView;

    private ButtonGroup<BoardInputValue> boardInputValueButtonGroup;
    private ButtonGroup<Integer> colorInputValueButtonGroup;

    private UndoButtonView undoButtonView;
    private RedoButtonView redoButtonView;
    private ClueButtonView clueButtonView;
    private OptionsView gameOptionsView;
    private Bitmap paintPalette;
    private RectF paintPaletteBounds;

    private SlideLeftButtonView slideLeftButtonView;
    private SlideRightButtonView slideRightButtonView;
    private SlideUpButtonView slideUpButtonView;
    private SlideDownButtonView slideDownButtonView;
    private MultiTouchBrushButtonView multiTouchBrushButtonView;
    private MultiTouchQuestionButtonView multiTouchQuestionButtonView;
    private MultiTouchDisqualifyButtonView multiTouchDisqualifyButtonView;
    private MultiTouchEraserButtonView multiTouchEraserButtonView;

    private Bitmap background;
    private BoardView boardView;
    private PuzzleSolvedView puzzleSolvedView;

    public GameView(Context context) {
        super(context);
        gameViewListener = (GameViewListener)context;
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public void render() {
        if (initDone && GameState.getGameState().equals(GameState.GAME)) {
            Canvas canvas = getHolder().lockCanvas();
            try {
                if (canvas != null) {
                    draw(canvas, PaintManager.INSTANCE.createPaint());
                }
            } catch (Exception ignored) {
            } finally {
                if (canvas != null) {
                    try {
                        getHolder().unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void initBackground(Paint paint) {
        if (background == null || background.isRecycled()) {
            Canvas tempCanvas;
            background = Bitmap.createBitmap(ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), Bitmap.Config.ARGB_8888);
            background.setDensity(Bitmap.DENSITY_NONE);
            tempCanvas = new Canvas(background);

            paint.setColor(backgroundColor);
            tempCanvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            paint.setAlpha(30);
            int foodItemDrawnCounter = 0;
            for(int x = 0; x < ApplicationSettings.INSTANCE.getScreenWidth(); x += 150) {
                for (int y = 0; y < ApplicationSettings.INSTANCE.getScreenHeight(); y += 150) {
                    tempCanvas.drawBitmap(animals.get((foodItemDrawnCounter++) % animals.size()), x, y, paint);
                }
            }
            paint.setAlpha(255);
        }
    }

    private void handleBackground(Canvas canvas, Paint paint, Rect bounds) {
        if (!MemoryManager.isCriticalMemory()) {
            initBackground(paint);
        }

        drawBackground(canvas, paint, bounds);
    }

    private void drawBackground(Canvas canvas, Paint paint, Rect bounds) {
        paint.setAlpha(255);
        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, bounds, bounds, paint);
        } else {
            paint.setColor(backgroundColor);
            canvas.drawRect(bounds, paint);
        }
    }

    public void clearBackground() {
        if (background != null) {
            this.background.recycle();
        }
    }

    private void coverBoardWithBackground(Canvas canvas, Paint paint) {
        handleBackground(canvas, paint, new Rect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), boardView.getBoardTop()));
        handleBackground(canvas, paint, new Rect(0, boardView.getBoardBottom(), ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight()));
    }

    private void draw(Canvas canvas, Paint paint) {
        super.draw(canvas);

        handleBackground(canvas, paint, new Rect(0, boardView.getBoardTop(), ApplicationSettings.INSTANCE.getScreenWidth(), boardView.getBoardBottom()));
        if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.JOYSTICK)) {
            drawOnJoystick(canvas, paint);
        } else {
            drawOnTouch(canvas, paint);
        }

        returnButtonView.draw(canvas, paint);
        puzzleSelectionSettingsButtonView.draw(canvas, paint);
        ButtonState undoButtonState = (PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isUndoable())? ButtonState.ENABLED : ButtonState.DISABLED;
        undoButtonView.draw(canvas, paint, undoButtonState);
        ButtonState redoButtonState = (PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isRedoable())? ButtonState.ENABLED : ButtonState.DISABLED;
        redoButtonView.draw(canvas, paint, redoButtonState);
        clueButtonView.draw(canvas, paint);
        canvas.drawBitmap(paintPalette, null, paintPaletteBounds, paint);

        if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            gameOptionsView.draw(canvas, paint);
        }

        if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            YesNoQuestion.INSTANCE.draw(canvas, paint);
        }

        if (PuzzleSelectionView.INSTANCE.getOverallPuzzle().isDone()) {
            puzzleSolvedView.draw(canvas, paint);
        }
    }

    private void drawOnJoystick(Canvas canvas, Paint paint) {
        BoardInputValue boardInputValue = null;
        if (multiTouchEraserButtonView.isPressed()) {
            boardInputValue = BoardInputValue.ERASER;
        } else if (multiTouchDisqualifyButtonView.isPressed()) {
            boardInputValue = BoardInputValue.DISQUALIFY;
        } else if (multiTouchQuestionButtonView.isPressed()) {
            boardInputValue = BoardInputValue.QUESTION_MARK;
        } else if (multiTouchBrushButtonView.isPressed()) {
            boardInputValue = BoardInputValue.BRUSH;
        }

        boardView.draw(canvas, paint, boardInputValue, colorInputValueButtonGroup.getCurrentPressedVal());
        coverBoardWithBackground(canvas, paint);

        slideUpButtonView.draw(canvas, paint);
        slideDownButtonView.draw(canvas, paint);
        slideLeftButtonView.draw(canvas, paint);
        slideRightButtonView.draw(canvas, paint);
        colorInputValueButtonGroup.draw(canvas, paint);
        multiTouchBrushButtonView.draw(canvas, paint);
        multiTouchEraserButtonView.draw(canvas, paint);
        multiTouchDisqualifyButtonView.draw(canvas, paint);
        multiTouchQuestionButtonView.draw(canvas, paint);
    }

    private void drawOnTouch(Canvas canvas, Paint paint) {
        boardView.draw(canvas, paint, boardInputValueButtonGroup.getCurrentPressedVal(), colorInputValueButtonGroup.getCurrentPressedVal());
        coverBoardWithBackground(canvas, paint);
        boardInputValueButtonGroup.draw(canvas, paint);
        if (boardInputValueButtonGroup.getCurrentPressedVal().equals(BoardInputValue.BRUSH)) {
            colorInputValueButtonGroup.draw(canvas, paint);
        } else {
            Integer color = colorInputValueButtonGroup.getCurrentPressedVal();
            Integer fakeColor = -1;
            colorInputValueButtonGroup.setCurrentPressedVal(fakeColor);
            colorInputValueButtonGroup.draw(canvas, paint);
            colorInputValueButtonGroup.setCurrentPressedVal(color);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        render();
        if (PuzzleSelectionView.INSTANCE.getSelectedPuzzle() != null && !PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isClueAvailable()) {
            new Thread(redrawWhileClueIsNotAvailable).start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private final Runnable redrawWhileClueIsNotAvailable = new Runnable() {
        @Override
        public void run() {
            Puzzle puzzle = PuzzleSelectionView.INSTANCE.getSelectedPuzzle();
            while (!puzzle.isClueAvailable() && GameState.getGameState().equals(GameState.GAME)) {
                render();
                PaintManager.INSTANCE.setLastRenderingTime(System.currentTimeMillis());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            render();
        }
    };

    private void checkIfFaultyPuzzle() {
        if (PuzzleSelectionView.INSTANCE.getOverallPuzzle().getSubPuzzles().size() > 0) { // do not report complex puzzles
            return;
        }

        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getSelectedPuzzle();
        int[][] expected = puzzle.getFilteredColors();
        int[][] actual = puzzle.getColoringProgressColors();

        if (puzzle.isUsingHintOnFirstStep()) {
            gameViewListener.reportFaultyPuzzle(GameMonitoring.NO_SOLUTION, puzzle.getUniqueId());
        }

        for (int x = 0; x < expected.length; x++) {
            for (int y = 0; y < expected[0].length; y++) {
                if (Color.alpha(actual[x][y]) != 0) {
                    if (actual[x][y] != expected[x][y]) {
                        gameViewListener.reportFaultyPuzzle(GameMonitoring.MULTIPLE_SOLUTIONS, puzzle.getUniqueId());
                        return;
                    }
                }
            }
        }
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        if (initDone) {
            gameViewListener.onViewTouched(event);

            if (PuzzleSelectionView.INSTANCE.getOverallPuzzle().isDone()) {
                puzzleSolvedView.onTouchEvent(gameViewListener);
            } else if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MINIMIZED)) {
                if (!PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isDone()) {
                    PuzzleSelectionView.INSTANCE.getSelectedPuzzle().increaseSolvingTime();
                    doOnGameSettingsMinimized(event);
                    if (PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isDone()) {
                        checkIfFaultyPuzzle();
                        boardView.clear();
                        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().finish();
                        ((GameMonitoringListener)gameViewListener).onFinishPuzzle();
                        if (PuzzleSelectionView.INSTANCE.getOverallPuzzle().isDone()) {
                            if (puzzleSolvedView.isShowingPopup()) {
                                gameViewListener.removeAds();
                            }
                        } else {
                            gameViewListener.onNextPuzzleButtonPressed();
                        }
                    }
                }
            } else {
                if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                    YesNoQuestion.INSTANCE.onTouchEvent();
                } else {
                    gameOptionsView.onTouchEvent();
                }
            }

            // maybe?
            if (PaintManager.INSTANCE.isReadyToRender(System.currentTimeMillis())) {
                render();
                PaintManager.INSTANCE.setLastRenderingTime(System.currentTimeMillis());
            }
        }

        return true;
    }

    private void applyTouchEventIdsOnJoystickInputMode(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        switch(maskedAction) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN: {
               if (slideLeftButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   slideLeftButtonView.setTouchEventId(pointerId);
               } else if (slideRightButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   slideRightButtonView.setTouchEventId(pointerId);
               } else if (slideUpButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                    slideUpButtonView.setTouchEventId(pointerId);
               } else if (slideDownButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   slideDownButtonView.setTouchEventId(pointerId);
               } else if (multiTouchBrushButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   multiTouchBrushButtonView.setTouchEventId(pointerId);
               } else if (multiTouchDisqualifyButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   multiTouchDisqualifyButtonView.setTouchEventId(pointerId);
               } else if (multiTouchEraserButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   multiTouchEraserButtonView.setTouchEventId(pointerId);
               } else if (multiTouchQuestionButtonView.getBounds().contains(TouchMonitor.INSTANCE.getDownCoordinates(pointerId).x, TouchMonitor.INSTANCE.getDownCoordinates(pointerId).y)) {
                   multiTouchQuestionButtonView.setTouchEventId(pointerId);
               }

                break;
            }
        }
    }

    private void removeTouchEventIdsOnJoystickInputMode(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                TouchMonitor.INSTANCE.removeTouchTrackerById(pointerId);
                break;
        }
    }

    private void doOnGameSettingsMinimized(MotionEvent event) {
        if (returnButtonView.wasPressed()) {
            TouchMonitor.INSTANCE.setTouchUp(false);
            returnButtonView.onButtonPressed();
        } else if (puzzleSelectionSettingsButtonView.wasPressed()) {
            TouchMonitor.INSTANCE.setTouchUp(false);
            puzzleSelectionSettingsButtonView.onButtonPressed();
        } else if (undoButtonView.wasPressed()) {
            TouchMonitor.INSTANCE.setTouchUp(false);
            undoButtonView.onButtonPressed();
            ((GameMonitoringListener)gameViewListener).onToolbarButtonPressed(GameMonitoring.UNDO);
        } else if (redoButtonView.wasPressed()) {
            TouchMonitor.INSTANCE.setTouchUp(false);
            redoButtonView.onButtonPressed();
            ((GameMonitoringListener)gameViewListener).onToolbarButtonPressed(GameMonitoring.REDO);
        } else if (clueButtonView.wasPressed()) {
            TouchMonitor.INSTANCE.setTouchUp(false);
            MyMediaPlayer.play("blop");
            if (PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isClueAvailable()) {
                ((GameMonitoringListener)gameViewListener).onToolbarButtonPressed(GameMonitoring.CLUE);
                boardView.useClue();
                clueButtonView.onButtonPressed();
                new Thread(redrawWhileClueIsNotAvailable).start();
            }
        } else {
            if (colorInputValueButtonGroup.press() && GameSettings.INSTANCE.getInput().equals(GameSettings.Input.TOUCH)) {
                boardInputValueButtonGroup.setCurrentPressedVal(BoardInputValue.BRUSH);
            }

            boardView.handleCamera();

            if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.JOYSTICK)) {
                applyTouchEventIdsOnJoystickInputMode(event);
                doOnJoystickInputMode();
                removeTouchEventIdsOnJoystickInputMode(event);
            } else {
                boardInputValueButtonGroup.press();
                boardView.editSlotRouter(boardInputValueButtonGroup.getCurrentPressedVal(), colorInputValueButtonGroup.getCurrentPressedVal());
            }
        }
    }

    private void doOnJoystickInputMode() {
        if (slideLeftButtonView.isPressed()) {
            slideLeftButtonView.onButtonPressed();
        } else if (slideRightButtonView.isPressed()) {
            slideRightButtonView.onButtonPressed();
        } else if (slideUpButtonView.isPressed()) {
            slideUpButtonView.onButtonPressed();
        } else if (slideDownButtonView.isPressed()) {
            slideDownButtonView.onButtonPressed();
        }

        if (multiTouchBrushButtonView.wasPressed()) {
            boardView.editSlotRouter(BoardInputValue.BRUSH, colorInputValueButtonGroup.getCurrentPressedVal());
        } else if (multiTouchDisqualifyButtonView.wasPressed()) {
            boardView.editSlotRouter(BoardInputValue.DISQUALIFY, colorInputValueButtonGroup.getCurrentPressedVal());
        } else if (multiTouchEraserButtonView.wasPressed()) {
            boardView.editSlotRouter(BoardInputValue.ERASER, colorInputValueButtonGroup.getCurrentPressedVal());
        } else if (multiTouchQuestionButtonView.wasPressed()) {
            boardView.editSlotRouter(BoardInputValue.QUESTION_MARK, colorInputValueButtonGroup.getCurrentPressedVal());
        }

        if (multiTouchBrushButtonView.isPressed() || multiTouchDisqualifyButtonView.isPressed() || multiTouchEraserButtonView.isPressed() || multiTouchQuestionButtonView.isPressed()) {
            boardView.moveTouchUpSlotToStartIfOutOfBounds();
        }
    }

    public void init(Context context, Paint paint) {
        this.initDone = false;
        puzzleSolvedView = new PuzzleSolvedView();
        puzzleSolvedView.init(context, paint);
        backgroundColor = ContextCompat.getColor(context, R.color.gameBackground);
        animals = new LinkedList<>();
        animals.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.aquarium_100));
        animals.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.cat_100));
        animals.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.bug_100));
        animals.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.reindeer_100));
        animals.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.turtle_100));

        int adSizeHeight = 0; // (AdManager.isRemoveAds())? 0 : AdSize.SMART_BANNER.getHeightInPixels(context);
        int top = adSizeHeight + ApplicationSettings.INSTANCE.getScreenHeight() / 100;
        int horizontalDistanceFromEdge = ApplicationSettings.INSTANCE.getScreenHeight() / 22;
        int buttonHeight = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        RectF returnButtonBounds = new RectF(
                horizontalDistanceFromEdge,
                top,
                horizontalDistanceFromEdge + buttonWidth,
                top + buttonHeight
        );

        returnButtonView = new ReturnButtonView(
                (ViewListener)context,
                context.getString(R.string.return_button),
                returnButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.return_100)}, context, paint);

        RectF settingsButtonBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() - horizontalDistanceFromEdge - buttonWidth,
                top,
                ApplicationSettings.INSTANCE.getScreenWidth() - horizontalDistanceFromEdge,
                top + buttonHeight
        );

        puzzleSelectionSettingsButtonView = new PuzzleSelectionSettingsButtonView(
                (ViewListener)context,
                context.getString(R.string.settings_description),
                settingsButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.settings_512)}, context, paint);

        gameOptionsView = new GameOptionsView();
        gameOptionsView.init(context, paint);

        if (GameSettings.INSTANCE.getInput().equals(GameSettings.Input.TOUCH)) {
            initTouchToolbar(context, paint);
        } else {
            initJoystickToolbar(context, paint);
        }

        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().fillPermanentDisqualify();

        render();
    }

    public void initTouchToolbar(Context context, Paint paint) {
        this.initDone = false;
        int toolbarButtonLength = ApplicationSettings.INSTANCE.getScreenWidth() * 12 / 100;
        int toolbarButtonPadding = ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 100;

        RectF brushButtonBounds = new RectF(
                toolbarButtonPadding,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        BrushButtonView brushButtonView = new BrushButtonView(
                (ViewListener)context,
                brushButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.paint_brush_100)}, context, paint);


        RectF eraserButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength),
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength),
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        EraserButtonView eraserButtonView = new EraserButtonView(
                (ViewListener)context,
                eraserButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.eraser_100)}, context, paint);

        RectF disqualifyButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        DisqualifyButtonView disqualifyButtonView = new DisqualifyButtonView(
                (ViewListener)context,
                disqualifyButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF questionButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 3,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 3,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        QuestionButtonView questionButtonView = new QuestionButtonView(
                (ViewListener)context,
                questionButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.question_100)}, context, paint);

        List<GroupedButtonView<BoardInputValue>> groupedBoardInputValueButtons = new LinkedList<>();
        groupedBoardInputValueButtons.add(brushButtonView);
        groupedBoardInputValueButtons.add(eraserButtonView);
        groupedBoardInputValueButtons.add(disqualifyButtonView);
        groupedBoardInputValueButtons.add(questionButtonView);
        boardInputValueButtonGroup = new ButtonGroup<>(groupedBoardInputValueButtons);

        RectF undoButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 4,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 4,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        undoButtonView = new UndoButtonView(
                (ViewListener)context,
                undoButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.undo_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.undo_black_white_100)}, context, paint);

        RectF redoButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 5,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 5,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        redoButtonView = new RedoButtonView(
                (ViewListener)context,
                redoButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.redo_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.redo_black_white_100)}, context, paint);

        RectF clueButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 6,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 6,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        clueButtonView = new ClueButtonView(
                (ViewListener)context,
                PuzzleSelectionView.INSTANCE.getSelectedPuzzle(),
                clueButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512), BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_black_white_512)}, context, paint);

        paintPaletteBounds = new RectF(
                toolbarButtonPadding,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 2,
                toolbarButtonPadding + toolbarButtonLength,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength)
        );

        paintPalette = BitmapLoader.INSTANCE.getImage(context, R.drawable.paint_palette_100);

        int xOffset = 1;
        int yOffset = 0;
        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getSelectedPuzzle();
        List<GroupedButtonView<Integer>> colorButtonViews = new ArrayList<>(puzzle.getColorSet().size());
        for(int c : puzzle.getColorSet()) {
            if (xOffset == 7) {
                xOffset = 1;
                yOffset = 1;
            }

            RectF colorButtonBounds = new RectF(
                    toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * xOffset,
                    ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * (2 + yOffset),
                    toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * xOffset,
                    ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength) * (1 + yOffset)
            );

            colorButtonViews.add(new ColorButtonView(
                    (ViewListener)context,
                    colorButtonBounds,
                    ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), c, context, paint));
            xOffset++;
        }

        colorInputValueButtonGroup = new ButtonGroup<>(colorButtonViews);

        int adSizeHeight = 0; // (AdManager.isRemoveAds())? 0 : AdSize.SMART_BANNER.getHeightInPixels(context);
        int boardTop = adSizeHeight + ApplicationSettings.INSTANCE.getScreenHeight() * 11 / 100;
        int boardMaxHeight = ApplicationSettings.INSTANCE.getScreenHeight() - boardTop - 3 * toolbarButtonPadding - 2 * toolbarButtonLength ;
        boardMaxHeight -= (yOffset * (toolbarButtonPadding + toolbarButtonLength));

        if (boardView == null) {
            boardView = new BoardView(
                    gameViewListener,
                    PuzzleSelectionView.INSTANCE.getSelectedPuzzle(),
                    boardTop,
                    boardMaxHeight,
                    Float.valueOf(context.getString(R.string.board_numbers_size_percentage)),
                    Float.valueOf(context.getString(R.string.zoomed_board_numbers_size_percentage)));
        } else {
            boardView.setBoardMaxHeight(boardMaxHeight);
        }


        boardView.init(context);
        this.initDone = true;
    }

    public void initJoystickToolbar(Context context, Paint paint) {
        this.initDone = false;
        int toolbarButtonLength = ApplicationSettings.INSTANCE.getScreenWidth() * 12 / 100;
        int toolbarButtonPadding = ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 100;

        RectF brushButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 11 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 11 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        multiTouchBrushButtonView = new MultiTouchBrushButtonView(
                (ViewListener)context,
                brushButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.paint_brush_100)}, context, paint);

        RectF eraserButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 9 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 9 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        multiTouchEraserButtonView = new MultiTouchEraserButtonView(
                (ViewListener)context,
                eraserButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.eraser_100)}, context, paint);

        RectF disqualifyButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 11 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 2,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 11 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength)
        );

        multiTouchDisqualifyButtonView = new MultiTouchDisqualifyButtonView(
                (ViewListener)context,
                disqualifyButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF questionButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 9 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 2,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 9 / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength)
        );

        multiTouchQuestionButtonView = new MultiTouchQuestionButtonView(
                (ViewListener)context,
                questionButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.question_100)}, context, paint);

        RectF undoButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 4,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 4,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength) * 2
        );

        undoButtonView = new UndoButtonView(
                (ViewListener)context,
                undoButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.undo_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.undo_black_white_100)}, context, paint);

        RectF redoButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 6,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 6,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength) * 2
        );

        redoButtonView = new RedoButtonView(
                (ViewListener)context,
                redoButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.redo_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.redo_black_white_100)}, context, paint);

        RectF clueButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 5,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 5,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength) * 2
        );

        clueButtonView = new ClueButtonView(
                (ViewListener)context,
                PuzzleSelectionView.INSTANCE.getSelectedPuzzle(),
                clueButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512), BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_black_white_512)}, context, paint);

        RectF slideUpButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength) * 2
        );

        slideUpButtonView = new SlideUpButtonView(
                (ViewListener)context,
                slideUpButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.slide_up_100)}, context, paint);

        slideUpButtonView.setDoOnTouchEvent(new Runnable() {
            @Override
            public void run() {
                if (!multiTouchBrushButtonView.isPressed() && !multiTouchDisqualifyButtonView.isPressed() && !multiTouchEraserButtonView.isPressed() && !multiTouchQuestionButtonView.isPressed()) {
                    boardView.moveTouchDownSlotUsingJoystick(0, -1);
                } else {
                    boardView.moveTouchUpSlotUsingJoystick(0, -1);
                }

                render();
            }
        });

        RectF slideDownButtonBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength),
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength) / 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding
        );

        slideDownButtonView = new SlideDownButtonView(
                (ViewListener)context,
                slideDownButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.slide_down_100)}, context, paint);

        slideDownButtonView.setDoOnTouchEvent(new Runnable() {
            @Override
            public void run() {
                if (!multiTouchBrushButtonView.isPressed() && !multiTouchDisqualifyButtonView.isPressed() && !multiTouchEraserButtonView.isPressed() && !multiTouchQuestionButtonView.isPressed()) {
                    boardView.moveTouchDownSlotUsingJoystick(0, +1);
                } else {
                    boardView.moveTouchUpSlotUsingJoystick(0, +1);
                }

                render();
            }
        });

        RectF slideLeftButtonBounds = new RectF(
                toolbarButtonPadding,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 2,
                toolbarButtonPadding + toolbarButtonLength,
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength)
        );

        slideLeftButtonView = new SlideLeftButtonView(
                (ViewListener)context,
                slideLeftButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.slide_left_100)}, context, paint);

        slideLeftButtonView.setDoOnTouchEvent(new Runnable() {
            @Override
            public void run() {
                if (!multiTouchBrushButtonView.isPressed() && !multiTouchDisqualifyButtonView.isPressed() && !multiTouchEraserButtonView.isPressed() && !multiTouchQuestionButtonView.isPressed()) {
                    boardView.moveTouchDownSlotUsingJoystick(-1, 0);
                } else {
                    boardView.moveTouchUpSlotUsingJoystick(-1, 0);
                }

                render();
            }
        });

        RectF slideRightButtonBounds = new RectF(
                toolbarButtonPadding  + (toolbarButtonPadding + toolbarButtonLength),
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 2,
                toolbarButtonPadding + toolbarButtonLength + (toolbarButtonPadding + toolbarButtonLength),
                ApplicationSettings.INSTANCE.getScreenHeight() - toolbarButtonPadding - (toolbarButtonPadding + toolbarButtonLength)
        );

        slideRightButtonView = new SlideRightButtonView(
                (ViewListener)context,
                slideRightButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.slide_right_100)}, context, paint);

        slideRightButtonView.setDoOnTouchEvent(new Runnable() {
            @Override
            public void run() {
                if (!multiTouchBrushButtonView.isPressed() && !multiTouchDisqualifyButtonView.isPressed() && !multiTouchEraserButtonView.isPressed() && !multiTouchQuestionButtonView.isPressed()) {
                    boardView.moveTouchDownSlotUsingJoystick(+1, 0);
                } else {
                    boardView.moveTouchUpSlotUsingJoystick(+1, 0);
                }

                render();
            }
        });

        int xOffset = 1;
        int yOffset = 0;
        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getSelectedPuzzle();
        List<GroupedButtonView<Integer>> colorButtonViews = new ArrayList<>(puzzle.getColorSet().size());
        int maxColorsInARow = 2;
        int colorButtonLength = toolbarButtonLength;
        if (puzzle.getColorSet().size() > 5) {
            maxColorsInARow = 3;
            if (puzzle.getColorSet().size() < 12) {
                colorButtonLength = ApplicationSettings.INSTANCE.getScreenWidth() * 73 / 1000;
            } else {
                colorButtonLength = ApplicationSettings.INSTANCE.getScreenWidth() * 6 / 100;
            }
        }

        paintPaletteBounds = new RectF(
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 2,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3,
                toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 2 + colorButtonLength,
                ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3 + colorButtonLength
        );

        paintPalette = BitmapLoader.INSTANCE.getImage(context, R.drawable.paint_palette_100);

        for(int c : puzzle.getColorSet()) {
            if (xOffset == maxColorsInARow) {
                xOffset = 0;
                yOffset++;
            }

            RectF colorButtonBounds = new RectF(
                    toolbarButtonPadding + (toolbarButtonPadding + toolbarButtonLength) * 2 + (toolbarButtonPadding + colorButtonLength) * xOffset,
                    ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3 + (toolbarButtonPadding + colorButtonLength) * yOffset,
                    toolbarButtonPadding + colorButtonLength + (toolbarButtonPadding + toolbarButtonLength) * 2 + (toolbarButtonPadding + colorButtonLength) * xOffset,
                    ApplicationSettings.INSTANCE.getScreenHeight() - (toolbarButtonPadding + toolbarButtonLength) * 3 + (toolbarButtonPadding + colorButtonLength) * yOffset + colorButtonLength
            );

            colorButtonViews.add(new ColorButtonView(
                    (ViewListener)context,
                    colorButtonBounds,
                    ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), c, context, paint));
            xOffset++;
        }

        colorInputValueButtonGroup = new ButtonGroup<>(colorButtonViews);

        int adSizeHeight = 0; // (AdManager.isRemoveAds())? 0 : AdSize.SMART_BANNER.getHeightInPixels(context);
        int boardTop = adSizeHeight + ApplicationSettings.INSTANCE.getScreenHeight() * 11 / 100;
        int boardMaxHeight = ApplicationSettings.INSTANCE.getScreenHeight() - boardTop - 4 * toolbarButtonPadding - 3 * toolbarButtonLength ;

        if (boardView == null) {
            boardView = new BoardView(
                    gameViewListener,
                    PuzzleSelectionView.INSTANCE.getSelectedPuzzle(),
                    boardTop,
                    boardMaxHeight,
                    Float.valueOf(context.getString(R.string.board_numbers_size_percentage)),
                    Float.valueOf(context.getString(R.string.zoomed_board_numbers_size_percentage)));
        } else {
            boardView.setBoardMaxHeight(boardMaxHeight);
        }

        boardView.init(context);
        this.initDone = true;
    }

    public void clear() {
        boardView.clear();
        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().clear();
    }
}