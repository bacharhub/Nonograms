package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.white.black.nonogram.R;

public enum PaintManager {

    INSTANCE;

    private static final long INTERVAL_BETWEEN_RENDERINGS = 25;

    private Typeface typeface;

    private long lastRenderingTime;

    public void setLastRenderingTime(long lastRenderingTime) {
        this.lastRenderingTime = lastRenderingTime;
    }

    public void setReadyToRender() {
        this.lastRenderingTime = 0;
    }

    public boolean isReadyToRender(long currentTime) {
        return isTimePassedSinceLastRender(INTERVAL_BETWEEN_RENDERINGS, currentTime);
    }

    private boolean isTimePassedSinceLastRender(long timePassed, long currentTime) {
        return (currentTime - timePassed) > lastRenderingTime;
    }

    public Paint createPaint() {
        Paint paint = new Paint();
        paint.setTypeface(typeface);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        return paint;
    }

    public void init(Context context) {
        typeface = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.font));
    }
}
