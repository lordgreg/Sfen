package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
    private static ViewGroup newRow;

    // days
    final String[] sDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};


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
    protected void openDialog(final Activity context,
                                     final ArrayList<DialogOptions> options,
                                     final String title) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        builder.setView(dialogView)
                .setIcon(context.getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle(title)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
    protected void openSubDialog(final Activity context, final DialogOptions opt, final int index) {
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
                                    cond.setSetting("radius", ""+ circle.getRadius());

                                    cond.setSetting("text1",
                                            ((opt.getOptionType() == DialogOptions.type.LOCATION_LEAVE) ?
                                                    "Outside" : "Inside") + " Location"
                                    );
                                    cond.setSetting("text2", "Lat: " + String.format("%.2f", marker.getPosition().latitude) +
                                            ", Long: " + String.format("%.2f", marker.getPosition().longitude) +
                                            ", Rad: "+ ((TextView)dialogMap.findViewById(R.id.radius_info)).getText().toString());


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
                        .setTitle("Pick day(s)")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

                final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

                // set 24hour format
                final TimePicker timeFrom = (TimePicker) timerangeView.findViewById(R.id.time_from);
                final TimePicker timeTo = (TimePicker) timerangeView.findViewById(R.id.time_to);
                //((TimePicker) timerangeView.findViewById(R.id.time_from)).setIs24HourView(true);
                //((TimePicker) timerangeView.findViewById(R.id.time_to)).setIs24HourView(true);
                timeFrom.setIs24HourView(true);
                timeTo.setIs24HourView(true);

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
                                        String.format("%02d", timeFrom.getCurrentHour()) + ":" +
                                        String.format("%02d", timeFrom.getCurrentMinute())
                                        + " to " +
                                        String.format("%02d", timeTo.getCurrentHour()) + ":" +
                                        String.format("%02d", timeTo.getCurrentMinute())
                                        + "");

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
                timePicker.setIs24HourView(true);


                // we're editing option, get stored time out of settings
                if (isEditing) {
                    timePicker.setCurrentHour(Integer.parseInt(opt.getSetting("hour")));
                    timePicker.setCurrentMinute(Integer.parseInt(opt.getSetting("minute")));
                }

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Pick time")
                        .setView(timePicker)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                // add new condition
                                final DialogOptions cond = new DialogOptions(opt.getTitle(),
                                        opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("hour", timePicker.getCurrentHour().toString());
                                cond.setSetting("minute", timePicker.getCurrentMinute().toString());
                                cond.setSetting("text1", "Specific Time");
                                cond.setSetting("text2", "At exactly " +
                                        String.format("%02d", timePicker.getCurrentHour()) + ":" +
                                        String.format("%02d", timePicker.getCurrentMinute())
                                        + "");

                                // editing.
                                if (isEditing)
                                    removeConditionOrAction(index, opt);

                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                    showMessageBox("Wifi is disabled. Please turn it on first.", false);
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
                    showMessageBox("You cannot edit Screen On/Off Condition. You can only remove it.", true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condScreen = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condScreen.setSetting("text1", opt.getTitle());
                condScreen.setSetting("text2", "Screen is "+
                        ((opt.getOptionType() == DialogOptions.type.SCREEN_ON) ? "On" : "Off")
                        +".");

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condScreen, 0);

                break;


            /**
             * CONDITION: GPS on/off
             */
            case GPS_ENABLED:
            case GPS_DISABLED:

                if (isEditing) {
                    showMessageBox("You cannot edit GPS On/Off Condition. You can only remove it.", true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condGps = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condGps.setSetting("text1", opt.getTitle());
                condGps.setSetting("text2", "GPS is "+
                        ((opt.getOptionType() == DialogOptions.type.GPS_ENABLED) ? "On" : "Off")
                        +".");

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condGps, 0);


                break;

            /**
             * CONDITION: Connecting/disconnecting on Cell Towers
             */
            case CELL_IN:
            case CELL_OUT:

                // array of selected items in dialog
                final ArrayList<String> mSelectedCells = new ArrayList<String>();

                // array of cell towers we are showing in dialog
                final ArrayList<String> mCellTowers = new ArrayList<String>();

                // if editing, retrieve saved cells
                ArrayList<String> mCellsFromSettings = null;
                if (isEditing) {
                    mCellsFromSettings = gson.fromJson(opt.getSetting("selectedcell"),
                                new TypeToken<List<String>>(){}.getType());

                    // if we have cells stored, add them to selected cells
                    // and add them to visible cells
                    if (mCellsFromSettings != null) {
                        mSelectedCells.addAll(mCellsFromSettings);
                        mCellTowers.addAll(mCellsFromSettings);
                    }
                }


                // get current cell tower ID
                //TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                CellConnectionInfo cellInfo = new CellConnectionInfo(context);

                if (cellInfo.isError()) {
                    showMessageBox(cellInfo.getError(), true);
                    break;
                }
                // save current cell id
                String mCellCurrent = cellInfo.getCellId();
                //String mCellCurrent = "1337:1337:1337";
                // set the size of booleans to match settings
                boolean[] mCellChecked = new boolean[mCellTowers.size()];

                // check all cells from settings
                // aka set their parameters to TRUE
                Arrays.fill(mCellChecked, Boolean.TRUE);

                // boolean that will change if current
                boolean mCellIsAlreadyStored = false;

                // loop through our list of cells;
                // if any from stored is our current, check it
                if (mCellTowers.size() > 0) {
                    for (int i = 0; i < mCellTowers.size(); i++) {

                        // found a match!
                        if (mCellTowers.contains(mCellCurrent)) {

                            mCellIsAlreadyStored = true;
                            //mCellChecked[i] = true;
                            //System.out.println("we found a match");
                            break;
                        }

                    }
                }

                System.out.println("cell towers we're showing: "+ mCellTowers.toString());


                //System.out.println("cell checked: "+ mCellChecked.toString());

                // if cell tower is new, and not storred in array add it to array
                if (!mCellIsAlreadyStored) {
                    mCellTowers.add(mCellCurrent);

                    // create new bool array
                    boolean[] mCellCheckedTmp = new boolean[mCellTowers.size()];
                    //mCellCheckedTmp =
                    System.arraycopy(mCellChecked, 0, mCellCheckedTmp, 0, mCellChecked.length);
                    mCellChecked = mCellCheckedTmp;
                }

                //System.out.println("cell checked: "+ mCellChecked.toString());
//                if (2==2) {
//                    System.out.println("*** test");
//                    return;
//                }

                // create array of strings from ArrayList for celltowers
                String[] mCellTowerStrings = new String[mCellTowers.size()];
                mCellTowerStrings = mCellTowers.toArray(mCellTowerStrings);

                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Pick Cell ID's (Current: "+ mCellCurrent +")")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // we have to pick at least one
                                if (mSelectedCells.size() == 0) {
                                    showMessageBox("Pick at least one radio tower!", true);
                                    return ;
                                }


                                // save condition & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());


                                cond.setSetting("selectedcell", (new Gson().toJson(mSelectedCells)));
                                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                cond.setSetting("text1", ((opt.getOptionType() == DialogOptions.type.CELL_IN) ? "Connected to " : "Not connected to ") + " specific cells");
                                cond.setSetting("text2", "Cells selected: "+ mSelectedCells.size());


                                // if we are editing in sub-dialog, clear previous entry
                                if (isEditing)
                                    removeConditionOrAction(index, opt);


                                addNewConditionOrAction(context, cond, index);


                            }
                        })
                        .setMultiChoiceItems(mCellTowerStrings, mCellChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked)
                                    mSelectedCells.add(mCellTowers.get(which));
                                else
                                    mSelectedCells.remove(mCellTowers.get(which));

                            }
                        })
                ;

                builder.show();


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
                    showMessageBox("There are no other events to pick", true);
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
                        .setTitle("Pick Events")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                // we have to pick at least one
                                if (mSelectedEvents.size() == 0) {
                                    showMessageBox("Pick at least one Event!", true);
                                    return ;
                                }


                                // save condition & create new row
                                final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                                cond.setSetting("selectevents", (new Gson().toJson(mSelectedEvents)));
                                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                                cond.setSetting("text1", "When event(s)"+ ((opt.getOptionType() == DialogOptions.type.EVENT_RUNNING) ? " " : " not ") + "running");
                                cond.setSetting("text2", "Events selected: "+ mSelectedEvents.size());


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
             * ACTION: SHOW NOTIFICATION
             */
            case ACT_NOTIFICATION:

            // are we trying to edit the notification? because, uhm... we can't
            if (isEditing) {
                showMessageBox("You cannot edit Notification action. You can only remove it.", true);
                return ;
            }

            // save action & create new row
            final DialogOptions cond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

            //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
            //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
            cond.setSetting("text1", opt.getTitle());
            cond.setSetting("text2", "Notification will appear.");

            //addNewAction(context, cond);
            addNewConditionOrAction(context, cond, 0);

            break;

            /**
             * ACTION: PLAY SFEN
             */
            case ACT_PLAYSFEN:

                // are we trying to edit the notification? because, uhm... we can't
                if (isEditing) {
                    showMessageBox("You cannot edit Play Sfen action. You can only remove it.", true);
                    return ;
                }

                // save action & create new row
                final DialogOptions condPlaySfen = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                condPlaySfen.setSetting("text1", opt.getTitle());
                condPlaySfen.setSetting("text2", "Sound of Sfen will be heard");

                //addNewAction(context, cond);
                addNewConditionOrAction(context, condPlaySfen, 0);

                break;

            /**
             * ACTION: ENABLE OR DISABLE WIFI
             */
            case ACT_WIFIENABLE:
            case ACT_WIFIDISABLE:

                if (isEditing) {
                    showMessageBox("You cannot edit Wifi enable/disable action. You can only remove it.", true);
                    return ;
                }


                // save action & create new row
                final DialogOptions wificond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                wificond.setSetting("text1", opt.getTitle());
                wificond.setSetting("text2", "Wifi will be "+
                        ((opt.getOptionType() == DialogOptions.type.ACT_WIFIENABLE) ? "enabled" : "disabled")
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
                    showMessageBox("You cannot edit Mobile data enable/disable action. You can only remove it.", true);
                    return ;
                }

                // save action & create new row
                final DialogOptions mobilecond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                mobilecond.setSetting("text1", opt.getTitle());
                mobilecond.setSetting("text2", "Mobile data will be "+
                                ((opt.getOptionType() == DialogOptions.type.ACT_MOBILEENABLE) ? "enabled" : "disabled")
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
                    showMessageBox("You cannot edit Vibration type action. You can only remove it.", true);
                    return ;
                }

                final String[] vibrationTypes = {"Short", "Medium", "Long"};



                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Vibration type")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                                vibCond.setSetting("text2", "Phone will vibrate");
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

                info.setText("Input text");

                if (isEditing) {
                    input.setText(opt.getSetting("text"));
                }
                else {
                    input.setText("Event triggered! It's time for Sfen Lambada dance!");
                }

                LinearLayout newView = new LinearLayout(context);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                newView.setLayoutParams(parms);
                newView.setOrientation(LinearLayout.VERTICAL);
                newView.setPadding(15, 15, 15, 15);
                newView.addView(info, 0);
                newView.addView(input, 1);



                builder
                        .setView(newView)
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle("Sfen!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

                                cond.setSetting("text1", "Open "+ packageInfo.applicationInfo.loadLabel(pm).toString());
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
                    showMessageBox("No applications installed.", false);
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
                    showMessageBox("You cannot edit Shortcut action. You can only delete it.", true);
                    return ;
                }
                /**
                 * widgets
                 */
