package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.view.buttons.LabeledPicButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;

public class ComplexPuzzleButtonView extends LabeledPicButtonView {

    private final RectF[] innerImageBoundsGrid;
    private final RectF[] pressedInnerImageBoundsGrid;

    public ComplexPuzzleButtonView(MenuViewListener menuViewListener, RectF bounds, String description, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(menuViewListener, bounds, description, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
        this.innerImageBoundsGrid = new RectF[] {
                new RectF(innerImageBounds.left, innerImageBounds.top, innerImageBounds.centerX(), innerImageBounds.centerY()),
                new RectF(innerImageBounds.centerX(), innerImageBounds.top, innerImageBounds.right, innerImageBounds.centerY()),
                new RectF(innerImageBounds.left, innerImageBounds.centerY(), innerImageBounds.centerX(), innerImageBounds.bottom),
                new RectF(innerImageBounds.centerX(), innerImageBounds.centerY(), innerImageBounds.right, innerImageBounds.bottom)
        };
        this.pressedInnerImageBoundsGrid = new RectF[] {
                new RectF(pressedInnerImageBounds.left, pressedInnerImageBounds.top, pressedInnerImageBounds.centerX(), pressedInnerImageBounds.centerY()),
                new RectF(pressedInnerImageBounds.centerX(), pressedInnerImageBounds.top, pressedInnerImageBounds.right, pressedInnerImageBounds.centerY()),
                new RectF(pressedInnerImageBounds.left, pressedInnerImageBounds.centerY(), pressedInnerImageBounds.centerX(), pressedInnerImageBounds.bottom),
                new RectF(pressedInnerImageBounds.centerX(), pressedInnerImageBounds.centerY(), pressedInnerImageBounds.right, pressedInnerImageBounds.bottom)
        };
    }

    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF[] boundsGrid = (isPressed())? pressedInnerImageBoundsGrid : innerImageBoundsGrid;
        for(RectF bounds : boundsGrid) {
            canvas.drawBitmap(innerImages[0], null, bounds, paint);
        }
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onComplexPuzzleButtonPressed();
    }
}
