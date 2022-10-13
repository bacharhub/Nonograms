package com.white.black.nonogram.view.buttons;

import android.graphics.RectF;

import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.listeners.ViewListener;

public abstract class ButtonView {

    protected final ViewListener viewListener;
    protected RectF bounds;

    ButtonView(ViewListener viewListener, RectF bounds) {
        this.viewListener = viewListener;
        this.bounds = bounds;
    }

    protected boolean isPressed() {
        return (TouchMonitor.INSTANCE.touchDown() &&
                (bounds.contains(TouchMonitor.INSTANCE.getMove().x, TouchMonitor.INSTANCE.getMove().y)) &&
                (bounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)));
    }

    public boolean wasPressed() {
        return (TouchMonitor.INSTANCE.touchUp() &&
                (bounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y)) &&
                (bounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)));
    }

    public abstract void onButtonPressed();

    public RectF getBounds() {
        return this.bounds;
    }
}
