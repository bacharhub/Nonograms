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

    private static final long INTERSTITIAL_POPUP_INTERVAL = 1000 * 120; // 2 minutes
    private static long lastInterstitialPopupTime;
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
        if (rewardedInstanceHandler.isLoadingRewardedAd()) {
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
                "ca-app-pub-6810954825772230/1111987109",
                new AdRequest.Builder().build(),
                rewardedAdLoadCallback
        );
    }

    private static void _unmuteSound(Context context){
        AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        aManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
    }
    public static void _muteSound(Context context){
        // TODO: check if it was a good idea to add sound to interstitials..
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
}
