package com.white.black.nonogram.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.utils.VipPromotionUtils;
import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
import com.white.black.nonogram.view.buttons.YesNoButtonView;
import com.white.black.nonogram.view.buttons.menu.PromoteVipButtonView;
import com.white.black.nonogram.view.listeners.ViewListener;

public class UseKeyPopup {

    private final String rewardDescription;
    private final Runnable onNoAnswer;
    private Runnable onYesAnswer;

    private Popup popup;
    private VipPopup vipPopup;
    private RectF popupBounds;
    private boolean showPopup;
    private boolean showVipPopup;
    private CloseWindowButtonView closeWindowButtonView;
    private PromoteVipButtonView promoteVipButtonView;
    private int darkBackgroundColor;

    public UseKeyPopup(
            Context context,
            Paint paint,
            String rewardDescription,
            Bitmap rewardIcon,
            Runnable onNoAnswer,
            Runnable onYesAnswer
    ) {
        this.rewardDescription = rewardDescription;
        this.onNoAnswer = onNoAnswer;
        this.onYesAnswer = onYesAnswer;
        setup(context, paint, rewardDescription, rewardIcon);
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

    private void setup(Context context, Paint paint, String rewardDescription, Bitmap rewardIcon) {
        popupBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 16 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 41 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 84 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 59 / 100
        );

        float closeButtonEdgeLength = popupBounds.width() / 6;

        RectF noButtonBounds = new RectF(
                popupBounds.centerX() + closeButtonEdgeLength / 5,
                popupBounds.bottom - popupBounds.height() * 4 / 10,
                popupBounds.right - closeButtonEdgeLength / 4,
                popupBounds.bottom - popupBounds.height() / 8
        );

        RectF yesButtonBounds = new RectF(
                popupBounds.left + closeButtonEdgeLength / 4,
                popupBounds.bottom - popupBounds.height() * 4 / 10,
                popupBounds.centerX() - closeButtonEdgeLength / 5,
                popupBounds.bottom - popupBounds.height() / 8
        );

        YesNoButtonView noButtonView = new YesNoButtonView(
                (ViewListener) context,
                context.getString(R.string.no),
                noButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);


        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener) context,
                context.getString(R.string.yes),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1),
                ContextCompat.getColor(context, R.color.settingsBrown2),
                ContextCompat.getColor(context, R.color.settingsBrown3),
                new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.key_512)},
                context,
                paint);

        Runnable onNoAnswer = () -> {
            this.onNoAnswer.run();
            showPopup = false;
            showVipPopup = false;
        };

        Runnable onYesAnswer = () -> {
            this.onYesAnswer.run();
            showPopup = false;
            showVipPopup = false;
        };

        this.popup = new Popup(
                context,
                popupBounds,
                rewardDescription,
                onYesAnswer,
                onNoAnswer,
                yesButtonView,
                noButtonView,
                BitmapLoader.INSTANCE.getImage(context, R.drawable.lock_512),
                null
        );

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

    public void draw(Canvas canvas, Paint paint) {
        if (showPopup) {
            paint.setColor(darkBackgroundColor);
            canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            popup.draw(canvas, paint);

            if (!popup.isAnswered()) {
                closeWindowButtonView.draw(canvas, paint);
                promoteVipButtonView.draw(canvas, paint);

                if (showVipPopup) {
                    vipPopup.draw(canvas, paint);
                }
            }
        }
    }

    public void onTouchEvent() {
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
                        popup.doOnNoAnswered();
                        popup.setAnswered(false);
                    }
                }
            } else if (popup.onTouchEvent()) {
                popup.setAnswered(false);
            }
        }
    }
}