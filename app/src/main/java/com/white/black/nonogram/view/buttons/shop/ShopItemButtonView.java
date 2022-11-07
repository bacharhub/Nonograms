package com.white.black.nonogram.view.buttons.shop;

import static com.white.black.nonogram.utils.SharedPreferenceUtils.coinsAvailable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.listeners.ViewListener;

public class ShopItemButtonView {

    private String description;
    private int cost;
    private RectF bounds;
    private int buyColor1;
    private int buyColor2;
    private int buyColor3;
    private final Runnable purchase;

    private Bitmap coin;
    private Bitmap coinBlackAndWhite;
    private RectF buyButtonBounds;
    private BuyShopItemButtonView buyShopItemButtonView;
    private Bitmap image;
    private RectF imageBounds;

    public ShopItemButtonView(Bitmap image, String description, int cost, RectF bounds, int buyColor1, int buyColor2, int buyColor3, Runnable purchase) {
        this.description = description;
        this.cost = cost;
        this.bounds = bounds;
        this.buyColor1 = buyColor1;
        this.buyColor2 = buyColor2;
        this.buyColor3 = buyColor3;
        this.purchase = purchase;
        this.image = image;
    }

    public void init(Context context, Paint paint) {
        imageBounds = new RectF(
                bounds.centerX() - bounds.width() * 30 / 100,
                bounds.top + bounds.width() * 10 / 100,
                bounds.centerX() + bounds.width() * 30 / 100,
                bounds.top + bounds.width() * 70 / 100
        );

        coin = BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64);
        coinBlackAndWhite = BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64_black_and_white);
        buyButtonBounds = new RectF(
                bounds.left + bounds.width() * 5 / 100,
                bounds.bottom - bounds.width() * 5 / 100 - 105,
                bounds.right - bounds.width() * 5 / 100,
                bounds.bottom - bounds.width() * 5 / 100
        );

        buyShopItemButtonView = new BuyShopItemButtonView(
                (ViewListener) context,
                String.valueOf(cost),
                buyButtonBounds,
                buyColor1,
                buyColor2,
                buyColor3,
                new Bitmap[]{coin, coinBlackAndWhite},
                context,
                paint,
                purchase,
                () -> coinsAvailable(context) >= cost
        );
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(bounds, 45, 45, paint);
        paint.setColor(buyColor1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRoundRect(bounds, 45, 45, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawBitmap(image, null, imageBounds, paint);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 23);
        canvas.drawText(description, imageBounds.centerX(), buyButtonBounds.top * 3 / 4 + imageBounds.bottom * 1 / 4, paint);
        buyShopItemButtonView.draw(canvas, paint);
    }

    public void onTouch() {
        if (buyShopItemButtonView.wasPressed()) {
            buyShopItemButtonView.onButtonPressed();
            MyMediaPlayer.play("purchase");
        }
    }
}
