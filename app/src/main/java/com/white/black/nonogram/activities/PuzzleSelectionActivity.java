package com.white.black.nonogram.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleReference;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.RewardedInstanceHandler;
import com.white.black.nonogram.SubPuzzle;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.utils.VipPromotionUtils;
import com.white.black.nonogram.view.Appearance;
import com.white.black.nonogram.view.PaintManager;
import com.white.black.nonogram.view.PuzzleCategorySelectionView;
import com.white.black.nonogram.view.PuzzleSelectionView;
import com.white.black.nonogram.view.YesNoQuestion;
import com.white.black.nonogram.view.listeners.MenuOptionsViewListener;
import com.white.black.nonogram.view.listeners.PuzzleSelectionViewListener;
import com.white.black.nonogram.view.listeners.Renderable;
import com.white.black.nonogram.view.listeners.VipPromoter;

public class PuzzleSelectionActivity extends Activity implements PuzzleSelectionViewListener, MenuOptionsViewListener, Renderable, VipPromoter {

    private void onRewarded() {
        puzzleCategorySelectionView.refreshPuzzleSelectionButtonView(PuzzleSelectionView.INSTANCE.getPuzzleReference());
        PuzzleSelectionView.INSTANCE.getPuzzleReference().getPuzzle(PuzzleSelectionActivity.this.getApplicationContext()).setPuzzleClass(Puzzle.PuzzleClass.FREE);
        PuzzleSelectionView.INSTANCE.getPuzzleReference().writeToSharedPreferences(PuzzleSelectionActivity.this.getApplicationContext());
    }

    private void onRewardedVideoAdClosed() {
        puzzleCategorySelectionView.setShowPopupFalse();
        puzzleCategorySelectionView.initPopup(PuzzleSelectionActivity.this);
        rewardedInstanceHandler.setRewardedVideoAd(null);
        puzzleCategorySelectionView.render();
    }

    private final OnUserEarnedRewardListener userEarnedRewardListener = rewardItem -> onRewarded();

