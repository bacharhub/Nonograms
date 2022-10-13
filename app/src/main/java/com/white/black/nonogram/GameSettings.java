package com.white.black.nonogram;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.white.black.nonogram.view.Appearance;

public enum GameSettings {

    INSTANCE;

    public enum Input {
        TOUCH,
        JOYSTICK
    }

    public enum Sound {
        ON,
        OFF
    }

    private Sound sound;
    private Input input;
    private Appearance appearance;

    GameSettings() {
        input = Input.TOUCH;
        appearance = Appearance.MINIMIZED;
    }

    public void initSound(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean soundOn = sharedPreferences.getBoolean("soundOn", true);
        if (soundOn) {
            sound = Sound.ON;
        } else {
            sound = Sound.OFF;
        }
    }

    public void onSoundButtonPressed(Context context) {
        if (sound.equals(Sound.ON)) {
            sound = Sound.OFF;
        } else {
            sound = Sound.ON;
        }

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        boolean soundOn = (sound == Sound.ON);
        prefsEditor.putBoolean("soundOn", soundOn);
        prefsEditor.apply();
    }

    public void onReviewButtonPressed(Context context) {
        ReviewManager manager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow((Activity) context, reviewInfo);
                flow.addOnCompleteListener(result -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(GameMonitoring.CHOOSE_TOOLBAR, GameMonitoring.VOTE_COMPLETE);
                    FirebaseAnalytics.getInstance(context).logEvent(GameMonitoring.GAME_EVENT, bundle);
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            }
        });
    }

    public void onInstructionsButtonPressed(Context context) {
        Uri uri = Uri.parse(context.getString(R.string.wikipedia));
        Intent wikipediaLink = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(wikipediaLink);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.wikipedia_is_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public void onIcons8ButtonPressed(Context context) {
        Uri uri = Uri.parse(context.getString(R.string.icons8_web));
        Intent wikipediaLink = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(wikipediaLink);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.wikipedia_is_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public void onJoystickButtonPressed() {
        input = Input.JOYSTICK;
    }

    public void onTouchButtonPressed() {
        input = Input.TOUCH;
    }

    public void onGameSettingsButtonPressed() {
        if (appearance.equals(Appearance.MINIMIZED)) {
            appearance = Appearance.MAXIMIZED;
        } else {
            appearance = Appearance.MINIMIZED;
        }
    }

    public Sound getSound() {
        return sound;
    }

    public Input getInput() {
        return input;
    }

    public Appearance getAppearance() {
        return appearance;
    }
}
