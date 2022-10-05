package com.white.black.nonogram.view.listeners;

import android.content.Context;

public interface PuzzleSelectionViewListener extends SecondaryViewListener {
    void onSwitchCategoryPressed();
    void onPuzzleButtonPressed();
    void onStartOverButtonPressed();
    void loadVideoAd();
    void onPurchaseVipPressed();
    void onPromoteVipPressed(Context context);
}
