package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends Activity {
    private static Main sInstance = null;
    private static Intent bgService = null;
    private ViewGroup mContainerView;
    NotificationManager mNM = null;
    private ArrayList<Event> events;

    // Map with options. Instead of creating more variables to use around
    // activities, I've created HashMap; putting settings here if needed.
    Map options = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set singleton instance
        if (sInstance == null) {
            sInstance = this;
        }

        // create notification manager
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // create & start service
        bgService = new Intent(this, BackgroundService.class);
        startService(bgService);


        // set container for our events
        mContainerView = (ViewGroup) findViewById(R.id.container);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (options.get("eventSave") == "1") {
            addNew();
        }
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
            startActivity(new Intent(Main.this, EventActivity.class));

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
                            mNM.cancel(1337);
                            finish();
                        }
                    })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
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


    //@Override
    /**
     * instead of stopping application, lets create new background activity and
     * put the app in notification
     */
    /*
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        // run this only if we press home or back button
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {

            Log.d("test", "keypress");



        }

        return false;
    }*/

    /**
     * add new item wizard
     */
    private void addNew() {
        //Toast.makeText(this, "yolo", Toast.LENGTH_SHORT).show();

        // add new item

        // lets clear the empty item first
        if (findViewById(android.R.id.empty).getVisibility() == View.VISIBLE) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }

        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.main_single_item, mContainerView, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(options.get("eventName").toString());


        // set listener for delete button
        newView.findViewById(R.id.single_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                mContainerView.removeView(newView);

                // If there are no rows remaining, show the empty view.
                if (mContainerView.getChildCount() == 0) {
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });

        mContainerView.addView(newView, 0);

        // clear variables in options
        options.put("eventSave", "");
        options.put("eventName", "");

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
     * create/update Notification
     */
    protected void setNotification(String title, String description, int icon) {
        Notification note = new Notification(icon, title, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, Main.class), 0);

        note.setLatestEventInfo(this, title, description, pi);
        note.flags |= Notification.FLAG_NO_CLEAR;

        if (mNM == null) {
            mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        mNM.notify(1337, note);

    }

}
