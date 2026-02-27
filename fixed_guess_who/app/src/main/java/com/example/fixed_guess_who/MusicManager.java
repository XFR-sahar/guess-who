package com.example.fixed_guess_who;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class MusicManager {

    private static MediaPlayer mediaPlayer;
    private static SoundPool soundPool;

    private static int soundVictory;
    private static int soundLose;
    private static int soundCoins;
    private static int sound_login;
    private static int activityCount = 0;

    public static void initSounds(Context context) {
        if (soundPool == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();

            sound_login = soundPool.load(context, R.raw.login_sound, 1);
            soundVictory = soundPool.load(context, R.raw.victory_sound, 1);
            soundLose = soundPool.load(context, R.raw.lose_sound, 1);
            soundCoins = soundPool.load(context, R.raw.coin_sound, 1);
        }
    }

    public static void onActivityStarted(Context context) {
        activityCount++;
        playBackgroundMusic(context);
    }

    public static void onActivityStopped() {
        activityCount--;
        if (activityCount <= 0) {
            activityCount = 0;
            pauseBackgroundMusic();
        }
    }

    private static boolean isSfxEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE);
        return prefs.getBoolean("sfx_on", true);
    }

    private static boolean isMusicEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("GameSettings", Context.MODE_PRIVATE);
        return prefs.getBoolean("music_on", true);
    }

    public static void sound_login(Context context) {
        if (isSfxEnabled(context) && soundPool != null) soundPool.play(sound_login, 1, 1, 0, 0, 1);
    }

    public static void playVictory(Context context) {
        if (isSfxEnabled(context) && soundPool != null) soundPool.play(soundVictory, 1, 1, 0, 0, 1);
    }

    public static void playLose(Context context) {
        if (isSfxEnabled(context) && soundPool != null) soundPool.play(soundLose, 1, 1, 0, 0, 1);
    }

    public static void playCoins(Context context) {
        if (isSfxEnabled(context) && soundPool != null) soundPool.play(soundCoins, 1, 1, 0, 0, 1);
    }

    // --- מוזיקת רקע ---

    public static void playBackgroundMusic(Context context) {
        if (!isMusicEnabled(context)) {
            pauseBackgroundMusic();
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background_music);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public static void pauseBackgroundMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}