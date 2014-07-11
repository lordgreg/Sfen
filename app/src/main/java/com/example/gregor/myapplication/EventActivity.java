package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class EventActivity extends Activity {
    private static EventActivity sInstance = null;
    protected ViewGroup mContainerCondition, mContainerAction;
    private TextView eventName;
    private GoogleMap map;
    Marker marker = null;
    Circle circle = null;
    LayoutInflater inflater;

    // placeholder for current Event
    private Event event;

    // arrays for conditions and actions
    protected ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();
    protected ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();


    // list of possible Conditions in Options
    static final ArrayList<DialogOptions> optConditions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions("Entering Location", "Entering location", R.drawable.ic_map, DialogOptions.type.LOCATION_ENTER));
        add(new DialogOptions("Leaving Location", "Leaving location", R.drawable.ic_map, DialogOptions.type.LOCATION_LEAVE));
        add(new DialogOptions("Time", "Time range", R.drawable.ic_time, DialogOptions.type.TIMERANGE));
        add(new DialogOptions("Days", "Day(s) of week", R.drawable.ic_date, DialogOptions.type.DAYSOFWEEK));
        add(new DialogOptions("Connecting to Wifi", "Connected to Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_CONNECT));
        add(new DialogOptions("Disconnecting from Wifi", "Disconnected from Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_DISCONNECT));
    }};

    /**
     * OPENS DIALOG WITH POSSIBLE CONDITIONS
     *
     * depending on choice, we will get forwarded to sub-dialog
     */
    /*
    private void openConditions() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        builder.setView(dialogView)
                .setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle("Pick condition")
                        // Add action buttons
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...

                    }
                });

        final AlertDialog alert = builder.create();

        // fill all options in container
        ViewGroup newRow;
        //DialogOptions opt;
        for (int i = 0; i < optConditions.size(); i++)
        {
            final DialogOptions opt = optConditions.get(i);

            newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, mContainerOptions, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(opt.getTitle());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(opt.getDescription());
            ((ImageButton) newRow.findViewById(R.id.dialog_icon))
                    .setImageDrawable(getResources().getDrawable(opt.getIcon()));
            mContainerOptions.addView(newRow);

            newRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    openSubDialog(opt);
                }
            });

            newRow = null;
        }


        alert.show();

    }
*/
    protected void openSubDialog(final DialogOptions opt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();


        switch(opt.getOptionType()) {

            // NEW LOCATION ENTER/LEAVE DIALOG
            case LOCATION_ENTER:
            case LOCATION_LEAVE:


                // create MAP object
                final View dialogMap = inflater.inflate(R.layout.dialog_sub_map, null);

                builder.setView(dialogMap)
                        .setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                        .setTitle("Pick location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                                if (marker == null) {
                                    Toast.makeText(Main.getInstance().getApplicationContext(),
                                            "Listen, you have to click on map to add marker!", Toast.LENGTH_LONG).show();
                                }
                                else {

                                    // this is amazing! we've managed to click and make a marker, lets add condition to list of conditions, ya?
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());
                                    cond.setSetting("latitude", ""+ marker.getPosition().latitude);
                                    cond.setSetting("longitude", ""+ marker.getPosition().longitude);
                                    conditions.add(cond);

                                    // add new row to conditions now
                                    final ViewGroup newRow = (ViewGroup) LayoutInflater.from(getBaseContext()).inflate(
                                            R.layout.condition_single_item, mContainerCondition, false);

                                    ((TextView) newRow.findViewById(android.R.id.text1)).setText(  ((opt.getOptionType() == DialogOptions.type.LOCATION_LEAVE) ? "Leaving " : "Entering ") + "Location");
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setText("Latitude: " + String.format("%.2f", marker.getPosition().latitude) + ", Longitude: " + String.format("%.2f", marker.getPosition().longitude));

                                    ((ImageButton) newRow.findViewById(R.id.condition_icon))
                                            .setImageDrawable(getResources().getDrawable(R.drawable.ic_map));

                                    newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //mContainerView.removeView(newView);
                                            mContainerCondition.removeView(newRow);
                                            conditions.remove(cond);
                                        }
                                    });

                                    mContainerCondition.addView(newRow, 0);




                                }

                                // always remove map after closing dialog if we don't want to get
                                // exceptions on how we got a duplicate!
                                // http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
                                MapFragment f = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                                if (f != null) {
                                    getFragmentManager().beginTransaction().remove(f).commit();
                                }
                                marker = null;
                                circle = null;
                            }

                        })

                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                // always remove map after closing dialog if we don't want to get
                                // exceptions on how we got a duplicate!
                                // http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
                                MapFragment f = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                                if (f != null) {
                                    getFragmentManager().beginTransaction().remove(f).commit();
                                }
                                marker = null;
                                circle = null;
                                //alert.dismiss();
                            }
                        });

                // open the dialog now :)
                builder.show();


                AndroidLocation loc;
                loc = new AndroidLocation(getApplicationContext());
                LatLng myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

                // it is possible we cannot find current location. if so, allow user to continue anyways!
                if (loc.isError()) {
                    Toast.makeText(this, loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : ""), Toast.LENGTH_LONG).show();
                    //txtView.append(loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : "")  +"\n");
                    myLocation = new LatLng(65.9667, -18.5333);
                }
                else {
                    Toast.makeText(Main.getInstance().getApplicationContext(),
                            "Click on map to mark your desired location.", Toast.LENGTH_SHORT).show();
                }

                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();


                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        if (marker != null) {
                            marker.remove();
                        }
                        if(circle != null) {
                            circle.remove();
                        }

                        // redraw radius circle and marker

                        marker = map.addMarker(new MarkerOptions().position(latLng));
                        circle = map.addCircle(new CircleOptions()
                                        .center(latLng)
                                        .radius(100)
                                        .strokeWidth(2)
                                        .strokeColor(0xff0099FF)
                                        .fillColor(0x550099FF)
                        );

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }
                });


                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

                break;

            /**
             * DAYSOFWEEK SUBDIALOG
             */
            case DAYSOFWEEK:
                final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // storage for selected days
                final ArrayList<Integer> mSelectedDays = new ArrayList<Integer>();

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Pick day(s)")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: add day picker now.

                                // we cannot continue if we didn't pick any days, right?
                                if (mSelectedDays.size() == 0) {
                                    Toast.makeText(Main.getInstance().getApplicationContext(),
                                            "Good job sport! And which days did you pick?", Toast.LENGTH_LONG).show();

                                } else {
                                    // lets sort the days first
                                    Collections.sort(mSelectedDays);

                                    // ..also, get selected days into string
                                    String allDays = "";
                                    for (int i = 0; i < mSelectedDays.size(); i++) {
                                        allDays += days[mSelectedDays.get(i)];

                                        if ((i + 1) != mSelectedDays.size()) {
                                            allDays += ", ";
                                        }
                                    }


                                    // save condition & create new row
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());
                                    cond.setSetting("selectedDays", mSelectedDays.toString());
                                    conditions.add(cond);

                                    // add new row to conditions now
                                    final ViewGroup newRow = (ViewGroup) LayoutInflater.from(getBaseContext()).inflate(
                                            R.layout.condition_single_item, mContainerCondition, false);

                                    ((TextView) newRow.findViewById(android.R.id.text1)).setText("Days ("+ mSelectedDays.size() +")");
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setText(allDays);
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setMovementMethod(new ScrollingMovementMethod());

                                    ((ImageButton) newRow.findViewById(R.id.condition_icon))
                                            .setImageDrawable(getResources().getDrawable(opt.getIcon()));

                                    newRow.findViewById(R.id.condition_single_container).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getApplicationContext(),
                                                    "clicked "+ cond.getTitle() + ", "+ cond.getOptionType(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mContainerCondition.removeView(newRow);
                                            conditions.remove(cond);
                                        }
                                    });

                                    mContainerCondition.addView(newRow, 0);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                            }
                        })
                        .setMultiChoiceItems(days, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                // selecting/removing choices
                                if (isChecked) {
                                    mSelectedDays.add(which);
                                } else {
                                    mSelectedDays.remove(which);
                                }

                            }
                        });
                //builder.create();
                builder.show();

                break;

            /**
             * DEFAULT SWITCH/CASE CALL
             */
            default:
                break;
        }


        //}

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // set singleton instance
        if (sInstance == null) {
            sInstance = this;
        }

        // define Util class
        Util util = new Util(this);


        mContainerCondition = (ViewGroup) findViewById(R.id.condition_container);
        mContainerAction = (ViewGroup) findViewById(R.id.action_container);


        // CONDITION
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerCondition, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(getString(R.string.condition_new));
        ((TextView) newView.findViewById(android.R.id.text2)).setText(getString(R.string.condition_new_sub));

        // LISTENER for NEW CONDITION
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.openDialogConditions(sInstance, optConditions);
                //util
                //openConditions();
                //Toast.makeText(getBaseContext(), "picking new condition", Toast.LENGTH_SHORT).show();
            }
        });

        mContainerCondition.addView(newView, 0);

        // ACTION
        final ViewGroup newAction = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerCondition, false);

        ((TextView) newAction.findViewById(android.R.id.text1)).setText(getString(R.string.action_new));
        ((TextView) newAction.findViewById(android.R.id.text2)).setText(getString(R.string.action_new_sub));

        // LISTENER for NEW CONDITION
        newAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
            }
        });

        mContainerAction.addView(newAction, 0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            Main.getInstance().options.put("eventSave", "0");

            finish();
            return true;
        }
        if (id == R.id.action_save) {
            return saveEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static EventActivity getInstance() {
        if (sInstance == null) {
            return new EventActivity();
        }
        else
            return sInstance;
    }


    /**
     * Saving event!
     */
    private boolean saveEvent() {

        // container for condition in dialog
/*
        if (conditions.size() > 0) {
            for (int i = 0; i < conditions.size(); i++) {
                Log.d("conditions",
                        conditions.get(i).getTitle() +", "+
                        conditions.get(i).getOptionType() +", "+
                        conditions.get(i).getSettings().toString() +", "
                );
            }
        }
        */

        // before saving, we have to ensure we have at least one condition and one activity.
        // if there's only one in ListView, it means its the one from "add new activity".
        int num = mContainerCondition.getChildCount();

        if (num <= 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_select_condition))
                    .setIcon(R.drawable.ic_launcher)
                            //.setIcon(android.R.drawable.ic_notification_clear_all)
                    .setTitle(getString(R.string.error))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        Log.d("info", "number of dialogOptions: "+ mContainerCondition.getChildCount());

        eventName = (TextView) findViewById(R.id.event_name);

        Main.getInstance().options.put("eventSave", "1");
        Main.getInstance().options.put("eventName", eventName.getText().toString());


        finish();
        return true;

    }
}
