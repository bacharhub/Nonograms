package com.white.black.nonogram.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.NextPuzzleButtonView;
import com.white.black.nonogram.view.buttons.ReturnButtonView;
import com.white.black.nonogram.view.buttons.YesNoButtonView;
import com.white.black.nonogram.view.listeners.GameMonitoringListener;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

class PuzzleSolvedView {

    private int backgroundColor;
    private RectF windowBounds;
    private RectF windowBackgroundBounds;
    private RectF windowInnerBackgroundBounds;
    private RectF puzzleImageBounds;
    private RectF puzzleImageBackgroundBounds;
    private RectF clockBounds;
    private RectF balloonsBounds;
    private RectF completeBounds;

    private LinearGradient gradient;
    private int windowBackgroundColor;
    private int curve;
    private float padding;

    private Bitmap clock;
    private Bitmap balloons;
    private Bitmap complete;

    private ReturnButtonView returnButtonView;
    private NextPuzzleButtonView nextPuzzleButtonView;

    private int numOfPuzzlesSolved;
    private boolean hideRating; // hide if a player hates the app
    private Popup popup;

    public boolean isShowingPopup() {
        return (!hideRating) && numOfPuzzlesSolved >= 10 && (numOfPuzzlesSolved % 5 == 0);
    }

    private void setUpPopup(Context context, Paint paint) {
        RectF popupWindowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 6 / 100,
                returnButtonView.getBackgroundBounds().bottom + ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 94 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 98 / 100
        );

        RectF popupWindowInnerBackgroundBounds = new RectF(popupWindowBounds.left + padding, popupWindowBounds.top + padding, popupWindowBounds.right - padding, popupWindowBounds.bottom - padding);

        String message = context.getString(R.string.enjoyingNonograms);

        RectF noButtonBounds = new RectF(
                popupWindowInnerBackgroundBounds.left + popupWindowInnerBackgroundBounds.width() / 18 - popupWindowInnerBackgroundBounds.width() / 30,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() * 15 / 100,
                popupWindowInnerBackgroundBounds.centerX() - popupWindowInnerBackgroundBounds.width() / 30,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() / 22
        );

