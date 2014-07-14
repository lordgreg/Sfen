package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gregor on 11.7.2014.
 */
public class Util extends Activity {

    // create marker
    private static Marker marker = null;
    private static Circle circle = null;
    private static GoogleMap map;
    private static boolean sBoolean;

    // days
    final static String[] sDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    /**
     * OPENS DIALOG WITH POSSIBLE CONDITIONS
     * <p/>
     * depending on choice, we will get forwarded to sub-dialog
     *
     * @param context       context from our parent activity (usually EventActivity
     * @param optConditions array of conditions defined in EventActivity
     */
    protected static void openDialogConditions(final Activity context, final ArrayList<DialogOptions> optConditions) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        builder.setView(dialogView)
                .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle("Pick condition")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // canceling main dialog
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();

        // fill all options in container
        ViewGroup newRow;

        for (int i = 0; i < optConditions.size(); i++) {
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
                    dialog.dismiss();

                    openSubDialog(context, opt);
                }
            });

            newRow = null;

            dialog.show();

        }
    }


    /**
     * SUBDIALOG with all options and onclick triggers
     */
    protected static void openSubDialog(final Activity context, final DialogOptions opt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        final FragmentManager fm = context.getFragmentManager();

        switch (opt.getOptionType()) {

            // NEW LOCATION ENTER/LEAVE DIALOG
            case LOCATION_ENTER:
            case LOCATION_LEAVE:

                // create MAP object
                final View dialogMap = inflater.inflate(R.layout.dialog_sub_map, null);


                builder.setView(dialogMap)
                        .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
                        .setTitle("Pick location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                dialog.dismiss();

                                if (marker == null) {
                                    showMessageBox("Click on map to add a location!", true);
                                } else {

                                    // this is amazing! we've managed to click and make a marker, lets add condition to list of conditions, ya?
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());
                                    cond.setSetting("latitude", "" + marker.getPosition().latitude);
                                    cond.setSetting("longitude", "" + marker.getPosition().longitude);

                                    cond.setSetting("text1", ((opt.getOptionType() == DialogOptions.type.LOCATION_LEAVE) ? "Leaving " : "Entering ") + "Location");
                                    cond.setSetting("text2", "Latitude: " + String.format("%.2f", marker.getPosition().latitude) + ", Longitude: " + String.format("%.2f", marker.getPosition().longitude));

                                    addNewCondition(context, cond);

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
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();

                                // always remove map after closing dialog if we don't want to get
                                // exceptions on how we got a duplicate!
                                // http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
                                MapFragment f = (MapFragment) fm.findFragmentById(R.id.map);
                                if (f != null) {
                                    fm.beginTransaction().remove(f).commit();
                                }
                                //marker = null;
                                //circle = null;

                            }
                        });

                // open the dialog now :)
                builder.show();


                AndroidLocation loc;
                loc = new AndroidLocation(context);
                LatLng myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

                // it is possible we cannot find current location. if so, allow user to continue anyways!
                if (loc.isError()) {
                    showMessageBox(loc.getError() + ((loc.getProvider() != "") ? " (" + loc.getProvider() + ")" : ""), true);
                    //Toast.makeText(context, loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : ""), Toast.LENGTH_LONG).show();
                    //txtView.append(loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : "")  +"\n");
                    myLocation = new LatLng(65.9667, -18.5333);
                } else {
                    showMessageBox("Click on map to mark your desired location.", false);
                }

                map = ((MapFragment) fm.findFragmentById(R.id.map)).getMap();

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (marker != null) {
                            marker.remove();
                        }
                        if (circle != null) {
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

                // storage for selected days
                final ArrayList<Integer> mSelectedDays = new ArrayList<Integer>();

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Pick day(s)")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: add day picker now.
                                dialog.dismiss();


                                // we cannot continue if we didn't pick any days, right?
                                if (mSelectedDays.size() == 0) {
                                    showMessageBox("Good job sport! And which days did you pick?", true);

                                } else {
                                    // lets sort the days first
                                    Collections.sort(mSelectedDays);

                                    // Get selected days to string so we will show that in description line
                                    String allDays = "";
                                    for (int i = 0; i < mSelectedDays.size(); i++) {
                                        allDays += sDays[mSelectedDays.get(i)];

                                        if ((i + 1) != mSelectedDays.size()) {
                                            allDays += ", ";
                                        }
                                    }


                                    // save condition & create new row
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                    //EventActivity.getInstance().conditions.add(cond);

                                    cond.setSetting("selectedDays", (new Gson().toJson(mSelectedDays)));
                                    cond.setSetting("text1", "Days (" + mSelectedDays.size() + ")");
                                    cond.setSetting("text2", allDays);

                                    addNewCondition(context, cond);

                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                                dialog.dismiss();

                            }
                        })
                        .setMultiChoiceItems(sDays, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                // selecting/removing choices
                                if (isChecked) {
                                    mSelectedDays.add(which);
                                } else {
                                    mSelectedDays.remove(mSelectedDays.indexOf(which));
                                }

                            }
                        });

                builder.show();

                break;

            /**
             * TIME RANGE DIALOG
             */
        case TIMERANGE:

            // create MAP object
            final View timerangeView = inflater.inflate(R.layout.dialog_sub_timerange, null);

            final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            // set 24hour format
            final TimePicker timeFrom = (TimePicker) timerangeView.findViewById(R.id.time_from);
            final TimePicker timeTo = (TimePicker) timerangeView.findViewById(R.id.time_to);
            //((TimePicker) timerangeView.findViewById(R.id.time_from)).setIs24HourView(true);
            //((TimePicker) timerangeView.findViewById(R.id.time_to)).setIs24HourView(true);
            timeFrom.setIs24HourView(true);
            timeTo.setIs24HourView(true);


            builder
                    .setView(timerangeView)
                    .setIcon(R.drawable.ic_launcher)

                    .setTitle("" + dateFormat.format(new Date()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            // always always always always always always always always always
                            // always dismiss and clearFocus if you want to retrieve input time
                            // instead of current time.
                            timeFrom.clearFocus();
                            timeTo.clearFocus();

                            // add new condition
                            final DialogOptions cond = new DialogOptions(opt.getTitle(),
                                    opt.getDescription(), opt.getIcon(), opt.getOptionType());

                            cond.setSetting("fromHour", timeFrom.getCurrentHour().toString());
                            cond.setSetting("fromMinute", timeFrom.getCurrentMinute().toString());
                            cond.setSetting("toHour", timeTo.getCurrentHour().toString());
                            cond.setSetting("toMinute", timeTo.getCurrentMinute().toString());
                            cond.setSetting("text1", "Time range");
                            cond.setSetting("text2", "From " +
                                    String.format("%02d", timeFrom.getCurrentHour()) +":"+
                                    String.format("%02d", timeFrom.getCurrentMinute())
                                    +" to "+
                                    String.format("%02d", timeTo.getCurrentHour()) +":"+
                                    String.format("%02d", timeTo.getCurrentMinute())
                                    +"");

                            addNewCondition(context, cond);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                        }
                    });

            builder.show();

            break;

            /**
             * WIFI Access Points
             */
            case WIFI_CONNECT:
            case WIFI_DISCONNECT:
                final ArrayList<Integer> mSelectedWifi = new ArrayList<Integer>();

                //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)
                WifiManager mManager = (WifiManager) Main.getInstance().getSystemService(Context.WIFI_SERVICE);
                List<WifiConfiguration> wifiList = mManager.getConfiguredNetworks();

                List<String> mWifiArray = new ArrayList<String>();

                if (wifiList == null) {
                    showMessageBox("Wifi is disabled. Please turn it on first.", false);
                    return ;
                }


                for (WifiConfiguration single : wifiList) {
                    //Log.e("wifi", ">>> "+ single.toString());
                    //myString.substring(1, myString.length()-1);
                    mWifiArray.add(single.SSID.substring(1, single.SSID.length() - 1));
                }
                final String[] stringArray = mWifiArray.toArray(new String[mWifiArray.size()]);

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Pick Wifi")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                                dialog.dismiss();

                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // close dialog
                                dialogInterface.dismiss();

                                // we cannot continue if we didn't pick any days, right?
                                if (mSelectedWifi.size() == 0) {
                                    showMessageBox("You almost made it! Next time, pick at least one option.", true);

                                } else {

                                    // Get selected days to string so we will show that in description line
                                    ArrayList<String> mSelectedSSID = new ArrayList<String>();
                                    String allDays = "";
                                    for (i = 0; i < mSelectedWifi.size(); i++) {
                                        allDays += stringArray[mSelectedWifi.get(i)];
                                        mSelectedSSID.add(stringArray[mSelectedWifi.get(i)]);

                                        if ((i + 1) != mSelectedWifi.size()) {
                                            allDays += ", ";
                                        }
                                    }

                                    // save condition & create new row
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());


                                    cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                                    //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                    cond.setSetting("text1", ((opt.getOptionType() == DialogOptions.type.WIFI_CONNECT) ? "Connecting to " : "Disconnecting from ") + "Wifi");
                                    cond.setSetting("text2", allDays);

                                    addNewCondition(context, cond);

                                }


                            }
                        })

                        .setMultiChoiceItems(stringArray, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                // selecting/removing choices
                                if (isChecked) {
                                    mSelectedWifi.add(which);
                                } else {
                                    mSelectedWifi.remove(mSelectedWifi.indexOf(which));
                                }

                            }
                        });

                builder.show();
                //mManager.getConfiguredNetworks();


                break;

            /**
             * DEFAULT SWITCH/CASE CALL
             */
            default:

                break;
        }


    }


    /**
     * Shows Toast, nothing else to see here, move along...
     *
     * @param message
     * @param showLong
     */
    protected static void showMessageBox(String message, boolean showLong) {
        Toast.makeText(Main.getInstance().getApplicationContext(),
                message,
                ((showLong) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
        ).show();
    }


    /**
     * create/update Notification
     *
     * @param title       is the title of notification
     * @param description is the description (bottom line)
     * @param icon        well, its obvious, isn't it? o_O
     */
    protected static void showNotification(String title, String description, int icon) {
        NotificationManager mNM = (NotificationManager) Main.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification note = new Notification(icon, title, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent pi = PendingIntent.getActivity(Main.getInstance(), 0,
                new Intent(Main.getInstance(), Main.class), 0);

        note.setLatestEventInfo(Main.getInstance(), title, description, pi);
        note.flags |= Notification.FLAG_NO_CLEAR;

        if (mNM == null) {
            mNM = (NotificationManager) Main.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        }

        mNM.notify(1337, note);
    }


    /**
     * ADD NEW CONDITION
     *
     * @param cond condition to be added
     */
    protected static void addNewCondition(final Activity context, final DialogOptions cond) {
        // add condition to list of conditions of Event
        // TODO: if adding NEW, this isn't problem
        // TODO: but if adding from existing, we need to save condition somewhere else (temp condition array)!
        if (EventActivity.getInstance().isUpdating) {
            EventActivity.getInstance().updatedConditions.add(cond);
        } else {
            EventActivity.getInstance().conditions.add(cond);
        }

        // get options that we need for interface
        String title = cond.getSetting("text1");
        String description = cond.getSetting("text2");
        int icon = cond.getIcon();

        // add new row to conditions now
        final ViewGroup newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.condition_single_item, EventActivity.getInstance().mContainerCondition, false);

        ((TextView) newRow.findViewById(android.R.id.text1)).setText(title);
        ((TextView) newRow.findViewById(android.R.id.text2))
                .setText(description);
        //((TextView) newRow.findViewById(android.R.id.text2))
        //        .setMovementMethod(new ScrollingMovementMethod());

        ((ImageButton) newRow.findViewById(R.id.condition_icon))
                .setImageDrawable(context.getResources().getDrawable(icon));

        newRow.findViewById(R.id.condition_single_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: clicking our newly added condition
                showMessageBox("clicked " + cond.getTitle() + ", " + cond.getOptionType(), false);
            }
        });

        newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when clicking recycle bin at condition, remove it from view and
                // from array of all conditions

                EventActivity.getInstance().mContainerCondition.removeView(newRow);

                // remove from conditions, depending on if we're adding to new event
                // or existing event
                if (EventActivity.getInstance().isUpdating) {
                    EventActivity.getInstance().updatedConditions.remove(
                            EventActivity.getInstance().updatedConditions.indexOf(cond)
                    );

                    // we changed something, so set the changed boolean
                    EventActivity.getInstance().isChanged = true;
                } else {
                    EventActivity.getInstance().conditions.remove(
                            EventActivity.getInstance().conditions.indexOf(cond)
                    );
                }

            }
        });

        EventActivity.getInstance().mContainerCondition.addView(newRow, 0);
    }

    /**
     * SHOW YES/NO DIALOG
     *
     * @param context
     * @param question is the message we're passing to user
     * @param options possible options (hashmap keys):
     *                "title" sets title
     *                "positiveButton" sets positive button string
     *                "negativeButton" sets negative button string
     *                "icon" (converts to int) sets icon for dialog
     * @return returns true/false depending on which button did user pressed.
     * @return returns the proper value in Main.options array.
     */
    protected static void showYesNoDialog(final Activity context, String question, HashMap<String, String> options) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        final FragmentManager fm = context.getFragmentManager();
        sBoolean = false;

        final String title = (options.get("title") != null) ? options.get("title") : "Question";
        final String positiveButton = (options.get("positiveButton") != null) ? options.get("positiveButton") : "Yes";
        final String negativeButton = (options.get("negativeButton") != null) ? options.get("negativeButton") : "No";
        final int icon = (options.get("icon") != null) ? Integer.parseInt(options.get("icon")) : R.drawable.ic_launcher;

        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("Question");
        builder.setMessage(question);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                sBoolean = true;
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                sBoolean = false;
            }
        });

        builder.show();


        Main.getInstance().options.put("showYesNoDialog", ""+ sBoolean);
        sBoolean = false;
    }

}