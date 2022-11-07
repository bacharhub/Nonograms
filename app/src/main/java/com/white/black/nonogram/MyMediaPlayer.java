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
        this.mediaPlayers.put("page_selection", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.page_selection), 1f));
        this.mediaPlayers.put("select", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.select), 1f));
        this.mediaPlayers.put("blop", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.blop), 1f));
        this.mediaPlayers.put("victory", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.victory), 0.7f));
        this.mediaPlayers.put("purchase", new MediaPlayerProxy(MediaPlayer.create(context, R.raw.purchase), 0.7f));
    }
}
