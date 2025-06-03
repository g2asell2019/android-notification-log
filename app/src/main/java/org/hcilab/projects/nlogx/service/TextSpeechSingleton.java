package org.hcilab.projects.nlogx.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.Entity.Country;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextSpeechSingleton {
    private static final String TAG = "MyTell";
    public static TextToSpeech tts;
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static SharedPreferences sharedPreferences;

    private static SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (sharedPreferences, key) -> {
                if (key.equals(Const.PREF_SPEECH_LANG) && isInitialized.get()) {
                    tts.setLanguage(getCurrentSpeechLanguage());
                }
            };
    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isInitializing.get()) {
            Log.d(TAG, "TTS already initializing");
            return;
        }

        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down TTS", e);
            }
        }

        isInitializing.set(true);
        isInitialized.set(false);

        mainHandler.post(() -> {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "TTS initialized successfully");
                    tts.setLanguage(getCurrentSpeechLanguage());
                    isInitialized.set(true);
                } else {
                    Log.e(TAG, "TTS initialization failed: " + status);
                }
                isInitializing.set(false);
            });

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "TTS started: " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "TTS completed: " + utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e(TAG, "TTS error: " + utteranceId);
                }
            });
        });

        // Missing from init() method:
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public static Locale getCurrentSpeechLanguage(){
        String countryName = sharedPreferences.getString(Const.PREF_SPEECH_LANG, "");
        Country country = Const.currentAvailableLocale.get(countryName);
        return country == null ? Const.DEFAULT_LOCALE
                : country.getLocale();
    }
    public static boolean speak(Context context, String text) {
        if (!isInitialized.get()) {
            Log.e(TAG, "TTS not initialized yet");
            // Try to initialize
            if (!isInitializing.get()) {
                init(context);
            }
            return false;
        }

        try {
            // Check if device is in silent mode
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                    am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                Log.d(TAG, "Device in silent/vibrate mode, not speaking");
                return false;
            }

            String utteranceId = UUID.randomUUID().toString();
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            Log.d(TAG, "speak() result: " + result);
            return result == TextToSpeech.SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text", e);
            return false;
        }
    }

    public static void shutdown() {
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
                // Missing from shutdown() method:
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
                isInitialized.set(false);
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down TTS", e);
            }
        }
    }
}