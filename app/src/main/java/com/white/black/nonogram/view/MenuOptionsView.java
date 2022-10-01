package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.settings.InstructionsButtonView;
import com.white.black.nonogram.view.buttons.settings.LaunchMarketButtonView;
import com.white.black.nonogram.view.buttons.settings.SettingsGoToFacebookButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.Locale;

public class MenuOptionsView extends OptionsView {

    private LaunchMarketButtonView launchMarketButtonView;
    private SettingsGoToFacebookButtonView settingsGoToFacebookButtonView;
    private InstructionsButtonView instructionsButtonView;

    @Override
    public void init(Context context, Paint paint) {
        super.init(context, paint);

        float verticalGapBetweenHorizontalButtons = toolbarBounds.height() / 5;
        float horizontalButtonHeight = toolbarBounds.height() * 2 / 3;
        float horizontalButtonWidth = toolbarBounds.width() * 7 / 10;

        RectF launchMarketButtonBounds = new RectF(
                toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons),
                toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) + horizontalButtonHeight
        );

        launchMarketButtonView = new LaunchMarketButtonView(
                (ViewListener)context,
                context.getString(R.string.rate),
                launchMarketButtonBounds,
                toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.good_quality_100)}, context, paint);

        if (Locale.getDefault().getCountry().equals("IL")) {
            RectF settingsGoToFacebookButtonBounds = new RectF(
                    toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                    toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 2,
                    toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                    toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 2 + horizontalButtonHeight
            );

            settingsGoToFacebookButtonView = new SettingsGoToFacebookButtonView(
                    (ViewListener)context,
                    context.getString(R.string.facebook),
                    settingsGoToFacebookButtonBounds,
                    toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.facebook_512)}, context, paint);
        } else {
            RectF instructionsButtonBounds = new RectF(
                    toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                    toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 2,
                    toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                    toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 2 + horizontalButtonHeight
            );

            instructionsButtonView = new InstructionsButtonView(
                    (ViewListener)context,
                    context.getString(R.string.instructions),
                    instructionsButtonBounds,
                    toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.user_manual_100)}, context, paint);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
       super.draw(canvas, paint);
       launchMarketButtonView.draw(canvas, paint);

       if (settingsGoToFacebookButtonView != null) {
           settingsGoToFacebookButtonView.draw(canvas, paint);
       }

       if (instructionsButtonView != null) {
           instructionsButtonView.draw(canvas, paint);
       }
    }

    @Override
    public void onTouchEvent() {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (launchMarketButtonView.wasPressed()) {
                launchMarketButtonView.onButtonPressed();
                MyMediaPlayer.play("blop");
            } else if (settingsGoToFacebookButtonView != null && settingsGoToFacebookButtonView.wasPressed()) {
                settingsGoToFacebookButtonView.onButtonPressed();
                MyMediaPlayer.play("blop");
            } else if (instructionsButtonView != null && instructionsButtonView.wasPressed()) {
                instructionsButtonView.onButtonPressed();
                MyMediaPlayer.play("blop");
            } else {
                super.onTouchEvent();
            }
        }
    }
}
