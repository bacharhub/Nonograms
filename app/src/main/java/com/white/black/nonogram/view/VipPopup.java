package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
import com.white.black.nonogram.view.buttons.YesNoButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class VipPopup {

    private int backgroundColor;
    private CloseWindowButtonView closeWindowButtonView;
    private RectF windowBackgroundBounds;
    private Popup popup;
    private Bitmap vip;
    private RectF vipImageBounds;

    private Bitmap removeAds;
    private RectF removeAdsImageBounds;
    private String noAds;

    private Bitmap unlockPuzzlesImage;
    private RectF unlockPuzzlesImageBounds;
    private String unlockPuzzles;

    private Bitmap programmerImage;
    private RectF programmerImageBounds;
    private String supportUs;

    private float fontSize;

    public Popup getPopup() {
        return popup;
    }

    public void setPrice(String price) {
        popup.getYesButtonView().setDescription(price);
    }

    public void update() {
        this.popup.setAnswered(AdManager.isRemoveAds());
    }

    public VipPopup(Context context, Paint paint, Runnable onYesAnswer, Runnable onNoAnswer) {
        RectF windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 25 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 85 / 100
        );

        float padding = windowBounds.width() / 60;
        RectF windowInnerBackgroundBounds = new RectF(windowBounds.left + padding, windowBounds.top + padding, windowBounds.right - padding, windowBounds.bottom - padding);
        float closeButtonEdgeLength = windowBounds.height() / 9;

        vip = BitmapLoader.INSTANCE.getImage(context, R.drawable.vip_100);
        vipImageBounds = new RectF(
                windowInnerBackgroundBounds.centerX() - (windowInnerBackgroundBounds.height() * 10 / 100) * 94 / 140,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 4 / 100,
                windowInnerBackgroundBounds.centerX() + (windowInnerBackgroundBounds.height() * 10 / 100) * 94 / 140,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 14 / 100
        );

        removeAds = BitmapLoader.INSTANCE.getImage(context, R.drawable.remove_ads_100);
        removeAdsImageBounds = new RectF(
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 6 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 22 / 100,
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 26 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 22 / 100 + windowInnerBackgroundBounds.width() * 20 / 100
        );

        noAds = context.getString(R.string.no_ads);

        unlockPuzzlesImage = BitmapLoader.INSTANCE.getImage(context, R.drawable.unlock_puzzles_100);
        unlockPuzzlesImageBounds = new RectF(
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 6 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 42 / 100,
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 26 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 42 / 100 + windowInnerBackgroundBounds.width() * 20 / 100
        );

        unlockPuzzles = context.getString(R.string.unlock_all_puzzles);

        programmerImage = BitmapLoader.INSTANCE.getImage(context, R.drawable.programmer_100);
        programmerImageBounds = new RectF(
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 6 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 62 / 100,
                windowInnerBackgroundBounds.left + windowInnerBackgroundBounds.width() * 26 / 100,
                windowInnerBackgroundBounds.top + windowInnerBackgroundBounds.height() * 62 / 100 + windowInnerBackgroundBounds.width() * 20 / 100
        );

        supportUs = context.getString(R.string.support_programmer);

        fontSize = Float.valueOf(context.getString(R.string.labeledTitlePicButtonFontSizeFactor)) * ApplicationSettings.INSTANCE.getScreenWidth() / 24;

        RectF yesButtonBounds = new RectF(
                windowInnerBackgroundBounds.left + windowBounds.width() / 15,
                windowBounds.bottom - closeButtonEdgeLength / 2 - closeButtonEdgeLength,
                windowInnerBackgroundBounds.right - windowBounds.width() / 15,
                windowBounds.bottom - closeButtonEdgeLength / 2
        );

        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener)context,
                context.getString(R.string.unknown_puzzle_name),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.largePuzzleRed1), ContextCompat.getColor(context, R.color.largePuzzleRed2), ContextCompat.getColor(context, R.color.largePuzzleRed3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.shopping_cart_100)}, context, paint);

        this.popup = new Popup(
                context, windowBounds, "", onYesAnswer, onNoAnswer, yesButtonView, null, BitmapLoader.INSTANCE.getImage(context, R.drawable.best_seller_100), null
        );

        windowBackgroundBounds = new RectF(windowBounds.left, windowBounds.top, windowBounds.right + windowBounds.width() * 2 / 100, windowBounds.bottom + windowBounds.height() * 2 / 100);

        backgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);

        int windowInnerBackgroundColor = ContextCompat.getColor(context, R.color.menuBackground);
        int gameSettingsWindowGradientTo = ContextCompat.getColor(context, R.color.gameSettingsWindowGradientTo);
        int windowBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsWindowBackground);

        RectF closeButtonBounds = new RectF(
                windowBackgroundBounds.right - closeButtonEdgeLength,
                windowBounds.top - windowBounds.width() / 15 - closeButtonEdgeLength,
                windowBackgroundBounds.right,
                windowBounds.top - windowBounds.width() / 15
        );

        closeWindowButtonView = new CloseWindowButtonView(
                (ViewListener)context,
                closeButtonBounds,
                windowInnerBackgroundColor, gameSettingsWindowGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
        paint.setAlpha(255);
        popup.draw(canvas, paint);
        closeWindowButtonView.draw(canvas, paint);
        canvas.drawBitmap(vip, null, vipImageBounds, paint);
        canvas.drawBitmap(removeAds, null, removeAdsImageBounds, paint);
        canvas.drawBitmap(unlockPuzzlesImage, null, unlockPuzzlesImageBounds, paint);
        canvas.drawBitmap(programmerImage, null, programmerImageBounds, paint);

        paint.setTextSize(fontSize);

        Rect textHeight = new Rect();
        paint.getTextBounds(noAds, 0, noAds.length(), textHeight);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText(noAds, (removeAdsImageBounds.right + windowBackgroundBounds.right) / 2, removeAdsImageBounds.centerY() + textHeight.height() / 2, paint);
        canvas.drawText(unlockPuzzles, (unlockPuzzlesImageBounds.right + windowBackgroundBounds.right) / 2, unlockPuzzlesImageBounds.centerY() + textHeight.height() / 2, paint);
        canvas.drawText(supportUs, (programmerImageBounds.right + windowBackgroundBounds.right) / 2, programmerImageBounds.centerY() + textHeight.height() / 2, paint);
    }

    public void onTouchEvent() {
        if (!windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y) &&
                !windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)) {
            popup.doOnNoAnswer();
        } else {
            popup.onTouchEvent();
        }
    }
}