    private final FullScreenContentCallback fullScreenContentCallback =
            new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Code to be invoked when the ad dismissed full screen content.
                    onRewardedVideoAdClosed();
                }
            };

    private final RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            rewardedInstanceHandler.setRewardedVideoAd(rewardedAd);
            rewardedInstanceHandler.setLoadingRewardedAd(false);
            rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);

            if (puzzleCategorySelectionView.isShowPopup()) {
                AdManager.showRewardedVideo(rewardedInstanceHandler, PuzzleSelectionActivity.this, userEarnedRewardListener);
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            rewardedInstanceHandler.setLoadingRewardedAd(false);
            onRewarded();
            onRewardedVideoAdClosed();

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.LOAD_VIDEO_AD_ERROR_CODE, loadAdError.toString());
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }

            super.onAdFailedToLoad(loadAdError);
        }
    };

    private FirebaseAnalytics mFirebaseAnalytics;
    private PuzzleCategorySelectionView puzzleCategorySelectionView;

    private RewardedInstanceHandler rewardedInstanceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Puzzles.isFirstLoadingDone()) {
            Intent intent = new Intent(PuzzleSelectionActivity.this, MenuActivity.class);
            PuzzleSelectionActivity.this.startActivity(intent);
            PuzzleSelectionActivity.this.finish();
            return;
        }

        puzzleCategorySelectionView = new PuzzleCategorySelectionView(this);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(puzzleCategorySelectionView);
        this.overridePendingTransition(R.anim.enter, 0);

        new Thread(() -> {
            puzzleCategorySelectionView.init(PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint());
            try {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(PuzzleSelectionActivity.this);
                rewardedInstanceHandler = new RewardedInstanceHandler();
            } catch (Exception ignored) {

            }
        }).start();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onViewTouched(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TouchMonitor.INSTANCE.setTouchDown(true, new Point((int) (event.getX()), (int) (event.getY())));
                TouchMonitor.INSTANCE.setTouchUp(false);
                PaintManager.INSTANCE.setReadyToRender();
            case MotionEvent.ACTION_MOVE:
                TouchMonitor.INSTANCE.setMove(new Point((int) (event.getX()), (int) (event.getY())));
                break;
            case MotionEvent.ACTION_UP:
                TouchMonitor.INSTANCE.setTouchDown(false);
                TouchMonitor.INSTANCE.setTouchUp(new Point((int) (event.getX()), (int) (event.getY())));
                TouchMonitor.INSTANCE.setCoordinatesGap();
                PaintManager.INSTANCE.setReadyToRender();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            onOptionsButtonPressed();
            puzzleCategorySelectionView.render();
        } else if (PuzzleSelectionView.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
            } else {
                PuzzleSelectionView.INSTANCE.setAppearance(Appearance.MINIMIZED);
            }

            MyMediaPlayer.play("blop");
            puzzleCategorySelectionView.render();
        } else if (puzzleCategorySelectionView.isShowPopup()) {
            if (puzzleCategorySelectionView.isShowVipPopup()) {
                MyMediaPlayer.play("blop");
                puzzleCategorySelectionView.setShowVipPopup(false);
                puzzleCategorySelectionView.render();
            } else {
                puzzleCategorySelectionView.getPopup().doOnNoAnswer();
                puzzleCategorySelectionView.getPopup().setAnswered(false);
            }
        } else {
            GameState.setGameState(GameState.MENU);
            PuzzleSelectionActivity.this.finish();
            this.overridePendingTransition(0, R.anim.leave);
            MyMediaPlayer.play("page_selection");

            puzzleCategorySelectionView.clearBackground();
        }
    }

    @Override
    public void onReturnButtonPressed() {
        onBackPressed();
    }

    @Override
    public void onSwitchCategoryPressed() {
        MyMediaPlayer.play("blop");
        Puzzles.nextPuzzle();
        puzzleCategorySelectionView.backToTop();
        puzzleCategorySelectionView.updateScrolledButtons();
        puzzleCategorySelectionView.render();

        if (MemoryManager.isCriticalMemory()) {
            puzzleCategorySelectionView.clearBitmapByCategory(Puzzles.getCurrent());
            Puzzles.releasePuzzlesOfOtherCategories();
        }

        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.NEXT_CATEGORY);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        }
    }

    @Override
    public void onPuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.PUZZLE_SELECTION)) {
            Puzzles.writeToSharedPreferencesLastPuzzle(PuzzleSelectionActivity.this.getApplicationContext(), PuzzleSelectionView.INSTANCE.getOverallPuzzle().getUniqueId());
            MyMediaPlayer.play("page_selection");
            PuzzleSelectionView.INSTANCE.getSelectedPuzzle().setLastTimeSolvingTimeIncreased(System.currentTimeMillis());
            GameState.setGameState(GameState.GAME);

            Intent intent = new Intent(PuzzleSelectionActivity.this, GameActivity.class);
            PuzzleSelectionActivity.this.startActivityForResult(intent, 0);

            InterstitialAd interstitialAd = ((MyApplication) getApplicationContext()).getInterstitialAd();
            if (AdManager.isTimeForInterstitial() && !AdManager.isRemoveAds() && interstitialAd != null && Puzzles.numOfSolvedPuzzles(PuzzleSelectionActivity.this) >= 2) {
                AdManager._muteSound(getApplicationContext());
                AdManager.showInterstitial(interstitialAd, PuzzleSelectionActivity.this);
            }

            puzzleCategorySelectionView.clearBackground();
            puzzleCategorySelectionView.clearAllBitmaps();

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.PUZZLE_CATEGORY, Puzzles.getCurrent().name());
            bundle.putString(GameMonitoring.ENTER_PUZZLE, PuzzleSelectionView.INSTANCE.getOverallPuzzle().getName());
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }
        }
    }

    @Override
    public void onStartOverButtonPressed() {
        YesNoQuestion.INSTANCE.init(PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint(), PuzzleSelectionActivity.this.getString(R.string.restart_message), () -> {
            PuzzleSelectionView.INSTANCE.getOverallPuzzle().clear();
            puzzleCategorySelectionView.refreshPuzzleSelectionButtonView(PuzzleSelectionView.INSTANCE.getPuzzleReference());
            PuzzleSelectionView.INSTANCE.init(PuzzleSelectionActivity.this, PuzzleSelectionView.INSTANCE.getPuzzleReference(), PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint());
            PuzzleSelectionView.INSTANCE.getPuzzleReference().writeToSharedPreferences(PuzzleSelectionActivity.this.getApplicationContext());
            YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.PUZZLE_CATEGORY, Puzzles.getCurrent().name());
            bundle.putString(GameMonitoring.START_OVER, PuzzleSelectionView.INSTANCE.getOverallPuzzle().getName());
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }

            puzzleCategorySelectionView.render();
        }, () -> {
            YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
            puzzleCategorySelectionView.render();
        });
    }

    @Override
    public void onOptionsButtonPressed() {
        GameSettings.INSTANCE.onGameSettingsButtonPressed();
        MyMediaPlayer.play("blop");
    }

    @Override
    public void onJoystickButtonPressed() {
        GameSettings.INSTANCE.onJoystickButtonPressed();
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.JOYSTICK);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        }

    }

    @Override
    public void onTouchButtonPressed() {
        GameSettings.INSTANCE.onTouchButtonPressed();
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.TOUCH);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        }

    }

    @Override
    public void onSoundButtonPressed() {
        GameSettings.INSTANCE.onSoundButtonPressed(PuzzleSelectionActivity.this.getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.SOUND);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        }

    }

    @Override
    public void onLaunchMarketButtonPressed() {
        if ((GameState.getGameState().equals(GameState.PUZZLE_SELECTION))) {
            GameSettings.INSTANCE.onReviewButtonPressed(PuzzleSelectionActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.VOTE);
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }

        }
    }

    private String getFacebookPageURL(Context context) {
        String FACEBOOK_URL = "https://www.facebook.com/nonogramisrael";
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
        if ((GameState.getGameState().equals(GameState.PUZZLE_SELECTION))) {
            GameState.setGameState(GameState.FACEBOOK);
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(PuzzleSelectionActivity.this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.FACEBOOK);
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }

            startActivity(facebookIntent);
        }
    }

    @Override
    public void onIcons8ButtonPressed() {
        if ((GameState.getGameState().equals(GameState.PUZZLE_SELECTION))) {
            GameState.setGameState(GameState.ICONS8);
            GameSettings.INSTANCE.onIcons8ButtonPressed(PuzzleSelectionActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.ICONS8);
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
            }

        }
    }

    @Override
    public void onInstructionsButtonPressed() {
        if ((GameState.getGameState().equals(GameState.PUZZLE_SELECTION))) {
            GameState.setGameState(GameState.WIKIPEDIA);
            GameSettings.INSTANCE.onInstructionsButtonPressed(PuzzleSelectionActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.INSTRUCTIONS);
            if (mFirebaseAnalytics != null) {
                mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
            }

        }
    }

    @Override
    public void onDestroy() {
        if (puzzleCategorySelectionView != null) {
            puzzleCategorySelectionView.clearPool();
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (GameState.getGameState().equals(GameState.CONTINUE_PUZZLE)) {
            PuzzleSelectionView.INSTANCE.setPuzzleReference(Puzzles.getLastPuzzle());
            PuzzleSelectionView.INSTANCE.setAppearance(Appearance.MAXIMIZED);
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            PuzzleSelectionView.INSTANCE.init(PuzzleSelectionActivity.this, Puzzles.getLastPuzzle(), PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint());
            onPuzzleButtonPressed();
            return;
        }

        if (!(GameState.getGameState().equals(GameState.NEXT_PUZZLE))) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
        }

        if (PuzzleSelectionView.INSTANCE.getPuzzleReference() != null) {
            puzzleCategorySelectionView.refreshPuzzleSelectionButtonView(PuzzleSelectionView.INSTANCE.getPuzzleReference());
            PuzzleSelectionView.INSTANCE.init(PuzzleSelectionActivity.this, PuzzleSelectionView.INSTANCE.getPuzzleReference(), PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint());
        }

        puzzleCategorySelectionView.render();

        moveToNextPuzzle();
    }

    private void moveToNextPuzzle() {
        if (GameState.getGameState().equals(GameState.NEXT_PUZZLE)) {
            boolean foundUnsolvedSubPuzzle = false;
            if (PuzzleSelectionView.INSTANCE.getOverallPuzzle().getPuzzleClass() == null || PuzzleSelectionView.INSTANCE.getOverallPuzzle().getPuzzleClass().equals(Puzzle.PuzzleClass.FREE) || AdManager.isRemoveAds()) {
                for (SubPuzzle subPuzzle : PuzzleSelectionView.INSTANCE.getOverallPuzzle().getSubPuzzles()) {
                    if (!subPuzzle.getPuzzle().isDone()) {
                        foundUnsolvedSubPuzzle = true;
                        break;
                    }
                }
            }

            if (!foundUnsolvedSubPuzzle) {
                PuzzleReference firstPuzzleChecked = PuzzleSelectionView.INSTANCE.getPuzzleReference();
                PuzzleReference currentPuzzleChecked = firstPuzzleChecked.getNextPuzzleNode();
                while (firstPuzzleChecked != currentPuzzleChecked) {
                    if (currentPuzzleChecked.getPuzzle(PuzzleSelectionActivity.this.getApplicationContext()).getPuzzleClass() == null || currentPuzzleChecked.getPuzzle(PuzzleSelectionActivity.this.getApplicationContext()).getPuzzleClass().equals(Puzzle.PuzzleClass.FREE) || AdManager.isRemoveAds()) {
                        if (!currentPuzzleChecked.getPuzzle(PuzzleSelectionActivity.this.getApplicationContext()).isDone()) {
                            PuzzleSelectionView.INSTANCE.init(PuzzleSelectionActivity.this, currentPuzzleChecked, PuzzleSelectionActivity.this, PaintManager.INSTANCE.createPaint());
                            Puzzles.moveToCategoryByPuzzle(PuzzleSelectionView.INSTANCE.getPuzzleReference());
                            foundUnsolvedSubPuzzle = true;
                            break;
                        }
                    }

                    currentPuzzleChecked = currentPuzzleChecked.getNextPuzzleNode();
                }
            }

            GameState.setGameState(GameState.PUZZLE_SELECTION);

            if (foundUnsolvedSubPuzzle) {
                onPuzzleButtonPressed();
            }
        }
    }

    @Override
    public void onRender() {
        puzzleCategorySelectionView.render();
    }

    @Override
    public void promote() {
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.VIP, GameMonitoring.VIP_PROMOTION_APPEARED);
        mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        MyMediaPlayer.play("blop");
        puzzleCategorySelectionView.getVipPopup().update();
        if (AdManager.isRemoveAds()) {
            puzzleCategorySelectionView.setShowVipPopup(true);
            puzzleCategorySelectionView.render();
        } else {
            puzzleCategorySelectionView.getVipPopup().setPrice("Loading..");
            puzzleCategorySelectionView.setShowVipPopup(true);
            puzzleCategorySelectionView.render();
            onPromoteVipPressed(PuzzleSelectionActivity.this);
        }
    }

    private void onBillingSetupFailed(Context context) {
        puzzleCategorySelectionView.setShowVipPopup(false);
        puzzleCategorySelectionView.render();
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(
                () -> new AlertDialog.Builder(context).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show()
        );
    }

    @Override
    public void onPromoteVipPressed(Context context) {
        VipPromotionUtils.INSTANCE.onPromoteVipPressed(
                context,
                () -> onBillingSetupFailed(PuzzleSelectionActivity.this),
                this::onRemoveAdsPurchaseFound,
                () -> {
                    puzzleCategorySelectionView.setShowVipPopup(false);
                    puzzleCategorySelectionView.setShowPopupFalse();
                    puzzleCategorySelectionView.clearAllBitmaps();
                    puzzleCategorySelectionView.render();
                },
                this::itemAlreadyOwned,
                () -> puzzleCategorySelectionView.getVipPopup().getPopup().setAnswered(AdManager.isRemoveAds()),
                mFirebaseAnalytics
        );
    }

    private void onRemoveAdsPurchaseFound(String price) {
        puzzleCategorySelectionView.getVipPopup().setPrice(price);
        puzzleCategorySelectionView.setShowVipPopup(true);
        puzzleCategorySelectionView.render();
    }

    private void itemAlreadyOwned() {
        AdManager.setRemoveAdsTrue(PuzzleSelectionActivity.this.getApplicationContext());
        puzzleCategorySelectionView.setShowPopupFalse();
        puzzleCategorySelectionView.clearAllBitmaps();
        puzzleCategorySelectionView.render();
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.VIP, GameMonitoring.REMOVE_ADS_ALREADY_OWNED);
        mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
    }
}