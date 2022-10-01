package com.white.black.nonogram.view.buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.listeners.ViewListener;

public abstract class TitlePicButtonView extends StatePicButtonView {

    private final String description;

    protected TitlePicButtonView(ViewListener viewListener, String description, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, context, paint);
        this.description = description;
        labeledPicFontSizeFactor = Float.valueOf(((Context)viewListener).getString(R.string.labeledTitlePicButtonFontSizeFactor));
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);

        RectF superBounds = (isPressed())? super.pressedInnerBackgroundBounds : super.innerBackgroundBounds;
        paint.setTextSize(labeledPicFontSizeFactor * ApplicationSettings.INSTANCE.getScreenWidth() / 24);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(description, superBounds.left + superBounds.width() / 12, superBounds.top + superBounds.height() * 3 / 10, paint);
    }
}
