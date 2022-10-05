package com.white.black.nonogram.view.listeners;

public interface GameViewListener extends SecondaryViewListener {
    void onNextPuzzleButtonPressed();
    void onZoomedSlotSelected();
    void onLaunchMarketButtonPressed();
    // void removeAds();
    void reportFaultyPuzzle(String cause, String uniqueId);
}
