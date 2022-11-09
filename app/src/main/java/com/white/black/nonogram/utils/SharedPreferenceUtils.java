package com.white.black.nonogram.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.white.black.nonogram.Puzzles;

public class SharedPreferenceUtils {

    public static int coinsAvailable(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int coins = sharedPreferences.getInt("coins", Puzzles.numOfSolvedPuzzles(context) * 10);
        return coins;
    }

    public static void useCoins(Context context, int numOfCoinsToConsume) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("coins", coinsAvailable(context) - numOfCoinsToConsume);
        prefsEditor.apply();
    }

    public static void addToCoins(Context context, int numOfCoinsToAdd) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("coins", coinsAvailable(context) + numOfCoinsToAdd);
        prefsEditor.apply();
    }

    public static int cluesAvailable(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("clue_count", 0);
    }

    public static void addToClueCount(Context context, int numOfCluesToAdd) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("clue_count", cluesAvailable(context) + numOfCluesToAdd);
        prefsEditor.apply();
    }

    public static int keysAvailable(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("keys", 0);
    }

    public static void addToKeyCount(Context context, int numOfKeysToAdd) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("keys", keysAvailable(context) + numOfKeysToAdd);
        prefsEditor.apply();
    }

    public static long timePassedSinceLastRewardTimestamp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastRewardTimestamp = sharedPreferences.getLong("last_reward_timestamp", 0);
        return System.currentTimeMillis() - lastRewardTimestamp;
    }

    public static void updateLastRewardTimestamp(Context context) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putLong("last_reward_timestamp", System.currentTimeMillis());
        prefsEditor.apply();
    }

    public static int lastRewardDay(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("last_reward_day", 1);
    }

    public static void incrementLastRewardDay(Context context) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putInt("last_reward_day", lastRewardDay(context) + 1);
        prefsEditor.apply();
    }
}