/*
                AppWidgetManager wmanager=AppWidgetManager.getInstance(Main.getInstance());
                List<AppWidgetProviderInfo>infoList=wmanager.getInstalledProviders();
                for(AppWidgetProviderInfo shinfo:infoList)
                {

                    Log.d("sfen", "Name-" + shinfo.label);
                    Log.d("sfen", "Configure Name-"+shinfo.configure);
                    Log.d("sfen", "Provider Name-"+shinfo.provider);


                }
                */
                final int REQUEST_PICK_SHORTCUT = 0x100;
                final int REQUEST_CREATE_SHORTCUT = 0x200;

                Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
                intent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
                ProfileActivity.getInstance().startActivityForResult(intent, REQUEST_PICK_SHORTCUT);

/*
                //com.google.android.gm/com.google.android.gm.widget.GmailWidgetProvider

                String packageName = "com.google.android.gm/com.google.android.gm.widget.GmailWidgetProvider";

                PackageManager pmOpen = Main.getInstance().getPackageManager();
                // open app
                //Intent appIntent = new Intent(Intent.ACTION_MAIN);
                //appIntent.setClassName("com.android.settings", "com.android.settings.Settings");
                Intent appIntent = pmOpen.getLaunchIntentForPackage(packageName);
                appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                if (appIntent != null)
                    startActivity(appIntent);
*/
/*
                List<ResolveInfo> appList = new ArrayList<ResolveInfo>();

                final PackageManager mPackageManager = Main.getInstance().getPackageManager();
                List<PackageInfo> packages = mPackageManager.getInstalledPackages(0);
                Intent mainIntent = new Intent(Intent.ACTION_MAIN);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                for(PackageInfo pi : packages) {
                    mainIntent.setPackage(pi.packageName);
                    List<ResolveInfo> activityList = mPackageManager.queryIntentActivities(mainIntent, 0);
                    for(ResolveInfo ri : activityList) {
                        appList.add(ri);
                        System.out.println("resolve info: "+ ri.resolvePackageName);
                        System.out.println("parent activity: "+ ri.activityInfo.parentActivityName);
                        System.out.println("resolve package name: "+ ri.resolvePackageName);

                    }
                }

                Collections.sort(appList, new ResolveInfo.DisplayNameComparator(mPackageManager));

                System.out.println("resolve info list");
                System.out.println(appList.toString());
*/



