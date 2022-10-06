package com.white.black.nonogram.view.listeners;

import android.content.Context;

public interface GameViewListener extends SecondaryViewListener {
    void onNextPuzzleButtonPressed();
    void onZoomedSlotSelected();
    void onLaunchMarketButtonPressed();
    // void removeAds();
    void reportFaultyPuzzle(String cause, String uniqueId);
    int numOfAvailableClues();
    void useClue();
    void onPromoteVipPressed(Context context);
}
