package com.white.black.nonogram.view;

import static com.white.black.nonogram.utils.SharedPreferenceUtils.addToClueCount;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.addToKeyCount;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.useCoins;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.view.buttons.shop.LeaveShopButtonView;
import com.white.black.nonogram.view.buttons.shop.ShopItemButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.ArrayList;
import java.util.List;

public class ShopView {

    private int backgroundColor;
    private RectF backgroundBounds;
    private BankView bankView;
    private Bitmap door;
    private boolean show;
    private LeaveShopButtonView leaveShopButtonView;
    private List<ShopItemButtonView> items;

    public void show() {
        this.show = true;
    }

    public boolean isShown() {
        return this.show;
    }

    public void hide() {
        this.show = false;
    }

    public void init(Context context, Paint paint) {
        backgroundColor = ContextCompat.getColor(context, R.color.shopBackground);
        backgroundBounds = new RectF(
                0,
                0,
                ApplicationSettings.INSTANCE.getScreenWidth(),
                ApplicationSettings.INSTANCE.getScreenHeight()
        );

        bankView = new BankView();
        bankView.init(context);

        door = BitmapLoader.INSTANCE.getImage(context, R.drawable.open_door_with_stroke_64);

        leaveShopButtonView = new LeaveShopButtonView(
                (ViewListener) context,
                new RectF(
                        100,
                        ApplicationSettings.INSTANCE.getScreenHeight() - 292,
                        292,
                        ApplicationSettings.INSTANCE.getScreenHeight() - 100
                ),
                Color.LTGRAY,
                Color.GRAY,
                Color.DKGRAY,
                new Bitmap[]{door},
                context,
                new Paint());

        addItemsToShop(context, paint);
    }

    private void itemPurchased(String itemPurchased, Context context) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.SHOP, itemPurchased);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    private void addItemsToShop(Context context, Paint paint) {
        int gap = ApplicationSettings.INSTANCE.getScreenWidth() * 4 / 100;
        int verticalItemWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 28 / 100;
        int verticalItemHeight = ApplicationSettings.INSTANCE.getScreenHeight() * 20 / 100;

        Bitmap bulb = BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512);
        Bitmap key = BitmapLoader.INSTANCE.getImage(context, R.drawable.key_512);

        items = new ArrayList<>(6);
        items.add(new ShopItemButtonView(
                bulb,
                "+1",
                10,
                new RectF(
                        gap,
                        280,
                        gap + verticalItemWidth,
                        280 + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink1),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink2),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_CLUE_1_PURCHASE, context);
                    addToClueCount(context, 1);
                    useCoins(context, 10);
                }
        ));

        items.add(new ShopItemButtonView(
                bulb,
                "+10",
                85,
                new RectF(
                        2 * gap + verticalItemWidth,
                        280,
                        2 * gap + 2 * verticalItemWidth,
                        280 + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink1),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink2),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_CLUE_10_PURCHASE, context);
                    addToClueCount(context, 10);
                    useCoins(context, 85);
                }
        ));

        items.add(new ShopItemButtonView(
                bulb,
                "+50",
                350,
                new RectF(
                        3 * gap + 2 * verticalItemWidth,
                        280,
                        3 * gap + 3 * verticalItemWidth,
                        280 + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink1),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink2),
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_CLUE_50_PURCHASE, context);
                    addToClueCount(context, 50);
                    useCoins(context, 350);
                }
        ));

        items.add(new ShopItemButtonView(
                key,
                "+1",
                30,
                new RectF(
                        gap,
                        280 + verticalItemHeight + gap,
                        gap + verticalItemWidth,
                        280 + verticalItemHeight + gap + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_KEY_1_PURCHASE, context);
                    addToKeyCount(context, 1);
                    useCoins(context, 30);
                }
        ));

        items.add(new ShopItemButtonView(
                key,
                "+5",
                125,
                new RectF(
                        2 * gap + verticalItemWidth,
                        280 + verticalItemHeight + gap,
                        2 * gap + 2 * verticalItemWidth,
                        280 + verticalItemHeight + gap + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_KEY_5_PURCHASE, context);
                    addToKeyCount(context, 5);
                    useCoins(context, 125);
                }
        ));

        items.add(new ShopItemButtonView(
                key,
                "+20",
                420,
                new RectF(
                        3 * gap + 2 * verticalItemWidth,
                        280 + verticalItemHeight + gap,
                        3 * gap + 3 * verticalItemWidth,
                        280 + verticalItemHeight + gap + verticalItemHeight
                ),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2),
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3),
                () -> {
                    itemPurchased(GameMonitoring.SHOP_KEY_20_PURCHASE, context);
                    addToKeyCount(context, 20);
                    useCoins(context, 420);
                }
        ));

        items.forEach(item -> item.init(context, paint));
    }

    public void draw(Canvas canvas, Paint paint) {
        if (show) {
            paint.setColor(backgroundColor);
            canvas.drawRect(backgroundBounds, paint);
            bankView.draw(canvas, paint);
            leaveShopButtonView.draw(canvas, paint);

            items.forEach(item -> item.draw(canvas, paint));
        }
    }

    public void onTouch() {
        if (leaveShopButtonView.wasPressed()) {
            leaveShopButtonView.onButtonPressed();
        }

        items.forEach(ShopItemButtonView::onTouch);
    }
}
