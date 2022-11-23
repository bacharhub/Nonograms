package com.white.black.nonogram.view;

import static com.white.black.nonogram.utils.SharedPreferenceUtils.cluesAvailable;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.coinsAvailable;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;

import java.util.function.Supplier;

public class BankView {

    private Supplier<Integer> coinsAvailable;

    private RectF coinsBankWindowBounds;
    private RectF coinsBankBackgroundBounds;
    private RectF coinsWindowInnerBackgroundBounds;
    private LinearGradient coinsBankWindowGradient;
    private RectF coinsBankCoinBounds;
    private Bitmap coin;
    private int coinsColor1;
    private int coinsColor2;
    private int coinsColor3;

    private Supplier<Integer> availableKeysSupplier;

    private RectF keysBankWindowBounds;
    private RectF keysBankBackgroundBounds;
    private RectF keysWindowInnerBackgroundBounds;
    private LinearGradient keysBankWindowGradient;
    private RectF keysBankCoinBounds;
    private Bitmap key;
    private int keysColor1;
    private int keysColor2;
    private int keysColor3;

    private Supplier<Integer> availableCluesSupplier;

    private RectF cluesBankWindowBounds;
    private RectF cluesBankBackgroundBounds;
    private RectF cluesWindowInnerBackgroundBounds;
    private LinearGradient cluesBankWindowGradient;
    private RectF cluesBankCoinBounds;
    private Bitmap clue;
    private int cluesColor1;
    private int cluesColor2;
    private int cluesColor3;

    public void init(Context context) {
        coin = BitmapLoader.INSTANCE.getImage(context, R.drawable.coin_64);
        coinsAvailable = () -> coinsAvailable(context);
        coinsColor1 = ContextCompat.getColor(context, R.color.smallPuzzleGreen1);
        coinsColor2 = ContextCompat.getColor(context, R.color.smallPuzzleGreen2);
        coinsColor3 = ContextCompat.getColor(context, R.color.smallPuzzleGreen3);

        coinsBankWindowBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100 + ApplicationSettings.INSTANCE.getScreenWidth() * 32 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100 + 150
        );

        coinsBankBackgroundBounds =
                new RectF(coinsBankWindowBounds.left, coinsBankWindowBounds.top, coinsBankWindowBounds.right + coinsBankWindowBounds.width() * 2 / 100, coinsBankWindowBounds.bottom + coinsBankWindowBounds.height() * 2 / 100);
        coinsWindowInnerBackgroundBounds =
                new RectF(coinsBankWindowBounds.left + 8, coinsBankWindowBounds.top + 8, coinsBankWindowBounds.right - 8, coinsBankWindowBounds.bottom - 8);

