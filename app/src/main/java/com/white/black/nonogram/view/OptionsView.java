package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
import com.white.black.nonogram.view.buttons.settings.Icons8ButtonView;
import com.white.black.nonogram.view.buttons.settings.JoystickButtonView;
import com.white.black.nonogram.view.buttons.settings.SoundButtonView;
import com.white.black.nonogram.view.buttons.TitlePicButtonView;
import com.white.black.nonogram.view.buttons.settings.TouchButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

class OptionsView {

    private int backgroundColor;
    private RectF windowBounds;
    private RectF windowBackgroundBounds;
    private RectF windowInnerBackgroundBounds;
    private RectF toolbarBackgroundBounds;
    RectF toolbarBounds;
    int toolbarBackgroundColor;
    int toolbarColor;
    private LinearGradient gradient;
    private int windowBackgroundColor;
    private int windowInnerBackgroundColor;
    private int curve;
    private CloseWindowButtonView closeWindowButtonView;
    private TitlePicButtonView joystickButtonView;
    private TitlePicButtonView touchButtonView;
    private SoundButtonView soundButtonView;
    private Icons8ButtonView icons8ButtonView;
    private String windowDescription;

    public void init(Context context, Paint paint) {
        backgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);
        windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 25 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 10,
                ApplicationSettings.INSTANCE.getScreenHeight() * 85 / 100
        );

        windowInnerBackgroundColor = ContextCompat.getColor(context, R.color.menuBackground);
        int gameSettingsWindowGradientTo = ContextCompat.getColor(context, R.color.gameSettingsWindowGradientTo);
        windowBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsWindowBackground);

        this.gradient = new LinearGradient(
                windowBounds.left,
                windowBounds.top,
                windowBounds.right,
                windowBounds.bottom,
                new int[]{windowInnerBackgroundColor, gameSettingsWindowGradientTo},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        windowBackgroundBounds = new RectF(windowBounds.left, windowBounds.top, windowBounds.right + windowBounds.width() * 2 / 100, windowBounds.bottom + windowBounds.height() * 2 / 100);
        this.curve = ApplicationSettings.INSTANCE.getScreenWidth() / 30;
        float padding = windowBounds.width() / 60;
        this.windowInnerBackgroundBounds = new RectF(windowBounds.left + padding, windowBounds.top + padding, windowBounds.right - padding, windowBounds.bottom - padding);

        this.toolbarColor = ContextCompat.getColor(context, R.color.settingsBrown1);
        this.toolbarBackgroundColor = ContextCompat.getColor(context, R.color.settingsBrown2);
        this.toolbarBackgroundBounds = new RectF(windowBounds.left, windowBounds.top + windowBounds.height() * 9 / 100, windowBounds.right, windowBounds.top + windowBounds.height() * 26 / 100);
        this.toolbarBounds = new RectF(windowInnerBackgroundBounds.left, toolbarBackgroundBounds.top, windowInnerBackgroundBounds.right, toolbarBackgroundBounds.bottom);

        float closeButtonEdgeLength = windowBounds.height() / 9;
        RectF closeButtonBounds = new RectF(
                windowBackgroundBounds.right - closeButtonEdgeLength,
                windowBounds.top - windowBounds.height() / 20 - closeButtonEdgeLength,
                windowBackgroundBounds.right,
                windowBounds.top - windowBounds.height() / 20
        );

        closeWindowButtonView = new CloseWindowButtonView(
                (ViewListener)context,
                closeButtonBounds,
                windowInnerBackgroundColor, gameSettingsWindowGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF joystickButtonBounds = new RectF(
                toolbarBounds.left + padding / 2,
                toolbarBounds.top + padding,
                toolbarBounds.centerX() - padding * 3 / 2,
                toolbarBounds.top + padding + toolbarBounds.height() * 82 / 100
        );

        joystickButtonView = new JoystickButtonView(
                (ViewListener)context,
                context.getString(R.string.joystick),
                joystickButtonBounds,
                windowInnerBackgroundColor, gameSettingsWindowGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.game_controller_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.game_controller_black_white_100)}, context, paint);

        RectF touchButtonBounds = new RectF(
                toolbarBounds.centerX() + padding / 2,
                toolbarBounds.top + padding,
                toolbarBounds.right - padding * 3 / 2,
                toolbarBounds.top + padding + toolbarBounds.height() * 83 / 100
        );

        touchButtonView = new TouchButtonView(
                (ViewListener)context,
                context.getString(R.string.touch),
                touchButtonBounds,
                windowInnerBackgroundColor, gameSettingsWindowGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.hand_cursor_512), BitmapLoader.INSTANCE.getImage(context, R.drawable.hand_cursor_black_white_512)}, context, paint);

        float verticalGapBetweenHorizontalButtons = toolbarBounds.height() / 5;
        float horizontalButtonHeight = toolbarBounds.height() * 2 / 3;
        float horizontalButtonWidth = toolbarBounds.width() * 7 / 10;

        RectF soundButtonBounds = new RectF(
                toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3,
                toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + horizontalButtonHeight
        );

        soundButtonView = new SoundButtonView(
                (ViewListener)context,
                context.getString(R.string.sound),
                soundButtonBounds,
                toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.audio_100), BitmapLoader.INSTANCE.getImage(context, R.drawable.mute_black_white_100)}, context, paint);

        RectF icons8ButtonBounds = new RectF(
                toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 3,
                toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) * 3 + horizontalButtonHeight
        );

        icons8ButtonView = new Icons8ButtonView(
                (ViewListener)context,
                context.getString(R.string.icons8),
                icons8ButtonBounds,
                toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.icons8_512)}, context, paint);


        windowDescription = context.getString(R.string.settings_description);
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
        paint.setColor(windowInnerBackgroundColor);
        canvas.drawRoundRect(windowInnerBackgroundBounds, curve, curve, paint);

        paint.setColor(toolbarBackgroundColor);
        canvas.drawRect(toolbarBackgroundBounds, paint);
        paint.setColor(toolbarColor);
        canvas.drawRect(toolbarBounds, paint);

        closeWindowButtonView.draw(canvas, paint);
        joystickButtonView.draw(canvas, paint);
        touchButtonView.draw(canvas, paint);
        soundButtonView.draw(canvas, paint);
        icons8ButtonView.draw(canvas, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenWidth() / 15);
        paint.setColor(toolbarBackgroundColor);
        canvas.drawText(windowDescription, toolbarBackgroundBounds.centerX(), toolbarBackgroundBounds.top - toolbarBackgroundBounds.height() / 10, paint);
    }

    public void onTouchEvent() {
        if (!windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y) &&
                !windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)) {
            closeWindowButtonView.onButtonPressed();
        } else if (joystickButtonView.wasPressed()) {
            joystickButtonView.onButtonPressed();
            MyMediaPlayer.play("blop");
        } else if (touchButtonView.wasPressed()) {
            touchButtonView.onButtonPressed();
            MyMediaPlayer.play("blop");
        } else if (soundButtonView.wasPressed()) {
            soundButtonView.onButtonPressed();
            MyMediaPlayer.play("blop");
        } else if (icons8ButtonView.wasPressed()) {
            icons8ButtonView.onButtonPressed();
            MyMediaPlayer.play("blop");
        }

        TouchMonitor.INSTANCE.setTouchUp(false);
    }
}
