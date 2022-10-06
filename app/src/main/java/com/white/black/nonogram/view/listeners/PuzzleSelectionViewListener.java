package com.white.black.nonogram.view.listeners;

import android.content.Context;

public interface PuzzleSelectionViewListener extends SecondaryViewListener {
    void onSwitchCategoryPressed();
    void onPuzzleButtonPressed();
    void onStartOverButtonPressed();
    void onPromoteVipPressed(Context context);
}
