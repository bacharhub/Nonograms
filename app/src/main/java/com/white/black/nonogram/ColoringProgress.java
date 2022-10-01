package com.white.black.nonogram;

public class ColoringProgress {
    private int[][] coloringProgress;
    private BoardInputValue[][] boardInputValues;

    public ColoringProgress(int[][] coloringProgress, BoardInputValue[][] boardInputValues) {
        this.coloringProgress = coloringProgress;
        this.boardInputValues = boardInputValues;
    }

    public int[][] getColoringProgress() {
        return this.coloringProgress;
    }

    public BoardInputValue[][] getBoardInputValues() {
        return boardInputValues;
    }

    public void setColoringProgress(int[][] coloringProgress) {
        this.coloringProgress = coloringProgress;
    }

    public void setBoardInputValues(BoardInputValue[][] boardInputValues) {
        this.boardInputValues = boardInputValues;
    }

    @Override
    public boolean equals(Object cp) {
        ColoringProgress coloringProgress = (ColoringProgress) cp;
        for (int x = 0; x < this.coloringProgress.length; x++) {
            for (int y = 0; y < this.coloringProgress[0].length; y++) {
                if ((this.coloringProgress[x][y] != coloringProgress.getColoringProgress()[x][y]) ||
                        ((this.boardInputValues[x][y] == null && coloringProgress.getBoardInputValues()[x][y] != null && !(coloringProgress.getBoardInputValues()[x][y].equals(BoardInputValue.ERASER)))) ||
                        ((this.boardInputValues[x][y] != null && coloringProgress.getBoardInputValues()[x][y] == null && !(this.boardInputValues[x][y].equals(BoardInputValue.ERASER)))) ||
                        (this.boardInputValues[x][y] != null && coloringProgress.getBoardInputValues()[x][y] != null && !(this.boardInputValues[x][y].equals(coloringProgress.getBoardInputValues()[x][y])))) {
                    return false;
                }
            }
        }

        return true;
    }
}
