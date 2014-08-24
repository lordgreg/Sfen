package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Gregor on 11.7.2014.
 */
public class Util extends Activity {

    // create marker
    private static Marker marker = null;
    private static Circle circle = null;
    private static GoogleMap map;
    private static boolean sBoolean;
    private static ViewGroup newRow;
    protected static enum ACTION_FROM {EVENT, PROFILE, NULL};
    protected static ACTION_FROM actionFrom;

    // days
    final static String[] sDays = {
            Main.getInstance().getString(R.string.day_monday),
            Main.getInstance().getString(R.string.day_tuesday),
            Main.getInstance().getString(R.string.day_wednesday),
            Main.getInstance().getString(R.string.day_thursday),
            Main.getInstance().getString(R.string.day_friday),
            Main.getInstance().getString(R.string.day_saturday),
            Main.getInstance().getString(R.string.day_sunday)};


    // empty constructor
    public Util() {

    }

    /**
     * OPENS DIALOG WITH POSSIBLE CONDITIONS
     * <p/>
     * depending on choice, we will get forwarded to sub-dialog
     *
     * @param context       context from our parent activity (usually EventActivity
     * @param options array of conditions defined in EventActivity
     * @param title shows title of dialog
     */
    protected static void openDialog(final Activity context,
                                     final ArrayList<DialogOptions> options,
                                     final String title) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        builder.setView(dialogView)
                .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle(title)
                .setNegativeButton(Main.getInstance().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // canceling main dialog
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();

        // fill all options in container
        //ViewGroup newRow;

        for (int i = 0; i < options.size(); i++) {
            final DialogOptions opt = options.get(i);

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
                    ((ViewGroup)mContainerOptions.getParent()).removeView(mContainerOptions);
                    openSubDialog(context, opt, 0);
                }
            });

            newRow = null;


            dialog.show();
        }


    }


    /**
     * SUBDIALOG with all options and onclick triggers
     */
    protected static void openSubDialog(final Activity context, final DialogOptions opt, final int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        final FragmentManager fm = context.getFragmentManager();
        final Gson gson = new Gson();

        // the only thing we have to check if we're editing entry is,
        // if we have at least one setting stored. if so, all is good in our wonderland
        final boolean isEditing = (opt.getSettings().size() > 0) ? true : false;

        switch (opt.getOptionType()) {

            // NEW LOCATION ENTER/LEAVE DIALOG
            case LOCATION_ENTER:
            case LOCATION_LEAVE:

                // create MAP object
                final View dialogMap = inflater.inflate(R.layout.dialog_sub_map, null);

                builder.setView(dialogMap)
                        .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
                        .setTitle(context.getString(R.string.location_pick))
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                dialog.dismiss();

                                if (marker == null) {
                                    showMessageBox(context.getString(R.string.map_click_to_add_location), true);
                                } else {

                                    // this is amazing! we've managed to click and make a marker, lets add condition to list of conditions, ya?
                                    final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());
                                    cond.setSetting("latitude", "" + marker.getPosition().latitude);
                                    cond.setSetting("longitude", "" + marker.getPosition().longitude);
                                    cond.setSetting("radius", "" + circle.getRadius());

                                    cond.setSetting("text1",
                                            ((opt.getOptionType() == DialogOptions.type.LOCATION_LEAVE) ?
                                                    context.getString(R.string.outside) : context.getString(R.string.inside)) +" "+ context.getString(R.string.outside_inside_location)
                                    );
                                    cond.setSetting("text2", "Lat: " + String.format("%.2f", marker.getPosition().latitude) +
                                            ", Long: " + String.format("%.2f", marker.getPosition().longitude) +
                                            ", Rad: " + ((TextView) dialogMap.findViewById(R.id.radius_info)).getText().toString());


                                    if (isEditing)
                                        removeConditionOrAction(index, opt);

                                    //addNewCondition(context, cond, index);
                                    addNewConditionOrAction(context, cond, index);

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

                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

//                if (loc == null) {
//
//                    showMessageBox(context.getString(R.string.location_not_available), false);
//
//                    return;
//
//                }



                LatLng myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

                // it is possible we cannot find current location. if so, allow user to continue anyways!
                if (loc.isError()) {
                    showMessageBox(loc.getError() + ((loc.getProvider() != "") ? " (" + loc.getProvider() + ")" : ""), true);
                    //Toast.makeText(context, loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : ""), Toast.LENGTH_LONG).show();
                    //txtView.append(loc.getError() + ((loc.getProvider()!="") ? " ("+ loc.getProvider() +")" : "")  +"\n");
                    myLocation = new LatLng(65.9667, -18.5333);
                } else {
                    showMessageBox(context.getString(R.string.map_click_to_mark_desired_location), false);
                }

                map = ((MapFragment) fm.findFragmentById(R.id.map)).getMap();

                if (map == null) {
                    //TBD
                    return;
                }

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (marker != null) {
                            marker.remove();
                        }
                        if (circle != null) {
                            circle.remove();
                        }
                        //System.out.println("text from raidus info: "+ ((TextView) dialogMap.findViewById(R.id.radius_info)).getText().toString());
                        // redraw radius circle and marker
                        marker = map.addMarker(new MarkerOptions().position(latLng));
                        circle = map.addCircle(new CircleOptions()
                                        .center(latLng)
                                        .radius(Double.parseDouble(
                                                ((TextView) dialogMap.findViewById(R.id.radius_info)).getText().toString()
                                        ))
                                        .strokeWidth(2)
                                        .strokeColor(0xff0099FF)
                                        .fillColor(0x550099FF)
                        );

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }
                });


                map.setMyLocationEnabled(true);



                if (isEditing) {
                    //android.util.Log.e("MAP", "yes, editing. settings: "+ opt.getSettings().toString());
                    // editing, set marker.
                    //LatLng mLatLngFromSettings = null;
                    //if (isEditing) {
                    //  mLatLngFromSettings = new LatLng(Double.parseDouble(opt.getSetting("latitude")), Double.parseDouble(opt.getSetting("longitude")));
                    //}
                    myLocation = new LatLng(Double.parseDouble(opt.getSetting("latitude")), Double.parseDouble(opt.getSetting("longitude")));

                    // redraw radius circle and marker
                    marker = map.addMarker(new MarkerOptions().position(myLocation));
                    circle = map.addCircle(new CircleOptions()
                                    .center(myLocation)
                                    .radius(Double.parseDouble(opt.getSetting("radius")))
                                    .strokeWidth(2)
                                    .strokeColor(0xff0099FF)
                                    .fillColor(0x550099FF)
                    );

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                }


                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));


                // SEEKBAR
                // don't continue, if we haven't clicked yet.
                int mRadius = (circle == null) ? 100 : (int)circle.getRadius();
                final SeekBar seeker = (SeekBar) dialogMap.findViewById(R.id.radius_seeker);
                seeker.setProgress(mRadius);
                ((TextView) dialogMap.findViewById(R.id.radius_info)).setText(String.valueOf(mRadius));
                final LatLng mLocationForSeeker = myLocation;
                seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        i = ((int) Math.round(i / 10)) * 10;
                        seeker.setProgress(i);
                        //textview.setText(progress + "");
                        ((TextView) dialogMap.findViewById(R.id.radius_info)).setText(String.valueOf(i));

                        // replace circle inside map
                        if (circle != null)
                            circle.remove();

                        circle = map.addCircle(new CircleOptions()
                                        .center((marker != null) ? marker.getPosition() : mLocationForSeeker)
                                        .radius(Double.valueOf(((TextView) dialogMap.findViewById(R.id.radius_info)).getText().toString()))
                                        .strokeWidth(2)
                                        .strokeColor(0xff0099FF)
                                        .fillColor(0x550099FF)
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });


                break;

            /**
             * DAYSOFWEEK SUBDIALOG
             */
            case DAYSOFWEEK:

                // storage for selected days
                final ArrayList<Integer> mSelectedDays = new ArrayList<Integer>();

                // array of checked items
                boolean[] checkedDays = new boolean[sDays.length];

                // editing? then fill boolean with picked days
                ArrayList<Integer> mDaysFromSettings = null;
                if (isEditing) {
                    mDaysFromSettings = gson.fromJson(opt.getSetting("selectedDays"),
                            new TypeToken<List<Integer>>(){}.getType());

                    //System.out.println("we have days stored: "+ mDaysFromSettings.toString() +"; size of all days "+ sDays.length +" and size of boolean "+ checkedDays.length);

                    // then enable array of booleans on the index
                    // loop through all days
                    for (int i = 0; i < sDays.length; i++) {
                        if (mDaysFromSettings.indexOf(i) != -1) {
                            checkedDays[i] = true; // <- saving to boolean
                            mSelectedDays.add(i); // <- adding to checked
                        }
                    }

                }

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_days))
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                                // we cannot continue if we didn't pick any days, right?
                                if (mSelectedDays.size() == 0) {
                                    showMessageBox(context.getString(R.string.days_none_selected), true);

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
                                    cond.setSetting("text1", context.getString(R.string.days_selected, mSelectedDays.size()));
                                    cond.setSetting("text2", allDays);

                                    // if we are editing in sub-dialog, clear previous entry
                                    if (isEditing)
                                        removeConditionOrAction(index, opt);

                                    //addNewCondition(context, cond, index);
                                    addNewConditionOrAction(context, cond, index);

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
                        .setMultiChoiceItems(sDays, checkedDays, new DialogInterface.OnMultiChoiceClickListener() {
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

                final TimePicker timeFrom = (TimePicker) timerangeView.findViewById(R.id.time_from);
                final TimePicker timeTo = (TimePicker) timerangeView.findViewById(R.id.time_to);
                boolean is24hour = android.text.format.DateFormat.is24HourFormat((Context) context);
                timeFrom.setIs24HourView(is24hour);
                timeTo.setIs24HourView(is24hour);

                final DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat((Context) context);//DateFormat.getTimeInstance();

                // editing?
                ArrayList<Integer> mTimeFromSettings = null;
                if (isEditing) {

                    timeFrom.setCurrentHour(Integer.parseInt(opt.getSetting("fromHour")));
                    timeFrom.setCurrentMinute(Integer.parseInt(opt.getSetting("fromMinute")));

                    timeTo.setCurrentHour(Integer.parseInt(opt.getSetting("toHour")));
                    timeTo.setCurrentMinute(Integer.parseInt(opt.getSetting("toMinute")));
                }


                builder
                        .setView(timerangeView)
                        .setIcon(R.drawable.ic_launcher)

                        .setTitle("" + dateFormat.format(new Date()))
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                                cond.setSetting("text1", context.getString(R.string.time_range));

                                cond.setSetting("text2", context.getString(R.string.range_from_to,
                                        ""+ String.format("%02d", timeFrom.getCurrentHour()) + ":" +
                                                String.format("%02d", timeFrom.getCurrentMinute())
                                        ,

                                        ""+ String.format("%02d", timeTo.getCurrentHour()) + ":" +
                                                String.format("%02d", timeTo.getCurrentMinute())
                                        ));

//                                cond.setSetting("text2", "From " +
//                                        String.format("%02d", timeFrom.getCurrentHour()) + ":" +
//                                        String.format("%02d", timeFrom.getCurrentMinute())
//                                        + " to " +
//                                        String.format("%02d", timeTo.getCurrentHour()) + ":" +
//                                        String.format("%02d", timeTo.getCurrentMinute())
//                                        + "");

                                // editing.
                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, index);

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
             * TIME: single time condition
             */
            case TIME:

                // create dialog parts
                final TimePicker timePicker = new TimePicker(context);
                is24hour = android.text.format.DateFormat.is24HourFormat((Context) context);
                timePicker.setIs24HourView(is24hour);

                // we're editing option, get stored time out of settings
                if (isEditing) {
                    timePicker.setCurrentHour(Integer.parseInt(opt.getSetting("hour")));
                    timePicker.setCurrentMinute(Integer.parseInt(opt.getSetting("minute")));
                }

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_time))
                        .setView(timePicker)
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                // add new condition
                                final DialogOptions cond = new DialogOptions(opt.getTitle(),
                                        opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("hour", timePicker.getCurrentHour().toString());
                                cond.setSetting("minute", timePicker.getCurrentMinute().toString());
                                cond.setSetting("text1", context.getString(R.string.specify_time));
                                cond.setSetting("text2", context.getString(R.string.time_exact,
                                        String.format("%02d", timePicker.getCurrentHour()) + ":" +
                                                String.format("%02d", timePicker.getCurrentMinute()))
                                );

                                // editing.
                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
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
                    showMessageBox(context.getString(R.string.wifi_is_disabled), false);
                    return ;
                }

                // array of checked items
                boolean[] checkedItems = new boolean[wifiList.size()];

                // if editing setting, get list of selected wifi ap's
                ArrayList<String> mSsidFromSettings = null;
                if (isEditing) {
                    mSsidFromSettings = gson.fromJson(opt.getSetting("selectedWifi"),
                            new TypeToken<List<String>>(){}.getType());

                    //System.out.println("we have ssid's stored: "+ mSsidFromSettings.toString());
                }
                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //final ArrayList<String> ssid = gson.fromJson(cond.getSetting("selectedWifi"),
                //        new TypeToken<List<String>>(){}.getType());

                for (WifiConfiguration single : wifiList) {

                    mWifiArray.add(single.SSID.substring(1, single.SSID.length() - 1));

                    // if we are editing options AND
                    // if current config is one of the ones stored
                    if (isEditing &&
                            mSsidFromSettings.indexOf(single.SSID.substring(1, single.SSID.length() - 1)) != -1) {

                        // then enable array of booleans on the index
                        checkedItems[ mWifiArray.indexOf(single.SSID.substring(1, single.SSID.length() - 1)) ] = true;

                        // also fill mSelectedWifi
                        mSelectedWifi.add(mWifiArray.indexOf(single.SSID.substring(1, single.SSID.length() - 1)));
                    }

                }
                final String[] stringArray = mWifiArray.toArray(new String[mWifiArray.size()]);

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_wifi))
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                                dialog.dismiss();

                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // close dialog
                                dialogInterface.dismiss();

                                // we cannot continue if we didn't pick any days, right?
                                if (mSelectedWifi.size() == 0) {
                                    showMessageBox(context.getString(R.string.wifi_pick_one_option), true);

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
                                    cond.setSetting("text1", ((opt.getOptionType() == DialogOptions.type.WIFI_CONNECT) ?
                                            context.getString(R.string.wifi_connected_to) :
                                            context.getString(R.string.wifi_disconnected_from)) +
                                            context.getString(R.string.wifi_info_suffix));
                                    cond.setSetting("text2", allDays);


                                    // if we are editing in sub-dialog, clear previous entry
                                    if (isEditing)
                                        removeConditionOrAction(index, opt);


                                    addNewConditionOrAction(context, cond, index);

                                }


                            }
                        })

                        .setMultiChoiceItems(stringArray, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
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
             * CONDITION: Screen on/off
             */
            case SCREEN_ON:
            case SCREEN_OFF:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_screen_on_off), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condScreen = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condScreen.setSetting("text1", opt.getTitle());
                condScreen.setSetting("text2",

                        context.getString(R.string.screen_is_on_off,
                                        ((opt.getOptionType() == DialogOptions.type.SCREEN_ON) ?
                                                context.getString(R.string.on) :
                                                        context.getString(R.string.off))

                        )

                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condScreen, 0);

                break;


            /**
             * CONDITION: bluetooth on/off
             */
            case BLUETOOTH_CONNECTED:
            case BLUETOOTH_DISCONNECTED:

                /**
                 * get devices
                 */
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    showMessageBox(context.getString(R.string.bluetooth_adapter_not_connected), false);
                    return ;

                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                /**
                 * none -.-
                 */
                if (pairedDevices.size() == 0) {

                    showMessageBox(context.getString(R.string.bluetooth_no_devices_exist), false);

                    return ;

                }


                ArrayList<String> btDevices = new ArrayList<String>();
                final ArrayList<String> btDevicesAddress = new ArrayList<String>();
                final ArrayList<String> btDevicesSelected = new ArrayList<String>();
                boolean[] btDevicesChecked = new boolean[pairedDevices.size()];

                /**
                 * editing preferences
                 */
                ArrayList<String> btFromSettings = new ArrayList<String>();
                if (isEditing) {

                    btFromSettings = gson.fromJson(opt.getSetting("BLUETOOTH_DEVICES"),
                            new TypeToken<List<String>>(){}.getType());

                }


                int btCurrent = 0;
                for(BluetoothDevice bt : pairedDevices) {
                    //btDevicesString.add(bt.getName());
                    //btDevices.put(bt.getAddress(), bt.getName());

                    // device is allready stored (from settings)
                    if (btFromSettings.contains(bt.getAddress())) {
                        btDevicesChecked[btCurrent] = true;
                        btDevicesSelected.add(bt.getAddress());
                    }
                    else {


                        //btDevicesSelected.add(bt.getAddress());
                    }

                    btDevicesAddress.add(bt.getAddress());
                    btDevices.add(bt.getName());

//                    System.out.println("BLUETOOTH DEVICE: "+ bt.getName() +", "+ bt.getAddress());


                    btCurrent++;
                }


                // create array of strings from arraylist
                final String[] btDevicesStrings = btDevices.toArray(new String[btDevices.size()]);


                builder
                        .setTitle(context.getString(R.string.bluetooth_devices))
                        .setIcon(R.drawable.ic_bluetooth)

                        .setMultiChoiceItems(btDevicesStrings, btDevicesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                // if checked, add them to selected array, otherwise remove
                                if (b)
                                    btDevicesSelected.add(btDevicesAddress.get(i));
                                else
                                    btDevicesSelected.remove(
                                            btDevicesSelected.indexOf(btDevicesAddress.get(i)));
                            }
                        })

                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                /**
                                 * got any selected?
                                 */
                                if (btDevicesSelected.size() == 0) {

                                    Util.showMessageBox(context.getString(R.string.bluetooth_no_devices_selected), false);

                                    return ;

                                }


                                /**
                                 *
                                 * create settings
                                 *
                                 */
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("BLUETOOTH_DEVICES", (new Gson().toJson(btDevicesSelected)));
                                cond.setSetting("text1", opt.getTitle());
                                cond.setSetting("text2",
                                        context.getString(R.string.bluetooth_devices_selected, btDevicesSelected.size()));

                                // if we are editing in sub-dialog, clear previous entry
                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, index);


                            }
                        })

                        .show();

                //setListAdapter(new ArrayAdapter<String>(this, R.layout.list, s));





