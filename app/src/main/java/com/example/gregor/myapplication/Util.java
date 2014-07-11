package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
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
import java.util.Collections;

/**
 * Created by Gregor on 11.7.2014.
 */
public class Util extends Activity {

    // create marker
    static Marker marker = null;
    static Circle circle = null;
    static GoogleMap map;

/**
 * OPENS DIALOG WITH POSSIBLE CONDITIONS
 *
 * depending on choice, we will get forwarded to sub-dialog
 *
 * @param context context from our parent activity (usually EventActivity
 * @param optConditions array of conditions defined in EventActivity
 */

    protected static void openDialogConditions(final Activity context, final ArrayList<DialogOptions> optConditions) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        builder.setView(dialogView)
                .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
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
                    .setImageDrawable(context.getResources().getDrawable(opt.getIcon()));
            mContainerOptions.addView(newRow);

            newRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    openSubDialog(context, opt);
                    //EventActivity.getInstance().openSubDialog(opt);
                }
            });

            newRow = null;
        }


        alert.show();

    }


    /**
     * SUBDIALOG with all options and onclick triggers
     */
    protected static void openSubDialog(final Activity context, final DialogOptions opt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        //FragmentManager fm = (Activity)context;
        //final FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();

        final FragmentManager fm = context.getFragmentManager();
        //final FragmentManager fm = context.getFr



        switch(opt.getOptionType()) {

            // NEW LOCATION ENTER/LEAVE DIALOG
            case LOCATION_ENTER:
            case LOCATION_LEAVE:

                // create MAP object
                final View dialogMap = inflater.inflate(R.layout.dialog_sub_map, null);
                //GoogleMap map;

                builder.setView(dialogMap)
                        .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
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
                                    EventActivity.getInstance().conditions.add(cond);

                                    // add new row to conditions now
                                    final ViewGroup newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                                            R.layout.condition_single_item, EventActivity.getInstance().mContainerCondition, false);

                                    ((TextView) newRow.findViewById(android.R.id.text1)).setText(  ((opt.getOptionType() == DialogOptions.type.LOCATION_LEAVE) ? "Leaving " : "Entering ") + "Location");
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setText("Latitude: " + String.format("%.2f", marker.getPosition().latitude) + ", Longitude: " + String.format("%.2f", marker.getPosition().longitude));

                                    ((ImageButton) newRow.findViewById(R.id.condition_icon))
                                            .setImageDrawable(context.getResources().getDrawable(R.drawable.ic_map));

                                    newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //mContainerView.removeView(newView);
                                            EventActivity.getInstance().mContainerCondition.removeView(newRow);
                                            EventActivity.getInstance().conditions.remove(cond);
                                        }
                                    });

                                    EventActivity.getInstance().mContainerCondition.addView(newRow, 0);




                                }

                                // always remove map after closing dialog if we don't want to get
                                // exceptions on how we got a duplicate!
                                // http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
                                MapFragment f = (MapFragment) fm.findFragmentById(R.id.map);
                                if (f != null) {
                                    fm.beginTransaction().remove(f).commit();
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
                                //FragmentManager fm = EventActivity.getInstance();
                                //FragmentManager fm = (FragmentActivity)context.getSupportFragmentManager();

                                MapFragment f = (MapFragment) fm.findFragmentById(R.id.map);
                                if (f != null) {
                                    fm.beginTransaction().remove(f).commit();
                                }
                                //marker = null;
                                //circle = null;
                                //alert.dismiss();
                            }
                        });

                // open the dialog now :)
                builder.show();


                AndroidLocation loc;
                loc = new AndroidLocation(context);
                LatLng myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

                // it is possible we cannot find current location. if so, allow user to continue anyways!
                if (loc.isError()) {
                    Toast.makeText(context, loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : ""), Toast.LENGTH_LONG).show();
                    //txtView.append(loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : "")  +"\n");
                    myLocation = new LatLng(65.9667, -18.5333);
                }
                else {
                    Toast.makeText(Main.getInstance().getApplicationContext(),
                            "Click on map to mark your desired location.", Toast.LENGTH_SHORT).show();
                }

                map = ((MapFragment) fm.findFragmentById(R.id.map)).getMap();


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
                                    EventActivity.getInstance().conditions.add(cond);

                                    // add new row to conditions now
                                    final ViewGroup newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                                            R.layout.condition_single_item, EventActivity.getInstance().mContainerCondition, false);

                                    ((TextView) newRow.findViewById(android.R.id.text1)).setText("Days ("+ mSelectedDays.size() +")");
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setText(allDays);
                                    ((TextView) newRow.findViewById(android.R.id.text2))
                                            .setMovementMethod(new ScrollingMovementMethod());

                                    ((ImageButton) newRow.findViewById(R.id.condition_icon))
                                            .setImageDrawable(context.getResources().getDrawable(opt.getIcon()));

                                    newRow.findViewById(R.id.condition_single_container).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(context,
                                                    "clicked "+ cond.getTitle() + ", "+ cond.getOptionType(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            EventActivity.getInstance().mContainerCondition.removeView(newRow);
                                            EventActivity.getInstance().conditions.remove(cond);
                                        }
                                    });

                                    EventActivity.getInstance().mContainerCondition.addView(newRow, 0);
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




}
