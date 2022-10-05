package com.white.black.nonogram.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.R;

import java.util.List;

public class PurchasesUpdatedHandler implements PurchasesUpdatedListener {

    private final Context context;
    private final Runnable onPurchase;
    private final Runnable onItemAlreadyOwned;
    private final Runnable onPurchaseUpdated;
    private final FirebaseAnalytics mFirebaseAnalytics;

    public PurchasesUpdatedHandler(Context context, Runnable onPurchase, Runnable onItemAlreadyOwned, Runnable onPurchaseUpdated, FirebaseAnalytics mFirebaseAnalytics) {
        this.context = context;
        this.onPurchase = onPurchase;
        this.onItemAlreadyOwned = onItemAlreadyOwned;
        this.onPurchaseUpdated = onPurchaseUpdated;
        this.mFirebaseAnalytics = mFirebaseAnalytics;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            AdManager.setRemoveAdsTrue(context.getApplicationContext());
            onPurchase.run();
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.VIP, GameMonitoring.REMOVE_ADS_PURCHASED);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.VIP, GameMonitoring.REMOVE_ADS_CANCELED);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            onItemAlreadyOwned.run();
        } else {
            // Handle any other error codes.
            if (!((Activity)context).isFinishing()) {
                new AlertDialog.Builder(context).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show();
            }

            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.VIP, GameMonitoring.REMOVE_ADS_NO_INTERNET_CONNECTION);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        }

        onPurchaseUpdated.run();
    }
}
