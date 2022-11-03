package com.white.black.nonogram;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public enum PuzzleFactory {

    INSTANCE;

    private static final int COLOR_DISTANCE = 10500;
    private static final int MAX_PUZZLE_SIZE = 400;
    private static final int MAX_PUZZLE_TO_10_SIZE = 900;
    private static final int MAX_PUZZLE_TO_15_SIZE = 2025;
    private static final int MIN_PUZZLE_LENGTH = 5;

    public Puzzle createOfBitmap(int id, String name, Bitmap bitmap) {
        int[][] originalColors = extractImageColors(bitmap);
        int[][] filteredColors = filterColorsByDistance(originalColors);
        int[][] croppedFilteredColors = crop(filteredColors);
        List<Integer> colorSet = sortColorList(new LinkedList<>(new HashSet<>(colorMatrixToList(croppedFilteredColors))));
        Numbers numbers = createNumbersOfColors(croppedFilteredColors);
        Puzzle puzzle = new Puzzle(System.currentTimeMillis(), id, name, croppedFilteredColors, colorSet, numbers, false, null);
        addSubPuzzles(puzzle);

        return puzzle;
    }

    public List<ColoredNumber> getRowColors(int y, int[][] colors) {
        List row = new LinkedList<>();
        int color = colors[0][y];
        int sequenceSize = 0;
        for (int x = 0; x < colors.length; x++) {
            if (Color.alpha(color) == 0 || color != colors[x][y]) {
                if (sequenceSize > 0) {
                    row.add(new ColoredNumber(sequenceSize, color));
                    sequenceSize = 0;
                }

                color = colors[x][y];
                if (Color.alpha(color) != 0) {
                    sequenceSize++;
                }
            } else {
                sequenceSize++;
            }
        }

        if (sequenceSize > 0) {
            row.add(new ColoredNumber(sequenceSize, color));
        }

        return row;
    }

    public List<ColoredNumber> getColumnColors(int x, int[][] colors) {
        List column = new LinkedList<>();
        int color = colors[x][0];
        int sequenceSize = 0;
        for (int y = 0; y < colors[0].length; y++) {
            if (Color.alpha(color) == 0 || color != colors[x][y]) {
                if (sequenceSize > 0) {
                    column.add(new ColoredNumber(sequenceSize, color));
                    sequenceSize = 0;
                }

                color = colors[x][y];
                if (Color.alpha(color) != 0) {
                    sequenceSize++;
                }
            } else {
                sequenceSize++;
            }
        }

        if (sequenceSize > 0) {
            column.add(new ColoredNumber(sequenceSize, color));
        }

        return column;
    }

    private Numbers createNumbersOfColors(int[][] colors) {
        List<List<ColoredNumber>> rows = new ArrayList<>(colors[0].length);

        for (int y = 0; y < colors[0].length; y++) {
            rows.add(getRowColors(y, colors));
        }

        List<List<ColoredNumber>> columns = new ArrayList<>(colors.length);

        for (int x = 0; x < colors.length; x++) {
            columns.add(getColumnColors(x, colors));
        }

        return new Numbers(rows, columns);
    }

    private void addSubPuzzles(Puzzle puzzle) {
        int puzzleSize = puzzle.getWidth() * puzzle.getHeight();
        int puzzleGridWidth;
        int puzzleGridHeight;

        if (puzzleSize <= MAX_PUZZLE_SIZE) {
            return;
        }

        if (puzzle.getColorSet().size() > 1) { // colorful
            int widthTimes = (int)Math.ceil((double)puzzle.getWidth() / 15);
            int heightTimes = (int)Math.ceil((double)puzzle.getHeight() / 20);

            puzzleGridWidth = puzzle.getWidth() / widthTimes;
            puzzleGridHeight = puzzle.getHeight() / heightTimes;

            while (puzzle.getWidth() % puzzleGridWidth <= 10) {
                puzzleGridWidth++;
            }

            while (puzzle.getHeight() % puzzleGridHeight <= 10) {
                puzzleGridHeight++;
            }
        } else { // black
            if (puzzleSize <= 25 * 25) {
                puzzleGridWidth = puzzle.getWidth() / 2;
                puzzleGridHeight = puzzle.getHeight() / 2;
            } else if (puzzleSize <= MAX_PUZZLE_TO_10_SIZE) {
                puzzleGridWidth = puzzle.getWidth() / 3;
                puzzleGridHeight = puzzle.getHeight() / 3;
            } else if (puzzleSize <= MAX_PUZZLE_TO_15_SIZE) {
                puzzleGridWidth = puzzle.getWidth() / 3;
                puzzleGridHeight = puzzle.getHeight() / 3;
            } else if (puzzleSize <= 60 * 60) {
                puzzleGridWidth = puzzle.getWidth() / 3;
                puzzleGridHeight = puzzle.getHeight() / 3;
            } else if (puzzleSize <= 80 * 80) {
                puzzleGridWidth = puzzle.getWidth() / 4;
                puzzleGridHeight = puzzle.getHeight() / 4;
            } else {
                puzzleGridWidth = puzzle.getWidth() / 5;
                puzzleGridHeight = puzzle.getHeight() / 5;
            }
        }

        for (int y = 0; y < puzzle.getHeight(); y += puzzleGridHeight) {
            int y1 = y;
            int y2 = y1 + (puzzleGridHeight - 1);
            for (int x = 0; x < puzzle.getWidth(); x += puzzleGridWidth) {
                int x1 = x;
                int x2 = x1 + (puzzleGridWidth - 1);

                if (puzzle.getWidth() - x2 <= MIN_PUZZLE_LENGTH) {
                    x = puzzle.getWidth() - 1;
                    x2 = x;
                }

                if (puzzle.getHeight() - y2 <= MIN_PUZZLE_LENGTH) {
                    y2 = puzzle.getHeight() - 1;
                }

                int[][] subPuzzleFilteredColors = new int[x2 - x1 + 1][y2 - y1 + 1];
                for (int colorX = x1; colorX <= x2; colorX++) {
                    for (int colorY = y1; colorY <= y2; colorY++) {
                        subPuzzleFilteredColors[colorX - x1][colorY - y1] = puzzle.getFilteredColors()[colorX][colorY];
                    }
                }

                List<Integer> subPuzzleColorSet = sortColorList(new LinkedList<Integer>(new HashSet(colorMatrixToList(subPuzzleFilteredColors))));
                //Bitmap subPuzzleFilteredBitmap = createBitmapOfFilteredColors(subPuzzleFilteredColors);
                Numbers numbers = createNumbersOfColors(subPuzzleFilteredColors);
                Puzzle subPuzzle = new Puzzle(System.currentTimeMillis(), puzzle.getId(), puzzle.getName(), subPuzzleFilteredColors, subPuzzleColorSet, numbers, false, null);

                puzzle.addSubPuzzle(new SubPuzzle(subPuzzle, x1, y1));
            }

            if (y2 == puzzle.getHeight() - 1) {
                y = puzzle.getHeight();
            }
        }
    }

    private int[][] crop(int[][] filteredColors) {
        int x1;
        int x2;
        int y1;
        int y2;
        boolean leftFound = false;
        boolean topFound = false;
        boolean rightFound = false;
        boolean bottomFound = false;

        for (x1 = 0; x1 < filteredColors.length && !leftFound; x1++) {
            for (int y = 0; y < filteredColors[0].length; y++) {
                if (Color.alpha(filteredColors[x1][y]) != 0) {
                    leftFound = true;
                }
            }
        }


        for (x2 = filteredColors.length - 1; x2 >= 0 && !rightFound; x2--) {
            for (int y = 0; y < filteredColors[0].length; y++) {
                if (Color.alpha(filteredColors[x2][y]) != 0) {
                    rightFound = true;
                }
            }
        }


        for (y1 = 0; y1 < filteredColors[0].length && !topFound; y1++) {
            for (int x = 0; x < filteredColors.length; x++) {
                if (Color.alpha(filteredColors[x][y1]) != 0) {
                    topFound = true;
                }
            }
        }


        for (y2 = filteredColors[0].length - 1; y2 >= 0 && !bottomFound; y2--) {
            for (int x = 0; x < filteredColors.length; x++) {
                if (Color.alpha(filteredColors[x][y2]) != 0) {
                    bottomFound = true;
                }
            }
        }

        x1--;
        x2++;
        y1--;
        y2++;

        int[][] croppedFilteredColors = new int[x2 - x1 + 1][y2 - y1 + 1];

        for (int x = x1; x < x1 + croppedFilteredColors.length; x ++) {
            for (int y = y1; y < y1 + croppedFilteredColors[0].length; y++) {
                croppedFilteredColors[x - x1][y - y1] = filteredColors[x][y];
            }
        }

        return croppedFilteredColors;
    }

    private List<Integer> colorMatrixToList(int[][] matrix) {
        List<Integer> list = new LinkedList<>();

        for(int[] colorArr : matrix) {
            for(int color : colorArr) {
                if (Color.alpha(color) != 0) {
                    list.add(color);
                }
            }
        }

        return list;
    }

    private enum ColorGroup {
        BLACKS,
        WHITES,
        GRAYS,
        REDS,
        YELLOWS,
        GREENS,
        CYANS,
        BLUES,
        MAGNETAS
    }

    private ColorGroup getColorGroupByColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float hue = hsv[0];
        float sat = hsv[1];
        float lgt = hsv[2];

        if (lgt < 0.2) return ColorGroup.BLACKS;
        if (lgt > 0.8) return ColorGroup.WHITES;

        if (sat < 0.25) return ColorGroup.GRAYS;

        if (hue < 30) return ColorGroup.REDS;
        if (hue < 90) return ColorGroup.YELLOWS;
        if (hue < 150) return ColorGroup.GREENS;
        if (hue < 210) return ColorGroup.CYANS;
        if (hue < 270) return ColorGroup.BLUES;
        if (hue < 330) return ColorGroup.MAGNETAS;

        return ColorGroup.REDS;
    }

    private List<Integer> sortColorList(List<Integer> colors) {
        Map<ColorGroup, List<Integer>> colorSpaceListMap = new HashMap<>();
        for (Integer color : colors) {
            ColorGroup colorGroup = getColorGroupByColor(color);
            if (colorSpaceListMap.get(colorGroup) == null) {
                colorSpaceListMap.put(colorGroup, new LinkedList<>());
            }

            colorSpaceListMap.get(colorGroup).add(color);
        }

        for (List<Integer> colorList : colorSpaceListMap.values()) {
            Collections.sort(colorList, colorComparator);
        }

        List<Integer> sortedColors = new LinkedList<>();
        for (List<Integer> colorList : colorSpaceListMap.values()) {
            sortedColors.addAll(colorList);
        }

        return sortedColors;
    }

    private final Comparator<Integer> colorComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (getDistanceBetweenColors(o1, Color.WHITE) < getDistanceBetweenColors(o2, Color.WHITE)) {
                return -1;
            } else if (getDistanceBetweenColors(o1, Color.WHITE) > getDistanceBetweenColors(o2, Color.WHITE)) {
                return 1;
            }

            return 0;
        }
    };

    public Bitmap createBitmapOfFilteredColors(int[][] filteredColors) {
        Bitmap filteredBitmap = Bitmap.createBitmap(filteredColors.length, filteredColors[0].length, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < filteredColors.length; x++) {
            for (int y = 0; y < filteredColors[0].length; y++) {
                filteredBitmap.setPixel(x, y, filteredColors[x][y]);
            }
        }

        return filteredBitmap;
    }

    private int[][] extractImageColors(Bitmap bitmap) {
        int[][] colors = new int[bitmap.getWidth()][bitmap.getHeight()];

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                colors[x][y] = bitmap.getPixel(x, y);
            }
        }

        return colors;
    }

    private int[][] filterColorsByDistance(int[][] previousColors) {
        int[][] newColors = new int[previousColors.length][previousColors[0].length];

        for (int x = 0; x < previousColors.length; x++) {
            for (int y = 0; y < previousColors[0].length; y++) {
                newColors[x][y] = getNewColor(newColors ,previousColors[x][y]);
            }
        }

        return newColors;
    }

    private int getNewColor(int[][] filteredColors, int color) {
        if (Color.alpha(color) < 150 || (getDistanceBetweenColors(color, Color.WHITE) < COLOR_DISTANCE)) {
            color &= 0x00000000;
        } else {
            color |= 0xFF000000;
        }

        for (int x = 0; x < filteredColors.length; x++) {
            for (int y = 0; y < filteredColors[0].length; y++) {
                if (getDistanceBetweenColors(color, filteredColors[x][y]) < COLOR_DISTANCE) {
                    return filteredColors[x][y];
                }
            }
        }

        return color;
    }

    public int getDistanceBetweenColors(int firstColor, int secondColor) {
        int alphaDistance = (Color.alpha(firstColor) - Color.alpha(secondColor)) * (Color.alpha(firstColor) - Color.alpha(secondColor));
        int redDistance = 2 * (Color.red(firstColor) - Color.red(secondColor)) * (Color.red(firstColor) - Color.red(secondColor));
        int greenDistance = 4 * (Color.green(firstColor) - Color.green(secondColor)) * (Color.green(firstColor) - Color.green(secondColor));
        int blueDistance = 3 * (Color.blue(firstColor) - Color.blue(secondColor)) * (Color.blue(firstColor) - Color.blue(secondColor));

        return alphaDistance + redDistance + greenDistance + blueDistance;
    }
}
