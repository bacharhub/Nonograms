package com.white.black.nonogram.view.buttons.boardinput.multitouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class MultiTouchButtonView extends PicButtonView {

    private final long EVENT_TIME_INTERVAL_MILLISECONDS = 70;
    private int touchEventId;
    private Runnable doOnTouchEvent;
    private Thread touchEventThread;
    private long lastEventTime;

    MultiTouchButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

// --Commented out by Inspection START (09/10/2018 20:08):
//    public int getTouchEventId() {
//        return touchEventId;
//    }
// --Commented out by Inspection STOP (09/10/2018 20:08)

    public void setTouchEventId(int touchEventId) {
        this.touchEventId = touchEventId;
    }

    public void setDoOnTouchEvent(Runnable doOnTouchEvent) {
        this.doOnTouchEvent = doOnTouchEvent;
    }

    @Override
    public void onButtonPressed() {
        if (isPressed() && (touchEventThread == null || !touchEventThread.isAlive())) {
            touchEventThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isPressed()) {
                        if (System.currentTimeMillis() > lastEventTime + EVENT_TIME_INTERVAL_MILLISECONDS) {
                            doOnTouchEvent.run();
                            lastEventTime = System.currentTimeMillis();
                        }
                    }
                }
            });

            touchEventThread.start();
        }
    }

    @Override
    public boolean isPressed() {
        return (TouchMonitor.INSTANCE.touchDown(touchEventId) &&
                (bounds.contains(TouchMonitor.INSTANCE.getMove(touchEventId).x, TouchMonitor.INSTANCE.getMove(touchEventId).y)) &&
                (bounds.contains(TouchMonitor.INSTANCE.getDownCoordinates(touchEventId).x, TouchMonitor.INSTANCE.getDownCoordinates(touchEventId).y)));
    }

    @Override
    public boolean wasPressed() {
        return (TouchMonitor.INSTANCE.touchUp(touchEventId) &&
                (bounds.contains(TouchMonitor.INSTANCE.getUpCoordinates(touchEventId).x, TouchMonitor.INSTANCE.getUpCoordinates(touchEventId).y)) &&
                (bounds.contains(TouchMonitor.INSTANCE.getDownCoordinates(touchEventId).x, TouchMonitor.INSTANCE.getDownCoordinates(touchEventId).y)));
    }
}
