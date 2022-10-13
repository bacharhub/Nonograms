package com.white.black.nonogram.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.LotteryView;
import com.white.black.nonogram.view.PaintManager;
import com.white.black.nonogram.view.listeners.LotteryViewListener;

public class LotteryActivity extends Activity implements LotteryViewListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private LotteryView lotteryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Puzzles.isFirstLoadingDone()) {
            Intent intent = new Intent(LotteryActivity.this, MenuActivity.class);
            LotteryActivity.this.startActivity(intent);
            LotteryActivity.this.finish();
            return;
        }

        lotteryView = new LotteryView(this);
        setContentView(lotteryView);
        this.overridePendingTransition(R.anim.enter, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                lotteryView.init(LotteryActivity.this, PaintManager.INSTANCE.createPaint());
                try {
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(LotteryActivity.this);
                } catch (Exception ignored) {

                }
            }
        }).start();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onViewTouched(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: TouchMonitor.INSTANCE.setTouchDown(true, new Point((int)(event.getX()), (int)(event.getY()))); TouchMonitor.INSTANCE.setTouchUp(false); PaintManager.INSTANCE.setReadyToRender();
            case MotionEvent.ACTION_MOVE: TouchMonitor.INSTANCE.setMove(new Point((int)(event.getX()), (int)(event.getY()))); break;
            case MotionEvent.ACTION_UP:
                TouchMonitor.INSTANCE.setTouchDown(false);
                TouchMonitor.INSTANCE.setTouchUp(new Point((int)(event.getX()), (int)(event.getY())));
                TouchMonitor.INSTANCE.setCoordinatesGap();
                PaintManager.INSTANCE.setReadyToRender();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        GameState.setGameState(GameState.MENU);
        LotteryActivity.this.finish();
        this.overridePendingTransition(0, R.anim.leave);
        MyMediaPlayer.play("page_selection");

        lotteryView.clearBackground();
    }

    @Override
    public void onReturnButtonPressed() {
        onBackPressed();
    }

    private String getFacebookPageURL(Context context) {
        String FACEBOOK_URL = "https://www.facebook.com/nonogramisrael/photos/a.1889721191105743/1897015360376326/?type=3&theater";
        String FACEBOOK_PAGE_ID = "1889696247774904";
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            boolean activated = packageManager.getApplicationInfo("com.facebook.katana", 0).enabled;
            if (activated) {
                if (versionCode >= 3002850) { //newer versions of fb app
                    return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
                } else { //older versions of fb app
                    return "fb://page/" + FACEBOOK_PAGE_ID;
                }
            } else {
                return FACEBOOK_URL; //normal web url
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    @Override
    public void onGoToFacebookButtonPressed() {
        if (GameState.getGameState().equals(GameState.LOTTERY)) {
            GameState.setGameState(GameState.FACEBOOK);
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(LotteryActivity.this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.BUTTON_PRESSED, GameMonitoring.FACEBOOK);
            mFirebaseAnalytics.logEvent(GameMonitoring.LOTTERY_EVENT, bundle);
            startActivity(facebookIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GameState.setGameState(GameState.LOTTERY);
    }
}
