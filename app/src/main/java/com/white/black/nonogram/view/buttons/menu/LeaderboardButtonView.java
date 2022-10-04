package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.function.Supplier;

public class LeaderboardButtonView extends PicButtonView {

    private RectF nameTagBackgroundBounds;
    private RectF nameTagBounds;
    private RectF nameTagInnerBackgroundBounds;
    private final Supplier<String> description;

    public LeaderboardButtonView(ViewListener viewListener, Supplier<String> description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 2 / 10,
                bounds.top + bounds.height() * 2 / 10,
                bounds.right - bounds.width() * 2 / 10,
                bounds.bottom - bounds.height() * 2 / 10), context, paint);
        this.description = description;
        init();
    }

    private void init() {
        nameTagBounds = new RectF(
                bounds.left,
                bounds.bottom + ApplicationSettings.INSTANCE.getScreenHeight() / 25,
                bounds.right,
                bounds.bottom + ApplicationSettings.INSTANCE.getScreenHeight() / 25 + bounds.height() * 3 / 10
        );

        nameTagBackgroundBounds = new RectF(
                this.backgroundBounds.left,
                this.nameTagBounds.top,
                this.backgroundBounds.right,
                this.nameTagBounds.bottom + nameTagBounds.height() * 10 / 100);
        padding = nameTagBounds.width() / 30;
        this.nameTagInnerBackgroundBounds = new RectF(nameTagBounds.left + padding, nameTagBounds.top + padding, nameTagBounds.right - padding, nameTagBounds.bottom - padding);
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);

        paint.setColor(backgroundColor);
        canvas.drawRoundRect(this.nameTagBackgroundBounds, this.curve, this.curve, paint);
        paint.setShader(gradient);
        canvas.drawRoundRect(nameTagBounds, this.curve, this.curve, paint);
        paint.setShader(null);
        paint.setColor(innerBackgroundColor);
        canvas.drawRoundRect(this.nameTagInnerBackgroundBounds, this.curve, this.curve, paint);
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 18);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(backgroundColor);
        canvas.drawText(description.get(), this.nameTagInnerBackgroundBounds.centerX(), this.nameTagInnerBackgroundBounds.centerY() + this.textHeight / 2, paint);
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onLeaderboardButtonPressed();
    }
}
