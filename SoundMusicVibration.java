package com.vipulfb.Unjumble;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class SoundMusicVibration {
    private Context mContext;
    public Vibrator vibrate;

    public SoundMusicVibration(Context context) {
        mContext = context;
        vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public boolean isSoundActive(){
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return getPrefs.getBoolean("sound",true);
    }
//    public boolean isMusicActive(){
//        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        return getPrefs.getBoolean("music",true);
//    }
    public boolean isVibrationActive(){
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return getPrefs.getBoolean("vibration",true);
    }

    public boolean isOrientationActive(){
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return getPrefs.getBoolean("orientation",true);
    }

}
