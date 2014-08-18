package gpapez.sfen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Profile Window
 */
public class FragmentEvent extends Fragment {

    /**
     * VARIABLES
     */

    // singleton
    private static FragmentEvent sInstance;

    // container for our profiles
    private ViewGroup mContainerView;

    // our fragment view
    private View mView;

    /**
     * preferences var
     */
    Preferences mPreferences;

    /**
     * events arr
     */
    ArrayList<Event> events = new ArrayList<Event>();



    /**
     * CONSTRUCTOR
     */
    public FragmentEvent() {


    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        /**
//         * save fragment state!
//         */
//        getFragmentManager()
//                .putFragment(outState, FragmentEvent.class.getName(),
//                        sInstance);
//
//
//    }

    //@Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * set singleton
         */
        sInstance = this;

        /**
         * set View
         */
        mView = inflater.inflate(R.layout.activity_main_events, container, false);


        /**
         * refresh events view
         *
         * this is our first run, let set all events running boolean to false
         *
         * if bgService is already running, don't touch anything!
         */
        refreshEventsView();


        /**
         * retrieve events info
         */
        mPreferences = new Preferences(Main.getInstance());
        events = (ArrayList<Event>) mPreferences.getPreferences("events", Preferences.REQUEST_TYPE.EVENTS);





        return mView;
    }


    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static FragmentEvent getInstance() {
        if (sInstance == null) {
            return new FragmentEvent();
        }
        else
            return sInstance;
    }


    /**
     * after resuming the app, we will usually come to the main activity with nothing on it.
     * this function will take care of that! go through events array and fill it up, yo?
     */
    public void refreshEventsView() {

        mContainerView = (ViewGroup) mView.findViewById(R.id.container_events);
        mContainerView.removeAllViews();


        // if events array is empty, show "add new event" textview
        if (BackgroundService.getInstance().events.size() == 0) {
            //Main.getInstance().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            mView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        }
        else {
            mView.findViewById(android.R.id.empty).setVisibility(View.GONE);
        }

        // fill the events from array
        for (final Event e : BackgroundService.getInstance().events) {
            final ViewGroup newRow = (ViewGroup) LayoutInflater.from(Main.getInstance()).inflate(
                    R.layout.main_single_item, mContainerView, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(e.getName());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                    (e.isRunning()) ? getString(R.string.active) :
                            ((e.isEnabled() ? sInstance.getString(R.string.enabled) : sInstance.getString(R.string.disabled)))
            );

            // change color depending on if event is running
            if (e.isRunning())
                ((TextView) newRow.findViewById(android.R.id.text1)).setTextColor(Color.BLUE);
            // or if it is disabled
            if (!e.isEnabled())
                ((TextView) newRow.findViewById(android.R.id.text1)).setTextColor(Color.GRAY);

            // add on long press event (text1, text2, event_container
            newRow.findViewById(android.R.id.text1).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickSingleEvent(e, newRow);
                    return true;
                }
            });

            newRow.findViewById(android.R.id.text2).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickSingleEvent(e, newRow);
                    return true;
                }
            });


            newRow.findViewById(R.id.event_container).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onLongClickSingleEvent(e, newRow);
                    return true;
                }
            });

            // EDIT EVENT > open Event activity and pass event object to it!
            newRow.findViewById(R.id.single_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleEvent(e);
                }
            });

            // same goes with text1, text2 and event_container
            newRow.findViewById(android.R.id.text1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleEvent(e);
                }
            });
            newRow.findViewById(android.R.id.text2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleEvent(e);
                }
            });
            newRow.findViewById(R.id.event_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleEvent(e);
                }
            });


            // add delete button event
            /*
            newRow.findViewById(R.id.single_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // delete row AND spot in events
                    mContainerView.removeView(newRow);
                    events.remove(e);
                    updateEventsFromPreferences();
                }
            });*/

            // add new row to container
            mContainerView.addView(newRow, 0);

            // update preferences
            BackgroundService.getInstance().mPreferences.setPreferences("events",
                    BackgroundService.getInstance().events);
            //updateEventsFromPreferences();

        }

    }


    /**
     * CLICK ON SINGLE EVENT OPENS EVENT ACTIVITY
     *
     * @param e Event
     */
    private void onClickSingleEvent(Event e) {
        Intent i = new Intent(Main.getInstance(), EventActivity.class);
        i.putExtra("sEvent", (new Gson().toJson(e)));
        i.putExtra("sEventIndexKey", BackgroundService.getInstance().events.indexOf(e));
        startActivity(i);
    }

    /**
     * LONG CLICK SINGLE EVENT
     *
     * should open popup with options.
     */
    private void onLongClickSingleEvent(final Event e, final ViewGroup newRow) {
        // array of options
        final String[] sOptions = {sInstance.getString(R.string.edit), ((e.isEnabled()) ? 
                sInstance.getString(R.string.disable) : sInstance.getString(R.string.enable)), 
                sInstance.getString(R.string.delete)};
        // show dialog with more options for single event
        final AlertDialog.Builder builder = new AlertDialog.Builder(Main.getInstance());
        builder
                //.setMessage("Service will be stopped and Events won't be triggered.\n\nAre you sure?")
                //.setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle(e.getName())
                .setItems(sOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // The 'which' argument contains the index position
                        // of the selected item
                        // 0 edit, 1 enable/disable, 2 delete
                        if (which == 0) {

                            onClickSingleEvent(e);

                        }
                        if (which == 1) {
                            if (e.isEnabled()) {
                                e.setEnabled(false);
                                e.setRunning(false);
                                e.setHasRun(false);
                                //e.setRunOnce(false);
                                //Util.showNotification(BackgroundService.getInstance(),
                                //        getString(R.string.app_name), "", R.drawable.ic_launcher);
                                BackgroundService.getInstance().sendBroadcast("EVENT_DISABLED");
                                //Main.getInstance().sendBroadcast("EVENT_DISABLED");
                            }
                            else {
                                e.setEnabled(true);
                                // sending broadcast that we've enabled event
                                BackgroundService.getInstance().sendBroadcast("EVENT_ENABLED");
                                //Main.getInstance().sendBroadcast("EVENT_ENABLED");

                                // mark green if we started the event
                                //((TextView) newRow.findViewById(android.R.id.text1)).setTextColor(Color.GREEN);
                            }

                            // update events array with new event
                            BackgroundService.getInstance().events.set(BackgroundService.getInstance().events.indexOf(e), e);
                            //updateEventsFromPreferences();

                            /**
                             * update preferences
                             */
                            BackgroundService.getInstance().mPreferences.setPreferences("events",
                                    BackgroundService.getInstance().events);

                            /**
                             * refresh view
                             */
                            refreshEventsView();

                            // enable/disable timers, if any
                            BackgroundService.getInstance().updateEventConditionTimers(new ArrayList<Event>(){{
                                add(e);
                            }});
                        }
                        if (which == 2) {
                            // disable timers, if any
                            e.setEnabled(false);
                            BackgroundService.getInstance().updateEventConditionTimers(new ArrayList<Event>(){{
                                add(e);
                            }});

                            // delete row AND spot in events
                            mContainerView.removeView(newRow);
                            BackgroundService.getInstance().events.remove(e);

                            // update preferences
                            BackgroundService.getInstance().mPreferences.setPreferences("events",
                                    BackgroundService.getInstance().events);
                        }

                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //return;
                    }
                });

        // open the dialog now :)
        builder.show();
    }


}
