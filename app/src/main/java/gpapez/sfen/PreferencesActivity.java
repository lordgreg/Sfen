package gpapez.sfen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *
 * Created by Gregor on 2.8.2014.
 */
public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * CONTRUCTOR
     *
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // TODO: disable non-working preferences
        findPreference("launchAtBoot").setEnabled(false);
        findPreference("wakelockTimer").setEnabled(false);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        System.out.println("preference "+ key +" was changed.");
    }
}
