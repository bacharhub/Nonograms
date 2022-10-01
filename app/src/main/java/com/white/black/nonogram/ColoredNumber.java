package com.white.black.nonogram;

public class ColoredNumber {
    private final int val;
    private final int color;

    public ColoredNumber(int val, int color) {
        this.val = val;
        this.color = color;
    }

    public int getVal() {
        return val;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object coloredNumber) {
        return this.val == ((ColoredNumber)coloredNumber).getVal() && this.color == ((ColoredNumber)coloredNumber).getColor();
    }
}
