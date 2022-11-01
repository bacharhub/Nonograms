package com.white.black.nonogram.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.RewardedInstanceHandler;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.utils.VipPromotionUtils;
import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
import com.white.black.nonogram.view.buttons.YesNoButtonView;
import com.white.black.nonogram.view.buttons.menu.PromoteVipButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.function.Consumer;

public class WatchAdPopup {

    private final String rewardDescription;
    private final Runnable onNoAnswer;
    private Runnable onRewarded;
    private final Consumer<LoadAdError> onAdFailedToLoad;
    private final Runnable onRewardedVideoAdClosed;

    private Popup popup;
    private VipPopup vipPopup;
    private RectF popupBounds;
    private boolean showPopup;
    private boolean showVipPopup;
    private CloseWindowButtonView closeWindowButtonView;
    private PromoteVipButtonView promoteVipButtonView;
    private int darkBackgroundColor;

    private final RewardedInstanceHandler rewardedInstanceHandler = new RewardedInstanceHandler();
    private final OnUserEarnedRewardListener userEarnedRewardListener = rewardItem -> onRewarded.run();
    private RewardedAdLoadCallback rewardedAdLoadCallback;
    private FullScreenContentCallback fullScreenContentCallback;

    public WatchAdPopup(
            Context context,
            Paint paint,
            String rewardDescription,
            Bitmap rewardIcon,
            Runnable onNoAnswer,
            Runnable onRewarded,
            Consumer<LoadAdError> onAdFailedToLoad,
            Runnable onRewardedVideoAdClosed
    ) {
        this.rewardDescription = rewardDescription;
        this.onNoAnswer = onNoAnswer;
        this.onRewarded = onRewarded;
        this.onAdFailedToLoad = onAdFailedToLoad;
        this.onRewardedVideoAdClosed = onRewardedVideoAdClosed;
        setup(context, paint, rewardDescription, rewardIcon);
    }

    public VipPopup getVipPopup() {
        return this.vipPopup;
    }

    public Popup getPopup() {
        return this.popup;
    }

    public void setShowPopup(boolean b) {
        this.showPopup = b;
    }

    public boolean isShowingPopup() {
        return this.showPopup;
    }

    public boolean isShowingVipPopup() {
        return this.showVipPopup;
    }

    public void setShowVipPopup(boolean showVipPopup) {
        this.showVipPopup = showVipPopup;
    }

    public void initPopup(Context context) {
        popup.setMessage(rewardDescription);
        popup.setTopLeftImage(BitmapLoader.INSTANCE.getImage(context, R.drawable.gift_100));
        popup.setAnswered(false);
        this.showVipPopup = false;
    }

    private void setup(Context context, Paint paint, String rewardDescription, Bitmap rewardIcon) {
        popupBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 16 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 41 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 84 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 59 / 100
        );

