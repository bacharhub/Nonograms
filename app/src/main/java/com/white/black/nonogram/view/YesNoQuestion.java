package com.white.black.nonogram.view;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Paint;
        import android.graphics.RectF;

        import androidx.core.content.ContextCompat;

        import com.white.black.nonogram.ApplicationSettings;
        import com.white.black.nonogram.BitmapLoader;
        import com.white.black.nonogram.R;
        import com.white.black.nonogram.TouchMonitor;
        import com.white.black.nonogram.view.buttons.CloseWindowButtonView;
        import com.white.black.nonogram.view.buttons.YesNoButtonView;
        import com.white.black.nonogram.view.listeners.ViewListener;

public enum YesNoQuestion {

    INSTANCE;

    private Appearance appearance;

    private int backgroundColor;
    private CloseWindowButtonView closeWindowButtonView;
    private RectF windowBackgroundBounds;
    private Popup popup;

    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    YesNoQuestion() {
        this.appearance = Appearance.MINIMIZED;
    }

    public void init(Context context, Paint paint, String message, Runnable onYesAnswer, Runnable onNoAnswer) {
        RectF windowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 15 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 45 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 85 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 65 / 100
        ) /* windowBounds */;

        float padding = windowBounds.width() / 60;
        RectF windowInnerBackgroundBounds = new RectF(windowBounds.left + padding, windowBounds.top + padding, windowBounds.right - padding, windowBounds.bottom - padding);
        float closeButtonEdgeLength = windowBounds.width() / 7;
        RectF noButtonBounds = new RectF(
                windowInnerBackgroundBounds.centerX() + closeButtonEdgeLength / 5,
                windowBounds.bottom - closeButtonEdgeLength / 2 - closeButtonEdgeLength,
                windowInnerBackgroundBounds.right - closeButtonEdgeLength / 4,
                windowBounds.bottom - closeButtonEdgeLength / 2
        );

        YesNoButtonView noButtonView = new YesNoButtonView(
                (ViewListener)context,
                context.getString(R.string.no),
                noButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        RectF yesButtonBounds = new RectF(
                windowInnerBackgroundBounds.left + closeButtonEdgeLength / 4,
                windowBounds.bottom - closeButtonEdgeLength / 2 - closeButtonEdgeLength,
                windowInnerBackgroundBounds.centerX() - closeButtonEdgeLength / 5,
                windowBounds.bottom - closeButtonEdgeLength / 2
        );

        YesNoButtonView yesButtonView = new YesNoButtonView(
                (ViewListener)context,
                context.getString(R.string.yes),
                yesButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.yes_512)}, context, paint);

        this.popup = new Popup(
                context, windowBounds, message, onYesAnswer, onNoAnswer, yesButtonView, noButtonView
        );

        windowBackgroundBounds = new RectF(windowBounds.left, windowBounds.top, windowBounds.right + windowBounds.width() * 2 / 100, windowBounds.bottom + windowBounds.height() * 2 / 100);

        backgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);

        int windowInnerBackgroundColor = ContextCompat.getColor(context, R.color.menuBackground);
        int gameSettingsWindowGradientTo = ContextCompat.getColor(context, R.color.gameSettingsWindowGradientTo);
        int windowBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsWindowBackground);

        RectF closeButtonBounds = new RectF(
                windowBackgroundBounds.right - closeButtonEdgeLength,
                windowBounds.top - windowBounds.width() / 15 - closeButtonEdgeLength,
                windowBackgroundBounds.right,
                windowBounds.top - windowBounds.width() / 15
        );

        closeWindowButtonView = new CloseWindowButtonView(
                (ViewListener)context,
                closeButtonBounds,
                windowInnerBackgroundColor, gameSettingsWindowGradientTo, windowBackgroundColor, new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.close_window_100)}, context, paint);

        this.appearance = Appearance.MAXIMIZED;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
        paint.setAlpha(255);
        popup.draw(canvas, paint);
        closeWindowButtonView.draw(canvas, paint);
    }

    public void onTouchEvent() {
        if (!windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getUpCoordinates().x, TouchMonitor.INSTANCE.getUpCoordinates().y) &&
                !windowBackgroundBounds.contains(TouchMonitor.INSTANCE.getDownCoordinates().x, TouchMonitor.INSTANCE.getDownCoordinates().y)) {
            popup.doOnNoAnswer();
        } else {
            popup.onTouchEvent();
        }
    }
}
