package com.white.black.nonogram.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.LotteryGoToFacebookButtonView;
import com.white.black.nonogram.view.buttons.ReturnButtonView;
import com.white.black.nonogram.view.listeners.LotteryViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.LinkedList;
import java.util.List;

public class LotteryView extends SurfaceView implements SurfaceHolder.Callback{

    private volatile boolean initDone;

    private String lotteryKey;

    private final LotteryViewListener lotteryViewListener;
    private List<Bitmap> gifts;
    private Bitmap key;
    private Bitmap headphones;
    private RectF leftHeadphonesBounds;
    private RectF rightHeadphonesBounds;
    private RectF keyBounds;

    private float titleFontSize;
    private String title;
    private int titleHeight;
    private RectF titleBounds;

    private float sloganFontSize;
    private float sloganHeight;
    private String earphonesSlogan;
    private String moreDetailsOnFacebook;
    private float earphonesSloganTop;
    private float moreDetailsOnFacebookTop;

    private LotteryGoToFacebookButtonView goToFacebookButtonView;

    private int backgroundColor;
    private ReturnButtonView returnButtonView;

    private RectF[] numberBoundsArr;
    private ColorPack[] numberColors;

    private Bitmap background;

    public LotteryView(Context context) {
        super(context);
        lotteryViewListener = (LotteryViewListener)context;
        getHolder().addCallback(this);
        setFocusable(true);
    }

