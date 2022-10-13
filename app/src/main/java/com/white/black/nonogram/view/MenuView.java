package com.white.black.nonogram.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.activities.MenuActivity;
import com.white.black.nonogram.view.buttons.menu.ColorfulPuzzleButtonView;
import com.white.black.nonogram.view.buttons.menu.ComplexPuzzleButtonView;
import com.white.black.nonogram.view.buttons.menu.LargePuzzleButtonView;
import com.white.black.nonogram.view.buttons.menu.LeaderboardButtonView;
import com.white.black.nonogram.view.buttons.menu.NormalPuzzleButtonView;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.buttons.menu.MenuSettingsButtonView;
import com.white.black.nonogram.view.buttons.menu.PromoteVipButtonView;
import com.white.black.nonogram.view.buttons.menu.SmallPuzzleButtonView;
import com.white.black.nonogram.view.listeners.MenuViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class MenuView extends SurfaceView implements SurfaceHolder.Callback {

    private volatile boolean initDone;

    private final MenuViewListener menuViewListener;
    private List<PicButtonView> picButtonViews;
    private List<Bitmap> food;
    private int backgroundColor;
    private int darkBackgroundColor;
    private Bitmap sun;
    private RectF sunBounds;
    private Bitmap pencil;
    private RectF pencilBounds;

    private Bitmap loadingGear;
    private RectF loadingGearBigBounds;
    private RectF loadingGearSmallBounds;
    private OptionsView menuOptionsView;
    private Bitmap background;

    private VipPopup vipPopup;
    private boolean showVipPopup;

    public VipPopup getVipPopup() {
        return vipPopup;
    }

    public boolean isShowVipPopup() {
        return showVipPopup;
    }

    public void setShowVipPopup(boolean showVipPopup) {
        this.showVipPopup = showVipPopup;
    }

    public MenuView(Context context) {
        super(context);
        menuViewListener = (MenuViewListener) context;
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public void render() {
        if (initDone && GameState.getGameState().equals(GameState.MENU)) {
            Canvas canvas = getHolder().lockCanvas();
            try {
                if (canvas != null) {
                    draw(canvas, PaintManager.INSTANCE.createPaint());
                }
            } catch (Exception ignored) {
            } finally {
                if (canvas != null) {
                    try {
                        getHolder().unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        if (MemoryManager.isCriticalMemory()) {
            if (background != null && !background.isRecycled()) {
                canvas.drawBitmap(background, 0, 0, paint);
            } else {
                paint.setColor(backgroundColor);
                canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
                canvas.drawBitmap(sun, null, sunBounds, paint);
                canvas.drawBitmap(pencil, null, pencilBounds, paint);
            }

            return;
        }

        if (background == null || background.isRecycled()) {
            Canvas tempCanvas;
            try {
                background = Bitmap.createBitmap(ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), Bitmap.Config.ARGB_8888);
                background.setDensity(Bitmap.DENSITY_NONE);
                tempCanvas = new Canvas(background);
            } catch (Exception ex) {
                tempCanvas = canvas;
                background = null;
            }

            paint.setColor(backgroundColor);
            tempCanvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            tempCanvas.drawBitmap(sun, null, sunBounds, paint);
            tempCanvas.drawBitmap(pencil, null, pencilBounds, paint);
            paint.setAlpha(30);
            int foodItemDrawnCounter = 0;
            for (int x = 0; x < ApplicationSettings.INSTANCE.getScreenWidth(); x += 150) {
                for (int y = 0; y < ApplicationSettings.INSTANCE.getScreenHeight(); y += 150) {
                    tempCanvas.drawBitmap(food.get((foodItemDrawnCounter++) % food.size()), x, y, paint);
                }
            }
            paint.setAlpha(255);
        }

        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, 0, 0, paint);
        }
    }

    public void clearBackground() {
        if (background != null) {
            this.background.recycle();
        }
    }

    private void draw(Canvas canvas, Paint paint) {
        super.draw(canvas);
        drawBackground(canvas, paint);

        for (PicButtonView picButtonView : picButtonViews) {
            picButtonView.draw(canvas, paint);
        }

        if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            menuOptionsView.draw(canvas, paint);
        }

        if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            YesNoQuestion.INSTANCE.draw(canvas, paint);
        }

        if (isShowVipPopup()) {
            this.vipPopup.draw(canvas, paint);
        }

        if (!Puzzles.isFirstLoadingDone()) {
            paint.setColor(darkBackgroundColor);
            canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);

            canvas.save();
            canvas.rotate((System.currentTimeMillis() / 10) % 360, loadingGearBigBounds.centerX(), loadingGearBigBounds.centerY());
            canvas.drawBitmap(loadingGear, null, loadingGearBigBounds, null);
            canvas.restore();

            canvas.save();
            canvas.rotate((System.currentTimeMillis() / 10) % 360, loadingGearSmallBounds.centerX(), loadingGearSmallBounds.centerY());
            canvas.drawBitmap(loadingGear, null, loadingGearSmallBounds, null);
            canvas.restore();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    if (PaintManager.INSTANCE.isReadyToRender(System.currentTimeMillis())) {
                        render();
                        PaintManager.INSTANCE.setLastRenderingTime(System.currentTimeMillis());
                    }
                } while (!Puzzles.isFirstLoadingDone());

                render();
            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        if (Puzzles.isFirstLoadingDone() && initDone) {
            menuViewListener.onViewTouched(event);
            if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MINIMIZED) && YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MINIMIZED) && !isShowVipPopup()) {
                for (PicButtonView picButtonView : picButtonViews) {
                    if (picButtonView.wasPressed()) {
                        TouchMonitor.INSTANCE.setTouchUp(false);
                        picButtonView.onButtonPressed();
                        break;
                    }
                }
            } else {
                if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                    menuOptionsView.onTouchEvent();
                } else if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                    if (TouchMonitor.INSTANCE.touchUp()) {
                        YesNoQuestion.INSTANCE.onTouchEvent();
                    }
                } else if (isShowVipPopup()) {
                    if (TouchMonitor.INSTANCE.touchUp()) {
                        this.vipPopup.onTouchEvent();
                        TouchMonitor.INSTANCE.setTouchUp(false);
                    }
                }
            }

            if (PaintManager.INSTANCE.isReadyToRender(System.currentTimeMillis())) {
                render();
                PaintManager.INSTANCE.setLastRenderingTime(System.currentTimeMillis());
            }
        }

        return true;
    }


    public void init(Context context, Paint paint) {
        this.initDone = false;

        backgroundColor = ContextCompat.getColor(context, R.color.menuBackground);
        darkBackgroundColor = ContextCompat.getColor(context, R.color.gameSettingsBackground);

        food = new LinkedList<>();
        food.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.bread_100));
        food.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.cupcake_100));
        food.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.rice_bowl_100));
        food.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.strawberry_100));
        food.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.tomato_100));

        loadingGear = BitmapLoader.INSTANCE.getImage(context, R.drawable.settings_512);
        loadingGearBigBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 2 - ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10,
                ApplicationSettings.INSTANCE.getScreenWidth() * 62 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 2 + ApplicationSettings.INSTANCE.getScreenWidth() * 2 / 10);
        loadingGearSmallBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 56 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 2 - ApplicationSettings.INSTANCE.getScreenWidth() * 32 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() * 80 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 2 - ApplicationSettings.INSTANCE.getScreenWidth() * 8 / 100);
        sun = BitmapLoader.INSTANCE.getImage(context, R.drawable.sun_black_white_512);
        sunBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() / 12,
                ApplicationSettings.INSTANCE.getScreenHeight() / 10,
                ApplicationSettings.INSTANCE.getScreenWidth() * 6 / 12,
                ApplicationSettings.INSTANCE.getScreenHeight() / 10 + ApplicationSettings.INSTANCE.getScreenWidth() * 5 / 12);
        pencil = BitmapLoader.INSTANCE.getImage(context, R.drawable.pencil_color_512);
        pencilBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 27 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 11,
                ApplicationSettings.INSTANCE.getScreenWidth() * 52 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() / 11 + ApplicationSettings.INSTANCE.getScreenWidth() * 25 / 100);

        float lineTop = ApplicationSettings.INSTANCE.getScreenHeight() * 4 / 10;
        float buttonEdgeLength = ApplicationSettings.INSTANCE.getScreenWidth() * 3 / 13;
        float lineLeft = ApplicationSettings.INSTANCE.getScreenWidth() / 13;
        float horizontalGap = ApplicationSettings.INSTANCE.getScreenWidth() / 13;
        float verticalGap = ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 14;

        picButtonViews = new LinkedList<>();

        MenuSettingsButtonView settingsButtonView = new MenuSettingsButtonView(
                menuViewListener,
                new RectF(
                        ApplicationSettings.INSTANCE.getScreenWidth() - lineLeft - (buttonEdgeLength * 2 / 3),
                        lineLeft + lineLeft / 2 + (buttonEdgeLength * 2 / 3),
                        ApplicationSettings.INSTANCE.getScreenWidth() - lineLeft,
                        lineLeft + (buttonEdgeLength * 2 / 3) + lineLeft / 2 + (buttonEdgeLength * 2 / 3)
                ),
                Color.LTGRAY,
                Color.DKGRAY,
                Color.BLACK,
                new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.settings_512)},
                context,
                paint);

        picButtonViews.add(settingsButtonView);

        RectF promoteVipButtonBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() - lineLeft - (buttonEdgeLength * 2 / 3) * 94 / 70,
                lineLeft,
                ApplicationSettings.INSTANCE.getScreenWidth() - lineLeft,
                lineLeft + (buttonEdgeLength * 2 / 3)
        );

        picButtonViews.add(new PromoteVipButtonView(
                (ViewListener) context,
                promoteVipButtonBounds,
                Color.LTGRAY, Color.DKGRAY, Color.BLACK, new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.vip_100)}, context, paint));

        picButtonViews.add(new SmallPuzzleButtonView(
                menuViewListener,
                new RectF(
                        lineLeft,
                        lineTop,
                        lineLeft + buttonEdgeLength,
                        lineTop + buttonEdgeLength
                ), context.getString(R.string.small_puzzle_description), ContextCompat.getColor(context, R.color.smallPuzzleGreen1), ContextCompat.getColor(context, R.color.smallPuzzleGreen2), ContextCompat.getColor(context, R.color.smallPuzzleGreen3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_green_512)}, context, paint));
        picButtonViews.add(new NormalPuzzleButtonView(
                menuViewListener,
                new RectF(
                        lineLeft + (buttonEdgeLength + horizontalGap),
                        lineTop,
                        lineLeft + (buttonEdgeLength + horizontalGap) + buttonEdgeLength,
                        lineTop + buttonEdgeLength
                ), context.getString(R.string.normal_puzzle_description), ContextCompat.getColor(context, R.color.normalPuzzleOrange1), ContextCompat.getColor(context, R.color.normalPuzzleOrange2), ContextCompat.getColor(context, R.color.normalPuzzleOrange3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_yellow_512)}, context, paint));
        picButtonViews.add(new LargePuzzleButtonView(
                menuViewListener,
                new RectF(
                        lineLeft + (buttonEdgeLength + horizontalGap) * 2,
                        lineTop,
                        lineLeft + (buttonEdgeLength + horizontalGap) * 2 + buttonEdgeLength,
                        lineTop + buttonEdgeLength
                ), context.getString(R.string.large_puzzle_description), ContextCompat.getColor(context, R.color.largePuzzleRed1), ContextCompat.getColor(context, R.color.largePuzzleRed2), ContextCompat.getColor(context, R.color.largePuzzleRed3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_red_512)}, context, paint));
        picButtonViews.add(new ComplexPuzzleButtonView(
                menuViewListener,
                new RectF(
                        lineLeft,
                        lineTop + (buttonEdgeLength + verticalGap),
                        lineLeft + buttonEdgeLength,
                        lineTop + (buttonEdgeLength + verticalGap) + buttonEdgeLength
                ), context.getString(R.string.complex_puzzle_description), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_dark_512)}, context, paint));
        picButtonViews.add(new ColorfulPuzzleButtonView(
                menuViewListener,
                new RectF(
                        lineLeft + (buttonEdgeLength + horizontalGap),
                        lineTop + (buttonEdgeLength + verticalGap),
                        lineLeft + (buttonEdgeLength + horizontalGap) + buttonEdgeLength,
                        lineTop + (buttonEdgeLength + verticalGap) + buttonEdgeLength
                ), context.getString(R.string.colorful_puzzle_description), ContextCompat.getColor(context, R.color.colorfulPuzzlePink1), ContextCompat.getColor(context, R.color.colorfulPuzzlePink2), ContextCompat.getColor(context, R.color.colorfulPuzzlePink3), new Bitmap[]{
                BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_red_512),
                BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_blue_512),
                BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_yellow_512),
                BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_green_512)
        }, context, paint));

        Supplier<String> leaderboardDescription = () -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String leaderboardScore = sharedPreferences.getString(context.getString(R.string.leaderboards_score), "#1");
            return leaderboardScore;
        };

        LeaderboardButtonView leaderboardButtonView = new LeaderboardButtonView(
                menuViewListener,
                leaderboardDescription,
                new RectF(
                        lineLeft + (buttonEdgeLength + horizontalGap) * 2,
                        lineTop + (buttonEdgeLength + verticalGap),
                        lineLeft + (buttonEdgeLength + horizontalGap) * 2 + buttonEdgeLength,
                        lineTop + (buttonEdgeLength + verticalGap) + buttonEdgeLength
                ),
                ContextCompat.getColor(context, R.color.settingsBrown1),
                ContextCompat.getColor(context, R.color.settingsBrown2),
                ContextCompat.getColor(context, R.color.settingsBrown3),
                new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.leaderboards_100)},
                context,
                paint
        );

        picButtonViews.add(leaderboardButtonView);

        menuOptionsView = new MenuOptionsView();
        menuOptionsView.init(context, paint);

        this.vipPopup = new VipPopup((Context) menuViewListener, paint, menuViewListener::onPurchaseVipPressed, () -> {
            setShowVipPopup(false);
            render();
        });

        this.initDone = true;
        render();
    }
}