        RectF yesButtonBounds = new RectF(
                popupBounds.centerX() - popupBounds.width() / 3,
                popupBounds.bottom - popupBounds.height() * 4 / 10,
                popupBounds.centerX() + popupBounds.width() / 3,
                popupBounds.bottom - popupBounds.height() / 8
        );

        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener) context,
                context.getString(R.string.watchAd),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1),
                ContextCompat.getColor(context, R.color.settingsBrown2),
                ContextCompat.getColor(context, R.color.settingsBrown3),
                new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.play_512)},
                context,
                paint);

        Runnable onNoAnswer = () -> {
            setShowPopup(false);
            initPopup(context);
            MyMediaPlayer.play("blop");
            this.onNoAnswer.run();
        };

        Runnable onYesAnswer = () -> {
            popup.setMessage(context.getString(R.string.loading));
            popup.setTopLeftImage(BitmapLoader.INSTANCE.getImage(context, R.drawable.sand_watch_100));
            AdManager.loadRewardedVideo(rewardedInstanceHandler, (Activity) context, userEarnedRewardListener, rewardedAdLoadCallback);
        };

        this.popup = new Popup(
                context,
                popupBounds,
                rewardDescription,
                onYesAnswer,
                onNoAnswer,
                yesButtonView,
                null,
                BitmapLoader.INSTANCE.getImage(context, R.drawable.gift_100),
                rewardIcon
        );

        float closeButtonEdgeLength = popupBounds.width() / 6;

        int closeButton1 = ContextCompat.getColor(context, R.color.menuBackground);
        int closeButton2 = ContextCompat.getColor(context, R.color.gameSettingsWindowGradientTo);
        int closeButton3 = ContextCompat.getColor(context, R.color.gameSettingsWindowBackground);

        RectF closeButtonBounds = new RectF(
                popupBounds.right - closeButtonEdgeLength,
                popupBounds.top - popupBounds.width() / 15 - closeButtonEdgeLength,
                popupBounds.right,
                popupBounds.top - popupBounds.width() / 15
        );

        closeWindowButtonView = new CloseWindowButtonView(
                (ViewListener) context,
                closeButtonBounds,
                closeButton1, closeButton2, closeButton3, new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF promoteVipButtonBounds = new RectF(
                closeButtonBounds.left - popupBounds.width() / 15 - closeButtonEdgeLength * 94 / 70,
                closeButtonBounds.top,
                closeButtonBounds.left - popupBounds.width() / 15,
                closeButtonBounds.bottom
        );

        promoteVipButtonView = new PromoteVipButtonView(
                (ViewListener) context,
                promoteVipButtonBounds,
                closeButton1, closeButton2, closeButton3, new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.vip_100)}, context, paint);

        rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                rewardedInstanceHandler.setRewardedVideoAd(rewardedAd);
                rewardedInstanceHandler.setLoadingRewardedAd(false);
                rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);

                if (showPopup) {
                    AdManager.showRewardedVideo(rewardedInstanceHandler, (Activity) context, userEarnedRewardListener);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedInstanceHandler.setLoadingRewardedAd(false);
                onRewarded.run();
                onRewardedVideoAdClosed(context);
                onAdFailedToLoad.accept(loadAdError);

                super.onAdFailedToLoad(loadAdError);
            }
        };

        fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Code to be invoked when the ad dismissed full screen content.
                        onRewardedVideoAdClosed(context);
                    }
                };

        darkBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);

        this.vipPopup = new VipPopup(
                context,
                paint,
                () -> VipPromotionUtils.INSTANCE.onPurchaseVipPressed((Activity) context),
                () -> {
                    setShowVipPopup(false);
                    this.onNoAnswer.run();
                }
        );
    }

    private void onRewardedVideoAdClosed(Context context) {
        setShowPopup(false);
        initPopup(context);
        rewardedInstanceHandler.setRewardedVideoAd(null);
        onRewardedVideoAdClosed.run();
    }

    public void draw(Canvas canvas, Paint paint) {
        if (showPopup) {
            paint.setColor(darkBackgroundColor);
            canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            popup.draw(canvas, paint);

            if (!popup.isAnswered()) { // loading ad
                closeWindowButtonView.draw(canvas, paint);
                promoteVipButtonView.draw(canvas, paint);

                if (showVipPopup) {
                    vipPopup.draw(canvas, paint);
                }
            }
        }
    }

    public void onTouchEvent() {
        if (popup.isAnswered()) { // loading ad, don't allow other actions
            return;
        }

        if (showVipPopup) {
            if (TouchMonitor.INSTANCE.touchUp()) {
                vipPopup.onTouchEvent();
            }
        } else if (showPopup) {
            if (!popupBounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y) &&
                    !popupBounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)) {
                if (TouchMonitor.INSTANCE.touchUp()) {
                    if (promoteVipButtonView.wasPressed()) {
                        promoteVipButtonView.onButtonPressed();
                    } else {
                        popup.doOnNoAnswer();
                        popup.setAnswered(false);
                    }
                }
            } else {
                popup.onTouchEvent();
            }
        }
    }
}