package com.white.black.nonogram.view.buttons.boardinput.singletouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.view.PuzzleSelectionView;
import com.white.black.nonogram.view.buttons.ButtonState;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class RedoButtonView extends PicButtonView {

    public RedoButtonView(ViewListener viewListener, RectF bounds, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(viewListener, bounds, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

    public void draw(Canvas canvas, Paint paint, ButtonState buttonState) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        if (buttonState.equals(ButtonState.ENABLED)) {
            canvas.drawBitmap(innerImages[0], null, bounds, paint);
        } else {
            canvas.drawBitmap(innerImages[1], null, bounds, paint);
        }
    }

    @Override
    public void onButtonPressed() {
        if (PuzzleSelectionView.INSTANCE.getSelectedPuzzle().isRedoable()) {
            PuzzleSelectionView.INSTANCE.getSelectedPuzzle().setRedo();
        }

        MyMediaPlayer.play("blop");
    }
}