        YesNoButtonView noButtonView = new YesNoButtonView(
                (ViewListener)context,
                context.getString(R.string.not_really),
                noButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.dislike_100)}, context, paint);

        RectF yesButtonBounds = new RectF(
                popupWindowInnerBackgroundBounds.centerX() + popupWindowInnerBackgroundBounds.width() / 30 - popupWindowInnerBackgroundBounds.width() / 35,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() * 15 / 100,
                popupWindowInnerBackgroundBounds.right - popupWindowInnerBackgroundBounds.width() / 19,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() / 22
        );

        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener)context,
                context.getString(R.string.yes_),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.like_100)}, context, paint);

        Runnable onNoAnswer = new Runnable() {
            @Override
            public void run() {
                popup.setMessage(context.getString(R.string.thank_feedback));
                try {
                    SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    prefsEditor.putBoolean("hideRating", true);
                    prefsEditor.apply();

                    ((GameMonitoringListener)context).onToolbarButtonPressed(GameMonitoring.DISLIKE_APP);
                } catch (Exception ignored) {

                }
            }
        };

        Runnable onYesAnswer = new Runnable() {
            @Override
            public void run() {
                ((GameMonitoringListener)context).onToolbarButtonPressed(GameMonitoring.LIKE_APP);

                YesNoButtonView noButtonView = new YesNoButtonView(
                        (ViewListener)context,
                        context.getString(R.string.no_thanks),
                        noButtonBounds,
                        ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.sad_smiley_100)}, context, paint);

                YesNoButtonView yesButtonView = new YesNoButtonView(
                        (ViewListener)context,
                        context.getString(R.string.ok_sure),
                        yesButtonBounds,
                        ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.heart_smiley_100)}, context, paint);

                String message = context.getString(R.string.would_you_rate);

                popup = new Popup(
                        context, popupWindowBounds, message, new Runnable() {
                    @Override
                    public void run() {
                        popup.setMessage(context.getString(R.string.thank_feedback));
                        ((GameViewListener) context).onLaunchMarketButtonPressed();
                        try {
                            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                            prefsEditor.putBoolean("hideRating", true);
                            prefsEditor.apply();

                            ((GameMonitoringListener)context).onToolbarButtonPressed(GameMonitoring.VOTE_APP);
                        } catch (Exception ignored) {

                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        popup.setMessage(context.getString(R.string.thank_feedback));
                        ((GameMonitoringListener)context).onToolbarButtonPressed(GameMonitoring.REFUSE_APP);
                    }
                }, yesButtonView, noButtonView
                );
            }
        };

        this.popup = new Popup(
                context, popupWindowBounds, message, onYesAnswer, onNoAnswer, yesButtonView, noButtonView
        );
    }

    public void init(Context context, Paint paint) {
        int color1 = Puzzles.getCurrent().getColorPack().getColor1();
        int color2 = Puzzles.getCurrent().getColorPack().getColor2();
        int color3 = Puzzles.getCurrent().getColorPack().getColor3();

        backgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.numOfPuzzlesSolved = sharedPreferences.getInt(context.getString(R.string.most_puzzles_solved), 0) + 1; // + 1 for the next solved puzzle
        this.hideRating = sharedPreferences.getBoolean("hideRating", false);

        if (isShowingPopup()) {
            windowBounds = new RectF(
                    ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                    ApplicationSettings.INSTANCE.getScreenHeight() * 10 / 100,
                    ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 10,
                    ApplicationSettings.INSTANCE.getScreenHeight() * 70 / 100
            );
        } else {
            windowBounds = new RectF(
                    ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                    ApplicationSettings.INSTANCE.getScreenHeight() * 18 / 100,
                    ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 10,
                    ApplicationSettings.INSTANCE.getScreenHeight() * 78 / 100
            );
        }

        int windowInnerBackgroundColor = color1;
        int windowInnerBackgroundGradientTo = color2;
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
        padding = windowBounds.width() / 60;
        this.windowInnerBackgroundBounds = new RectF(windowBounds.left + padding, windowBounds.top + padding, windowBounds.right - padding, windowBounds.bottom - padding);

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

        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getOverallPuzzle();
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

        clock = BitmapLoader.INSTANCE.getImage(context, R.drawable.alarm_clock_100);
        balloons = BitmapLoader.INSTANCE.getImage(context, R.drawable.balloons_100);
        complete = BitmapLoader.INSTANCE.getImage(context, R.drawable.complete_512);

        int verticalGapFromWindowBackground = ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100;
        float top = windowBackgroundBounds.bottom + verticalGapFromWindowBackground;
        int buttonHeight = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        balloonsBounds = new RectF(windowBounds.right - buttonWidth * 2 / 3, windowBounds.bottom - buttonWidth * 2 / 3, windowBounds.right + buttonWidth / 3, windowBounds.bottom + buttonWidth / 3);
        completeBounds = new RectF(windowBounds.right - buttonWidth / 2, windowBounds.top - buttonWidth / 2, windowBounds.right + buttonWidth / 2, windowBounds.top + buttonWidth / 2);

        RectF returnButtonBounds = new RectF(
                windowBackgroundBounds.left,
                top,
                windowBackgroundBounds.left + buttonWidth,
                top + buttonHeight
        );

        returnButtonView = new ReturnButtonView(
                (ViewListener)context,
                context.getString(R.string.return_button),
                returnButtonBounds,
                color1, color2, color3, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.return_100)}, context, paint);

        RectF nextPuzzleButtonBounds = new RectF(
                returnButtonBounds.right + verticalGapFromWindowBackground,
                top,
                windowBounds.right,
                top + buttonHeight
        );

        nextPuzzleButtonView = new NextPuzzleButtonView(
                (ViewListener)context,
                context.getString(R.string.next_puzzle),
                nextPuzzleButtonBounds,
                color1, color2, color3, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.play_100)}, context, paint);

        setUpPopup(context, paint);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setTextAlign(Paint.Align.LEFT);
        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getOverallPuzzle();
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

        canvas.drawBitmap(clock, null, clockBounds, paint);
        paint.setTextSize(PuzzleSelectionView.getPuzzleSolvingTimeDescFontSize());
        paint.setColor(Color.BLACK);
        canvas.drawText(puzzle.getSolvingTimeHumanFormat(), clockBounds.right + clockBounds.width() / 4, clockBounds.centerY() + PuzzleSelectionView.INSTANCE.getPuzzleSolvingTimeDescHeight() / 2, paint);
        paint.setTextSize(PuzzleSelectionView.getPuzzleDimensionsFontSize());
        float distFromEdge = windowInnerBackgroundBounds.width() / 30;
        float puzzleDimensionsY = windowInnerBackgroundBounds.top + distFromEdge + PuzzleSelectionView.INSTANCE.getPuzzleDimensionsDescHeight();
        canvas.drawText(PuzzleSelectionView.INSTANCE.getPuzzleDimensionsDesc(), windowInnerBackgroundBounds.left + distFromEdge, puzzleDimensionsY, paint);
        paint.setTextSize(PuzzleSelectionView.getPuzzleNameFontSize());
        paint.setTextAlign(Paint.Align.CENTER);
        String puzzleNameDesc = puzzle.getName();

        if (puzzleNameDesc.length() > 15) {
            if (puzzleNameDesc.length() > 20) {
                paint.setTextSize(PuzzleSelectionView.getPuzzleNameFontSize() * 6 / 10);
            } else {
                paint.setTextSize(PuzzleSelectionView.getPuzzleNameFontSize() * 8 / 10);
            }
        }

        canvas.drawText(puzzleNameDesc, windowInnerBackgroundBounds.centerX(), puzzleDimensionsY / 2 + puzzleImageBackgroundBounds.top / 2 + PuzzleSelectionView.INSTANCE.getPuzzleNameDescHeight() / 2, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        canvas.drawBitmap(puzzle.getFilteredBitmap(), null, puzzleImageBounds, paint);

        returnButtonView.draw(canvas, paint);
        nextPuzzleButtonView.draw(canvas, paint);

        canvas.drawBitmap(balloons, null, balloonsBounds, paint);
        canvas.drawBitmap(complete, null, completeBounds, paint);

        if (isShowingPopup()) {
            popup.draw(canvas, paint);
        }
    }


    public void onTouchEvent(GameViewListener gameViewListener) {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (returnButtonView.wasPressed()) {
                gameViewListener.onReturnButtonPressed();
            } else if (nextPuzzleButtonView.wasPressed()) {
                gameViewListener.onNextPuzzleButtonPressed();
            } else {
                if (isShowingPopup()) {
                    popup.onTouchEvent();
                }
            }

            TouchMonitor.INSTANCE.setTouchUp(false);
        }
    }
}
