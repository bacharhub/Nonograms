package com.white.black.nonogram.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public enum VipPromotionUtils {
    INSTANCE;

    private BillingClient mBillingClient;
    private SkuDetails removeAdsDetails;

    public void onPurchaseVipPressed(Activity activity) {
        if (removeAdsDetails != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(removeAdsDetails)
                    .build();
            BillingResult billingResult = mBillingClient.launchBillingFlow(activity, flowParams);
            int responseCode = billingResult.getResponseCode();
            if (responseCode != BillingClient.BillingResponseCode.OK && !activity.isFinishing()) {
                new AlertDialog.Builder(activity).setMessage(R.string.no_internet_connection)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    public void onPromoteVipPressed(
            Context context,
            Runnable onBillingSetupFailed,
            Consumer<String> onRemoveAdsPurchaseFound,
            Runnable onPurchase,
            Runnable onItemAlreadyOwned,
            Runnable onPurchaseUpdated,
            FirebaseAnalytics mFirebaseAnalytics
    ) {
        mBillingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(
                new PurchasesUpdatedHandler(context, onPurchase, onItemAlreadyOwned, onPurchaseUpdated, mFirebaseAnalytics)
        ).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                int billingResponseCode = billingResult.getResponseCode();
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    queryRemoveAdsSkuDetails(context, onRemoveAdsPurchaseFound);
                } else {
                    onBillingSetupFailed.run();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Activity activity = (Activity) context;
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    new AlertDialog.Builder(context).setMessage(R.string.no_internet_connection)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
            }
        });
    }

    private void queryRemoveAdsSkuDetails(Context context, Consumer<String> onRemoveAdsPurchaseFound) {
        List<String> skuList = new ArrayList<>();
        skuList.add("remove_ads");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        int responseCode = billingResult.getResponseCode();
                        if (responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                if ("remove_ads".equals(sku)) {
                                    removeAdsDetails = skuDetails;
                                    onRemoveAdsPurchaseFound.accept(price);
                                }
                            }
                        } else if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ||
                                responseCode == BillingClient.BillingResponseCode.ERROR ||
                                responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ||
                                responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
                            if (!((Activity) context).isFinishing()) {
                                new AlertDialog.Builder(context).setMessage(R.string.no_internet_connection)
                                        .setNeutralButton(android.R.string.ok, null).show();
                            }
                        }
                    }
                }
        );
    }
}
