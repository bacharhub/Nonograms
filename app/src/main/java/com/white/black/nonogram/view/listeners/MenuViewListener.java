package com.white.black.nonogram.view.listeners;

public interface MenuViewListener extends ViewListener {
    void onCloseShopButtonPressed();

    void onShopButtonPressed();

    void onSmallPuzzleButtonPressed();

    void onNormalPuzzleButtonPressed();

    void onLargePuzzleButtonPressed();

    void onComplexPuzzleButtonPressed();

    void onColorfulPuzzleButtonPressed();

    void onLotteryButtonPressed();

    void onLeaderboardButtonPressed();

    void onPrivacyPolicyButtonPressed();

    void onPromoteVipPressed();

    void onPurchaseVipPressed();
}
