package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Gregor on 10.8.2014.
 */
public class ShortcutProfileActivity extends Activity {

    private static Context sInstance;
    private ArrayList<Profile> profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * instance set
         */
        sInstance = this;

        /**
         * if we have extras and a proper ID, we will run Profile settings & actions
         */
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            int profileID = extras.getInt("PROFILE_TO_RUN");

            Profile p = Profile.getProfileByUniqueID(profileID);

            if (p != null) {

                /**
                 * update active profile
                 */
                Profile.updateActiveProfile(profileID);

                /**
                 * run settings & actions
                 */
                BackgroundService.getInstance().runProfileSettings(p);
                BackgroundService.getInstance().runProfileActions(p);

            }

            else {

                Util.showMessageBox("Profile does not exist. Delete shortcut and create new one!",
                        false);

            }


            /**
             * extras was found, don't continue anything else now..
             */
            finish();
            return ;

        }



        /**
         * get profiles from preferences
         */
        Preferences mPreferences = new Preferences(Main.getInstance());

        if (mPreferences == null) {

            Util.showMessageBox("Cannot retrieve preferences...", false);
            finish();
            return;

        }


        profiles = (ArrayList<Profile>) mPreferences
                .getPreferences("profiles", Preferences.REQUEST_TYPE.PROFILES);

        if (profiles == null) {

            Util.showMessageBox("Sfen doesn't have any profiles stored.", false);
            finish();
            return;

        }


        /**
         * show profiles selection dialog
         */
        showProfileDialog();


    }

    private void showProfileDialog() {



        String[] profileNames = new String[profiles.size()];

        for (int i = 0; i < profiles.size(); i++) {

            profileNames[i] = profiles.get(i).getName();

        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setItems(profileNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        Intent.ShortcutIconResource icon =
                                Intent.ShortcutIconResource.fromContext(sInstance, profiles.get(i).getIcon());

                        Intent intent = new Intent();

                        Intent launchIntent = new Intent(sInstance, ShortcutProfileActivity.class);
                        launchIntent.putExtra("PROFILE_TO_RUN", profiles.get(i).getUniqueID());

                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profiles.get(i).getName());
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

                        setResult(RESULT_OK, intent);
                        finish();

                    }
                })
                .setTitle("Select profile");


        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();

    }


}
