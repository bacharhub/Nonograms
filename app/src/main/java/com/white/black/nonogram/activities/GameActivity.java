package com.white.black.nonogram.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;
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
import com.white.black.nonogram.view.YesNoQuestion;
import com.white.black.nonogram.view.Appearance;
import com.white.black.nonogram.view.GameView;
import com.white.black.nonogram.view.PaintManager;
import com.white.black.nonogram.view.PuzzleSelectionView;
import com.white.black.nonogram.view.listeners.GameMonitoringListener;
import com.white.black.nonogram.view.listeners.GameOptionsViewListener;
import com.white.black.nonogram.view.listeners.GameViewListener;
import com.white.black.nonogram.view.listeners.VipPromoter;

import java.util.UUID;

public class GameActivity extends Activity implements GameViewListener, GameOptionsViewListener, GameMonitoringListener, VipPromoter {

    // private AdView adView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GameView gameView;
    private RelativeLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Puzzles.isFirstLoadingDone()) /* null static fields */ {
            Intent intent = new Intent(GameActivity.this, MenuActivity.class);
            GameActivity.this.startActivity(intent);
            GameActivity.this.finish();
            return;
        }

        gameView = new GameView(this);
        // adView = new AdView(GameActivity.this.getApplicationContext());

        ll = new RelativeLayout(this);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.addView(gameView); // The SurfaceView object
        setContentView(ll);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addAds(ll);
                } catch (Exception ignored) {

                }
            }
        }).start();*/

        this.overridePendingTransition(R.anim.enter, 0);

        new Thread(() -> {
            gameView.init(GameActivity.this, PaintManager.INSTANCE.createPaint());
            try {
                // Obtain the FirebaseAnalytics instance.
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(GameActivity.this);
            } catch (Exception ignored) {

            }
        }).start();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /*private void addAds(RelativeLayout ll) {
        AdManager.loadBanner(GameActivity.this, adView, ll);
    }*/

    @Override
    public void onPause() {
        /*if (adView != null) {
            adView.pause();
        }*/

        super.onPause();
    }

    public void reportFaultyPuzzle(String cause, String uniqueId) {
        Bundle bundle = new Bundle();
        bundle.putString(cause, uniqueId);
        mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
    }

    @Override
    public void onViewTouched(MotionEvent event) {
        if (MemoryManager.isNoMemory()) {
            // removeAds();
            Puzzles.releasePuzzlesOfOtherCategories();
        }

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        switch(maskedAction) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN: {
                TouchMonitor.INSTANCE.setTouchDown(pointerId,true, new Point((int)(event.getX(pointerIndex)), (int)(event.getY(pointerIndex))));
                TouchMonitor.INSTANCE.setTouchUp(pointerId,false);
                PaintManager.INSTANCE.setReadyToRender();
            }

            case MotionEvent.ACTION_MOVE: TouchMonitor.INSTANCE.setMove(pointerId, new Point((int)(event.getX(pointerIndex)), (int)(event.getY(pointerIndex)))); break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                TouchMonitor.INSTANCE.setTouchDown(pointerId,false);
                TouchMonitor.INSTANCE.setTouchUp(pointerId, new Point((int)(event.getX(pointerIndex)), (int)(event.getY(pointerIndex))));
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
        if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
                MyMediaPlayer.play("blop");
            } else {
                onOptionsButtonPressed();
            }

            gameView.render();
        } else {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            GameActivity.this.finish();
            this.overridePendingTransition(0, R.anim.leave);
            MyMediaPlayer.play("page_selection");

            gameView.clearBackground();
        }
    }

    @Override
    public void onStartOverButtonPressed() {
        YesNoQuestion.INSTANCE.init(GameActivity.this, PaintManager.INSTANCE.createPaint(), getString(R.string.restart_message), new Runnable() {
            @Override
            public void run() {
                YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
                GameSettings.INSTANCE.onGameSettingsButtonPressed();
                gameView.clear();
                Bundle bundle = new Bundle();
                bundle.putString(GameMonitoring.PUZZLE_CATEGORY, Puzzles.getCurrent().name());
                bundle.putString(GameMonitoring.START_OVER, PuzzleSelectionView.INSTANCE.getOverallPuzzle().getName());
                mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
            }
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
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(30);
        }
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

   /* @Override
    public void removeAds() {
        if (adView != null) {
            ViewGroup viewGroup = ((ViewGroup)adView.getParent());
            if (viewGroup != null) {
                viewGroup.removeView(adView);
            }

            adView.removeAllViews();
            adView.setAdListener(null);
            adView.destroy();
            adView = null;
        }
    }*/

    @Override
    public void onLaunchMarketButtonPressed() {
        if (((GameState.getGameState().equals(GameState.GAME)))) {
            GameState.setGameState(GameState.PLAYSTORE);
            GameSettings.INSTANCE.onReviewButtonPressed(GameActivity.this);
        }
    }

    private void increaseNumOfPuzzlesSolved() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
        int numOfPuzzlesSolved = sharedPreferences.getInt(GameActivity.this.getString(R.string.most_puzzles_solved), /*-1*/ 0);
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
        /*if (adView != null) {
            adView.resume();
        }*/

        GameState.setGameState(GameState.GAME);
        PuzzleSelectionView.INSTANCE.getSelectedPuzzle().setLastTimeSolvingTimeIncreased(System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        // removeAds();
        super.onDestroy();
    }

    @Override
    public void promote() {

    }
}
