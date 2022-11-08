package com.white.black.nonogram.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.R;
import com.white.black.nonogram.RewardType;
import com.white.black.nonogram.utils.DailyRewardUtil;
import com.white.black.nonogram.view.buttons.ClaimDailyRewardButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class DailyRewardView {

    private int backgroundColor;
    private RectF backgroundBounds;
    private RectF windowBounds;
    private LinearGradient windowGradient;
    private Pair<RewardType, Integer> todayReward;
    private Pair<RewardType, Integer> tomorrowReward;
    private Pair<RewardType, Integer> dayAfterTomorrowReward;
    private DailyRewardUtil dailyRewardUtil;
    private RectF todayRectangleBounds;
    private RectF tomorrowRectangleBounds;
    private RectF dayAfterTomorrowRectangleBounds;
    private Bitmap coin;
    private Bitmap key;
    private Bitmap clue;
    private int todayRectangleColor;
    private int tomorrowRectangleColor;

    private ClaimDailyRewardButtonView claimButtonView;

    public void init(Context context, Paint paint) {
        backgroundColor = ContextCompat.getColor(context, R.color.shopBackground);
        backgroundBounds = new RectF(
                0,
                0,
                ApplicationSettings.INSTANCE.getScreenWidth(),
                ApplicationSettings.INSTANCE.getScreenHeight()
        );

        todayRectangleColor = Color.WHITE;
        tomorrowRectangleColor = ContextCompat.getColor(context, R.color.menuBackground);

        coin = BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64);
        key = BitmapLoader.INSTANCE.getImage(context, R.drawable.key_512);
        clue = BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512);

        dailyRewardUtil = new DailyRewardUtil();
        todayReward = dailyRewardUtil.getTodayReward(context);
        tomorrowReward = dailyRewardUtil.getTomorrowReward(context);
        dayAfterTomorrowReward = dailyRewardUtil.getDayAfterTomorrowReward(context);

        windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 5 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 38 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 95 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 58 / 100
        );

        windowGradient = new LinearGradient(
                windowBounds.left,
                windowBounds.top,
                windowBounds.left,
                windowBounds.bottom,
                new int[]{Color.WHITE, ContextCompat.getColor(context, R.color.smallPuzzleGreen1)},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        Bitmap giftImage = BitmapLoader.INSTANCE.getImage(context, R.drawable.gift_100);
        claimButtonView = new ClaimDailyRewardButtonView(
                (ViewListener) context,
                "Claim!",
                new RectF(
                        windowBounds.centerX() - ApplicationSettings.INSTANCE.getScreenWidth() * 19 / 100,
                        windowBounds.bottom - ApplicationSettings.INSTANCE.getScreenHeight() * 1 / 100,
                        windowBounds.centerX() + ApplicationSettings.INSTANCE.getScreenWidth() * 19 / 100,
                        windowBounds.bottom + ApplicationSettings.INSTANCE.getScreenHeight() * 6 / 100
                ),
                ContextCompat.getColor(context, R.color.smallPuzzleGreen1), ContextCompat.getColor(context, R.color.smallPuzzleGreen2), ContextCompat.getColor(context, R.color.smallPuzzleGreen3),
                new Bitmap[]{giftImage},
                context,
                paint
        );

        float horizontalGapBetweenItems = windowBounds.width() * 4 / 100;
        float verticalGapFromBottom = horizontalGapBetweenItems;
        float itemWidth = windowBounds.width() * 28 / 100;

        todayRectangleBounds = new RectF(
                windowBounds.left + horizontalGapBetweenItems,
                windowBounds.bottom - verticalGapFromBottom - windowBounds.height() * 95 / 100,
                windowBounds.left + horizontalGapBetweenItems + itemWidth,
                windowBounds.bottom - verticalGapFromBottom
        );

        tomorrowRectangleBounds = new RectF(
                windowBounds.left + 2 * horizontalGapBetweenItems + itemWidth,
                windowBounds.bottom - verticalGapFromBottom - windowBounds.height() * 67 / 100,
                windowBounds.left + 2 * horizontalGapBetweenItems + 2 * itemWidth,
                windowBounds.bottom - verticalGapFromBottom
        );

        dayAfterTomorrowRectangleBounds = new RectF(
                windowBounds.left + 3 * horizontalGapBetweenItems + 2 * itemWidth,
                windowBounds.bottom - verticalGapFromBottom - windowBounds.height() * 67 / 100,
                windowBounds.left + 3 * horizontalGapBetweenItems + 3 * itemWidth,
                windowBounds.bottom - verticalGapFromBottom
        );
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(backgroundColor);
        canvas.drawRect(backgroundBounds, paint);
        paint.setShader(windowGradient);
        canvas.drawRoundRect(windowBounds, 45, 45, paint);
        paint.setShader(null);

        paint.setColor(todayRectangleColor);
        canvas.drawRoundRect(todayRectangleBounds, 45, 45, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Today", todayRectangleBounds.centerX(), todayRectangleBounds.top + ApplicationSettings.INSTANCE.getScreenHeight() * 38 / 1000, paint);
        canvas.drawText("Tomorrow", tomorrowRectangleBounds.centerX(), todayRectangleBounds.top + ApplicationSettings.INSTANCE.getScreenHeight() * 38 / 1000, paint);

        paint.setColor(tomorrowRectangleColor);
        canvas.drawRoundRect(tomorrowRectangleBounds, 45, 45, paint);

        canvas.drawRoundRect(dayAfterTomorrowRectangleBounds, 45, 45, paint);

        drawReward(canvas, paint, todayReward, todayRectangleBounds);
        drawReward(canvas, paint, tomorrowReward, tomorrowRectangleBounds);
        drawReward(canvas, paint, dayAfterTomorrowReward, dayAfterTomorrowRectangleBounds);

        claimButtonView.draw(canvas, paint);
    }

    private void drawReward(Canvas canvas, Paint paint, Pair<RewardType, Integer> reward, RectF rewardBounds) {
        canvas.drawBitmap(
                imageByRewardType(reward.first),
                null,
                new RectF(rewardBounds.centerX() - 64,
                        todayRectangleBounds.centerY() - 64,
                        rewardBounds.centerX() + 64,
                        todayRectangleBounds.centerY() + 64),
                paint
        );

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 42);
        canvas.drawText("+" + reward.second, rewardBounds.centerX(), todayRectangleBounds.bottom - todayRectangleBounds.height() / 10, paint);
    }

    private Bitmap imageByRewardType(RewardType rewardType) {
        Bitmap bitmap = null;
        switch (rewardType) {
            case COINS:
                bitmap = coin;
                break;
            case CLUES:
                bitmap = clue;
                break;
            case KEYS:
                bitmap = key;
                break;
        }

        return bitmap;
    }

    public void onTouch() {
        if (claimButtonView.wasPressed()) {
            claimButtonView.onButtonPressed();
        }
    }
}
