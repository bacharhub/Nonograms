package com.white.black.nonogram;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdManager {

    private static final long INTERSTITIAL_POPUP_INTERVAL = 1000 * 180; // 3 minutes
    private static final long VIDEO_POPUP_INTERVAL = 3000; //1000 * 360; // 6 minutes
    private static long lastInterstitialPopupTime = System.currentTimeMillis();
    private static boolean removeAds;

    public static void showRewardedVideo(
            RewardedInstanceHandler rewardedInstanceHandler,
            Activity activity,
            OnUserEarnedRewardListener userEarnedRewardListener
    ) {
        if (rewardedInstanceHandler != null) {
            RewardedAd rewardedVideoAd = rewardedInstanceHandler.getRewardedVideoAd();
            if (rewardedVideoAd != null) {
                lastInterstitialPopupTime = System.currentTimeMillis();
                rewardedVideoAd.show(
                        activity,
                        userEarnedRewardListener
                );
            }
        }
    }

    public static boolean isRemoveAds() {
        return removeAds;
    }

    public static void init(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        removeAds = sharedPreferences.getBoolean("remove_ads", false);
    }

    public static void setRemoveAdsTrue(Context context) {
        try {
            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            removeAds = true;
            prefsEditor.putBoolean("remove_ads", removeAds);
            prefsEditor.apply();
        } catch (Exception ignore) {

        }
    }

    public static void loadRewardedVideo(
            RewardedInstanceHandler rewardedInstanceHandler,
            Activity context,
            OnUserEarnedRewardListener userEarnedRewardListener,
            RewardedAdLoadCallback rewardedAdLoadCallback
    ) {
        // Use an activity context to get the rewarded video instance.
        if (rewardedInstanceHandler.isLoadingRewardedAd()) { // still loading?
            return;
        }

        RewardedAd ad = rewardedInstanceHandler.getRewardedVideoAd();
        if (ad != null) { // loading done
            showRewardedVideo(rewardedInstanceHandler, context, userEarnedRewardListener);
            return;
        }

        rewardedInstanceHandler.setLoadingRewardedAd(true);
        RewardedAd.load(
                context,
                "ca-app-pub-6810954825772230/1111987109",/*"ca-app-pub-3940256099942544/5224354917",*/ // for testing*/,
                new AdRequest.Builder().build(),
                rewardedAdLoadCallback
        );
    }

    /*private static void loadAdmobBanner(AdView adView, Context context, RelativeLayout ll) {
        if (!MemoryManager.isLowMemory()) {
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(new Runnable() {
                public void run() {
                    adView.setAdSize(AdSize.SMART_BANNER);
                    adView.setAdUnitId("ca-app-pub-6810954825772230/7132472456"); // banner unit id
                    AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("A0E4010295027A20A294B65F0E570045").addTestDevice("09C786AA0313E7BD913792B9330CAB47").build();
                    adView.loadAd(adRequest);
                    ll.addView(adView); // The ad banner
                }
            });
        }
    }*/

    private static void _unmuteSound(Context context){
        AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        aManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
    }
    public static void _muteSound(Context context){
        AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        aManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
    }

    private static void loadInterstitial(Context context, InterstitialAdLoadCallback interstitialAdLoadCallback) {
        InterstitialAd.load(
                context,
                "ca-app-pub-6810954825772230/4958165792",
                new AdRequest.Builder().build(),
                interstitialAdLoadCallback
        );
    }

    public static void onInterstitialAdClosed(Context context, InterstitialAdLoadCallback interstitialAdLoadCallback) {
        reloadInterstitialAd(context, interstitialAdLoadCallback);
        _unmuteSound(context);
    }

    public static boolean isTimeForInterstitial() {
        return System.currentTimeMillis() > lastInterstitialPopupTime + INTERSTITIAL_POPUP_INTERVAL;
    }

    public static void showInterstitial(InterstitialAd interstitialAd, Activity activity) {
        interstitialAd.show(activity);
        lastInterstitialPopupTime = System.currentTimeMillis();
    }

    public static void reloadInterstitialAd(Context context, InterstitialAdLoadCallback interstitialAdLoadCallback) {
        if (MemoryManager.isLowMemory()) {
            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            ses.schedule(
                    () -> reloadInterstitialAd(context, interstitialAdLoadCallback), 1, TimeUnit.MINUTES
            );
            ses.shutdown();
        } else {
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(
                    () -> loadInterstitial(context, interstitialAdLoadCallback)
            );
        }
    }

    /*public static void loadBanner(Context context, AdView adView, RelativeLayout ll) {
        if (MemoryManager.isLowMemory() || isRemoveAds()) {
            return;
        }

        loadAdmobBanner(adView, context, ll);
    }*/

    /*

    public static void loadInterstitialAndBanner(Context context, AdView adView, RelativeLayout ll) {
        if (MemoryManager.isLowMemory() || isRemoveAds()) {
            return;
        }

        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-6810954825772230/4958165792");
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                Handler mainHandler = new Handler(context.getMainLooper());
                mainHandler.post(new Runnable() {
                                     public void run() {
                                         _muteSound(context);
                                         interstitialAd.show();
                                     }
                                 }
                );
            }

            @Override
            public void onAdClosed() {
                interstitialAd.setAdListener(null);
                _unmuteSound(context);
                loadAdmobBanner(adView, context, ll);
            }

            @Override
            public void onAdFailedToLoad(int var) {
                lastInterstitialPopupTime = 0;
                interstitialAd.setAdListener(null);
                loadAdmobBanner(adView, context, ll);
            }
        });

        if (System.currentTimeMillis() > lastInterstitialPopupTime + INTERSTITIAL_POPUP_INTERVAL) {
            lastInterstitialPopupTime = System.currentTimeMillis();
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(new Runnable() {
                                 public void run() {
                                         interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("A0E4010295027A20A294B65F0E570045").build());
                                 }
                             }
            );
        } else {
                loadAdmobBanner(adView, context, ll);
        }
    }

     */
}
