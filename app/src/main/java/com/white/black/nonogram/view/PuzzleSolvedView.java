package com.white.black.nonogram.view;

import static com.white.black.nonogram.GameMonitoring.REWARDED_AD_EXTRA_COINS;
import static com.white.black.nonogram.GameMonitoring.REWARDED_AD_EXTRA_COINS_CANCELED;
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

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.WatchAdButtonView;
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
    private LinearGradient windowGradient;

    private RectF rewardWindowBounds;
    private RectF rewardWindowBackgroundBounds;
    private RectF rewardWindowInnerBackgroundBounds;
    private RectF coinRewardBounds;
    private RectF coinAdRewardBounds;
    private RectF rewardCheckBounds;
    private RectF rewardAdCheckBounds;
    private RectF rewardAdVideoBounds;
    private LinearGradient rewardWindowGradient;
    private Bitmap coin;
    private Bitmap videoAdIcon;
    private Bitmap rewardCheck;
    private WatchAdButtonView watchAdButtonView;
    private boolean isVideoWatched;
    private WatchAdPopup videoPopup;

    private int windowBackgroundColor;
    private int curve;
    private float padding;

    private Bitmap clock;
    private Bitmap complete;

    private int numOfPuzzlesSolved;
    private boolean hideRating; // hide if a player hates the app
    private Popup popup;
    private Bitmap background;
    private final Rect backgroundBounds = new Rect(
            0,
            0,
            ApplicationSettings.INSTANCE.getScreenWidth(),
            ApplicationSettings.INSTANCE.getScreenHeight()
    );

    private RadialGradient radialGradient;

    public boolean isShowingExtraCoinsVideo() {
        return this.videoPopup.isShowingPopup();
    }

    public Popup getExtraCoinsPopup() {
        return this.videoPopup.getPopup();
    }

    public void showRewardedAdVideo() {
        this.videoPopup.setShowPopup(true);
        this.videoPopup.getPopup().doOnYesAnswered();
    }

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

    private void onRewardedAdOffered(Context context, boolean canceled) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.REWARDED_AD_OFFER, canceled ? REWARDED_AD_EXTRA_COINS_CANCELED : REWARDED_AD_EXTRA_COINS);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    private void onRewarded(Context context, int numOfCoins) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int coins = sharedPreferences.getInt("coins", Puzzles.numOfSolvedPuzzles(context) * 15);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt("coins", coins + numOfCoins);
        prefsEditor.apply();
    }

    private void onAdFailedToLoad(LoadAdError loadAdError, Context context) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.LOAD_VIDEO_AD_ERROR_CODE, loadAdError.toString());
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    public void init(Context context, Paint paint) {
        int color1 = Puzzles.getCurrent().getColorPack().getColor1();
        int color2 = Puzzles.getCurrent().getColorPack().getColor2();
        int color3 = Puzzles.getCurrent().getColorPack().getColor3();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.numOfPuzzlesSolved = numOfSolvedPuzzles(context) + 1; // + 1 for the next solved puzzle
        this.hideRating = sharedPreferences.getBoolean("hideRating", false);

        videoPopup = new WatchAdPopup(
                context,
                paint,
                "+30 Coins!",
                BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64),
                () -> onRewardedAdOffered(context, true),
                () -> {
                    onRewarded(context, 30);
                    onRewardedAdOffered(context, false);
                    isVideoWatched = true;
                },
                (error) -> onAdFailedToLoad(error, context),
                () -> {
                }
        );

        windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 3 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 15 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 7 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 55 / 100
        );

        radialGradient = new RadialGradient(
                windowBounds.centerX(),
                windowBounds.centerY(),
                ApplicationSettings.INSTANCE.getScreenHeight() / 2,
                new int[]{Color.argb(255, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );

        int windowInnerBackgroundColor = color1;
        int windowInnerBackgroundGradientTo = color2;
        windowBackgroundColor = color3;

        this.windowGradient = new LinearGradient(
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
        coin = BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64);
        videoAdIcon = BitmapLoader.INSTANCE.getImage(context, R.drawable.video_64);
        rewardCheck = BitmapLoader.INSTANCE.getImage(context, R.drawable.done_512);

        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        completeBounds = new RectF(windowBounds.right - buttonWidth / 2, windowBounds.top - buttonWidth / 2, windowBounds.right + buttonWidth / 2, windowBounds.top + buttonWidth / 2);

        rewardWindowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 3 / 10,
                windowBounds.bottom + ApplicationSettings.INSTANCE.getScreenHeight() * 4 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 7 / 10,
                windowBounds.bottom + ApplicationSettings.INSTANCE.getScreenHeight() * 19 / 100
        );

        this.rewardWindowGradient = new LinearGradient(
                rewardWindowBounds.left,
                rewardWindowBounds.top,
                rewardWindowBounds.right,
                rewardWindowBounds.bottom,
                new int[]{windowInnerBackgroundColor, windowInnerBackgroundGradientTo},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        rewardWindowInnerBackgroundBounds = new RectF(
                rewardWindowBounds.left + padding,
                rewardWindowBounds.top + padding,
                rewardWindowBounds.right - padding,
                rewardWindowBounds.bottom - padding
        );

        rewardWindowBackgroundBounds = new RectF(
                rewardWindowBounds.left,
                rewardWindowBounds.top,
                rewardWindowBounds.right + rewardWindowBounds.width() * 2 / 100,
                rewardWindowBounds.bottom + rewardWindowBounds.height() * 2 / 100
        );

        coinRewardBounds = new RectF(
                rewardWindowBounds.left + rewardWindowBounds.width() / 15,
                rewardWindowBounds.top + rewardWindowBounds.height() * 35 / 100,
                rewardWindowBounds.left + rewardWindowBounds.width() / 15 + 128,
                rewardWindowBounds.top + rewardWindowBounds.height() * 35 / 100 + 128
        );

        coinAdRewardBounds = new RectF(
                coinRewardBounds.left,
                coinRewardBounds.top + rewardWindowBounds.height() * 3 / 10,
                coinRewardBounds.right,
                coinRewardBounds.bottom + rewardWindowBounds.height() * 3 / 10
        );

        rewardCheckBounds = new RectF(
                rewardWindowBounds.left + rewardWindowBounds.width() * 65 / 100,
                coinRewardBounds.top - rewardWindowBounds.height() * 3 / 100,
                rewardWindowBounds.left + rewardWindowBounds.width() * 65 / 100 + 128,
                coinRewardBounds.bottom - rewardWindowBounds.height() * 3 / 100
        );

        rewardAdCheckBounds = new RectF(
                rewardCheckBounds.left,
                rewardCheckBounds.top + rewardWindowBounds.height() * 3 / 10,
                rewardCheckBounds.right,
                rewardCheckBounds.bottom + rewardWindowBounds.height() * 3 / 10
        );

        rewardAdVideoBounds = new RectF(
                rewardAdCheckBounds.left,
                rewardAdCheckBounds.top + rewardWindowBounds.height() * 2 / 100,
                rewardAdCheckBounds.right,
                rewardAdCheckBounds.bottom + rewardWindowBounds.height() * 2 / 100
        );

        watchAdButtonView = new WatchAdButtonView(
                (ViewListener) context,
                rewardWindowBounds,
                Color.RED,
                Color.WHITE,
                Color.RED,
                new Bitmap[]{videoAdIcon},
                context,
                paint
        );

        setUpPopup(context, paint);
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, null, backgroundBounds, paint);
            return;
        }

        paint.setShader(
                new LinearGradient(
                        0, 0, 0, ApplicationSettings.INSTANCE.getScreenHeight(),
                        new int[]{Color.rgb(252, 149, 179), Color.rgb(255, 232, 206)},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                )
        );

        paint.setDither(true);
        canvas.drawRect(backgroundBounds, paint);
        paint.setShader(null);
        paint.setDither(false);
    }

    private void drawRays(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setShader(radialGradient);
        paint.setDither(true);

        Point src = new Point((int) windowBounds.centerX(), (int) windowBounds.centerY());
        int radius = ApplicationSettings.INSTANCE.getScreenHeight() * 3 / 4;
        double indexWithInterval = System.currentTimeMillis() / 100.0;
        for (double i = indexWithInterval; i < indexWithInterval + 360; i += 52) {
            Point start = new Point((int) (src.x + radius * Math.cos(Math.toRadians(i))), (int) (src.y + radius * Math.sin(Math.toRadians(i))));
            Point end = new Point((int) (src.x + radius * Math.cos(Math.toRadians(i + 15))), (int) (src.y + radius * Math.sin(Math.toRadians(i + 15))));

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.setLastPoint(src.x, src.y);
            path.lineTo(start.x, start.y);
            path.lineTo(end.x, end.y);
            path.lineTo(src.x, src.y);
            path.close();
            canvas.drawPath(path, paint);
        }

        paint.setShader(null);
        paint.setDither(false);
    }

    public void clearBackground() {
        if (background != null) {
            this.background.recycle();
        }
    }

    private void handleBackground(Canvas canvas, Paint paint) {
        if (!MemoryManager.isCriticalMemory()) {
            initBackground(paint);
        }

        drawBackground(canvas, paint);
    }

    private void initBackground(Paint paint) {
        if (background == null || background.isRecycled()) {
            Bitmap background = Bitmap.createBitmap(backgroundBounds.width(), backgroundBounds.height(), Bitmap.Config.RGB_565);
            background.setDensity(Bitmap.DENSITY_NONE);
            Canvas tempCanvas = new Canvas(background);
            drawBackground(tempCanvas, paint);
            this.background = background;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        handleBackground(canvas, paint);
        drawRays(canvas, paint);

        Puzzle puzzle = PuzzleSelectionView.INSTANCE.getOverallPuzzle();

        paint.setColor(windowBackgroundColor);
        canvas.drawRoundRect(windowBackgroundBounds, curve, curve, paint);
        paint.setShader(windowGradient);
        canvas.drawRoundRect(windowBounds, curve, curve, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(windowInnerBackgroundBounds, curve, curve, paint);

        paint.setColor(windowBackgroundColor);
        canvas.drawRoundRect(rewardWindowBackgroundBounds, curve, curve, paint);
        paint.setShader(rewardWindowGradient);
        canvas.drawRoundRect(rewardWindowBounds, curve, curve, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rewardWindowInnerBackgroundBounds, curve, curve, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenWidth() / 20);
        paint.setColor(Color.BLACK);
        canvas.drawText("Reward:", rewardWindowBounds.centerX(), rewardWindowBounds.top + rewardWindowBounds.height() / 5, paint);
        canvas.drawBitmap(coin, null, coinRewardBounds, paint);
        canvas.drawBitmap(coin, null, coinAdRewardBounds, paint);
        canvas.drawBitmap(rewardCheck, null, rewardCheckBounds, paint);
        if (isVideoWatched) {
            canvas.drawBitmap(rewardCheck, null, rewardAdCheckBounds, paint);
        } else {
            canvas.drawBitmap(videoAdIcon, null, rewardAdVideoBounds, paint);
        }

        paint.setTextAlign(Paint.Align.LEFT);
        Rect numOfCoinsDescriptionBounds = new Rect();
        paint.getTextBounds("+15", 0, "+15".length(), numOfCoinsDescriptionBounds);
        canvas.drawText("+15", coinRewardBounds.right + 10, coinRewardBounds.centerY() + numOfCoinsDescriptionBounds.height() / 3, paint);
        canvas.drawText("+30", coinRewardBounds.right + 10, coinAdRewardBounds.centerY() + numOfCoinsDescriptionBounds.height() / 3, paint);

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

        if (puzzleNameDesc.length() > 12) {
            if (puzzleNameDesc.length() > 15) {
                if (puzzleNameDesc.length() > 18) {
                    paint.setTextSize(PuzzleSelectionView.getPuzzleNameFontSize() * 5 / 10);
                } else {
                    paint.setTextSize(PuzzleSelectionView.getPuzzleNameFontSize() * 6 / 10);
                }
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

        videoPopup.draw(canvas, paint);
    }


    public void onTouchEvent(GameViewListener gameViewListener) {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (videoPopup != null && videoPopup.isShowingPopup()) {
                videoPopup.onTouchEvent();
            } else if (watchAdButtonView.wasPressed() && !isVideoWatched) {
                watchAdButtonView.onButtonPressed();
            } else if (!(isShowingPopup() && popup.onTouchEvent())) {
                gameViewListener.onNextPuzzleButtonPressed();
            }

            TouchMonitor.INSTANCE.setTouchUp(false);
        }
    }
}