//                if (isEditing) {
//                    showMessageBox(context.getString(R.string.cannot_edit_bluetooth), true);
//                    return ;
//                }
//
//                // save action & create new row
//                final DialogOptions condBt = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());
//
//                condBt.setSetting("text1", opt.getTitle());
//                condBt.setSetting("text2",
//
//                        context.getString(R.string.bluetooth_state,
//                                ((opt.getOptionType() == DialogOptions.type.BLUETOOTH_CONNECTED) ?
//                                        context.getString(R.string.on) :
//                                        context.getString(R.string.off))
//
//
//                                )
//                );
//
//                addNewConditionOrAction(context, condBt, 0);

                break;


            /**
             * CONDITION: headset toggle
             */
            case HEADSET_CONNECTED:
            case HEADSET_DISCONNECTED:


                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_headstate_condition), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condHs = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                condHs.setSetting("text1", opt.getTitle());
                condHs.setSetting("text2",

                        context.getString(R.string.headset_state,
                                ((opt.getOptionType() == DialogOptions.type.HEADSET_CONNECTED) ?
                                        context.getString(R.string.connected) :
                                        context.getString(R.string.disconnected))


                        )
                );

                addNewConditionOrAction(context, condHs, 0);


                break;

            /**
             * CONDITION: GPS on/off
             */
            case GPS_ENABLED:
            case GPS_DISABLED:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_gps_condition), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condGps = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condGps.setSetting("text1", opt.getTitle());
                condGps.setSetting("text2",
                        context.getString(R.string.gps_state,
                                ((opt.getOptionType() == DialogOptions.type.GPS_ENABLED) ?
                                        context.getString(R.string.on) :
                                        context.getString(R.string.off))


                        )
                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condGps, 0);


                break;

            /**
             * CONDITION: Connecting/disconnecting on Cell Towers
             */
            case CELL_IN:
            case CELL_OUT:

                // array of ALL items
                final ArrayList<Cell> mCellsToShow = new ArrayList<Cell>();

                // array of SELECTED items
                final ArrayList<Cell> mCellsSelected = new ArrayList<Cell>();

                /**
                 * get cells from settings
                 */
                ArrayList<Cell> mCellsFromSettings = null;
                if (isEditing) {
                    mCellsFromSettings = gson.fromJson(opt.getSetting("selectedcell"),
                            new TypeToken<List<Cell>>(){}.getType());

                    // if we have cells stored, add them to selected cells
                    // and add them to visible cells
                    if (mCellsFromSettings != null) {
                        /**
                         * sort descending
                         */
                        Collections.sort(mCellsFromSettings, Collections.reverseOrder());


                        mCellsToShow.addAll(mCellsFromSettings);
                        //mCellsSelected.addAll(mCellsFromSettings);
                    }
                }

                /**
                 * get cells from HISTORY
                 */
                ArrayList<Cell> mHistoryCells = Cell.getSavedCellsFromPreferences();
                if (mHistoryCells != null) {

                    if (mHistoryCells.size() > 0) {

                        /**
                         * sort history descending
                         */
                        Collections.sort(mHistoryCells, Collections.reverseOrder());

                        /**
                         * check if history cell is already listed in preferences.
                         */
                        for (Cell single : mHistoryCells) {

                            /**
                             * current cell from history NOT in saved cells
                             */
                            if (!mCellsToShow.contains(single))
                                mCellsToShow.add(single);

                        }

                    }

                }


                /**
                 * get CURRENT cell
                 */
                CellConnectionInfo cellInfo = new CellConnectionInfo(context);

                if (!cellInfo.isError()) {
                    Cell tempCell = new Cell(cellInfo.getCellId(), Calendar.getInstance());

                    // does our list contain current cell?
                    // if not, add it
                    if (!mCellsToShow.contains(tempCell)) {

                        mCellsToShow.add(tempCell);

                    }

                    // if cells to show contains current cell, update its date
                    else {

                        mCellsToShow.set(
                                mCellsToShow.indexOf(tempCell),
                                tempCell
                        );

                    }
                }


                /**
                 *
                 * dialog start
                 *
                 */
                //inflater = LayoutInflater.from(context);
                final View dialogViewCell = inflater.inflate(R.layout.dialog_pick_condition, null);
                final ViewGroup mCellTowers = (ViewGroup) dialogViewCell.findViewById(R.id.condition_pick);



                //builder = new AlertDialog.Builder(context);
                Util.showMessageBox(context.getString(R.string.click_to_select_deselect), false);

                builder
                        .setTitle(context.getString(R.string.cell_tower_selection))
                        .setIcon(R.drawable.ic_cell)
                        .setView(dialogViewCell)
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                                /**
                                 *
                                 * SAVING ITEMS
                                 *
                                 */
                                // we have to pick at least one
                                if (mCellsSelected.size() == 0) {
                                    showMessageBox(context.getString(R.string.no_radio_tower_selected), true);
                                    return;
                                }


                                // save condition & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());


                                cond.setSetting("selectedcell", (new Gson().toJson(mCellsSelected)));
                                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                cond.setSetting("text1",
                                        ((opt.getOptionType() == DialogOptions.type.CELL_IN) ?
                                                context.getString(R.string.connected_to_specific_cells) :
                                                context.getString(R.string.not_connected_to_specific_cells))


                                );
                                cond.setSetting("text2", context.getString(R.string.cells_selected, mCellsSelected.size()));


                                // if we are editing in sub-dialog, clear previous entry
                                if (isEditing)
                                    removeConditionOrAction(index, opt);


                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                ;

                /**
                 * create dialog
                 */
                final AlertDialog dialogCell = builder.create();

                /**
                 * fill dialog with cells
                 */
                //newRow;
                for (final Cell single : mCellsToShow) {
                    final ViewGroup newRow = (ViewGroup) inflater.inflate(R.layout.dialog_simplerow_single, mCellTowers, false);


                    ((TextView) newRow.findViewById(android.R.id.text1)).setText(single.getCellId());
                    ((TextView) newRow.findViewById(android.R.id.text2))
                            .setText(Util.getDateLong(single.getStoreDate(), context));


                    /**
                     * if we're updating, lets set selected current if it was retrieved from settings
                     */
                    if (isEditing) {
                        if (mCellsFromSettings.contains(single)) {
                            newRow.setSelected(true);

                            // also, add this cell to "selected" array
                            mCellsSelected.add(single);

                        }
                    }

                    mCellTowers.addView(newRow);

                    /**
                     * clicking single row
                     */
                    newRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (newRow.isSelected()) {
                                mCellsSelected.remove(single);
                                newRow.setSelected(false);
                                //System.out.println("removing tower from list: "+ mCellsSelected.toString());
                            }
                            else {
                                //mCellsToShow.remove(single);
                                mCellsSelected.add(single);
                                newRow.setSelected(true);
                                //System.out.println("adding tower to list: "+ mCellsSelected.toString());
                            }
                        }
                    });


                    dialogCell.show();

                }


                dialogCell.show();




                break;


            /**
             * CONDITION: another event running / not running
             */
            case EVENT_RUNNING:
            case EVENT_NOTRUNNING:

                // array of selected items in dialog
                final ArrayList<Integer> mSelectedEvents = new ArrayList<Integer>();

                // array of cell towers we are showing in dialog
                final ArrayList<String> mShownEvents = new ArrayList<String>();
                final ArrayList<Integer> mShownEventsIDs = new ArrayList<Integer>();


                // if editing, retrieve saved cells
                ArrayList<Integer> mEventsFromSettings = null;
                if (isEditing) {
                    // we have ID's saved, not names!
                    mEventsFromSettings = gson.fromJson(opt.getSetting("selectevents"),
                            new TypeToken<List<Integer>>(){}.getType());

                    if (mEventsFromSettings != null)
                        mSelectedEvents.addAll(mEventsFromSettings);
                }


                // fill shown events array, but exclude current event (if editing!)

                for (final Event e : BackgroundService.getInstance().events) {
                    // if we're editing AND current event isn't the editing one OR
                    // if we're not editing
                    // put event to array
                    if (EventActivity.getInstance().event != null &&
                            (e.getUniqueID() != EventActivity.getInstance().event.getUniqueID()) ||
                            EventActivity.getInstance().event == null) {
                        mShownEvents.add(e.getName());
                        mShownEventsIDs.add(e.getUniqueID());

                        // also, fill the selected events array
                        //mSelectedEvents.add(e.getUniqueID());
                    }

                }

                // if there are no other events, we're screwed o_O
                if (mShownEvents.size() == 0) {
                    showMessageBox(context.getString(R.string.no_events_to_pick), true);
                    return;
                }

                // create array of booleans
                boolean[] mEventsChecked = new boolean[mShownEvents.size()];


                // if we're editing, update boolean array with positives
                if (isEditing) {
                    //System.out.println("events from settings: "+ mEventsFromSettings.toString());
                    for (int i = 0; i < mShownEvents.size(); i++) {
                        // is current event ID equal to saved ID
                        if (mEventsFromSettings.contains(mShownEventsIDs.get(i))) {
                            // it does; so update bool array
                            mEventsChecked[i] = true;
                        }
                    }
                }

                // convert ArrayList<String> to String[]
                String[] mShownEventsString = new String[mShownEvents.size()];
                mShownEventsString = mShownEvents.toArray(mShownEventsString);

                // finally, open a dialog
                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_events))
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // we have to pick at least one
                                if (mSelectedEvents.size() == 0) {
                                    showMessageBox(context.getString(R.string.pick_at_least_one_event), true);
                                    return ;
                                }


                                // save condition & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("selectevents", (new Gson().toJson(mSelectedEvents)));
                                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                cond.setSetting("text1",
                                                ((opt.getOptionType() == DialogOptions.type.EVENT_RUNNING) ?
                                                        context.getString(R.string.when_events_running) :
                                                        context.getString(R.string.when_events_not_running))

                                );
                                cond.setSetting("text2", context.getString(R.string.events_selected, mSelectedEvents.size()));


                                // if we are editing in sub-dialog, clear previous entry
                                if (isEditing)
                                    removeConditionOrAction(index, opt);


                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setMultiChoiceItems(mShownEventsString, mEventsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked)
                                    mSelectedEvents.add(mShownEventsIDs.get(which));
                                else
                                    mSelectedEvents.remove(mShownEventsIDs.get(which));

                            }
                        })
                ;

                builder.show();



                break;


            /**
             * CONDITION: Event Conditions true/false
             */
            case EVENT_CONDITIONS_TRUE:
            case EVENT_CONDITIONS_FALSE:

                // array of selected items in dialog
                final ArrayList<Integer> mSelectedEventsCond = new ArrayList<Integer>();

                // array of cell towers we are showing in dialog
                final ArrayList<String> mShownEventsCon = new ArrayList<String>();
                final ArrayList<Integer> mShownEventsCondIDs = new ArrayList<Integer>();


                // if editing, retrieve saved cells
                mEventsFromSettings = null;
                if (isEditing) {
                    // we have ID's saved, not names!
                    mEventsFromSettings = gson.fromJson(opt.getSetting("selectevents"),
                            new TypeToken<List<Integer>>(){}.getType());

                    if (mEventsFromSettings != null)
                        mSelectedEventsCond.addAll(mEventsFromSettings);
                }


                // fill shown events array, but exclude current event (if editing!)

                for (final Event e : BackgroundService.getInstance().events) {
                    // if we're editing AND current event isn't the editing one OR
                    // if we're not editing
                    // put event to array
                    if (EventActivity.getInstance().event != null &&
                            (e.getUniqueID() != EventActivity.getInstance().event.getUniqueID()) ||
                            EventActivity.getInstance().event == null) {
                        mShownEventsCon.add(e.getName());
                        mShownEventsCondIDs.add(e.getUniqueID());

                        // also, fill the selected events array
                        //mSelectedEvents.add(e.getUniqueID());
                    }

                }

                // if there are no other events, we're screwed o_O
                if (mShownEventsCon.size() == 0) {
                    showMessageBox(context.getString(R.string.no_events_to_pick), true);
                    return;
                }

                // create array of booleans
                mEventsChecked = new boolean[mShownEventsCon.size()];


                // if we're editing, update boolean array with positives
                if (isEditing) {
                    //System.out.println("events from settings: "+ mEventsFromSettings.toString());
                    for (int i = 0; i < mShownEventsCon.size(); i++) {
                        // is current event ID equal to saved ID
                        if (mEventsFromSettings.contains(mShownEventsCondIDs.get(i))) {
                            // it does; so update bool array
                            mEventsChecked[i] = true;
                        }
                    }
                }

                // convert ArrayList<String> to String[]
                mShownEventsString = new String[mShownEventsCon.size()];
                mShownEventsString = mShownEventsCon.toArray(mShownEventsString);

                // finally, open a dialog
                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_events))
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // we have to pick at least one
                                if (mSelectedEventsCond.size() == 0) {
                                    showMessageBox(context.getString(R.string.pick_at_least_one_event), true);
                                    return ;
                                }


                                // save condition & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("selectevents", (new Gson().toJson(mSelectedEventsCond)));
                                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                cond.setSetting("text1",
                                        ((opt.getOptionType() == DialogOptions.type.EVENT_CONDITIONS_TRUE) ?
                                                context.getString(R.string.when_events_conditions_true) :
                                                context.getString(R.string.when_events_conditions_false))

                                );
                                cond.setSetting("text2", context.getString(R.string.events_selected, mSelectedEventsCond.size()));


                                // if we are editing in sub-dialog, clear previous entry
                                if (isEditing)
                                    removeConditionOrAction(index, opt);


                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setMultiChoiceItems(mShownEventsString, mEventsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked)
                                    mSelectedEventsCond.add(mShownEventsCondIDs.get(which));
                                else
                                    mSelectedEventsCond.remove(mShownEventsCondIDs.get(which));

                            }
                        })
                ;

                builder.show();



                break;



            /**
             * CASE: Battery Level
             */
            case BATTERY_LEVEL:

                /**
                 * battery level will be ranged with 2 seekbars
                 */
                final SeekBar batteryFrom = new SeekBar(context);
                final SeekBar batteryTo = new SeekBar(context);
                batteryFrom.setMax(100);
                batteryFrom.setMinimumWidth(50);
                batteryTo.setMax(100);
                batteryFrom.setProgress(10);
                batteryTo.setProgress(90);

                final TextView infoFrom = new TextView(context);
                final TextView infoTo = new TextView(context);

                /**
                 * if updating, import the seekbar entries
                 */
                if (isEditing) {

                    batteryFrom.setProgress(
                            Integer.parseInt(opt.getSetting("BATTERY_LEVEL_FROM"))
                    );

                    batteryTo.setProgress(
                            Integer.parseInt(opt.getSetting("BATTERY_LEVEL_TO"))
                    );

                }

                // update from & to text
                infoFrom.setText(context.getString(R.string.from) +
                        " ("+ batteryFrom.getProgress() +"%)");
                infoTo.setText(context.getString(R.string.to) +
                        " ("+ batteryTo.getProgress() +"%)");


                // here comes the super fun stuff!
                batteryTo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        infoTo.setText(context.getString(R.string.to) +
                                " ("+ batteryTo.getProgress() +"%)");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                batteryFrom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        infoFrom.setText(context.getString(R.string.from) +
                                " ("+ batteryFrom.getProgress() +"%)");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


                ScrollView scrollView = new ScrollView(context);
                LinearLayout newView = new LinearLayout(context);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                newView.setLayoutParams(parms);
                newView.setOrientation(LinearLayout.VERTICAL);
                newView.setPadding(15, 15, 15, 15);

                newView.addView(infoFrom);
                newView.addView(batteryFrom);
                newView.addView(infoTo);
                newView.addView(batteryTo);

                scrollView.addView(newView);

                builder
                        .setView(scrollView)
                        .setIcon(R.drawable.ic_battery)
                        .setTitle(context.getString(R.string.battery_level_description))
                        .setNegativeButton(context.getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })

                        .setPositiveButton(context.getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();


                                        /**
                                         * from>to?
                                         */
                                        if (batteryFrom.getProgress() > batteryTo.getProgress()) {

                                            showMessageBox(
                                                    context.getString(R.string.battery_from_cannot_be_higher_than_to),
                                                    false);

                                        }


                                        /**
                                         * store new entries here
                                         */
                                        else {


                                            // add new condition
                                            final DialogOptions cond = new DialogOptions(opt.getTitle(),
                                                    opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                            cond.setSetting("text1", context.getString(R.string.battery_level));
                                            cond.setSetting("text2",
                                                    context.getString(R.string.battery_at,
                                                            batteryFrom.getProgress() +"%",
                                                            batteryTo.getProgress() +"%"
                                                    )
                                            );

                                            /**
                                             * create setting with battery level without percentage
                                             */
                                            cond.setSetting("BATTERY_LEVEL_FROM",
                                                    String.valueOf(batteryFrom.getProgress())
                                            );

                                            cond.setSetting("BATTERY_LEVEL_TO",
                                                    String.valueOf(batteryTo.getProgress())
                                            );


                                            // editing.
                                            if (isEditing)
                                                removeConditionOrAction(index, opt);

                                            addNewConditionOrAction(context, cond, index);


                                        }


                                    }
                                })

                        .show();




