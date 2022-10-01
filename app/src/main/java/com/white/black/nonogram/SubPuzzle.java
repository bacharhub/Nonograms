package com.white.black.nonogram;

public class SubPuzzle {
    private final Puzzle puzzle;
    private final int x;
    private final int y;

    public SubPuzzle(Puzzle puzzle, int x, int y) {
        this.puzzle = puzzle;
        this.x = x;
        this.y = y;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
