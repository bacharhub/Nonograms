package com.white.black.nonogram.activities;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.white.black.nonogram.AdManager;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private InterstitialAd interstitialAd;

    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        List<String> testDevices = new ArrayList<>();
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
        testDevices.add("A0E4010295027A20A294B65F0E570045");
        testDevices.add("09C786AA0313E7BD913792B9330CAB47");
        testDevices.add("A033C8466F1C5F4E9E80D9500024587F");
        testDevices.add("63A8CC2D9D953FF4F0231700B27EBC6B");
        testDevices.add("B948E78CFEF1B53FBD4A086A40F1C20D");

        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        new Thread(() -> {
            AdManager.init(MyApplication.this);
            if (AdManager.isRemoveAds()) {
                return;
            }

            AdManager.loadInterstitial(MyApplication.this, interstitialAdLoadCallback);
        }).start();
    }

    private final FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdDismissedFullScreenContent() {
            interstitialAd = null;
            AdManager.onInterstitialAdClosed(MyApplication.this, interstitialAdLoadCallback);
        }
    };

    private final InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd ad) {
            // The mInterstitialAd reference will be null until an ad is loaded.
            interstitialAd = ad;
            interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            // Handle the error
            AdManager.reloadInterstitialAd(MyApplication.this, interstitialAdLoadCallback);
        }
    };
}
