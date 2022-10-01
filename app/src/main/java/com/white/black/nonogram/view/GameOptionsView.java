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
import com.white.black.nonogram.view.buttons.settings.StartOverButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class GameOptionsView extends OptionsView {

    private StartOverButtonView startOverButtonView;
    private InstructionsButtonView instructionsButtonView;

    @Override
    public void init(Context context, Paint paint) {
        super.init(context, paint);

        float verticalGapBetweenHorizontalButtons = toolbarBounds.height() / 5;
        float horizontalButtonHeight = toolbarBounds.height() * 2 / 3;
        float horizontalButtonWidth = toolbarBounds.width() * 7 / 10;

        RectF startOverButtonViewBounds = new RectF(
                toolbarBounds.left + (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons),
                toolbarBounds.right - (toolbarBounds.width() - horizontalButtonWidth) / 2,
                toolbarBounds.bottom + toolbarBounds.height() / 3 + (horizontalButtonHeight + verticalGapBetweenHorizontalButtons) + horizontalButtonHeight
        );

        startOverButtonView = new StartOverButtonView(
                (ViewListener)context,
                context.getString(R.string.restart),
                startOverButtonViewBounds,
                toolbarColor, toolbarBackgroundColor, ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.restart_512)}, context, paint);

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

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        startOverButtonView.draw(canvas, paint);
        instructionsButtonView.draw(canvas, paint);
    }

    @Override
    public void onTouchEvent() {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (startOverButtonView.wasPressed()) {
                startOverButtonView.onButtonPressed();
                MyMediaPlayer.play("blop");
            } else if (instructionsButtonView.wasPressed()) {
                instructionsButtonView.onButtonPressed();
                MyMediaPlayer.play("blop");
            } else {
                super.onTouchEvent();
            }
        }
    }
}
