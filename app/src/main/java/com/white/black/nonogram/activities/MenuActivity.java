package com.white.black.nonogram.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.PuzzleReference;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.services.AlarmBroadcastReceiver;
import com.white.black.nonogram.view.Appearance;
import com.white.black.nonogram.view.MenuView;
import com.white.black.nonogram.view.PaintManager;
import com.white.black.nonogram.view.YesNoQuestion;
import com.white.black.nonogram.view.listeners.MenuOptionsViewListener;
import com.white.black.nonogram.view.listeners.MenuViewListener;
import com.white.black.nonogram.view.listeners.VipPromoter;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends Activity implements MenuViewListener, MenuOptionsViewListener, PurchasesUpdatedListener, VipPromoter {

    private final static int RC_SIGN_IN = 100;

    private FirebaseAnalytics mFirebaseAnalytics;
    private MenuView menuView;
    private SkuDetails removeAdsDetails;
    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuView = new MenuView(this);
        setContentView(menuView);

        new Thread(() -> {
            init();
            try {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(MenuActivity.this);
            } catch (Exception ignored) {

            }
        }).start();

        triggerNotificationAlarm();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                if (signedInAccount != null) {
                    PlayersClient playersClient = Games.getPlayersClient(MenuActivity.this, signedInAccount);
                    Task<Player> player = playersClient.getCurrentPlayer();
                    player.addOnCompleteListener(task -> {
                        updateLeaderboard();
                        showLeaderboard();
                    });
                }
            } else {
                String message = null;
                if (result != null) {
                    message = result.getStatus().getStatusMessage();
                }

                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                if (!isFinishing()) {
                    new AlertDialog.Builder(this).setMessage(message)
                            .setNeutralButton(android.R.string.ok, null).show();
                }

                GameState.setGameState(GameState.MENU);
            }
        }
    }

    private static final int RC_LEADERBOARD_UI = 9004;

    private void showLeaderboard() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            Games.getLeaderboardsClient(this, googleSignInAccount)
                    .getLeaderboardIntent(getString(R.string.most_puzzles_solved))
                    .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GameState.setGameState(GameState.MENU);
        menuView.render();
    }

    private void triggerNotificationAlarm() {
        Intent _intent = new Intent(MenuActivity.this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MenuActivity.this, 0, _intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager)MenuActivity.this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        final long TIME_TO_WAIT_MILLISECONDS =  1000 * 3600 * 6; // 6 hours
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_TO_WAIT_MILLISECONDS, pendingIntent);
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

    private void init() {
        initGameDimensions();
        PaintManager.INSTANCE.init(MenuActivity.this.getApplicationContext());
        menuView.init(MenuActivity.this, PaintManager.INSTANCE.createPaint());
        GameSettings.INSTANCE.initSound(MenuActivity.this.getApplicationContext());

        new Thread(() -> {
            Puzzles.init(MenuActivity.this.getApplicationContext());

            if (Puzzles.hasPlayerSolvedAtLeastOnePuzzle(MenuActivity.this)) {
                continuePuzzleQuestion();
            } else {
                goToTutorial();
            }
        }).start();

        new Thread(() -> MyMediaPlayer.initialize(MenuActivity.this.getApplicationContext())).start();
    }

    private void goToTutorial() {
        PuzzleReference tutorialPuzzle = Puzzles.getTutorialPuzzleReference();
        Puzzles.setTutorialPuzzleAsLastPuzzle();
        GameState.setGameState(GameState.CONTINUE_PUZZLE);
        Puzzles.moveToCategoryByPuzzle(tutorialPuzzle);
        GameSettings.INSTANCE.onTouchButtonPressed();
        Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
        MenuActivity.this.startActivityForResult(intent, 0);
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.TUTORIAL_PUZZLE);
        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
    }

    private void continuePuzzleQuestion() {
        if (Puzzles.getLastPuzzle() != null && !Puzzles.getLastPuzzle().getPuzzle(MenuActivity.this.getApplicationContext()).isDone()) {
            YesNoQuestion.INSTANCE.init(MenuActivity.this, PaintManager.INSTANCE.createPaint(), MenuActivity.this.getString(R.string.continue_message), () -> {
                YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
                GameState.setGameState(GameState.CONTINUE_PUZZLE);
                Puzzles.moveToCategoryByPuzzle(Puzzles.getLastPuzzle());
                Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
                MenuActivity.this.startActivityForResult(intent, 0);
                Bundle bundle = new Bundle();
                bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.CONTINUE_PUZZLE);
                mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
                menuView.render();
                menuView.clearBackground();
            }, () -> {
                YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
                menuView.render();
            });

            menuView.render();
            MyMediaPlayer.play("blop");
        }
    }

    private void initGameDimensions() {
        DisplayMetrics dm = new DisplayMetrics();
        MenuActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        ApplicationSettings.INSTANCE.setScreenHeight(dm.heightPixels);
        ApplicationSettings.INSTANCE.setScreenWidth(dm.widthPixels);
    }

    @Override
    public void onSmallPuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            Puzzles.setCategory(Puzzles.SMALL);
            MyMediaPlayer.play("page_selection");
            Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.SMALL_PUZZLES);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onNormalPuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            Puzzles.setCategory(Puzzles.NORMAL);
            MyMediaPlayer.play("page_selection");
            Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.NORMAL_PUZZLES);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onLargePuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            Puzzles.setCategory(Puzzles.LARGE);
            MyMediaPlayer.play("page_selection");
            Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.LARGE_PUZZLES);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onComplexPuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            Puzzles.setCategory(Puzzles.COMPLEX);
            MyMediaPlayer.play("page_selection");
            Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.COMPLEX_PUZZLES);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onColorfulPuzzleButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.PUZZLE_SELECTION);
            Puzzles.setCategory(Puzzles.COLORFUL);
            MyMediaPlayer.play("page_selection");
            Intent intent = new Intent(MenuActivity.this, PuzzleSelectionActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.COLORFUL_PUZZLES);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onLotteryButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.LOTTERY);
            MyMediaPlayer.play("page_selection");

            Intent intent = new Intent(MenuActivity.this, LotteryActivity.class);
            MenuActivity.this.startActivityForResult(intent, 0);

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.LOTTERY);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            menuView.clearBackground();
        }
    }

    @Override
    public void onLeaderboardButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            MyMediaPlayer.play("page_selection");
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.LEADERBOARD);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);

            GameState.setGameState(GameState.LEADERBOARD);
            if (!isSignedIn()) {
                startSignInIntent();
            } else {
                updateLeaderboard();
                showLeaderboard();
            }
        }
    }

    @Override
    public void onPrivacyPolicyButtonPressed() {
        if ((GameState.getGameState().equals(GameState.MENU))) {
            GameState.setGameState(GameState.PRIVACY_POLICY);

            Uri uri = Uri.parse("https://fakezohan.wixsite.com/website");
            Intent wikipediaLink = new Intent(Intent.ACTION_VIEW, uri);
            try {
                MenuActivity.this.startActivity(wikipediaLink);
            } catch (ActivityNotFoundException ignored) {

            }

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.PRIVACY_POLICY);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        }
    }

    private void updateLeaderboard() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(MenuActivity.this);
        if (googleSignInAccount != null) {
            LeaderboardsClient client = Games.getLeaderboardsClient(MenuActivity.this, googleSignInAccount);
            client.submitScore(getString(R.string.most_puzzles_solved), numOfPuzzlesSolvedSoFar());
            client.loadCurrentPlayerLeaderboardScore(
                    getString(R.string.most_puzzles_solved),
                            LeaderboardVariant.TIME_SPAN_ALL_TIME,
                            LeaderboardVariant.COLLECTION_PUBLIC
                    ).addOnSuccessListener(scoreAnnotatedData -> {
                        LeaderboardScore score = scoreAnnotatedData.get();
                        if (score != null) {
                            String scoreAsString = "#" + score.getRank();
                            writeScoreToSharedPreferences(scoreAsString);
                        }
                    });
        }
    }

    private int numOfPuzzlesSolvedSoFar() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MenuActivity.this);
        int numOfPuzzlesSolved = sharedPreferences.getInt(MenuActivity.this.getString(R.string.most_puzzles_solved), 0);
        return numOfPuzzlesSolved;
    }

    private void writeScoreToSharedPreferences(String scoreAsString) {
        try {
            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(MenuActivity.this).edit();
            prefsEditor.putString(MenuActivity.this.getString(R.string.leaderboards_score), scoreAsString);
            prefsEditor.apply();
        } catch (Exception ignored) { }
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
        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
    }

    @Override
    public void onTouchButtonPressed() {
        GameSettings.INSTANCE.onTouchButtonPressed();
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.TOUCH);
        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
    }

    @Override
    public void onSoundButtonPressed() {
        GameSettings.INSTANCE.onSoundButtonPressed(MenuActivity.this.getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.SOUND);
        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
    }

    @Override
    public void onLaunchMarketButtonPressed() {
        if (((GameState.getGameState().equals(GameState.MENU)))) {
            GameState.setGameState(GameState.PLAYSTORE);
            GameSettings.INSTANCE.onReviewButtonPressed(MenuActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.VOTE);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
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
        if (((GameState.getGameState().equals(GameState.MENU)))) {
            GameState.setGameState(GameState.FACEBOOK);
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(MenuActivity.this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.FACEBOOK);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
            startActivity(facebookIntent);
        }
    }

    @Override
    public void onIcons8ButtonPressed() {
        if ((GameState.getGameState().equals(GameState.MENU))) {
            GameState.setGameState(GameState.ICONS8);
            GameSettings.INSTANCE.onIcons8ButtonPressed(MenuActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.ICONS8);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        }
    }

    @Override
    public void onInstructionsButtonPressed() {
        if (GameState.getGameState().equals(GameState.MENU)) {
            GameState.setGameState(GameState.WIKIPEDIA);
            GameSettings.INSTANCE.onInstructionsButtonPressed(MenuActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_SETTINGS, GameMonitoring.INSTRUCTIONS);
            mFirebaseAnalytics.logEvent(GameMonitoring.GAME_EVENT, bundle);
        }
    }

    @Override
    public void onBackPressed() {
        if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            onOptionsButtonPressed();
            menuView.render();
        } else if (YesNoQuestion.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
            YesNoQuestion.INSTANCE.setAppearance(Appearance.MINIMIZED);
            MyMediaPlayer.play("blop");
            menuView.render();
        } else if (menuView.isShowVipPopup()) {
            menuView.setShowVipPopup(false);
            MyMediaPlayer.play("blop");
            menuView.render();
        } else {
            try {
                Intent start = new Intent(Intent.ACTION_MAIN);
                start.addCategory(Intent.CATEGORY_HOME);
                start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(start);
            } catch (SecurityException se) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            AdManager.setRemoveAdsTrue(MenuActivity.this.getApplicationContext());
            menuView.setShowVipPopup(false);
            menuView.render();
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.REMOVE_ADS_PURCHASED);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.REMOVE_ADS_CANCELED);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            AdManager.setRemoveAdsTrue(MenuActivity.this.getApplicationContext());
            menuView.render();
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.REMOVE_ADS_ALREADY_OWNED);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        } else {
            // Handle any other error codes.
            if (!isFinishing()) {
                new AlertDialog.Builder(this).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show();
            }

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.REMOVE_ADS_NO_INTERNET_CONNECTION);
            mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        }

        menuView.getVipPopup().getPopup().setAnswered(AdManager.isRemoveAds());
    }

    private void queryRemoveAdsSkuDetails() {
        List<String> skuList = new ArrayList<>();
        skuList.add("remove_ads");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    int responseCode = billingResult.getResponseCode();
                    if (responseCode == BillingClient.BillingResponseCode.OK
                            && skuDetailsList != null) {
                        for (SkuDetails skuDetails : skuDetailsList) {
                            String sku = skuDetails.getSku();
                            String price = skuDetails.getPrice();
                            if ("remove_ads".equals(sku)) {
                                removeAdsDetails = skuDetails;
                                menuView.getVipPopup().setPrice(price);
                                menuView.render();
                            }
                        }
                    } else if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ||
                            responseCode == BillingClient.BillingResponseCode.ERROR ||
                            responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ||
                            responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
                        if (!isFinishing()) {
                            new AlertDialog.Builder(MenuActivity.this).setMessage(R.string.no_internet_connection)
                                    .setNeutralButton(android.R.string.ok, null).show();
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.REMOVE_ADS_NO_INTERNET_CONNECTION);
                        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
                    }
                }
        );
    }

    public void onPurchaseVipPressed() {
        if (removeAdsDetails != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(removeAdsDetails)
                    .build();
            BillingResult billingResult = mBillingClient.launchBillingFlow(MenuActivity.this, flowParams);
            int responseCode = billingResult.getResponseCode();
            if (responseCode != BillingClient.BillingResponseCode.OK && !isFinishing()) {
                new AlertDialog.Builder(MenuActivity.this).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    @Override
    public void onPromoteVipPressed() {
        menuView.getVipPopup().setPrice("Loading..");
        menuView.setShowVipPopup(true);
        menuView.render();
        mBillingClient = BillingClient.newBuilder(MenuActivity.this).enablePendingPurchases().setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                int billingResponseCode = billingResult.getResponseCode();
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    queryRemoveAdsSkuDetails();
                } else {
                    menuView.setShowVipPopup(false);
                    menuView.render();
                    Handler mainHandler = new Handler(MenuActivity.this.getMainLooper());
                    mainHandler.post(
                            () -> new AlertDialog.Builder(MenuActivity.this).setMessage(R.string.no_internet_connection)
                                    .setNeutralButton(android.R.string.ok, null).show()
                    );
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                if (!isFinishing()) {
                    new AlertDialog.Builder(MenuActivity.this).setMessage(R.string.no_internet_connection)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
            }
        });
    }

    @Override
    public void promote() {
        Bundle bundle = new Bundle();
        bundle.putString(GameMonitoring.CHOOSE_CATEGORY, GameMonitoring.VIP_PROMOTION_APPEARED);
        mFirebaseAnalytics.logEvent(GameMonitoring.MENU_EVENT, bundle);
        MyMediaPlayer.play("blop");
        menuView.getVipPopup().update();
        if (AdManager.isRemoveAds()) {
            menuView.setShowVipPopup(true);
            menuView.render();
        } else {
            onPromoteVipPressed();
        }
    }
}
