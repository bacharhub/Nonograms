package com.white.black.nonogram.view.buttons.boardinput;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.TouchMonitor;

import java.util.List;

public class ButtonGroup<V> {

    private final List<GroupedButtonView<V>> buttons;
    private V currentPressedVal;

    public ButtonGroup(List<GroupedButtonView<V>> buttons) {
        this.buttons = buttons;
        this.currentPressedVal = this.buttons.get(0).getPressedButtonValue();
    }

    public boolean press() {
        for(GroupedButtonView<V> button : this.buttons) {
            if (button.wasPressed()) {
                MyMediaPlayer.play("blop");
                this.currentPressedVal = button.getPressedButtonValue();
                TouchMonitor.INSTANCE.setTouchUp(false);
                return true;
            }
        }

        return false;
    }

    public void draw(Canvas canvas, Paint paint) {
        for(GroupedButtonView<V> button : this.buttons) {
            button.draw(canvas, paint, this.currentPressedVal);
        }
    }

    public V getCurrentPressedVal() {
        return currentPressedVal;
    }

    public void setCurrentPressedVal(V currentPressedVal) {
        this.currentPressedVal = currentPressedVal;
    }
}