//
//                final String[] mBatteryLevels = new String[] {
//                        "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"
//                };
//
//
//                /**
//                 * set which level is going to be checked by default
//                 *
//                 * (100% aka. last item in array)
//                 */
//                int mCheckedBattery = mBatteryLevels.length;
//
//                if (isEditing) {
//                    /**
//                     * since our battery setting is saved as 10, 20, 30,...
//                     * we have to get the key of it. divide by 10 and subtract 1
//                     */
//                    mCheckedBattery =
//                            (
//                                    Integer.parseInt(opt.getSetting("BATTERY_LEVEL")) / 10
//                            ) - 1;
//                }
//
//
//                builder
//                        //.setView(timerangeView)
//                        .setSingleChoiceItems(mBatteryLevels, mCheckedBattery, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//
//                                // add new condition
//                                final DialogOptions cond = new DialogOptions(opt.getTitle(),
//                                        opt.getDescription(), opt.getIcon(), opt.getOptionType());
//
//                                cond.setSetting("text1", context.getString(R.string.battery_level));
//                                cond.setSetting("text2", context.getString(R.string.battery_at, mBatteryLevels[which]));
//
//                                /**
//                                 * create setting with battery level without percentage
//                                 */
//                                cond.setSetting("BATTERY_LEVEL",
//                                        mBatteryLevels[which].replace("%", "")
//                                        );
//
//                                // editing.
//                                if (isEditing)
//                                    removeConditionOrAction(index, opt);
//
//                                addNewConditionOrAction(context, cond, index);
//
//                            }
//                        })
//                        .setIcon(R.drawable.ic_battery)
//
//                        .setTitle(context.getString(R.string.pick_battery_level))
//                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                builder.show();

                break;

            /**
             * CASE: Battery Status
             */
            case BATTERY_STATUS:

                final String[] mBatteryStatuses = new String[] {
                        context.getString(R.string.battery_charging),
                        context.getString(R.string.battery_discharging),
                        context.getString(R.string.battery_not_charging),
                        context.getString(R.string.battery_full)
                };


                /**
                 * set which level is going to be checked by default
                 *
                 * (100% aka. last item in array)
                 */
                int mCheckedBatteryStatus = mBatteryStatuses.length;

                if (isEditing) {
                    mCheckedBatteryStatus =
                                    Integer.parseInt(opt.getSetting("BATTERY_STATUS_KEY"));
                }


                builder
                        //.setView(timerangeView)
                        .setSingleChoiceItems(mBatteryStatuses, mCheckedBatteryStatus, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // add new condition
                                final DialogOptions cond = new DialogOptions(opt.getTitle(),
                                        opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("text1", context.getString(R.string.battery_status));
                                cond.setSetting("text2",
                                        context.getString(R.string.battery_status_description2,
                                                mBatteryStatuses[which])


                                );

                                /**
                                 * create setting with battery level without percentage
                                 */
                                cond.setSetting("BATTERY_STATUS", mBatteryStatuses[which]);
                                cond.setSetting("BATTERY_STATUS_KEY", String.valueOf(which));


                                // editing.
                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, index);

                            }
                        })
                        .setIcon(R.drawable.ic_battery)

                        .setTitle(context.getString(R.string.battery_level))
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.show();

                break;


            /**
             * ACTION: SHOW NOTIFICATION
             */
            case ACT_NOTIFICATION:

            // are we trying to edit the notification? because, uhm... we can't
            if (isEditing) {
                showMessageBox(context.getString(R.string.cannot_edit_notification_action), true);
                return ;
            }

            // save action & create new row
            final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

            //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
            //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
            cond.setSetting("text1", opt.getTitle());
            cond.setSetting("text2", context.getString(R.string.notification_will_appear));

            //addNewAction(context, cond);
            addNewConditionOrAction(context, cond, 0);

            break;

            /**
             * ACTION: PLAY SFEN
             */
            case ACT_PLAYSFEN:

                // are we trying to edit the notification? because, uhm... we can't
                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_play_sfen_action), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condPlaySfen = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condPlaySfen.setSetting("text1", opt.getTitle());
                condPlaySfen.setSetting("text2", context.getString(R.string.sfen_play_description));

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condPlaySfen, 0);

                break;

            /**
             * ACTION: ENABLE OR DISABLE WIFI
             */
            case ACT_WIFIENABLE:
            case ACT_WIFIDISABLE:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_wifi_action), true);
                    return ;
                }


                // save action & create new row
                final DialogOptions wificond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                wificond.setSetting("text1", opt.getTitle());
                wificond.setSetting("text2",
                        context.getString(R.string.wifi_description,
                                ((opt.getOptionType() == DialogOptions.type.ACT_WIFIENABLE) ?
                                        context.getString(R.string.enabled) :
                                        context.getString(R.string.disabled))
                                )
                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, wificond, 0);


                break;

            /**
             * ACT: MOBILE DATA ENABLE/DISABLE
             */
            case ACT_MOBILEENABLE:
            case ACT_MOBILEDISABLE:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_mobile_data_action), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions mobilecond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                mobilecond.setSetting("text1", opt.getTitle());
                mobilecond.setSetting("text2",

                        context.getString(R.string.mobile_data_description,
                                ((opt.getOptionType() == DialogOptions.type.ACT_MOBILEENABLE) ?
                                        context.getString(R.string.enabled) :
                                        context.getString(R.string.disabled))
                        )

                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, mobilecond, 0);



                break;

            /**
             * ACT: VIBRATE
             */
            case ACT_VIBRATE:

                // if we're editing, return error
                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_vibration_action), true);
                    return ;
                }

                final String[] vibrationTypes = {
                        context.getString(R.string.vibration_short),
                        context.getString(R.string.vibration_medium),
                        context.getString(R.string.vibration_long)};



                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.vibration_type))
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                                dialog.dismiss();

                            }
                        })
                        .setItems(vibrationTypes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // save action & create new row
                                final DialogOptions vibCond = new DialogOptions(opt.getTitle(), opt.getDescription(),
                                        opt.getIcon(), opt.getOptionType());

                                vibCond.setSetting("text1", opt.getTitle());
                                vibCond.setSetting("text2", context.getString(R.string.phone_will_vibrate));
                                vibCond.setSetting("vibrationtype", vibrationTypes[which]);

                                addNewConditionOrAction(context, vibCond, 0);
                            }
                        });

                builder.show();

                break;

            /**
             * ACT: DIALOG WITH TEXT
             */
            case ACT_DIALOGWITHTEXT:

                // Set an EditText view to get user input
                final TextView info = new TextView(context);
                final EditText input = new EditText(context);

                info.setText(context.getString(R.string.input_text));

                if (isEditing) {
                    input.setText(opt.getSetting("text"));
                }
                else {
                    input.setText(context.getString(R.string.input_text_default_string));
                }

                scrollView = new ScrollView(context);
                newView = new LinearLayout(context);
                parms = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                newView.setLayoutParams(parms);
                newView.setOrientation(LinearLayout.VERTICAL);
                newView.setPadding(15, 15, 15, 15);
                newView.addView(info, 0);
                newView.addView(input, 1);


                scrollView.addView(newView);

                builder
                        .setView(scrollView)
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Sfen!")
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // save action & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(),
                                        opt.getIcon(), opt.getOptionType());

                                cond.setSetting("text1", opt.getTitle());
                                cond.setSetting("text2", input.getText().toString());
                                cond.setSetting("text", input.getText().toString());

                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, 0);

                            }
                        })
                        //.set
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // just close the dialog if we didn't select the days
                                dialog.dismiss();

                            }
                        });


                builder.show();


                break;

            /**
             * ACT: open application
             */
            case ACT_OPENAPPLICATION:

                // package manager is..
                //private static PackageManager mPackageManager = getPackageManager();
                //mPackageManager.addPermission();
