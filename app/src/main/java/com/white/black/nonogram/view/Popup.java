package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.YesNoButtonView;

public class Popup {

    private String message;
    private Runnable onYesAnswer;
    private Runnable onNoAnswer;
    private RectF windowBounds;
    private RectF windowBackgroundBounds;
    private RectF windowInnerBackgroundBounds;
    private int messageColor;
    private LinearGradient gradient;
    private int windowBackgroundColor;
    private int windowInnerBackgroundColor;
    private int curve;
    private YesNoButtonView yesButtonView;
    private YesNoButtonView noButtonView;
    private float yesNoQuestionFontSizeFactor;
    private Bitmap topLeftImage;
    private RectF topLeftImageBounds;

    private boolean answered;

    public void doOnNoAnswer() {
        TouchMonitor.INSTANCE.setTouchUp(false);
        onNoAnswer.run();
        MyMediaPlayer.play("blop");
        answered = true;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private boolean isAnswered() {
        return this.answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public void setTopLeftImage(Bitmap topLeftImage) {
        this.topLeftImage = topLeftImage;
    }

    public YesNoButtonView getYesButtonView() {
        return yesButtonView;
    }

    public Popup(Context context, RectF windowBounds,
                 String message, Runnable onYesAnswer, Runnable onNoAnswer,
                 YesNoButtonView yesButtonView, YesNoButtonView noButtonView, Bitmap topLeftImage) {
        this.message = message;
        this.onNoAnswer = onNoAnswer;
        this.onYesAnswer = onYesAnswer;
        this.windowBounds = windowBounds;

        windowInnerBackgroundColor = ContextCompat.getColor(context, R.color.menuBackground);
        int gameSettingsWindowGradientTo = ContextCompat.getColor(context, R.color.gameSettingsWindowGradientTo);
        windowBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsWindowBackground);
        this.messageColor = ContextCompat.getColor(context, R.color.settingsBrown2);

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

        this.yesButtonView = yesButtonView;
        this.noButtonView = noButtonView;

        yesNoQuestionFontSizeFactor = Float.valueOf(context.getString(R.string.yesNoQuestionFontSizeFactor));

        this.topLeftImage = topLeftImage;
        float roseWidth = Math.min(windowBounds.height(), windowBounds.width()) / 5;
        this.topLeftImageBounds = new RectF(
                windowBounds.left - roseWidth * 2 / 3,
                windowBounds.top - roseWidth * 2 / 3,
                windowBounds.left + roseWidth,
                windowBounds.top + roseWidth
        );
    }

    public Popup(Context context, RectF windowBounds,
                 String message, Runnable onYesAnswer, Runnable onNoAnswer,
                 YesNoButtonView yesButtonView, YesNoButtonView noButtonView) {
        this(context, windowBounds, message, onYesAnswer, onNoAnswer, yesButtonView, noButtonView, BitmapLoader.INSTANCE.getImage(context, R.drawable.rose_96));
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(windowBackgroundColor);
        canvas.drawRoundRect(windowBackgroundBounds, curve, curve, paint);
        paint.setShader(gradient);
        canvas.drawRoundRect(windowBounds, curve, curve, paint);
        paint.setShader(null);
        paint.setColor(windowInnerBackgroundColor);
        canvas.drawRoundRect(windowInnerBackgroundBounds, curve, curve, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(yesNoQuestionFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 15);
        paint.setColor(messageColor);

        if (isAnswered()) {
            Rect textBounds = new Rect();
            paint.getTextBounds(message, 0, message.length(), textBounds);
            canvas.drawText(message, windowBounds.centerX(), windowBounds.centerY() + textBounds.height() / 2, paint);
        } else {
            canvas.drawText(message, windowBounds.centerX(), windowBounds.top + windowBounds.height() * 3 / 10, paint);
            if (yesButtonView != null) {
                yesButtonView.draw(canvas, paint);
            }

            if (noButtonView != null) {
                noButtonView.draw(canvas, paint);
            }
        }

        canvas.drawBitmap(topLeftImage, null, this.topLeftImageBounds, paint);
    }

    public void onTouchEvent() {
        if (TouchMonitor.INSTANCE.touchUp()) {
            if (!isAnswered()) {
                if (yesButtonView != null && yesButtonView.wasPressed()) {
                    TouchMonitor.INSTANCE.setTouchUp(false);
                    onYesAnswer.run();
                    MyMediaPlayer.play("blop");
                    answered = true;
                } else if (noButtonView != null && noButtonView.wasPressed()) {
                    doOnNoAnswer();
                }
            }
        }
    }
}
