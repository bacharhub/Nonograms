package com.white.black.nonogram;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Puzzle {

    public PuzzleClass getPuzzleClass() {
        return puzzleClass;
    }

    public void setPuzzleClass(PuzzleClass puzzlePuzzleClass) {
        this.puzzleClass = puzzlePuzzleClass;
    }

    private PuzzleClass puzzleClass;

    public enum PuzzleClass {
        VIP,
        FREE
    }

    private final static long NEW_PERIOD = 3600 * 24 * 14 * 1000; //14 days
    private final long puzzleFirstLoadTime;
    private static final long INTERVAL_BETWEEN_CLUES_MILLISECONDS = 30 * 1000;
    private final int id;
    private final String name;

    private final int[][] filteredColors;
    private final List<Integer> colorSet;
    private final List<SubPuzzle> subPuzzles;
    private long solvingTime = 0;
    private long lastTimeSolvingTimeIncreased;
    private final Numbers numbers;

    private ColoringProgress coloringProgress;
    private FixedStack<ColoringProgress> fixedUndo = new FixedStack<>(10);
    private final Stack<ColoringProgress> redo = new Stack<>();
    private long lastClueUsedTime;
    private boolean usingHintOnFirstStep;

    public void setUsingHintOnFirstStep(boolean usingHintOnFirstStep) {
        this.usingHintOnFirstStep = usingHintOnFirstStep;
    }

    public boolean isUsingHintOnFirstStep() {
        return usingHintOnFirstStep;
    }

    public int getId() {
        return id;
    }

    public FixedStack<ColoringProgress> getFixedUndo() {
        if (fixedUndo == null) {
            fixedUndo = new FixedStack<>(20);
        }

        return fixedUndo;
    }

    public Puzzle (long puzzleFirstLoadTime, int id, String name/*, Bitmap filteredBitmap*/, int[][] filteredColors, List<Integer> colorSet, Numbers numbers) {
        this.puzzleFirstLoadTime = puzzleFirstLoadTime;
        this.id = id;
        this.name = name;
        this.filteredColors = filteredColors;
        this.colorSet = colorSet;
        this.subPuzzles = new LinkedList<>();
        this.numbers = numbers;
    }

    private void initializeColoringProgress() {
        this.coloringProgress = new ColoringProgress(new int[filteredColors.length][filteredColors[0].length], new BoardInputValue[filteredColors.length][filteredColors[0].length]);
    }

    public boolean isNew() {
        return System.currentTimeMillis() < this.puzzleFirstLoadTime + NEW_PERIOD;
    }

    private void initializeSolvingTime() {
        this.solvingTime = 0;
    }

    public void increaseSolvingTime() {
        this.solvingTime += (System.currentTimeMillis() - lastTimeSolvingTimeIncreased);
        this.lastTimeSolvingTimeIncreased = System.currentTimeMillis();
    }

    public void setLastTimeSolvingTimeIncreased(long lastTimeSolvingTimeIncreased) {
        this.lastTimeSolvingTimeIncreased = lastTimeSolvingTimeIncreased;
    }

    public long getLastClueUsedTime() {
        return lastClueUsedTime;
    }

    public static long getIntervalBetweenCluesMilliseconds() {
        return INTERVAL_BETWEEN_CLUES_MILLISECONDS;
    }

    public boolean isClueAvailable() {
        return System.currentTimeMillis() > lastClueUsedTime + INTERVAL_BETWEEN_CLUES_MILLISECONDS;
    }

    public void setClueNotAvailable() {
        lastClueUsedTime = System.currentTimeMillis();
    }

    private void setClueAvailable() {
        lastClueUsedTime = 0;
    }

    public void putBoardInputValue(int x, int y, BoardInputValue boardInputValue) {
        if (!hasColoringProgress()) {
            initializeColoringProgress();
        }

        coloringProgress.getBoardInputValues()[x][y] = boardInputValue;
    }

    public BoardInputValue getBoardInputValue(int x, int y) {
        if (!hasColoringProgress()) {
            initializeColoringProgress();
        }

        return coloringProgress.getBoardInputValues()[x][y];
    }

    public int[][] getColoringProgressColors() {
        return coloringProgress.getColoringProgress();
    }

    private long getSolvingTime() {
        return solvingTime;
    }

    public Numbers getNumbers() {
        return this.numbers;
    }

    public boolean hasColoringProgress() {
        return coloringProgress != null;
    }

    public void fillPermanentDisqualify() {
        if (!hasColoringProgress()) {
            initializeColoringProgress();
        }

        for (int x = 0; x < getNumbers().getColumns().size(); x++) {
            for (int y = 0; y < getNumbers().getRows().size(); y++) {
                if (getNumbers().getRows().get(y).size() == 0 || getNumbers().getColumns().get(x).size() == 0) {
                    putBoardInputValue(x, y, BoardInputValue.PERMANENT_DISQUALIFY);
                }
            }
        }
    }

    public ColoringProgress copyColoringProgress() {
        if (coloringProgress == null) {
            initializeColoringProgress();
        }

        int[][] copiedColoringProgress = new int[coloringProgress.getColoringProgress().length][coloringProgress.getColoringProgress()[0].length];
        for (int x = 0; x < coloringProgress.getColoringProgress().length; x++) {
            for (int y = 0; y < coloringProgress.getColoringProgress()[0].length; y++) {
                copiedColoringProgress[x][y] = coloringProgress.getColoringProgress()[x][y];
            }
        }

        BoardInputValue[][] copiedBoardInputValue = new BoardInputValue[coloringProgress.getBoardInputValues().length][coloringProgress.getBoardInputValues()[0].length];
        for (int x = 0; x < coloringProgress.getBoardInputValues().length; x++) {
            for (int y = 0; y < coloringProgress.getBoardInputValues()[0].length; y++) {
                copiedBoardInputValue[x][y] = coloringProgress.getBoardInputValues()[x][y];
            }
        }

        return new ColoringProgress(copiedColoringProgress, copiedBoardInputValue);
    }

    public void addColoringProgressToUndo(ColoringProgress coloringProgress) {
        getFixedUndo().push(coloringProgress);
        redo.clear();
    }

    public boolean isUndoable() {
        return !getFixedUndo().empty();
    }

    public void setUndo() {
        if (isUndoable()) {
            ColoringProgress poppedColoringProgress = getFixedUndo().pop();
            int[][] lastColoringProgress = poppedColoringProgress.getColoringProgress();
            BoardInputValue[][] lastBoardInputValue = poppedColoringProgress.getBoardInputValues();
            redo.push(copyColoringProgress());
            coloringProgress.setColoringProgress(lastColoringProgress);
            coloringProgress.setBoardInputValues(lastBoardInputValue);

            if (getFixedUndo().empty()) {
                usingHintOnFirstStep = false;
            }
        }
    }

    public boolean isRedoable() {
        return !redo.empty();
    }

    public void setRedo() {
        if (isRedoable()) {
            ColoringProgress poppedColoringProgress = redo.pop();
            int[][] lastColoringProgress = poppedColoringProgress.getColoringProgress();
            BoardInputValue[][] lastBoardInputValue = poppedColoringProgress.getBoardInputValues();
            getFixedUndo().push(copyColoringProgress());
            coloringProgress.setColoringProgress(lastColoringProgress);
            coloringProgress.setBoardInputValues(lastBoardInputValue);
        }
    }

    public String getSolvingTimeHumanFormat() {
        long totalSolvingTime = solvingTime;
        if (subPuzzles.size() > 0) {
            for (SubPuzzle subPuzzle : subPuzzles) {
                totalSolvingTime += subPuzzle.getPuzzle().getSolvingTime();
            }
        }

        int seconds = (int) (totalSolvingTime / 1000) % 60 ;
        int minutes = (int) ((totalSolvingTime / (1000*60)) % 60);
        int hours   = (int) ((totalSolvingTime / (1000*60*60)) % 24);

        StringBuilder duration = new StringBuilder();
        if (hours > 0) {
            if (hours <= 9) {
                duration.append("0");
            }

            duration.append(hours).append(":");
        }

        if (minutes <= 9) {
            duration.append("0");
        }

        duration.append(minutes).append(":");

        if (seconds <= 9) {
            duration.append("0");
        }

        duration.append(seconds);

        return duration.toString();
    }

    public int getWidth() {
        return filteredColors.length;
    }

    public int getHeight() {
        return filteredColors[0].length;
    }

    public void addSubPuzzle(SubPuzzle subPuzzle) {
        this.subPuzzles.add(subPuzzle);
    }

    public List<SubPuzzle> getSubPuzzles() {
        return subPuzzles;
    }

    public Bitmap getFilteredBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(filteredColors.length, filteredColors[0].length, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < filteredColors.length; x++) {
            for (int y = 0; y < filteredColors[0].length; y++) {
                bitmap.setPixel(x, y, filteredColors[x][y]);
            }
        }

        return bitmap;
    }

    public int[][] getFilteredColors() {
        return filteredColors;
    }

    public List<Integer> getColorSet() {
        return colorSet;
    }

    public void finish() {
        if (!hasColoringProgress()) {
            initializeColoringProgress();
        }

        for(int x = 0; x < this.filteredColors.length; x++) {
            for (int y = 0; y < this.filteredColors[0].length; y++) {
                if (BoardInputValue.BRUSH.equals(this.coloringProgress.getBoardInputValues()[x][y])) {
                    this.coloringProgress.getBoardInputValues()[x][y] = null;
                }
            }
        }

        for(int x = 0; x < this.filteredColors.length; x++) {
            for (int y = 0; y < this.filteredColors[0].length; y++) {
                this.coloringProgress.getColoringProgress()[x][y] = this.filteredColors[x][y];
                if (this.filteredColors[x][y] != 0) {
                    this.coloringProgress.getBoardInputValues()[x][y] = BoardInputValue.BRUSH;
                }
            }
        }
    }

    public void colorAtPoint(int x, int y, int color) {
        if (!hasColoringProgress()) {
            initializeColoringProgress();
        }

        this.coloringProgress.getColoringProgress()[x][y] = color;
    }

    public boolean isDone() {
        if (subPuzzles.size() > 0) {
            for (SubPuzzle subPuzzle : subPuzzles) {
                if (!subPuzzle.getPuzzle().isDone()) {
                    return false;
                }
            }

            return true;
        }

        return checkCompletion();
    }

    private boolean checkCompletion() {
        if (colorSet.size() == 0) {
            return true;
        }

        if (!hasColoringProgress()) {
            return false;
        }

        int rowCounter = 0;
        for (List<ColoredNumber> row : getNumbers().getRows()) {
            if (!row.equals(PuzzleFactory.INSTANCE.getRowColors(rowCounter++, getColoringProgressColors()))) {
                return false;
            }
        }

        int columnCounter = 0;
        for (List<ColoredNumber> column : getNumbers().getColumns()) {
            if (!column.equals(PuzzleFactory.INSTANCE.getColumnColors(columnCounter++, getColoringProgressColors()))) {
                return false;
            }
        }

        return true;
    }

    public boolean isPartiallyDone() {
        if (!hasColoringProgress()) {
            return false;
        }

        for(int x = 0; x < this.filteredColors.length; x++) {
            for (int y = 0; y < this.filteredColors[0].length; y++) {
                if (Color.alpha(this.coloringProgress.getColoringProgress()[x][y]) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public String getUniqueId() {
        return name + "_" + id;
    }

    public void clearUndoRedo() {
        getFixedUndo().clear();
        redo.clear();
    }

    public void clear() {
        this.coloringProgress = null;
        fillPermanentDisqualify();
        clearUndoRedo();
        setClueAvailable();
        this.initializeSolvingTime();

        if (subPuzzles.size() > 0) {
            for (SubPuzzle subPuzzle : subPuzzles) {
                subPuzzle.getPuzzle().clear();
            }
        }
    }
}
