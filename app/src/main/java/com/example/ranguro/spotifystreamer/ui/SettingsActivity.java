package com.example.ranguro.spotifystreamer.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.ranguro.spotifystreamer.R;

public class SettingsActivity extends PreferenceActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }


}
