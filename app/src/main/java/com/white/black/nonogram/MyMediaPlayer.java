package com.white.black.nonogram;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.concurrent.ConcurrentHashMap;

public class MyMediaPlayer {

    private static final MyMediaPlayer myMediaPlayer = new MyMediaPlayer();
    private ConcurrentHashMap<String, MediaPlayerProxy> mediaPlayers;

    private class MediaPlayerProxy {
        private final MediaPlayer mediaPlayer;
        private final float recommendedVolume;

        MediaPlayerProxy(MediaPlayer mediaPlayer, float recommendedVolume) {
            this.mediaPlayer = mediaPlayer;
            this.recommendedVolume = recommendedVolume;
            unmute();
        }

        MediaPlayer getMediaPlayer() {
            return this.mediaPlayer;
        }

        void unmute() {
            if (mediaPlayer != null) {
                this.mediaPlayer.setVolume(recommendedVolume, recommendedVolume);
            }
        }
    }

    public static void play(String soundName) {
        if (GameSettings.INSTANCE.getSound().equals(GameSettings.Sound.ON)) {
            if (myMediaPlayer.mediaPlayers != null) {
                MediaPlayerProxy mediaPlayerProxy = myMediaPlayer.mediaPlayers.get(soundName);
                if (mediaPlayerProxy != null) {
                    MediaPlayer mediaPlayer = mediaPlayerProxy.getMediaPlayer();
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.seekTo(0);
                            mediaPlayer.pause();
                        }

                        mediaPlayer.start();
                    }
                }
            }
        }
    }

    public synchronized static void initialize(Context context) {
        try {
            if (myMediaPlayer.mediaPlayers == null) {
                myMediaPlayer.loadSound(context);
            }
        } catch (Exception ignored) {

        }
    }

    private void loadSound(Context context) {
        this.mediaPlayers = new ConcurrentHashMap<>();
        //this.mediaPlayers.put("achievement_unlocked", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.achievement_unlocked), 1f));
        //this.mediaPlayers.put("clock_ticking", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.clock_ticking), 1f));
        //this.mediaPlayers.put("collecting_coins", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.collecting_coins), 1f));
        this.mediaPlayers.put("page_selection", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.page_selection), 1f));
        this.mediaPlayers.put("select", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.select), 1f));
        this.mediaPlayers.put("blop", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.blop), 1f));
        this.mediaPlayers.put("victory", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.victory), 0.7f));
        /*MediaPlayer skyAndCloud = MediaPlayer.create(context, R.raw.sky_and_cloud);
        skyAndCloud.setOnCompletionListener(mp -> {
                    play("family_breakfast");
                }
        );

        this.mediaPlayers.put("sky_and_cloud", new MediaPlayerProxy(skyAndCloud, 0.4f));

        MediaPlayer familyBreakfast = MediaPlayer.create(context, R.raw.family_breakfast);
        familyBreakfast.setOnCompletionListener(mp -> {
                    play("acoustic_secrets");
                }
        );

        this.mediaPlayers.put("family_breakfast", new MediaPlayerProxy(familyBreakfast, 0.4f));

        MediaPlayer acousticSecrets = MediaPlayer.create(context, R.raw.acoustic_secrets);
        acousticSecrets.setOnCompletionListener(mp -> {
                    play("sky_and_cloud");
                }
        );

        this.mediaPlayers.put("acoustic_secrets", new MediaPlayerProxy(acousticSecrets, 0.4f));*/
    }
}