//get a list of installed apps.
                //List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);



                //final ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);
                //final ViewGroup installedApps = (ViewGroup) context.findViewById(R.id.condition_pick);
                final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
                final ViewGroup installedApps = (ViewGroup) dialogView.findViewById(R.id.condition_pick);


                installedApps.setVerticalScrollBarEnabled(true);
                        //findViewById();

                builder
                        .setView(dialogView)
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Sfen!")
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });


                final AlertDialog dialog = builder.create();

                // remove view
                //ViewGroup parent = (ViewGroup) installedApps.getParent();
                //parent.removeView(installedApps);

                final PackageManager pm = context.getPackageManager();

                for (final PackageInfo packageInfo : pm.getInstalledPackages(PackageManager.GET_META_DATA)) {
                    //if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    //    break;
                    // don't show apps that don't have launch activity
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null ||
                            (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                            ) {


//                    System.out.println("Installed package :" + packageInfo.packageName);
//                    System.out.println("Source dir : " + packageInfo.applicationInfo.sourceDir);
//                    System.out.println("version name: "+ packageInfo.versionName);

//                        System.out.println("app name: "+ packageInfo.applicationInfo.loadLabel(pm).toString());
//                        System.out.println("Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName).getAction());

                        //newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, mContainerOptions, false);
                        newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, installedApps, false);

                        ((TextView) newRow.findViewById(android.R.id.text1)).setText(packageInfo.applicationInfo.loadLabel(pm).toString());
                        ((TextView) newRow.findViewById(android.R.id.text2)).setText(packageInfo.packageName);


                        ((ImageButton) newRow.findViewById(R.id.dialog_icon))
                                .setImageDrawable(packageInfo.applicationInfo.loadIcon(pm));

                        // create onclick listener
                        newRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                dialog.dismiss();
                                ((ViewGroup)installedApps.getParent()).removeView(installedApps);

                                // save new action
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(),
                                        opt.getIcon(), opt.getOptionType());

                                cond.setSetting("text1",
                                        context.getString(R.string.open_application_description2,
                                                packageInfo.applicationInfo.loadLabel(pm).toString())
                                );
                                cond.setSetting("text2", packageInfo.packageName);
                                cond.setSetting("packagename", packageInfo.packageName);



                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, 0);


                            }
                        });

                        installedApps.addView(newRow);

                        newRow = null;

                    }

                }

                // if we don't have installed apps, add blank text
                if (installedApps == null) {
                    showMessageBox(context.getString(R.string.no_applications_installed), false);
                    return ;
                }



                //builder.show();
                dialog.show();


                break;


            /**
             * ACT: open shortcut
             */
            case ACT_OPENSHORTCUT:


                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_shortcut_action), true);
                    return ;
                }

                final int REQUEST_PICK_SHORTCUT = 0x100;
                final int REQUEST_CREATE_SHORTCUT = 0x200;

                final Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
                intent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.select_shortcut));



                AsyncTask<Void,Void,Void> mAsyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {


                            if (actionFrom == ACTION_FROM.PROFILE)
                                ProfileActivity.getInstance().startActivityForResult(intent, REQUEST_PICK_SHORTCUT);

                            if (actionFrom == ACTION_FROM.EVENT)
                                EventActivity.getInstance().startActivityForResult(intent, REQUEST_PICK_SHORTCUT);


                            return null;
                        }
                    }.execute();


                break;

            /**
             * ACT: DISABLE/ENABLE LOCK SCREEN
             */
            case ACT_LOCKSCREENENABLE:
            case ACT_LOCKSCREENDISABLE:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_lockscreen_action), true);
                    return ;
                }

                // save action & create new row
                final DialogOptions lockcond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                lockcond.setSetting("text1", opt.getTitle());
                lockcond.setSetting("text2",

                        context.getString(R.string.lockscreen_action_description,
                                ((opt.getOptionType() == DialogOptions.type.ACT_LOCKSCREENENABLE) ?
                                        context.getString(R.string.enabled) :
                                        context.getString(R.string.disabled))
                        )

                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, lockcond, 0);


                break;


            /**
             * ACT: RUN EVENT
             */
            case ACT_RUNEVENT:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.cannot_edit_run_event_action), true);
                    return ;
                }

                if (BackgroundService.getInstance().events.size() == 0) {
                    showMessageBox(context.getString(R.string.no_events_created), true);
                    return ;
                }

                /**
                 * create array of all possible events
                 */
                String[] events = new String[BackgroundService.getInstance().events.size()];

                for (int i = 0; i < BackgroundService.getInstance().events.size(); i++) {
                    events[i] = BackgroundService.getInstance().events.get(i).getName();
                }

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(context.getString(R.string.pick_event))
                        .setItems(events, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // save action & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("text1", opt.getTitle());
                                cond.setSetting("text2", 
                                        context.getString(R.string.event_will_run,
                                                BackgroundService.getInstance().events.get(i).getName()));

                                cond.setSetting("EVENT_UNIQUEID",
                                        String.valueOf(BackgroundService.getInstance().events.get(i).getUniqueID())
                                );

                                //addNewAction(context, cond);
                                addNewConditionOrAction(context, cond, 0);


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

                break;



            case ACT_RUNSCRIPT:

                if (isEditing) {
                    showMessageBox(context.getString(R.string.runscript_cannot_edit), true);
                    return ;
                }


                /**
                 * open default file manager
                 */
                final Intent intentFileManager = new Intent();
                intentFileManager.setAction(Intent.ACTION_GET_CONTENT);
                intentFileManager.setType("file/*");
                //context.startActivityForResult(intentFileManager);
                final int REQUEST_FILEMANAGER_SHORTCUT = 101;

                if (actionFrom == ACTION_FROM.PROFILE)
                    ProfileActivity.getInstance().startActivityForResult(intentFileManager,
                            REQUEST_FILEMANAGER_SHORTCUT);

                if (actionFrom == ACTION_FROM.EVENT)
                    EventActivity.getInstance().startActivityForResult(intentFileManager,
                            REQUEST_FILEMANAGER_SHORTCUT);




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
    protected static void showNotification(final Service context, String title, String description, int icon) {

        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //NotificationManager mNM = Main.getInstance().mNM;
        Notification note = new Notification(icon, title, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, Main.class), 0);

        note.setLatestEventInfo(context, title, description, pi);
        note.flags |= Notification.FLAG_NO_CLEAR;

        if (mNM == null) {
            mNM = (NotificationManager) Main.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            //mNM.notify(1337, note);
        }

        //mNM.notify(1337, note);

        //context.getApplicationContext().st
        context.startForeground(1337, note);

    }

    /**
     * remove single Condition or Action
     *
     * @param
     */
    private static void removeConditionOrAction(final int index, final DialogOptions entry) {

        /**
         * if removing action, call ProfileActivity function
         */
        if (entry.isAction() && actionFrom == ACTION_FROM.PROFILE) {
            ProfileActivity.getInstance().removeAction(index, entry);
            return ;
        }

        // when clicking recycle bin at condition/action, remove it from view and
        // from array of all conditions/actions

        // remove ACTION from container first
        if (entry.isAction()) {
            EventActivity.getInstance().mContainerAction.removeViewAt(index);
        }
        // remove CONDITION from container first
        else {
            EventActivity.getInstance().mContainerCondition.removeViewAt(index);
        }
        //container.removeView(newRow);

        // UPDATING SINGLE EVENT!!!
        // remove from conditions, depending on if we're adding to new event
        // or existing event
        if (EventActivity.getInstance().isUpdating) {
            // updating ACTION
            if (entry.isAction()) {
                EventActivity.getInstance().updatedActions.remove(
                        EventActivity.getInstance().updatedActions.indexOf(entry)
                );
            }
            // otherwise, updating CONDITION
            else {
                // since we're deleting condition, we have to call
                // updateChecker: updateEventConditionTimers
                // we are forcing event of current entry to disable, then enable
                // to do that, we are creating new TEMP event, add only current condition
                // set temp event to disabled and run updateEventConditionTimers.
                // good luck with that :|
                final Event mTempEvent = EventActivity.getInstance().event;
                //boolean mWasEventEnabled = (mTempEvent.isEnabled()) ? true : false;
                mTempEvent.setEnabled(false);
                mTempEvent.setConditions(new ArrayList<DialogOptions>(){{
                    add(entry);
                }});
                BackgroundService.getInstance().updateEventConditionTimers(
                        new ArrayList<Event>(){{add(mTempEvent);}}
                );

                EventActivity.getInstance().updatedConditions.remove(
                        EventActivity.getInstance().updatedConditions.indexOf(entry)
                );
            }

            // we changed something, so set the changed boolean
            EventActivity.getInstance().isChanged = true;


        }

        // CREATING SINGLE EVENT!!!
        else {
            // adding ACTION
            if (entry.isAction()) {
                EventActivity.getInstance().actions.remove(
                        EventActivity.getInstance().actions.indexOf(entry)
                );
            }
            // adding CONDITION
            else {
                EventActivity.getInstance().conditions.remove(
                        EventActivity.getInstance().conditions.indexOf(entry)
                );
            }
        }


        // clear actionFrom
        actionFrom = ACTION_FROM.NULL;


    }


    /**
     * add new condition OR action
     */
    protected static void addNewConditionOrAction(final Activity context, final DialogOptions entry, final int index) {

        //System.out.println("*** ACTION FROM "+ actionFrom);

        /**
         * if adding action, call ProfileActivity function
         */
        if (entry.isAction() && actionFrom == ACTION_FROM.PROFILE) {
            ProfileActivity.getInstance().addNewAction(context, entry, index);
            return ;
        }


        // the only thing we have to check if we're editing entry is,
        // if we have at least one setting stored. if so, all is good in our wonderland
        //final boolean isEditing = (cond.getSettings().size() > 0) ? true : false;

        // add condition to list of conditions of Event
        if (EventActivity.getInstance().isUpdating) {
            // updating action/cond
            if (entry.isAction())
                EventActivity.getInstance().updatedActions.add(entry);
            else
                EventActivity.getInstance().updatedConditions.add(entry);
        }
        // adding NEW
        else {
            //entry.setSetting("uniqueID", new Random().nextInt());
            if (entry.isAction())
                EventActivity.getInstance().actions.add(entry);
            else
                EventActivity.getInstance().conditions.add(entry);
        }

        // get options that we need for interface
        String title = entry.getSetting("text1");
        String description = entry.getSetting("text2");
        int icon = entry.getIcon();

        // add new row to actions/conditions now
        final ViewGroup newRow;

        if (entry.isAction()) {
            newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                    R.layout.condition_single_item, EventActivity.getInstance().mContainerAction, false);
        }
        else {
            newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                    R.layout.condition_single_item, EventActivity.getInstance().mContainerCondition, false);
        }

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
                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);
                openSubDialog(context, entry, index);
                //showMessageBox("clicked " + entry.getTitle() + ", " + entry.getOptionType() +" type: "+ entry.isItemConditionOrAction() +" on index "+ index, false);
            }
        });

        newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when clicking recycle bin at condition, remove it from view and
                // from array of all conditions

                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);
                removeConditionOrAction(index, entry);

            }
        });


        // add action to container
        if (entry.isAction())
            EventActivity.getInstance().mContainerAction.addView(newRow, index);
            // add condition to container
        else
            EventActivity.getInstance().mContainerCondition.addView(newRow, index);


        // clear actionFrom
        actionFrom = ACTION_FROM.NULL;

    }


    protected static boolean hasGooglePlayServices() {

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(BackgroundService.getInstance()) ==
                ConnectionResult.SUCCESS)
            return true;
        else {
            showMessageBox(Main.getInstance().getString(R.string.google_play_service_not_installed), true);
            return false;
        }


    }

    protected String replaceTextPatterns(String text) {
        String rText = text;

        return rText;
    }

    protected static String getDateLong(Calendar cal, Context context) {
        return android.text.format.DateFormat.getDateFormat(context).format(cal.getTime()) +
                " " +
                android.text.format.DateFormat.getTimeFormat(context).format(cal.getTime());
    }
}