        coinsBankWindowGradient = new LinearGradient(
                coinsBankWindowBounds.left,
                coinsBankWindowBounds.top,
                coinsBankWindowBounds.right,
                coinsBankWindowBounds.bottom,
                new int[]{coinsColor1, coinsColor2},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        coinsBankCoinBounds = new RectF(
                coinsBankWindowBounds.left + 30,
                coinsBankWindowBounds.centerY() - 64,
                coinsBankWindowBounds.left + 30 + 128,
                coinsBankWindowBounds.centerY() + 64
        );

        key = BitmapLoader.INSTANCE.getImage(context, R.drawable.key_512);
        availableKeysSupplier = () -> availableKeys(context);
        keysColor1 = ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1);
        keysColor2 = ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2);
        keysColor3 = ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3);

        keysBankWindowBounds = new RectF(
                coinsBankWindowBounds.right + ApplicationSettings.INSTANCE.getScreenHeight() * 15 / 1000,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                coinsBankWindowBounds.right + ApplicationSettings.INSTANCE.getScreenHeight() * 15 / 1000 + ApplicationSettings.INSTANCE.getScreenWidth() * 27 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100 + 150
        );

        keysBankBackgroundBounds =
                new RectF(keysBankWindowBounds.left, keysBankWindowBounds.top, keysBankWindowBounds.right + keysBankWindowBounds.width() * 2 / 100, keysBankWindowBounds.bottom + keysBankWindowBounds.height() * 2 / 100);
        keysWindowInnerBackgroundBounds =
                new RectF(keysBankWindowBounds.left + 8, keysBankWindowBounds.top + 8, keysBankWindowBounds.right - 8, keysBankWindowBounds.bottom - 8);

        keysBankWindowGradient = new LinearGradient(
                keysBankWindowBounds.left,
                keysBankWindowBounds.top,
                keysBankWindowBounds.right,
                keysBankWindowBounds.bottom,
                new int[]{keysColor1, keysColor2},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        keysBankCoinBounds = new RectF(
                keysBankWindowBounds.left + 30,
                keysBankWindowBounds.centerY() - 64,
                keysBankWindowBounds.left + 30 + 128,
                keysBankWindowBounds.centerY() + 64
        );

        clue = BitmapLoader.INSTANCE.getImage(context, R.drawable.bulb_512);
        availableCluesSupplier = () -> cluesAvailable(context);
        cluesColor1 = ContextCompat.getColor(context, R.color.colorfulPuzzlePink1);
        cluesColor2 = ContextCompat.getColor(context, R.color.colorfulPuzzlePink2);
        cluesColor3 = ContextCompat.getColor(context, R.color.colorfulPuzzlePink3);

        cluesBankWindowBounds = new RectF(
                keysBankWindowBounds.right + ApplicationSettings.INSTANCE.getScreenHeight() * 15 / 1000,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                ApplicationSettings.INSTANCE.getScreenWidth() - ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100,
                ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 100 + 150
        );

        cluesBankBackgroundBounds =
                new RectF(cluesBankWindowBounds.left, cluesBankWindowBounds.top, cluesBankWindowBounds.right + cluesBankWindowBounds.width() * 2 / 100, cluesBankWindowBounds.bottom + cluesBankWindowBounds.height() * 2 / 100);
        cluesWindowInnerBackgroundBounds =
                new RectF(cluesBankWindowBounds.left + 8, cluesBankWindowBounds.top + 8, cluesBankWindowBounds.right - 8, cluesBankWindowBounds.bottom - 8);

        cluesBankWindowGradient = new LinearGradient(
                cluesBankWindowBounds.left,
                cluesBankWindowBounds.top,
                cluesBankWindowBounds.right,
                cluesBankWindowBounds.bottom,
                new int[]{cluesColor1, cluesColor2},
                new float[]{0f, 1f},
                Shader.TileMode.MIRROR);

        cluesBankCoinBounds = new RectF(
                cluesBankWindowBounds.left + 30,
                cluesBankWindowBounds.centerY() - 64,
                cluesBankWindowBounds.left + 30 + 128,
                cluesBankWindowBounds.centerY() + 64
        );
    }

    private int availableKeys(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int keys = sharedPreferences.getInt("keys", 0);
        return keys;
    }

    public void draw(Canvas canvas, Paint paint) {
        drawCoinsBank(canvas, paint);
        drawKeysBank(canvas, paint);
        drawCluesBank(canvas, paint);
    }

    private void drawCoinsBank(Canvas canvas, Paint paint) {
        paint.setColor(coinsColor3);
        canvas.drawRoundRect(coinsBankBackgroundBounds, 30, 30, paint);
        paint.setShader(coinsBankWindowGradient);
        canvas.drawRoundRect(coinsBankWindowBounds, 30, 30, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(coinsWindowInnerBackgroundBounds, 30, 30, paint);
        canvas.drawBitmap(coin, null, coinsBankCoinBounds, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 40);
        Rect numOfCoinsDescriptionBounds = new Rect();
        String numOfCoinsAvailable = String.valueOf(coinsAvailable.get());
        paint.getTextBounds(numOfCoinsAvailable, 0, numOfCoinsAvailable.length(), numOfCoinsDescriptionBounds);
        paint.setColor(Color.BLACK);
        canvas.drawText(numOfCoinsAvailable, coinsBankCoinBounds.right + 15, coinsBankCoinBounds.centerY() + numOfCoinsDescriptionBounds.height() / 2, paint);
    }

    private void drawKeysBank(Canvas canvas, Paint paint) {
        paint.setColor(keysColor3);
        canvas.drawRoundRect(keysBankBackgroundBounds, 30, 30, paint);
        paint.setShader(keysBankWindowGradient);
        canvas.drawRoundRect(keysBankWindowBounds, 30, 30, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(keysWindowInnerBackgroundBounds, 30, 30, paint);
        canvas.drawBitmap(key, null, keysBankCoinBounds, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 40);
        Rect numOfCoinsDescriptionBounds = new Rect();
        String numOfCoinsAvailable = String.valueOf(availableKeysSupplier.get());
        paint.getTextBounds(numOfCoinsAvailable, 0, numOfCoinsAvailable.length(), numOfCoinsDescriptionBounds);
        paint.setColor(Color.BLACK);
        canvas.drawText(numOfCoinsAvailable, keysBankCoinBounds.right + 15, keysBankCoinBounds.centerY() + numOfCoinsDescriptionBounds.height() / 2, paint);
    }

    private void drawCluesBank(Canvas canvas, Paint paint) {
        paint.setColor(cluesColor3);
        canvas.drawRoundRect(cluesBankBackgroundBounds, 30, 30, paint);
        paint.setShader(cluesBankWindowGradient);
        canvas.drawRoundRect(cluesBankWindowBounds, 30, 30, paint);
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(cluesWindowInnerBackgroundBounds, 30, 30, paint);
        canvas.drawBitmap(clue, null, cluesBankCoinBounds, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(ApplicationSettings.INSTANCE.getScreenHeight() / 40);
        Rect numOfCoinsDescriptionBounds = new Rect();
        String numOfCoinsAvailable = String.valueOf(availableCluesSupplier.get());
        paint.getTextBounds(numOfCoinsAvailable, 0, numOfCoinsAvailable.length(), numOfCoinsDescriptionBounds);
        paint.setColor(Color.BLACK);
        canvas.drawText(numOfCoinsAvailable, cluesBankCoinBounds.right + 15, cluesBankCoinBounds.centerY() + numOfCoinsDescriptionBounds.height() / 2, paint);
    }
}
