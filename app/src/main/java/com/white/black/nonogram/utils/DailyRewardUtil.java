package com.white.black.nonogram.utils;

import static com.white.black.nonogram.utils.SharedPreferenceUtils.addToClueCount;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.addToCoins;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.addToKeyCount;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.incrementLastRewardDay;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.lastRewardDay;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.lastRewardTimestamp;
import static com.white.black.nonogram.utils.SharedPreferenceUtils.updateLastRewardTimestamp;

import android.content.Context;
import android.util.Pair;

import com.white.black.nonogram.RewardType;

public class DailyRewardUtil {

    private final static long DAILY_REWARD_INTERVAL = 3600 * 24;

    public Pair<RewardType, Integer> getTodayReward(Context context) {
        return getRewardByDay(lastRewardDay(context));
    }

    public Pair<RewardType, Integer> getTomorrowReward(Context context) {
        return getRewardByDay(lastRewardDay(context) + 1);
    }

    public Pair<RewardType, Integer> getDayAfterTomorrowReward(Context context) {
        return getRewardByDay(lastRewardDay(context) + 2);
    }

    public Pair<RewardType, Integer> getRewardByDay(int day) {
        int value = 50 + (day - 1) * 5;

        if (value % 30 == 0) {
            return new Pair<>(RewardType.KEYS, value / 30);
        }

        if (value % 10 == 0) {
            return new Pair<>(RewardType.CLUES, value / 10);
        }

        return new Pair<>(RewardType.COINS, value);
    }

    public boolean isTimeForDailyReward(Context context) {
        return lastRewardTimestamp(context) > DAILY_REWARD_INTERVAL;
    }

    public void claimReward(Context context) {
        Pair<RewardType, Integer> reward = getRewardByDay(lastRewardDay(context));

        switch (reward.first) {
            case COINS: addToCoins(context, reward.second); break;
            case CLUES: addToClueCount(context, reward.second); break;
            case KEYS: addToKeyCount(context, reward.second); break;
        }

        updateLastRewardTimestamp(context);
        incrementLastRewardDay(context);
    }
}