/*

                final PackageManager pm1 = context.getPackageManager();
                mPackageManager.queryIntentActivities(mainIntent, 0);


                for (final PackageInfo packageInfo : pm1.getInstalledPackages(PackageManager.GET_META_DATA)) {
                    //if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    //    break;
                    // don't show apps that don't have launch activity
                    if (pm1.getLaunchIntentForPackage(packageInfo.packageName) != null ||
                            (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                            ) {
                    }


                    System.out.println("*** "+ packageInfo.applicationInfo.loadLabel(pm1).toString());

                    //String test

                }*/





                break;

            /**
             * ACT: DISABLE/ENABLE LOCK SCREEN
             */
            case ACT_LOCKSCREENENABLE:
            case ACT_LOCKSCREENDISABLE:

                if (isEditing) {
                    showMessageBox("You cannot edit Lock screen enable/disable action. You can only remove it.", true);
                    return ;
                }

                // save action & create new row
                final DialogOptions lockcond = new DialogOptions(opt.getTitle(), opt.getDescription(), opt.getIcon(), opt.getOptionType());

                //cond.setSetting("selectedWifi", (new Gson().toJson(mSelectedSSID)));
                //cond.setSetting("text1", "Days ("+ selectedWifi.size() +")");
                lockcond.setSetting("text1", opt.getTitle());
                lockcond.setSetting("text2", "Lock screen will be "+
                                ((opt.getOptionType() == DialogOptions.type.ACT_LOCKSCREENENABLE) ? "enabled" : "disabled")
                );

                //addNewAction(context, cond);
                addNewConditionOrAction(context, lockcond, 0);


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
    private void removeConditionOrAction(final int index, final DialogOptions entry) {

        /**
         * if removing action, call ProfileActivity function
         */
        if (entry.isAction()) {
            ProfileActivity.getInstance().removeAction(index, entry);
            return ;
        }


        // when clicking recycle bin at condition/action, remove it from view and
        // from array of all conditions/actions

        // remove CONDITION from container first
        EventActivity.getInstance().mContainerCondition.removeViewAt(index);
        //container.removeView(newRow);

        // UPDATING SINGLE EVENT!!!
        // remove from conditions, depending on if we're adding to new event
        // or existing event
        if (EventActivity.getInstance().isUpdating) {

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


            // we changed something, so set the changed boolean
            EventActivity.getInstance().isChanged = true;


        }

        // CREATING SINGLE EVENT!!!
        else {
            // adding CONDITION
            EventActivity.getInstance().conditions.remove(
                    EventActivity.getInstance().conditions.indexOf(entry)
            );
        }
    }


    /**
     * add new condition OR action
     */
    protected void addNewConditionOrAction(final Activity context, final DialogOptions entry, final int index) {

        /**
         * if adding action, call ProfileActivity function
         */
        if (entry.isAction()) {
            ProfileActivity.getInstance().addNewAction(context, entry, index);
            return ;
        }


        // the only thing we have to check if we're editing entry is,
        // if we have at least one setting stored. if so, all is good in our wonderland
        //final boolean isEditing = (cond.getSettings().size() > 0) ? true : false;

        // add condition to list of conditions of Event
        if (EventActivity.getInstance().isUpdating) {
            EventActivity.getInstance().updatedConditions.add(entry);
        }
        // adding NEW
        else {
            //entry.setSetting("uniqueID", new Random().nextInt());
            EventActivity.getInstance().conditions.add(entry);
        }

        // get options that we need for interface
        String title = entry.getSetting("text1");
        String description = entry.getSetting("text2");
        int icon = entry.getIcon();

        // add new row to actions/conditions now
        final ViewGroup newRow;

        newRow = (ViewGroup) LayoutInflater.from(context).inflate(
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
                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);
                openSubDialog(context, entry, index);
                //showMessageBox("clicked " + entry.getTitle() + ", " + entry.getOptionType() +" type: "+ entry.isItemConditionOrAction() +" on index "+ index, false);
            }
        });

        /**
         * delete button for single item
         */
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
        EventActivity.getInstance().mContainerCondition.addView(newRow, index);

    }


    protected static boolean hasGooglePlayServices() {

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(BackgroundService.getInstance()) ==
                ConnectionResult.SUCCESS)
            return true;
        else {
            showMessageBox("Google Play Services not installed!", true);
            return false;
        }


    }

    protected String replaceTextPatterns(String text) {
        String rText = text;

        return rText;
    }



}