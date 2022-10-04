package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.listeners.ViewListener;

public class PicButtonView extends ButtonView {

    protected final Bitmap[] innerImages;

    private LinearGradient pressedGradient;
    RectF pressedBounds;
    protected RectF pressedInnerBackgroundBounds;

    protected LinearGradient gradient;
    protected RectF backgroundBounds;
    protected RectF innerBackgroundBounds;

    protected final RectF innerImageBounds;
    protected RectF pressedInnerImageBounds;

    protected float padding;
    protected int curve;
    protected int innerBackgroundColor;
    protected int backgroundColor;
    protected int textHeight;

    protected float labeledPicFontSizeFactor;

    public RectF getBackgroundBounds() {
        return backgroundBounds;
    }

    protected PicButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, RectF innerImageBounds, Context context, Paint paint) {
        super(viewListener, bounds);
        this.innerImages = innerImages;
        this.innerImageBounds = innerImageBounds;
        init(color1, color2, color3, context, paint);
    }

    private void init(int color1, int color2, int color3, Context context, Paint paint) {
        this.innerBackgroundColor = Color.WHITE;
        this.backgroundColor = color3;
        this.gradient = new LinearGradient(
                bounds.left,
                bounds.top,
                bounds.right,
                bounds.bottom,
                new int[]{color1, color2},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);
        this.curve = ApplicationSettings.INSTANCE.getScreenWidth() / 30;
        this.backgroundBounds = new RectF(bounds.left, bounds.top, bounds.left + bounds.width() * 104 / 100, bounds.top + bounds.height() * 110 / 100);
        padding = Math.min(bounds.height() / 30, bounds.width() / 30);
        this.innerBackgroundBounds = new RectF(bounds.left + padding, bounds.top + padding, bounds.right - padding, bounds.bottom - padding);

        labeledPicFontSizeFactor = Float.valueOf(((Context)viewListener).getString(R.string.labeledPicFontSizeFactor));
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 18);
        Rect textBounds = new Rect();
        paint.getTextBounds(context.getString(R.string.letter), 0, 1, textBounds);
        this.textHeight = textBounds.height();

        pressedBounds = new RectF(bounds.left + bounds.width() * 4 / 100, bounds.top + bounds.height() * 10 / 100, bounds.left + bounds.width() * 104 / 100, bounds.top + bounds.height() * 110 / 100);
        pressedInnerBackgroundBounds = new RectF(pressedBounds.left + padding, pressedBounds.top + padding, pressedBounds.right - padding, pressedBounds.bottom - padding);
        this.pressedGradient = new LinearGradient(
                pressedBounds.left,
                pressedBounds.top,
                pressedBounds.right,
                pressedBounds.bottom,
                new int[]{color2, color1},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);
        pressedInnerImageBounds = new RectF(innerImageBounds.left + bounds.width() * 4 / 100, innerImageBounds.top + bounds.height() * 10 / 100, innerImageBounds.right + bounds.width() * 4 / 100, innerImageBounds.bottom + bounds.height() * 10 / 100);
    }
    
    public void draw(Canvas canvas, Paint paint) {
        draw(canvas, paint, isPressed());
    }

    void draw(Canvas canvas, Paint paint, boolean isPressed) {
        if (isPressed) {
            paint.setShader(this.pressedGradient);
            canvas.drawRoundRect(this.pressedBounds, this.curve, this.curve, paint);
            paint.setShader(null);
            paint.setColor(innerBackgroundColor);
            canvas.drawRoundRect(this.pressedInnerBackgroundBounds, this.curve, this.curve, paint);
        } else {
            paint.setColor(backgroundColor);
            canvas.drawRoundRect(this.backgroundBounds, this.curve, this.curve, paint);
            paint.setShader(gradient);
            canvas.drawRoundRect(bounds, this.curve, this.curve, paint);
            paint.setShader(null);
            paint.setColor(innerBackgroundColor);
            canvas.drawRoundRect(this.innerBackgroundBounds, this.curve, this.curve, paint);
        }
    }

    @Override
    public void onButtonPressed() {

    }
}
