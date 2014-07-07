package com.vipulfb.Unjumble;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.google.android.gms.analytics.GoogleAnalytics;

public class Preference extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onStart(){
        super.onStart();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

}
