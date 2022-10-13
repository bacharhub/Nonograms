package com.white.black.nonogram;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
        Uri uri = Uri.parse("market://details?id=com.white.black.nonogram");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.rate_us_next_time), Toast.LENGTH_LONG).show();
        }
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
