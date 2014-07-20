package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Activity {
    private static Main sInstance = null;
    private static Intent bgService = null;
    protected boolean isVisible = false;
    private ViewGroup mContainerView;
    protected ArrayList<Event> events = new ArrayList<Event>();

    // Map with options. Instead of creating more variables to use around
    // activities, I've created HashMap; putting settings here if needed.
    HashMap<String, String> options = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set singleton instance
        sInstance = this;

        // set container for our events
        mContainerView = (ViewGroup) findViewById(R.id.container);


        // fetch events from settings of some sort
        events = getEventsFromPreferences();


        // this is our first run, let set all events running boolean to false
        for (int i = 0; i < events.size(); i++) {
            events.get(i).setRunning(false);
        }


        // create & start service
        bgService = new Intent(this, BackgroundService.class);
        startService(bgService);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // set to visible
        isVisible = true;

        //if (options.get("eventSave") == "1") {
            //addNewEvent();
            refreshEventsView();
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();

        // set to invisible
        isVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_new) {
            startActivity(new Intent(this, EventActivity.class));

            return true;
        }
        // close the application and, of course background process
        if (id == R.id.action_exit) {
            // quitting? so soon? ask nicely, windows mode, if person is sure!
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage("Service will be stopped and Events won't be triggered.\n\nAre you sure?")
                    .setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                    .setTitle(getString(R.string.error))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            stopService(bgService);
                            BackgroundService.getInstance().stopForeground(true);
                            finish();

                        }
                    })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            //return;
                        }
                    });

            // open the dialog now :)
            builder.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // adding instance of Analytics object
        EasyTracker.getInstance(this).activityStart(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        // finishing analytics activity.
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }


    /**
     * after resuming the app, we will usually come to the main activity with nothing on it.
     * this function will take care of that! go through events array and fill it up, yo?
     */
    protected void refreshEventsView() {
        // always clear container first
        mContainerView.removeAllViews();

         // if events array is empty, show "add new event" textview
        if (events.size() == 0) {
            findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }

        // fill the events from array
        for (final Event e : events) {
            final ViewGroup newRow = (ViewGroup) LayoutInflater.from(this).inflate(
                    R.layout.main_single_item, mContainerView, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(e.getName());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                    (e.isRunning()) ? "Active" :
                            ((e.isEnabled() ? "Enabled" : "Disabled"))
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
            updateEventsFromPreferences();

        }

    }

    /**
     * update preferences with events
     */
    private void updateEventsFromPreferences() {
        // preferences object
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);

        // retrieve object from preferences
        Gson gson = new Gson();
        String json = mPrefs.getString("events", "");

        // store all to preferences again
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        json = gson.toJson(events);
        prefsEditor.putString("events", json);
        prefsEditor.commit();

    }

    /**
     * get from preferences
     */
    private ArrayList<Event> getEventsFromPreferences() {
        // preferences object
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);

        // return object
        ArrayList<Event> returnObj = new ArrayList<Event>();

        // retrieve object from preferences
        Gson gson = new Gson();
        String json = mPrefs.getString("events", "");

        //protected ArrayList<Event> events = new ArrayList<Event>();
        ArrayList<Event> eventsPrefs = gson.fromJson(json, new TypeToken<List<Event>>(){}.getType());

        // if preferences exist and current events array don't
        if (eventsPrefs != null) {
            if (eventsPrefs.size() > 0) {
                returnObj = eventsPrefs;
            }
        }

        return returnObj;

    }

    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static Main getInstance() {
        if (sInstance == null) {
            return new Main();
        }
        else
            return sInstance;
    }


    /**
     * SEND NEW BROADCAST
     */
    protected void sendBroadcast(String broadcast) {
        if (broadcast.length() > 0) {
            Intent intent = new Intent();
            //android.util.Log.e("send broadcast", sInstance.getClass().getPackage().getName() +"."+ broadcast);
            intent.setAction(sInstance.getClass().getPackage().getName() +"."+ broadcast);
            sendBroadcast(intent);
        }
    }


    /**
     * CLICK ON SINGLE EVENT OPENS EVENT ACTIVITY
     *
     * @param e Event
     */
    private void onClickSingleEvent(Event e) {
        Intent i = new Intent(getApplicationContext(), EventActivity.class);
        i.putExtra("sEvent", (new Gson().toJson(e)));
        i.putExtra("sEventIndexKey", events.indexOf(e));
        startActivity(i);
    }

    /**
     * LONG CLICK SINGLE EVENT
     *
     * should open popup with options.
     */
    private void onLongClickSingleEvent(final Event e, final ViewGroup newRow) {
        // array of options
        final String[] sOptions = {"Edit", ((e.isEnabled()) ? "Disable" : "Enable"), "Delete"};
        // show dialog with more options for single event
        final AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);
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
                        if (which == 1) {
                            if (e.isEnabled()) {
                                e.setRunning(false);
                                e.setEnabled(false);
                                //Util.showNotification(BackgroundService.getInstance(),
                                //        getString(R.string.app_name), "", R.drawable.ic_launcher);
                                sendBroadcast("EVENT_DISABLED");
                            }
                            else {
                                e.setEnabled(true);
                                // sending broadcast that we've enabled event
                                sendBroadcast("EVENT_ENABLED");

                                // mark green if we started the event
                                //((TextView) newRow.findViewById(android.R.id.text1)).setTextColor(Color.GREEN);
                            }

                            // update events array
                            events.set(events.indexOf(e), e);
                            updateEventsFromPreferences();
                            refreshEventsView();

                        }
                        if (which == 2) {
                            // delete row AND spot in events
                            mContainerView.removeView(newRow);
                            events.remove(e);
                            updateEventsFromPreferences();
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
