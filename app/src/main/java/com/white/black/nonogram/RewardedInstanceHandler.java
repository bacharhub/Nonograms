package com.white.black.nonogram;

import com.google.android.gms.ads.rewarded.RewardedAd;

public class RewardedInstanceHandler {

    private RewardedAd rewardedVideoAd;
    private boolean isLoadingRewardedAd;

    public RewardedAd getRewardedVideoAd() {
        return rewardedVideoAd;
    }

    public void setRewardedVideoAd(RewardedAd rewardedVideoAd) {
        this.rewardedVideoAd = rewardedVideoAd;
    }

    public void setLoadingRewardedAd(boolean b) {
        this.isLoadingRewardedAd = b;
    }

    public boolean isLoadingRewardedAd() {
        return this.isLoadingRewardedAd;
    }
}
