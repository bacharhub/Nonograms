package com.white.black.nonogram.view.buttons.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.buttons.LabeledPicButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;

public class LotteryButtonView extends LabeledPicButtonView {

    private Bitmap newIcon;
    private RectF newIconBounds;

    public LotteryButtonView(MenuViewListener menuViewListener, RectF bounds, String description, int color1, int color2, int color3, Bitmap[] innerImages, Context context, Paint paint) {
        super(menuViewListener, bounds, description, color1, color2, color3, innerImages, new RectF(
                bounds.left + bounds.width() * 1 / 10,
                bounds.top + bounds.height() * 1 / 10,
                bounds.right - bounds.width() * 1 / 10,
                bounds.bottom - bounds.height() * 1 / 10), context, paint);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        RectF bounds = (isPressed())? pressedInnerImageBounds : innerImageBounds;
        canvas.drawBitmap(innerImages[0], null, bounds, paint);
        canvas.drawBitmap(newIcon, null, newIconBounds, paint);
    }

    public void init(Context context) {
        newIcon = BitmapLoader.INSTANCE.getImage(context, R.drawable.new_100);
        newIconBounds = new RectF(
          this.bounds.right - this.bounds.width() / 4,
          this.bounds.top - this.bounds.width() / 4,
          this.bounds.right + this.bounds.width() / 4,
          this.bounds.top + this.bounds.width() / 4
        );
    }

    @Override
    public void onButtonPressed() {
        ((MenuViewListener)viewListener).onLotteryButtonPressed();
    }
}
