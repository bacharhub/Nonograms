package com.white.black.nonogram.view;

import static com.white.black.nonogram.GameMonitoring.UNLOCKED_PUZZLE_WITH_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.AdManager;
import com.white.black.nonogram.ApplicationSettings;
import com.white.black.nonogram.BitmapLoader;
import com.white.black.nonogram.GameMonitoring;
import com.white.black.nonogram.GameSettings;
import com.white.black.nonogram.GameState;
import com.white.black.nonogram.MemoryManager;
import com.white.black.nonogram.MyMediaPlayer;
import com.white.black.nonogram.Puzzle;
import com.white.black.nonogram.PuzzleReference;
import com.white.black.nonogram.Puzzles;
import com.white.black.nonogram.R;
import com.white.black.nonogram.TouchMonitor;
import com.white.black.nonogram.view.buttons.PicButtonView;
import com.white.black.nonogram.view.buttons.PuzzleSelectionButtonView;
import com.white.black.nonogram.view.buttons.PuzzleSelectionSettingsButtonView;
import com.white.black.nonogram.view.buttons.ReturnButtonView;
import com.white.black.nonogram.view.buttons.SwitchCategoryButtonView;
import com.white.black.nonogram.view.listeners.PuzzleSelectionViewListener;
import com.white.black.nonogram.view.listeners.ViewListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PuzzleCategorySelectionView extends ScrollableView {

    private static final int PUZZLES_BOTTOM = ApplicationSettings.INSTANCE.getScreenHeight() * 92 / 100;
    private static final int PUZZLES_TOP = ApplicationSettings.INSTANCE.getScreenHeight() * 2 / 10;
    private static final int NUM_OF_PUZZLES_A_ROW = 3;
    private final PuzzleSelectionViewListener puzzleSelectionViewListener;
    private int backgroundColor;
    private List<Bitmap> socialNetworks;
    private ReturnButtonView returnButtonView;
    private PuzzleSelectionSettingsButtonView puzzleSelectionSettingsButtonView;
    private OptionsView menuOptionsView;
    private SwitchCategoryButtonView smallCategoryButtonView;
    private SwitchCategoryButtonView normalCategoryButtonView;
    private SwitchCategoryButtonView largeCategoryButtonView;
    private SwitchCategoryButtonView complexCategoryButtonView;
    private SwitchCategoryButtonView colorfulCategoryButtonView;
    private Map<Puzzles, PicButtonView[]> puzzleSelectionButtonViewMap;
    private Map<Puzzles, Set<String>> initializedPuzzleSelectionButtons;
    private Bitmap newPuzzleIcon;
    private Bitmap background;
    private Supplier<Integer> availableKeysSupplier;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    public VipPopup getVipPopup() {
        return rewardedAdPopup.getVipPopup();
    }

    public boolean isShowingRewardedAdVipPopup() {
        return rewardedAdPopup.isShowingVipPopup();
    }

    public boolean isShowingUseKeyVipPopup() {
        return useKeyPopup.isShowingVipPopup();
    }

    public void setShowRewardedAdVipPopup(boolean showVipPopup) {
        rewardedAdPopup.setShowVipPopup(showVipPopup);
    }

    public void setShowUseKeyVipPopup(boolean showVipPopup) {
        useKeyPopup.setShowVipPopup(showVipPopup);
    }

    private WatchAdPopup rewardedAdPopup;
    private UseKeyPopup useKeyPopup;

    public void setShowPopupFalse() {
        this.rewardedAdPopup.setShowPopup(false);
    }

    public boolean isShowingRewardedAdPopup() {
        return rewardedAdPopup.isShowingPopup();
    }

    public boolean isShowingUseKeyPopup() {
        return useKeyPopup.isShowingPopup();
    }

    public void clearPool() {
        pool.shutdown();
    }

    public Popup getRewardedAdPopup() {
        return rewardedAdPopup.getPopup();
    }

    public Popup getUseKeyPopup() {
        return useKeyPopup.getPopup();
    }

    /* used for calculating scrolled items */
    private int puzzleWidth;
    private int puzzleHeight;
    private int horizontalGapBetweenPuzzles;
    private int verticalGapBetweenPuzzles;

    public PuzzleCategorySelectionView(Context context) {
        super(context);
        puzzleSelectionViewListener = (PuzzleSelectionViewListener) context;
        backToTop();
    }

    public void backToTop() {
        super.verticalBoost = 0;
        super.verticalGap = PUZZLES_TOP;
        super.top = PUZZLES_TOP;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (puzzleSelectionButtonViewMap != null && (puzzleSelectionButtonViewMap.get(Puzzles.getCurrent()) != null)) {
            render();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void update() {
        update((((Puzzles.getCurrent().getPuzzleReferences().size() - 1) / NUM_OF_PUZZLES_A_ROW) + 1) * (puzzleHeight + verticalGapBetweenPuzzles), PUZZLES_TOP, PUZZLES_BOTTOM);
    }

    public void stopScrolling() {
        verticalBoost = 0;
    }

    public void clearBitmapByCategory(Puzzles category) {
        if (puzzleSelectionButtonViewMap != null) {
            PicButtonView[] listPuzzleSelectionButtonView = puzzleSelectionButtonViewMap.get(category);
            if (listPuzzleSelectionButtonView != null) {
                for (int i = 0; i < listPuzzleSelectionButtonView.length; i++) {
                    if (listPuzzleSelectionButtonView[i] != null && listPuzzleSelectionButtonView[i] instanceof PuzzleSelectionButtonView) {
                        ((PuzzleSelectionButtonView) listPuzzleSelectionButtonView[i]).recycleBitmap();
                    }
                }
            }
        }
    }

    public void clearAllBitmaps() {
        if (puzzleSelectionButtonViewMap != null) {
            for (Puzzles puzzleCategory : Puzzles.values()) {
                clearBitmapByCategory(puzzleCategory);
            }
        }
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        if (MemoryManager.isCriticalMemory()) {
            if (background != null && !background.isRecycled()) {
                canvas.drawBitmap(background, 0, 0, paint);
            } else {
                paint.setColor(backgroundColor);
                canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            }

            return;
        }

        if (background == null || background.isRecycled()) {
            Canvas tempCanvas;
            try {
                background = Bitmap.createBitmap(ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), Bitmap.Config.ARGB_8888);
                background.setDensity(Bitmap.DENSITY_NONE);
                tempCanvas = new Canvas(background);
            } catch (Exception ex) {
                tempCanvas = canvas;
                background = null;
            }

            paint.setColor(backgroundColor);
            tempCanvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), ApplicationSettings.INSTANCE.getScreenHeight(), paint);
            paint.setAlpha(30);
            int foodItemDrawnCounter = 0;
            for (int x = 0; x < ApplicationSettings.INSTANCE.getScreenWidth(); x += 150) {
                for (int y = 0; y < ApplicationSettings.INSTANCE.getScreenHeight(); y += 150) {
                    tempCanvas.drawBitmap(socialNetworks.get((foodItemDrawnCounter++) % socialNetworks.size()), x, y, paint);
                }
            }
            paint.setAlpha(255);
        }

        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, 0, 0, paint);
        }
    }

    private void drawBackgroundTop(Canvas canvas, Paint paint) {
        if (background == null || background.isRecycled()) {
            paint.setColor(backgroundColor);
            canvas.drawRect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), PUZZLES_TOP - verticalGapBetweenPuzzles, paint);
            paint.setAlpha(30);
            int socialNetworksDrawnCounter = 0;
            for (int x = 0; x < ApplicationSettings.INSTANCE.getScreenWidth(); x += 150) {
                for (int y = 0; y < PUZZLES_TOP - verticalGapBetweenPuzzles - 100; y += 150) {
                    canvas.drawBitmap(socialNetworks.get((socialNetworksDrawnCounter++) % socialNetworks.size()), x, y, paint);
                }
            }
            paint.setAlpha(255);
        } else {
            canvas.drawBitmap(background, new Rect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), PUZZLES_TOP - verticalGapBetweenPuzzles),
                    new Rect(0, 0, ApplicationSettings.INSTANCE.getScreenWidth(), PUZZLES_TOP - verticalGapBetweenPuzzles), paint);
        }
    }

    public void clearBackground() {
        if (background != null) {
            this.background.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (GameState.getGameState().equals(GameState.PUZZLE_SELECTION) || GameState.getGameState().equals(GameState.NEXT_PUZZLE)) {
            super.draw(canvas);
            drawBackground(canvas, paint);
            drawPuzzles(canvas, paint);
            drawBackgroundTop(canvas, paint);
            returnButtonView.draw(canvas, paint);
            puzzleSelectionSettingsButtonView.draw(canvas, paint);
            drawSwitchCategoryButtonView(canvas, paint);
            if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                menuOptionsView.draw(canvas, paint);
            }

            if (PuzzleSelectionView.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED)) {
                PuzzleSelectionView.INSTANCE.draw(canvas, paint);
            }

            useKeyPopup.draw(canvas, paint);
            rewardedAdPopup.draw(canvas, paint);
        }
    }

    @Override
    protected void onScroll() {
        updateScrolledButtons();
    }

    public void refreshPuzzleSelectionButtonView(PuzzleReference puzzleReference) {
        if (puzzleSelectionButtonViewMap != null && initializedPuzzleSelectionButtons != null) {
            Set<String> currentInitializedPuzzleSelectionButtons = this.initializedPuzzleSelectionButtons.get(Puzzles.getCurrent());
            if (currentInitializedPuzzleSelectionButtons != null) {
                if (currentInitializedPuzzleSelectionButtons.contains(puzzleReference.getUniqueId())) {
                    PicButtonView[] puzzleSelectionButtonView = puzzleSelectionButtonViewMap.get(Puzzles.getCurrent());
                    for (int i = 0; i < puzzleSelectionButtonView.length; i++) {
                        if (puzzleSelectionButtonView[i] != null && puzzleSelectionButtonView[i] instanceof PuzzleSelectionButtonView && ((PuzzleSelectionButtonView) puzzleSelectionButtonView[i]).getPuzzleReference() == puzzleReference) {
                            ((PuzzleSelectionButtonView) puzzleSelectionButtonView[i]).recycleBitmap();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void drawSwitchCategoryButtonView(Canvas canvas, Paint paint) {
        if (Puzzles.getCurrent().equals(Puzzles.SMALL)) {
            smallCategoryButtonView.draw(canvas, paint);
        } else if (Puzzles.getCurrent().equals(Puzzles.NORMAL)) {
            normalCategoryButtonView.draw(canvas, paint);
        } else if (Puzzles.getCurrent().equals(Puzzles.LARGE)) {
            largeCategoryButtonView.draw(canvas, paint);
        } else if (Puzzles.getCurrent().equals(Puzzles.COMPLEX)) {
            complexCategoryButtonView.draw(canvas, paint);
        } else {
            colorfulCategoryButtonView.draw(canvas, paint);
        }
    }

    private class ScrolledButtonLoader implements Runnable {
        private final ArrayList<PuzzleReference> puzzles;
        private final int i;
        private final Puzzles category;
        private final ScrolledButtonLocation scrolledButtonLocation;

        ScrolledButtonLoader(ArrayList<PuzzleReference> puzzles, int i, Puzzles category, ScrolledButtonLocation scrolledButtonLocation) {
            this.puzzles = puzzles;
            this.i = i;
            this.category = category;
            this.scrolledButtonLocation = scrolledButtonLocation;
        }

        @Override
        public void run() {
            PuzzleReference puzzleReference = puzzles.get(i);
            PicButtonView puzzleSelectionButtonView = null;

            final RectF bounds = new RectF(returnButtonView.getBounds().left + scrolledButtonLocation.getHorizontalIndex() * (horizontalGapBetweenPuzzles + puzzleWidth),
                    scrolledButtonLocation.getY(),
                    returnButtonView.getBounds().left + scrolledButtonLocation.getHorizontalIndex() * (horizontalGapBetweenPuzzles + puzzleWidth) + puzzleWidth,
                    scrolledButtonLocation.getY() + puzzleHeight);

            if (initializedPuzzleSelectionButtons.get(category).contains(puzzleReference.getUniqueId())) {
                puzzleSelectionButtonView = puzzleSelectionButtonViewMap.get(category)[i];
            } else {
                if (bounds.top > ApplicationSettings.INSTANCE.getScreenHeight()) {
                    return;
                }

                initializedPuzzleSelectionButtons.get(category).add(puzzleReference.getUniqueId());

                Runnable taskToExecute = () -> {
                    puzzleSelectionButtonViewMap.get(category)[i] = createPuzzleSelectionButtonView(category, puzzleReference, PaintManager.INSTANCE.createPaint(), bounds);
                    if (puzzleSelectionButtonViewMap.get(category).length - 1 == i) {
                        render();
                    } else {
                        update();
                    }
                };

                if (pool.isShutdown() || pool.isTerminated()) {
                    new Thread(taskToExecute).start();
                } else {
                    pool.execute(taskToExecute);
                }
            }

            if (puzzleSelectionButtonView != null) {
                puzzleSelectionButtonView.getBounds().set(bounds);
                puzzleSelectionButtonView.getBackgroundBounds().set(bounds.left,
                        bounds.top,
                        bounds.left + bounds.width() * 104 / 100,
                        bounds.top + bounds.height() * 110 / 100);
            }
        }
    }

    private class ScrolledButtonLocation {
        private int horizontalIndex;
        private int y;

        ScrolledButtonLocation(int horizontalIndex, int y) {
            this.horizontalIndex = horizontalIndex;
            this.y = y;
        }

        int getHorizontalIndex() {
            return horizontalIndex;
        }

        void setHorizontalIndex(int horizontalIndex) {
            this.horizontalIndex = horizontalIndex;
        }

        int getY() {
            return y;
        }

        void setY(int y) {
            this.y = y;
        }
    }

    public void updateScrolledButtons() {
        Puzzles category = Puzzles.getCurrent();
        ScrolledButtonLocation scrolledButtonLocation = new ScrolledButtonLocation(0, top);

        ArrayList<PuzzleReference> puzzles = category.getPuzzleReferences();
        int numOfPuzzles = puzzles.size();

        for (int i = 0; i < numOfPuzzles; i++) {
            ScrolledButtonLoader scrolledButtonLoader = new ScrolledButtonLoader(puzzles, i, category, new ScrolledButtonLocation(scrolledButtonLocation.getHorizontalIndex(), scrolledButtonLocation.getY()));
            scrolledButtonLoader.run();

            if (scrolledButtonLocation.getHorizontalIndex() == (NUM_OF_PUZZLES_A_ROW - 1)) {
                scrolledButtonLocation.setHorizontalIndex(0);
                scrolledButtonLocation.setY(scrolledButtonLocation.getY() + verticalGapBetweenPuzzles + puzzleHeight);
            } else {
                scrolledButtonLocation.setHorizontalIndex(scrolledButtonLocation.getHorizontalIndex() + 1);
            }
        }
    }

    private void drawPuzzles(Canvas canvas, Paint paint) {
        Puzzles category = Puzzles.getCurrent();

        ArrayList<PuzzleReference> puzzles = category.getPuzzleReferences();
        int numOfPuzzles = puzzles.size();

        for (int i = 0; i < numOfPuzzles; i++) {
            PuzzleReference puzzleReference = puzzles.get(i);
            PicButtonView puzzleSelectionButtonView = puzzleSelectionButtonViewMap.get(category)[i];

            if (puzzleReference.isLoaded() && puzzleSelectionButtonView != null) {
                RectF bounds = puzzleSelectionButtonView.getBounds();

                if ((bounds.bottom < (PUZZLES_TOP - verticalGapBetweenPuzzles))) {
                    continue;
                }

                if ((bounds.top > ApplicationSettings.INSTANCE.getScreenHeight())) {
                    return;
                }

                puzzleSelectionButtonView.draw(canvas, paint);

                if (puzzleReference.getPuzzle(((Context) puzzleSelectionViewListener).getApplicationContext()).isNew() && !puzzleReference.getPuzzle(((Context) puzzleSelectionViewListener).getApplicationContext()).isDone()) {
                    RectF newPuzzleIconBounds = new RectF(
                            bounds.right - bounds.width() / 3,
                            bounds.top - bounds.width() / 3,
                            bounds.right + bounds.width() / 3,
                            bounds.top + bounds.width() / 3
                    );

                    canvas.drawBitmap(newPuzzleIcon, null, newPuzzleIconBounds, paint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.initDone) {
            puzzleSelectionViewListener.onViewTouched(event);
            if (GameSettings.INSTANCE.getAppearance().equals(Appearance.MINIMIZED)) {
                if (PuzzleSelectionView.INSTANCE.getAppearance().equals(Appearance.MINIMIZED)) {
                    if (useKeyPopup.isShowingPopup()) {
                        useKeyPopup.onTouchEvent();
                        TouchMonitor.INSTANCE.setTouchUp(false);
                    } else if (rewardedAdPopup.isShowingPopup()) {
                        rewardedAdPopup.onTouchEvent();
                        TouchMonitor.INSTANCE.setTouchUp(false);
                    } else {
                        if (returnButtonView.wasPressed()) {
                            TouchMonitor.INSTANCE.setTouchUp(false);
                            returnButtonView.onButtonPressed();
                        } else if (puzzleSelectionSettingsButtonView.wasPressed()) {
                            TouchMonitor.INSTANCE.setTouchUp(false);
                            puzzleSelectionSettingsButtonView.onButtonPressed();
                        } else if (smallCategoryButtonView.wasPressed() /* any of the categories - not just small */) {
                            TouchMonitor.INSTANCE.setTouchUp(false);
                            smallCategoryButtonView.onButtonPressed();
                        } else {
                            for (int i = 0; i < puzzleSelectionButtonViewMap.get(Puzzles.getCurrent()).length; i++) {
                                PicButtonView puzzleSelectionButtonView = puzzleSelectionButtonViewMap.get(Puzzles.getCurrent())[i];
                                if (puzzleSelectionButtonView instanceof PuzzleSelectionButtonView && puzzleSelectionButtonView.wasPressed()) {
                                    MyMediaPlayer.play("blop");
                                    TouchMonitor.INSTANCE.setTouchUp(false);

                                    Puzzle.PuzzleClass puzzleClass = ((PuzzleSelectionButtonView) puzzleSelectionButtonView).getPuzzleReference().getPuzzle((Context) puzzleSelectionViewListener).getPuzzleClass();

                                    PuzzleSelectionView.INSTANCE.init((Context) puzzleSelectionViewListener, ((PuzzleSelectionButtonView) puzzleSelectionButtonView).getPuzzleReference(), puzzleSelectionViewListener, PaintManager.INSTANCE.createPaint());
                                    if (AdManager.isRemoveAds()) {
                                        PuzzleSelectionView.INSTANCE.setAppearance(Appearance.MAXIMIZED);
                                    } else if (puzzleClass != null && puzzleClass.equals(Puzzle.PuzzleClass.VIP)) {
                                        int availableKeys = availableKeysSupplier.get();
                                        if (availableKeys > 0) {
                                            useKeyPopup.setShowPopup(true);
                                        } else {
                                            rewardedAdPopup.setShowPopup(true);
                                        }
                                    } else {
                                        PuzzleSelectionView.INSTANCE.setAppearance(Appearance.MAXIMIZED);
                                    }

                                    break;
                                }
                            }
                        }
                    }
                } else {
                    PuzzleSelectionView.INSTANCE.onTouchEvent();
                }
            } else {
                menuOptionsView.onTouchEvent();
            }

            if (PuzzleSelectionView.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED) ||
                    GameSettings.INSTANCE.getAppearance().equals(Appearance.MAXIMIZED) ||
                    rewardedAdPopup.isShowingPopup() || useKeyPopup.isShowingPopup()) {
                render();
                return true;
            }

            update();
        }

        return true;
    }

    private void unlockPuzzle(Context context) {
        refreshPuzzleSelectionButtonView(PuzzleSelectionView.INSTANCE.getPuzzleReference());
        PuzzleSelectionView.INSTANCE.getPuzzleReference().getPuzzle(context.getApplicationContext()).setPuzzleClass(Puzzle.PuzzleClass.FREE);
        PuzzleSelectionView.INSTANCE.getPuzzleReference().writeToSharedPreferences(context.getApplicationContext());
    }

    private void onRewardedAdOffered(Context context, boolean accepted) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.REWARDED_AD_OFFER, accepted ? GameMonitoring.REWARDED_AD_OFFER_ACCEPTED : GameMonitoring.REWARDED_AD_OFFER_REFUSED);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    private void onAdFailedToLoad(LoadAdError loadAdError, Context context) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.LOAD_VIDEO_AD_ERROR_CODE, loadAdError.toString());
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    private int availableKeys(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int keys = sharedPreferences.getInt("keys", 1);
        return keys;
    }

    private void useKey(Context context) {
        int availableKeys = availableKeys(context);
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("keys", availableKeys - 1);
        prefsEditor.apply();

        try {
            Bundle bundle = new Bundle();
            bundle.putString(GameMonitoring.REWARDED_AD_OFFER, UNLOCKED_PUZZLE_WITH_KEY);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            mFirebaseAnalytics.logEvent(GameMonitoring.GALLERY_EVENT, bundle);
        } catch (Exception ignored) {
        }
    }

    public void init(Context context, Paint paint) {
        this.initDone = false;
        PuzzleSelectionView.INSTANCE.setAppearance(Appearance.MINIMIZED);
        backgroundColor = ContextCompat.getColor(context, R.color.puzzleSelectionBackground);

        availableKeysSupplier = () -> availableKeys(context);

        socialNetworks = new LinkedList<>();
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.facebook_100));
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.tumblr_100));
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.line_100));
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.twitter_100));
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.instagram_100));
        socialNetworks.add(BitmapLoader.INSTANCE.getImage(context, R.drawable.tiktok_100));

        newPuzzleIcon = BitmapLoader.INSTANCE.getImage(context, R.drawable.new_100);

        int top = ApplicationSettings.INSTANCE.getScreenHeight() / 22;
        int horizontalDistanceFromEdge = top;
        int buttonHeight = ApplicationSettings.INSTANCE.getScreenHeight() / 12;
        int buttonWidth = ApplicationSettings.INSTANCE.getScreenWidth() * 22 / 100;

        RectF returnButtonBounds = new RectF(
                horizontalDistanceFromEdge,
                top,
                horizontalDistanceFromEdge + buttonWidth,
                top + buttonHeight
        );

        returnButtonView = new ReturnButtonView(
                (ViewListener) context,
                context.getString(R.string.return_button),
                returnButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.return_100)}, context, paint);

        RectF settingsButtonBounds = new RectF(
                ApplicationSettings.INSTANCE.getScreenWidth() - horizontalDistanceFromEdge - buttonWidth,
                top,
                ApplicationSettings.INSTANCE.getScreenWidth() - horizontalDistanceFromEdge,
                top + buttonHeight
        );

        puzzleSelectionSettingsButtonView = new PuzzleSelectionSettingsButtonView(
                (ViewListener) context,
                context.getString(R.string.settings_description),
                settingsButtonBounds,
                ContextCompat.getColor(context, R.color.settingsBrown1), ContextCompat.getColor(context, R.color.settingsBrown2), ContextCompat.getColor(context, R.color.settingsBrown3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.settings_512)}, context, paint);

        menuOptionsView = new MenuOptionsView();
        menuOptionsView.init(context, paint);

        RectF switchCategoryButtonViewBounds = new RectF(
                (returnButtonBounds.right + settingsButtonBounds.left) / 2 - returnButtonBounds.width() / 2,
                top,
                (returnButtonBounds.right + settingsButtonBounds.left) / 2 + returnButtonBounds.width() / 2,
                top + buttonHeight
        );

        smallCategoryButtonView = new SwitchCategoryButtonView((ViewListener) context,
                context.getString(R.string.small_puzzle_description),
                switchCategoryButtonViewBounds,
                ContextCompat.getColor(context, R.color.smallPuzzleGreen1), ContextCompat.getColor(context, R.color.smallPuzzleGreen2), ContextCompat.getColor(context, R.color.smallPuzzleGreen3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_green_512)}, context, paint);

        normalCategoryButtonView = new SwitchCategoryButtonView((ViewListener) context,
                context.getString(R.string.normal_puzzle_description),
                switchCategoryButtonViewBounds,
                ContextCompat.getColor(context, R.color.normalPuzzleOrange1), ContextCompat.getColor(context, R.color.normalPuzzleOrange2), ContextCompat.getColor(context, R.color.normalPuzzleOrange3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_yellow_512)}, context, paint);

        largeCategoryButtonView = new SwitchCategoryButtonView((ViewListener) context,
                context.getString(R.string.large_puzzle_description),
                switchCategoryButtonViewBounds,
                ContextCompat.getColor(context, R.color.largePuzzleRed1), ContextCompat.getColor(context, R.color.largePuzzleRed2), ContextCompat.getColor(context, R.color.largePuzzleRed3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_red_512)}, context, paint);

        complexCategoryButtonView = new SwitchCategoryButtonView((ViewListener) context,
                context.getString(R.string.complex_puzzle_description),
                switchCategoryButtonViewBounds,
                ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_dark_512)}, context, paint);

        colorfulCategoryButtonView = new SwitchCategoryButtonView((ViewListener) context,
                context.getString(R.string.colorful_puzzle_description),
                switchCategoryButtonViewBounds,
                ContextCompat.getColor(context, R.color.colorfulPuzzlePink1), ContextCompat.getColor(context, R.color.colorfulPuzzlePink2), ContextCompat.getColor(context, R.color.colorfulPuzzlePink3), new Bitmap[]{BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_blue_512)}, context, paint);

        puzzleWidth = (int) ((puzzleSelectionSettingsButtonView.getBounds().right - returnButtonView.getBounds().left) / (NUM_OF_PUZZLES_A_ROW + 1));
        puzzleHeight = puzzleWidth * 5 / 3;
        horizontalGapBetweenPuzzles = ((int) (puzzleSelectionSettingsButtonView.getBounds().right - returnButtonView.getBounds().left) - NUM_OF_PUZZLES_A_ROW * puzzleWidth) / (NUM_OF_PUZZLES_A_ROW - 1);
        verticalGapBetweenPuzzles = puzzleHeight / 5;

        puzzleSelectionButtonViewMap = new HashMap<>();
        puzzleSelectionButtonViewMap.put(Puzzles.SMALL, new PicButtonView[Puzzles.SMALL.getPuzzleReferences().size()]);
        puzzleSelectionButtonViewMap.put(Puzzles.NORMAL, new PicButtonView[Puzzles.NORMAL.getPuzzleReferences().size()]);
        puzzleSelectionButtonViewMap.put(Puzzles.LARGE, new PicButtonView[Puzzles.LARGE.getPuzzleReferences().size()]);
        puzzleSelectionButtonViewMap.put(Puzzles.COMPLEX, new PicButtonView[Puzzles.COMPLEX.getPuzzleReferences().size()]);
        puzzleSelectionButtonViewMap.put(Puzzles.COLORFUL, new PicButtonView[Puzzles.COLORFUL.getPuzzleReferences().size()]);

        initializedPuzzleSelectionButtons = new HashMap<>();
        initializedPuzzleSelectionButtons.put(Puzzles.SMALL, new HashSet<>(Puzzles.getNumOfPuzzlesPerCategory()));
        initializedPuzzleSelectionButtons.put(Puzzles.NORMAL, new HashSet<>(Puzzles.getNumOfPuzzlesPerCategory()));
        initializedPuzzleSelectionButtons.put(Puzzles.LARGE, new HashSet<>(Puzzles.getNumOfPuzzlesPerCategory()));
        initializedPuzzleSelectionButtons.put(Puzzles.COMPLEX, new HashSet<>(Puzzles.getNumOfPuzzlesPerCategory()));
        initializedPuzzleSelectionButtons.put(Puzzles.COLORFUL, new HashSet<>(Puzzles.getNumOfPuzzlesPerCategory()));

        this.rewardedAdPopup = new WatchAdPopup(
                context,
                paint,
                context.getString(R.string.freePuzzle),
                BitmapLoader.INSTANCE.getImage(context, R.drawable.puzzle_pink_512),
                () -> {
                    render();
                    onRewardedAdOffered(context, false);
                },
                () -> {
                    unlockPuzzle(context);
                    onRewardedAdOffered(context, true);
                },
                (error) -> onAdFailedToLoad(error, context),
                this::render
        );

        this.useKeyPopup = new UseKeyPopup(
                context,
                paint,
                context.getString(R.string.use_key_question),
                null,
                this::render,
                () -> {
                    unlockPuzzle(context);
                    useKey(context);
                }
        );

        this.initDone = true;
        updateScrolledButtons();
        render();
    }

    private PuzzleSelectionButtonView createPuzzleSelectionButtonView(Puzzles category, PuzzleReference puzzleReference, Paint paint, RectF bounds) {
        int color1 = category.getColorPack().getColor1();
        int color2 = category.getColorPack().getColor2();
        int color3 = category.getColorPack().getColor3();

        return new PuzzleSelectionButtonView(puzzleSelectionViewListener, puzzleReference, bounds, color1, color2, color3, (Context) puzzleSelectionViewListener, paint);
    }
}
