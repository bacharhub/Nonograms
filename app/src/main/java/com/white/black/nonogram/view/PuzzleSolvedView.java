package com.white.black.nonogram.view;

import static com.white.black.nonogram.Puzzles.numOfSolvedPuzzles;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
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
import com.white.black.nonogram.view.buttons.YesNoButtonView;
import com.white.black.nonogram.view.listeners.GameMonitoringListener;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

class PuzzleSolvedView {

    private RectF windowBounds;
    private RectF windowBackgroundBounds;
    private RectF windowInnerBackgroundBounds;
    private RectF puzzleImageBounds;
    private RectF puzzleImageBackgroundBounds;
    private RectF clockBounds;
    private RectF completeBounds;

    private LinearGradient gradient;
    private int windowBackgroundColor;
    private int curve;
    private float padding;

    private Bitmap clock;
    private Bitmap complete;

    private int numOfPuzzlesSolved;
    private boolean hideRating; // hide if a player hates the app
    private Popup popup;

    public boolean isShowingPopup() {
        return (!hideRating) && numOfPuzzlesSolved >= 5 && (numOfPuzzlesSolved % 7 == 0);
    }

    private void setUpPopup(Context context, Paint paint) {
        RectF popupWindowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 6 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 835 / 1000,
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
                (ViewListener) context,
                context.getString(R.string.not_really),
                noButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.dislike_100)}, context, paint);

        RectF yesButtonBounds = new RectF(
                popupWindowInnerBackgroundBounds.centerX() + popupWindowInnerBackgroundBounds.width() / 30 - popupWindowInnerBackgroundBounds.width() / 35,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() * 15 / 100,
                popupWindowInnerBackgroundBounds.right - popupWindowInnerBackgroundBounds.width() / 19,
                popupWindowBounds.bottom - popupWindowInnerBackgroundBounds.width() / 22
        );

        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener) context,
                context.getString(R.string.yes_),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.like_100)}, context, paint);

        Runnable onNoAnswer = () -> {
            popup.setMessage(context.getString(R.string.thank_feedback));
            try {
                SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                prefsEditor.putBoolean("hideRating", true);
                prefsEditor.apply();

                ((GameMonitoringListener) context).onToolbarButtonPressed(GameMonitoring.DISLIKE_APP);
            } catch (Exception ignored) {

            }
        };

        Runnable onYesAnswer = () -> {
            ((GameMonitoringListener) context).onToolbarButtonPressed(GameMonitoring.LIKE_APP);

            YesNoButtonView noButtonView1 = new YesNoButtonView(
                    (ViewListener) context,
                    context.getString(R.string.no_thanks),
                    noButtonBounds,
                    ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.sad_smiley_100)}, context, paint);

            YesNoButtonView yesButtonView1 = new YesNoButtonView(
                    (ViewListener) context,
                    context.getString(R.string.ok_sure),
                    yesButtonBounds,
                    ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.heart_smiley_100)}, context, paint);

            String message1 = context.getString(R.string.would_you_rate);

            popup = new Popup(
                    context, popupWindowBounds, message1, () -> {
                popup.setMessage(context.getString(R.string.thank_feedback));
                ((GameViewListener) context).onLaunchMarketButtonPressed();
                try {
                    SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    prefsEditor.putBoolean("hideRating", true);
                    prefsEditor.apply();

                    ((GameMonitoringListener) context).onToolbarButtonPressed(GameMonitoring.VOTE_APP);
                } catch (Exception ignored) {

                }
            }, () -> {
                popup.setMessage(context.getString(R.string.thank_feedback));
                ((GameMonitoringListener) context).onToolbarButtonPressed(GameMonitoring.REFUSE_APP);
            }, yesButtonView1, noButtonView1
            );
        };

        this.popup = new Popup(
                context, popupWindowBounds, message, onYesAnswer, onNoAnswer, yesButtonView, noButtonView
        );
    }

    public void init(Context context, Paint paint) {
        int color1 = Puzzles.getCurrent().getColorPack().getColor1();
        int color2 = Puzzles.getCurrent().getColorPack().getColor2();
        int color3 = Puzzles.getCurrent().getColorPack().getColor3();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.numOfPuzzlesSolved = numOfSolvedPuzzles(context) + 1; // + 1 for the next solved puzzle
        this.hideRating = sharedPreferences.getBoolean("hideRating", false);

        windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 3 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 3 / 10,
                ApplicationSettings.INSTANCE.getScreenWidth() * 7 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 7 / 10
        );

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
        complete = BitmapLoader.INSTANCE.getImage(context, R.drawable.complete_512);

        int verticalGapFromWindowBackground = ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100;
        float top = windowBackgroundBounds.bottom + verticalGapFromWindowBackground;
        int buttonHeight = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        completeBounds = new RectF(windowBounds.right - buttonWidth / 2, windowBounds.top - buttonWidth / 2, windowBounds.right + buttonWidth / 2, windowBounds.top + buttonWidth / 2);

        setUpPopup(context, paint);
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        paint.setShader(
                new LinearGradient(
                        0, 0, 0, ApplicationSettings.INSTANCE.getScreenHeight(),
                        new int[]{Color.rgb(252, 149, 179), Color.rgb(255, 232, 206)},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                )
        );

        paint.setDither(true);
        canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
        paint.setShader(null);
        paint.setDither(false);
    }

    private void drawRays(Canvas canvas, Paint paint) {
        int point1_returnedX = ApplicationSettings.INSTANCE.getScreenWidth() / 2;
        int point1_returnedY = ApplicationSettings.INSTANCE.getScreenHeight() / 2;
        int point2_returnedX = ApplicationSettings.INSTANCE.getScreenWidth() * 4 / 10;
        int point2_returnedY = 0;
        int point3_returnedX = ApplicationSettings.INSTANCE.getScreenWidth() * 6 / 10;
        int point3_returnedY = 0;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Point a = new Point(point1_returnedX, point1_returnedY);
        Point b = new Point(point2_returnedX, point2_returnedY);
        Point c = new Point(point3_returnedX, point3_returnedY);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.setLastPoint(a.x, a.y);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();

        paint.setShader(
                new RadialGradient(
                        ApplicationSettings.INSTANCE.getScreenWidth() / 2, ApplicationSettings.INSTANCE.getScreenHeight() / 2, ApplicationSettings.INSTANCE.getScreenHeight() / 2,
                        new int[]{Color.rgb(255, 255, 255), Color.rgb(252, 149, 179)},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                )
        );

        paint.setDither(true);
        canvas.drawPath(path, paint);
        paint.setShader(null);
        paint.setDither(false);
    }

    public void draw(Canvas canvas, Paint paint) {
        drawBackground(canvas, paint);
        drawRays(canvas, paint);

        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getOverallPuzzle();

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

        canvas.drawBitmap(complete, null, completeBounds, paint);

        if (isShowingPopup()) {
            popup.draw(canvas, paint);
        }
    }


    public void onTouchEvent(GameViewListener gameViewListener) {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (!(isShowingPopup() && popup.onTouchEvent())) {
                gameViewListener.onNextPuzzleButtonPressed();
            }

            TouchMonitor.INSTANCE.setTouchUp(false);
        }
    }
}
