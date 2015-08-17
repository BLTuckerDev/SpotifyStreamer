package com.bltucker.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    public static void launch(Context context){
        Intent settingsIntent = new Intent(context, SettingsActivity.class);
        context.startActivity(settingsIntent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
