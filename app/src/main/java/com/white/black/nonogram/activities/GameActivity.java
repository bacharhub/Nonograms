package com.white.black.nonogram.activities;

import static com.white.black.nonogram.Puzzles.numOfSolvedPuzzles;
import static java.util.concurrent.Executors.newCachedThreadPool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.utils.VipPromotionUtils;
import com.white.black.nonogram.view.Appearance;
import com.white.black.nonogram.view.GameView;
import com.white.black.nonogram.view.PaintManager;
import com.white.black.nonogram.view.PuzzleSelectionView;
import com.white.black.nonogram.view.YesNoQuestion;
import com.white.black.nonogram.view.listeners.GameMonitoringListener;
import com.white.black.nonogram.view.listeners.GameOptionsViewListener;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.VipPromoter;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends Activity implements GameViewListener, GameOptionsViewListener, GameMonitoringListener, VipPromoter {

    private FirebaseAnalytics mFirebaseAnalytics;
    private GameView gameView;
    private RelativeLayout ll;
    private ExecutorService pool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Puzzles.isFirstLoadingDone()) {
            Intent intent = new Intent(GameActivity.this, MenuActivity.class);
            GameActivity.this.startActivity(intent);
            GameActivity.this.finish();
            return;
        }

        gameView = new GameView(this);

        ll = new RelativeLayout(this);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.addView(gameView);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(ll);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.overridePendingTransition(R.anim.enter, 0);

        new Thread(() -> {
            gameView.init(GameActivity.this, PaintManager.INSTANCE.createPaint());
            try {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(GameActivity.this);
            } catch (Exception ignored) {

            }
        }).start();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void reportFaultyPuzzle(String cause, String uniqueId) {
        Bundle bundle = new Bundle();
        bundle.putString(cause, uniqueId);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public int numOfAvailableClues() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
        return sharedPreferences.getInt("clue_count", 3);
    }

    @Override
    public void useClue() {
        int numOfAvailableClues = numOfAvailableClues();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt("clue_count", numOfAvailableClues - 1);
        prefsEditor.apply();
    }

    @Override
    public void onViewTouched(MotionEvent event) {
        if (MemoryManager.isNoMemory()) {
            Puzzles.releasePuzzlesOfOtherCategories();
        }

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN: {
                TouchMonitor.INSTANCE.setTouchDown(pointerId, true, new Point((int) (event.getX(pointerIndex)), (int) (event.getY(pointerIndex))));
                TouchMonitor.INSTANCE.setTouchUp(pointerId, false);
                PaintManager.INSTANCE.setReadyToRender();
            }

            case MotionEvent.ACTION_MOVE:
                TouchMonitor.INSTANCE.setMove(pointerId, new Point((int) (event.getX(pointerIndex)), (int) (event.getY(pointerIndex))));
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                TouchMonitor.INSTANCE.setTouchDown(pointerId, false);
                TouchMonitor.INSTANCE.setTouchUp(pointerId, new Point((int) (event.getX(pointerIndex)), (int) (event.getY(pointerIndex))));
                TouchMonitor.INSTANCE.setCoordinatesGap(pointerId);
                PaintManager.INSTANCE.setReadyToRender();

                if (!MemoryManager.isNoMemory()) {
                    PuzzleSelectionView.INSTANCE.getPuzzleReference().writeToSharedPreferences(GameActivity.this.getApplicationContext());
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (Puzzles.hasPlayerSolvedAtLeastOnePuzzle(GameActivity.this)) {
            if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                    YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
                    MyMediaPlayer.play("blop");
                } else {
                    onOptionsButtonPressed();
                }
            } else if (gameView.isShowPopup()) {
                if (gameView.isShowVipPopup()) {
                    MyMediaPlayer.play("blop");
                    gameView.setShowVipPopup(false);
                } else {
                    gameView.getPopup().doOnNoAnswer();
                    gameView.getPopup().setAnswered(false);
                }
            } else {
                GameState.setGameState(GameState.PUZZLE_SELECTION);
                GameActivity.this.finish();
                this.overridePendingTransition(0, R.anim.leave);
                MyMediaPlayer.play("page_selection");

                gameView.clearBackground();
            }
        } else {
            Intent start = new Intent(Intent.ACTION_MAIN);
            start.addCategory(Intent.CATEGORY_HOME);
            start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(start);
        }
    }

    @Override
    public void onStartOverButtonPressed() {
        YesNoQuestion.INSTANCE.init(GameActivity.this, PaintManager.INSTANCE.createPaint(), getString(R.string.restart_message), () -> {
            YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
            GameSettings.INSTANCE.onGameSettingsButtonPressed();
            gameView.clear();
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.PUZZLE_CATEGORY, Puzzles.getCurrent().name());
            bundle.putString(GameMonitoring.START_OVER, PuzzleSelectionView.INSTANCE.getOverallPuzzle().getName());
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        }, () -> YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED));
    }

    @Override
    public void onOptionsButtonPressed() {
        MyMediaPlayer.play("blop");
        GameSettings.INSTANCE.onGameSettingsButtonPressed();
        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().setLastTimeSolvingTimeIncreased(System.currentTimeMillis());
    }

    @Override
    public void onJoystickButtonPressed() {
        GameSettings.INSTANCE.onJoystickButtonPressed();
        gameView.initJoystickToolbar(GameActivity.this, PaintManager.INSTANCE.createPaint());
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.JOYSTICK);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public void onTouchButtonPressed() {
        GameSettings.INSTANCE.onTouchButtonPressed();
        gameView.initTouchToolbar(GameActivity.this, PaintManager.INSTANCE.createPaint());
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.TOUCH);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public void onSoundButtonPressed() {
        GameSettings.INSTANCE.onSoundButtonPressed(GameActivity.this.getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.SOUND);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public void onInstructionsButtonPressed() {
        if ((GameState.getGameState().equals(GameState.GAME))) {
            GameState.setGameState(GameState.WIKIPEDIA);
            GameSettings.INSTANCE.onInstructionsButtonPressed(GameActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.INSTRUCTIONS);
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        }
    }

    @Override
    public void onIcons8ButtonPressed() {
        if ((GameState.getGameState().equals(GameState.GAME))) {
            GameState.setGameState(GameState.ICONS8);
            GameSettings.INSTANCE.onIcons8ButtonPressed(GameActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.ICONS8);
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        }
    }

    @Override
    public void onReturnButtonPressed() {
        onBackPressed();
    }

    @Override
    public void onNextPuzzleButtonPressed() {
        if (PuzzleSelectionView.INSTANCE.getPuzzleReference().getNextPuzzleNode() != null) {
            onBackPressed();
            GameState.setGameState(GameState.NEXT_PUZZLE);
            onToolbarButtonPressed(GameMonitoring.NEXT_PUZZLE);
        }
    }

    @Override
    public void onZoomedSlotSelected() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    public void onFinishPuzzle() {
        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().clearUndoRedo();
        PuzzleSelectionView.INSTANCE.getPuzzleReference().writeToSharedPreferences(GameActivity.this.getApplicationContext());

        Puzzles.writeToSharedPreferencesLastPuzzle(GameActivity.this.getApplicationContext(), null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor.putString("userId", userId);
            prefsEditor.apply();
        }

        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.PUZZLE_SOLVER_ID, userId);
        bundle.putString(GameMonitoring.SOLVED_PUZZLE_CATEGORY, Puzzles.getCurrent().name());
        bundle.putString(GameMonitoring.FINISH_PUZZLE, PuzzleSelectionView.INSTANCE.getOverallPuzzle().getName());
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);

        increaseNumOfPuzzlesSolved();
        MyMediaPlayer.play("victory");
    }

    @Override
    public void onLaunchMarketButtonPressed() {
        if (((GameState.getGameState().equals(GameState.GAME)))) {
            GameSettings.INSTANCE.onReviewButtonPressed(GameActivity.this);
        }
    }

    private void increaseNumOfPuzzlesSolved() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
        int numOfPuzzlesSolved = numOfSolvedPuzzles(GameActivity.this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(getString(R.string.most_puzzles_solved), numOfPuzzlesSolved + 1);
        prefsEditor.apply();
    }

    @Override
    public void onToolbarButtonPressed(String toolbarButtonName) {
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_TOOLBAR, toolbarButtonName);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        pool = newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });

        pool.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long start = System.currentTimeMillis();
                    gameView.render();
                    long end = System.currentTimeMillis();
                    long duration = end - start;
                    long timeToSleep = Math.max(5, 16 - duration);
                    if (timeToSleep > 0) {
                        Thread.sleep(timeToSleep);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        });

        GameState.setGameState(GameState.GAME);
        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().setLastTimeSolvingTimeIncreased(System.currentTimeMillis());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    @Override
    public void promote() {
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.VIP, GameMonitoring.VIP_PROMOTION_APPEARED);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        MyMediaPlayer.play("blop");
        gameView.getVipPopup().update();
        if (AdManager.isRemoveAds()) {
            gameView.setShowVipPopup(true);
        } else {
            gameView.getVipPopup().setPrice(getString(R.string.loading));
            gameView.setShowVipPopup(true);
            onPromoteVipPressed(GameActivity.this);
        }
    }

    @Override
    public void onPromoteVipPressed(Context context) {
        VipPromotionUtils.INSTANCE.onPromoteVipPressed(
                context,
                () -> onBillingSetupFailed(GameActivity.this),
                this::onRemoveAdsPurchaseFound,
                () -> {
                    gameView.setShowVipPopup(false);
                    gameView.setShowPopupFalse();
                },
                this::itemAlreadyOwned,
                () -> gameView.getVipPopup().getPopup().setAnswered(AdManager.isRemoveAds()),
                mFirebaseAnalytics
        );
    }

    private void onBillingSetupFailed(Context context) {
        gameView.setShowVipPopup(false);
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(
                () -> new AlertDialog.Builder(context).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show()
        );
    }

    private void onRemoveAdsPurchaseFound(String price) {
        gameView.getVipPopup().setPrice(price);
        gameView.setShowVipPopup(true);
    }

    private void itemAlreadyOwned() {
        AdManager.setRemoveAdsTrue(GameActivity.this.getApplicationContext());
        gameView.setShowPopupFalse();
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.VIP, GameMonitoring.REMOVE_ADS_ALREADY_OWNED);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }
}