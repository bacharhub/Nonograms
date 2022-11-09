package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.white.black.nonogram.TouchMonitor;

public abstract class ScrollableView extends SurfaceView implements SurfaceHolder.Callback {

    volatile boolean initDone;
    int top = 0;
    float verticalGap = 0;
    float verticalBoost = 0;
    private long lastScrollTime;

    public ScrollableView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    private boolean isAccelerating() {
        return Math.abs(this.verticalBoost) > 1;
    }

    private void scroll(int topMin, int topMax) {
        if (TouchMonitor.INSTANCE.getCoordinatesGap().x == 0 && TouchMonitor.INSTANCE.getCoordinatesGap().y == 0) {
            if (TouchMonitor.INSTANCE.touchDown()) {
                this.verticalBoost = 0f;
            }

            if (isAccelerating()) {
                if (System.currentTimeMillis() - lastScrollTime > 5) {
                    this.lastScrollTime = System.currentTimeMillis();
                    this.verticalGap += this.verticalBoost / 20;
                    this.verticalBoost = this.verticalBoost * 19 / 20;
                }
            }

            if (this.verticalGap < topMin) {
                this.verticalBoost = 0;
                this.verticalGap = topMin;
            }

            if (this.verticalGap > topMax) {
                this.verticalBoost = 0;
                this.verticalGap = topMax;
            }

            this.top = Math.min(Math.max((int) this.verticalGap + (TouchMonitor.INSTANCE.getMove().y - TouchMonitor.INSTANCE.getDownCoordinates().y), topMin), topMax);
        } else {
            doOnCoordinatesGapChanged(topMin, topMax);
        }

        onScroll();
    }

    private void doOnCoordinatesGapChanged(int topMin, int topMax) {
        this.verticalGap += (TouchMonitor.INSTANCE.getCoordinatesGap().y);
        if ((TouchMonitor.INSTANCE.getUpTime() - TouchMonitor.INSTANCE.getDownTime()) < 400) {
            this.verticalBoost += (int) (TouchMonitor.INSTANCE.getCoordinatesGap().y * (600.0 / (TouchMonitor.INSTANCE.getUpTime() - TouchMonitor.INSTANCE.getDownTime())));
        }

        this.top = Math.min(Math.max((int) this.verticalGap, topMin), topMax);
        TouchMonitor.INSTANCE.setMove(TouchMonitor.INSTANCE.getDownCoordinates());
        TouchMonitor.INSTANCE.initializeCoordinatesGap();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void update(int viewHeight, int viewTop, int viewBottom) {
        if (isAccelerating()) {
            return;
        }

        new Thread(() -> {
            try {
                synchronizedUpdate(viewHeight, viewTop, viewBottom);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private synchronized void synchronizedUpdate(int viewHeight, int viewTop, int viewBottom) throws InterruptedException {
        do {
            scroll(
                    viewBottom - viewHeight,
                    viewTop
            );

            long lastRenderingTimestamp = PaintManager.INSTANCE.getLastRenderingTime();
            long now = System.currentTimeMillis();
            long duration = now - lastRenderingTimestamp;
            long timeToSleep = Math.max(10, 16 - duration);

            Thread.sleep(timeToSleep);
            PaintManager.INSTANCE.setLastRenderingTime(now);
            render();
        } while (isAccelerating());
    }

    protected abstract void draw(Canvas canvas, Paint paint);

    public void render() {
        if (initDone) {
            Canvas canvas = getHolder().lockCanvas();
            try {
                if (canvas != null) {
                    draw(canvas, PaintManager.INSTANCE.createPaint());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        getHolder().unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    protected abstract void onScroll();
}
