package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Gregor on 14.8.2014.
 */
public class ShortcutEventActivity extends Activity {


    private static Context sInstance;
    private ArrayList<Event> events;

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

            int eventID = extras.getInt("EVENT_TO_RUN");

            Event e = Event.returnEventByUniqueID(eventID);

            if (e != null) {

                BackgroundService.getInstance().startSingleEvent(e);
                /**
                 * update notification
                 */
                BackgroundService.getInstance().mNotification.showNotification();

            }

            else {

                Util.showMessageBox(getString(R.string.event_does_not_exist),
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

            Util.showMessageBox(getString(R.string.cannot_retrieve_preferences), false);
            finish();
            return;

        }


        events = (ArrayList<Event>) mPreferences
                .getPreferences("events", Preferences.REQUEST_TYPE.EVENTS);

        if (events == null) {

            Util.showMessageBox(getString(R.string.no_events_stored), false);
            finish();
            return;

        }


        /**
         * show events selection dialog
         */
        showEventDialog();


    }

    private void showEventDialog() {



        String[] eventNames = new String[events.size()];

        for (int i = 0; i < events.size(); i++) {

            eventNames[i] = events.get(i).getName();

        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setItems(eventNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        Intent.ShortcutIconResource icon =
                                Intent.ShortcutIconResource.fromContext(sInstance, R.drawable.ic_launcher);

                        Intent intent = new Intent();

                        Intent launchIntent = new Intent(sInstance, ShortcutEventActivity.class);
                        launchIntent.putExtra("EVENT_TO_RUN", events.get(i).getUniqueID());

                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, events.get(i).getName());
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

                        setResult(RESULT_OK, intent);
                        finish();

                    }
                })
                .setTitle(getString(R.string.select_event));


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
