package gpapez.sfen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 *
 * Created by Gregor on 2.8.2014.
 */
public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static Context sInstance;

    /**
     * CONTRUCTOR
     *
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sInstance = this;

        addPreferencesFromResource(R.xml.preferences);

//        findPreference("launchAtBoot").setEnabled(false);


        /**
         * cell tower listeners
         */
        Preference dialogPreference;

        dialogPreference = (Preference) getPreferenceScreen()
                .findPreference("openCellTowersHistoryDialog");
        dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Cell.openCellTowersHistoryDialog(sInstance);

                return true;
            }
        });


        dialogPreference = (Preference) getPreferenceScreen()
                .findPreference("openCellTowerRecordDialog");
        dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Cell.openCellTowerRecordDialog(sInstance);

                return true;
            }
        });



    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        System.out.println("preference "+ key +" was changed.");

        /**
         * if we pressed rootEnable and is now SELECTED..
         */
        if (key.equals("rootEnable") &&
                Preferences.getSharedPreferences().getBoolean("rootEnable", false)) {

            /**
             * invoke root requirement for Sfen
             */
            Sudo sudo = new Sudo();
            sudo.isRootEnabled();


        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }





}