    private void render() {
        if (initDone && GameState.getGameState().equals(GameState.LOTTERY)) {
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
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void drawBackground(Canvas canvas, Paint paint) {
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
            paint.setAlpha(30);
            int foodItemDrawnCounter = 0;
            for(int x = 0; x < ApplicationSettings.INSTANCE.getScreenWidth(); x += 150) {
                for (int y = 0; y < ApplicationSettings.INSTANCE.getScreenHeight(); y += 150) {
                    tempCanvas.drawBitmap(gifts.get((foodItemDrawnCounter++) % gifts.size()), x, y, paint);
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

        returnButtonView.draw(canvas, paint);

        canvas.drawBitmap(key, null, keyBounds, paint);
        canvas.drawBitmap(headphones, null, leftHeadphonesBounds, paint);
        canvas.drawBitmap(headphones, null, rightHeadphonesBounds, paint);

        drawColorfulRectangle(canvas, paint, Puzzles.LARGE.getColorPack(), titleBounds);
        paint.setTextSize(titleFontSize);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, titleBounds.centerX(), titleBounds.centerY() + titleHeight / 2, paint);

        paint.setTextSize(sloganFontSize);
        paint.setColor(Color.BLACK);
        canvas.drawText(earphonesSlogan, ApplicationSettings.INSTANCE.getScreenWidth() / 2, earphonesSloganTop + sloganHeight / 2, paint);
        canvas.drawText(moreDetailsOnFacebook, ApplicationSettings.INSTANCE.getScreenWidth() / 2, moreDetailsOnFacebookTop + sloganHeight / 2, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        drawKeyNumbers(canvas, paint);

        goToFacebookButtonView.draw(canvas, paint);
    }

    private void drawColorfulRectangle(Canvas canvas, Paint paint, ColorPack colorPack, RectF bounds) {
        paint.setColor(colorPack.getColor3());
        float dis = (bounds.height() * 5 / 100);

        RectF topLeftBounds = new RectF(
                bounds.centerX() - bounds.width() * 50 / 100,
                bounds.centerY() - bounds.height() * 50 / 100,
                bounds.centerX() + bounds.width() * 45 / 100,
                bounds.centerY() + bounds.height() * 45 / 100
        );

        Path path = new Path();
        path.moveTo(bounds.left, bounds.top);
        path.lineTo(topLeftBounds.left, topLeftBounds.bottom);
        path.lineTo(topLeftBounds.left + dis, bounds.bottom);
        path.lineTo(bounds.right, bounds.bottom);
        path.lineTo(bounds.right, bounds.top + dis);
        path.lineTo(bounds.right - dis, bounds.top);
        path.lineTo(bounds.left, bounds.top);

        canvas.drawPath(path, paint);

        int gradient1 = colorPack.getColor1();
        int gradient2 = colorPack.getColor2();

        paint.setShader(new LinearGradient(
                topLeftBounds.left,
                topLeftBounds.top,
                topLeftBounds.right,
                topLeftBounds.bottom,
                new int[]{gradient1, gradient2},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR));
        canvas.drawRect(topLeftBounds, paint);
        paint.setShader(null);
    }

    private void drawKeyNumber(Canvas canvas, Paint paint, ColorPack colorPack, RectF bounds, String num) {
        drawColorfulRectangle(canvas, paint, colorPack, bounds);
        Rect numberBounds = new Rect();
        paint.getTextBounds(num, 0, num.length(), numberBounds);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawText(num, bounds.centerX(), bounds.centerY() + numberBounds.height() / 2, paint);
    }

    private void drawKeyNumbers(Canvas canvas, Paint paint) {
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenWidth() / 13);
        paint.setTextAlign(Paint.Align.CENTER);
        for(int i = 0; i < numberColors.length; i++) {
            drawKeyNumber(canvas, paint, numberColors[i], numberBoundsArr[i], String.valueOf(lotteryKey.toCharArray()[i]));
        }

        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        render();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        if (this.initDone) {
            lotteryViewListener.onViewTouched(event);
            if (returnButtonView.wasPressed()) {
                returnButtonView.onButtonPressed();
                TouchMonitor.INSTANCE.setTouchUp(false);
            } else if (goToFacebookButtonView.wasPressed()) {
                goToFacebookButtonView.onButtonPressed();
                TouchMonitor.INSTANCE.setTouchUp(false);
            }

            if (PaintManager.INSTANCE.isReadyToRender(System.currentTimeMillis())) {
                render();
                PaintManager.INSTANCE.setLastRenderingTime(System.currentTimeMillis());
            }
        }

        return true;
    }

    private void getLotteryKeyNum(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.lotteryKey = sharedPreferences.getString(context.getString(R.string.earphones_lottery), null);
        if (this.lotteryKey == null) {
            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            this.lotteryKey = String.valueOf((System.currentTimeMillis() % 4525) * 221);
            while(this.lotteryKey.length() < 6) {
                this.lotteryKey = "0" + this.lotteryKey;
            }
            prefsEditor.putString(context.getString(R.string.earphones_lottery), lotteryKey);
            prefsEditor.apply();
        }
    }

    public void init(Context context, Paint paint) {
        this.initDone = false;
        getLotteryKeyNum(context);
        backgroundColor = ContextCompat.getColor(context, R.color.lotteryBackground);

        numberColors = new ColorPack[6];
        numberColors[0] = Puzzles.SMALL.getColorPack();
        numberColors[1] = Puzzles.NORMAL.getColorPack();
        numberColors[2] = Puzzles.LARGE.getColorPack();
        numberColors[3] = Puzzles.COMPLEX.getColorPack();
        numberColors[4] = Puzzles.COLORFUL.getColorPack();
        numberColors[5] = new ColorPack(ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3));

        numberBoundsArr = new RectF[6];

        int gapBetweenNumbers = ApplicationSettings.INSTANCE.getScreenWidth() * 23 / 1000;
        int left = gapBetweenNumbers;
        int numberWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 14 / 100;
        int bottom = ApplicationSettings.INSTANCE.getScreenHeight() - numberWidth;

        for (int i = 0; i < 6; i++) {
            numberBoundsArr[i] = new RectF(
                    left + i * (numberWidth + gapBetweenNumbers),
                    bottom - numberWidth,
                    left + i * (numberWidth + gapBetweenNumbers) + numberWidth,
                    bottom
            );
        }

        gifts = new LinkedList<>();
        gifts.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.sock_100));
        gifts.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.heart_balloon_100));
        gifts.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.gift_card_100));
        gifts.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.christmas_ball_100));
        gifts.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.gift_100));

        key = BitmapLoader.INSTANCE.getImage(context, R.drawable.key_512);
        int keyLength = ApplicationSettings.INSTANCE.getScreenWidth() / 2;
        keyBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() - keyLength,
                ApplicationSettings.INSTANCE.getScreenHeight() - keyLength,
                ApplicationSettings.INSTANCE.getScreenWidth(),
                ApplicationSettings.INSTANCE.getScreenHeight()
        );

        headphones = BitmapLoader.INSTANCE.getImage(context, R.drawable.headphones_100);
        int headphonesLength = ApplicationSettings.INSTANCE.getScreenWidth() / 6;
        int distanceFromEdge = ApplicationSettings.INSTANCE.getScreenWidth() / 10;
        leftHeadphonesBounds = new RectF(
                distanceFromEdge,
                ApplicationSettings.INSTANCE.getScreenHeight() * 16 / 100,
                distanceFromEdge + headphonesLength,
                ApplicationSettings.INSTANCE.getScreenHeight() * 16 / 100 + headphonesLength
        );

        rightHeadphonesBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() - (distanceFromEdge + headphonesLength),
                ApplicationSettings.INSTANCE.getScreenHeight() * 16 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() - distanceFromEdge,
                ApplicationSettings.INSTANCE.getScreenHeight() * 16 / 100 + headphonesLength
        );

        float distanceBetweenLeftRightHeadphones = rightHeadphonesBounds.left - leftHeadphonesBounds.right;
        title = context.getString(R.string.lottery);
        String letter = context.getString(R.string.letter);
        titleFontSize = ApplicationSettings.INSTANCE.getScreenWidth() / 15;
        paint.setTextSize(titleFontSize);
        Rect titleTextBounds = new Rect();
        paint.getTextBounds(letter, 0, letter.length(), titleTextBounds);
        titleHeight = titleTextBounds.height();
        titleBounds = new RectF(
                leftHeadphonesBounds.right + distanceBetweenLeftRightHeadphones / 10,
                leftHeadphonesBounds.centerY() - headphonesLength * 3 / 10,
                rightHeadphonesBounds.left - distanceBetweenLeftRightHeadphones / 10,
                leftHeadphonesBounds.centerY() + headphonesLength * 3 / 10
        );

        int distanceFromTitle = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        sloganFontSize = ApplicationSettings.INSTANCE.getScreenWidth() / 23;
        earphonesSlogan = context.getString(R.string.bluetooth_earphones_question);
        moreDetailsOnFacebook = context.getString(R.string.more_details_on_facebook);
        Rect sloganTextBounds = new Rect();
        paint.setTextSize(sloganFontSize);
        paint.getTextBounds(earphonesSlogan, 0, earphonesSlogan.length(), sloganTextBounds);
        sloganHeight = sloganTextBounds.height();

        earphonesSloganTop = titleBounds.bottom + distanceFromTitle;
        moreDetailsOnFacebookTop = earphonesSloganTop + distanceFromTitle;

        RectF goToFacebookButtonBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() * 25 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 5 / 10,
                ApplicationSettings.INSTANCE.getScreenWidth() * 75 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 58 / 100
        );

        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenWidth() / 15);
        goToFacebookButtonView = new LotteryGoToFacebookButtonView(
                (ViewListener)context,
                context.getString(R.string.facebook),
                goToFacebookButtonBounds,
                Puzzles.COMPLEX.getColorPack().getColor1(), Puzzles.COMPLEX.getColorPack().getColor2(), Puzzles.COMPLEX.getColorPack().getColor3(), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.facebook_512)}, context, paint);


        int top = ApplicationSettings.INSTANCE.getScreenHeight() / 22;
        int horizontalDistanceFromEdge = top;
        int buttonHeight = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        RectF returnButtonBounds = new RectF(
                horizontalDistanceFromEdge,
                top,
                horizontalDistanceFromEdge + buttonWidth,
                top + buttonHeight
        );

        returnButtonView = new ReturnButtonView(
                (ViewListener)context,
                context.getString(R.string.return_button),
                returnButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[] {BitmapLoader.INSTANCE.getImage(context, R.drawable.return_100)}, context, paint);

        this.initDone = true;
        render();
    }
}
