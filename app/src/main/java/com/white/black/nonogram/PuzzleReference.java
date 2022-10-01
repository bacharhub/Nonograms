package com.white.black.nonogram;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class PuzzleReference {

    private Puzzle puzzle;
    private final String puzzleName;
    private int puzzleId;
    private int imageId;
    private PuzzleReference nextPuzzleNode;
    private Puzzle.PuzzleClass puzzleClass;
    private final String uniqueId;

    public PuzzleReference(Puzzle.PuzzleClass puzzleClass, String puzzleName, int puzzleId, int imageId) {
        this.puzzleName = puzzleName;
        this.puzzleId = puzzleId;
        this.imageId = imageId;
        this.puzzleClass = puzzleClass;
        this.uniqueId = puzzleName + "_" + puzzleId;
    }

    public void load(Context context) {
        if (!isLoaded()) {
            synchronized (puzzleName) {
                if (!isLoaded()) {
                    readPuzzle(context);
                }
            }
        }
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void release() {
        this.puzzle = null;
    }

    public boolean isLoaded() {
        return puzzle != null;
    }

    public Puzzle getPuzzle(Context context) {
        load(context); // loads only if is not loaded yet
        return puzzle;
    }

    public PuzzleReference getNextPuzzleNode() {
        return nextPuzzleNode;
    }

    public void setNextPuzzleNode(PuzzleReference puzzleNode) {
        this.nextPuzzleNode = puzzleNode;
    }

    private boolean readFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getUniqueId(), null);

        if (json == null) {
            return false;
        }

        puzzle = gson.fromJson(json, Puzzle.class);
        return true;
    }

    public void writeToSharedPreferences(Context context) {
        try {
            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            Gson gson = new Gson();
            String json = gson.toJson(puzzle);
            prefsEditor.putString(puzzle.getUniqueId(), json);
            prefsEditor.apply();
        } catch (Exception ignored) {

        }
    }

    private void readPuzzle(Context context) {
        if (!readFromSharedPreferences(context)) {
            puzzle = PuzzleFactory.INSTANCE.createOfBitmap(puzzleId, puzzleName, BitmapLoader.INSTANCE.getImage(context, imageId));
            puzzle.setPuzzleClass(puzzleClass);
            writeToSharedPreferences(context);
        }
    }
}
