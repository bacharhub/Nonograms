package com.white.black.nonogram;

import java.util.List;

public class Numbers {
    private final List<List<ColoredNumber>> rows;
    private final List<List<ColoredNumber>> columns;

    public Numbers(List<List<ColoredNumber>> rows, List<List<ColoredNumber>> columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public List<List<ColoredNumber>> getRows() {
        return rows;
    }

    public List<List<ColoredNumber>> getColumns() {
        return columns;
    }
}
