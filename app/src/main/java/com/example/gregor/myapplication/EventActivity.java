package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class EventActivity extends Activity {
    private ViewGroup mContainerCondition, mContainerAction, mContainerOptions;
    private TextView eventName;

    // conditions array with icons
    Map<String, Integer> opt_conditions = new HashMap<String, Integer>() {{
       put("Location", android.R.drawable.ic_dialog_map);
       put("Time", android.R.drawable.ic_menu_today);
       put("Message", android.R.drawable.ic_dialog_email);
    }};

    private void openConditions() {
        // container for condition in dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        // add all options from hashmap to string[]
        String[] options = new String[opt_conditions.size()];
        int i = 0;
        for (String key : opt_conditions.keySet()) {
            //System.out.println( key );
            options[i++] = key;
        }
         //   options[i] = opt_conditions.keySet().con
        //}

        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.container_dialog_options);

        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.dialog_pick_single, mContainerOptions, false);

        mContainerOptions.addView(newView, 0);
        //mContainerOptions.addView(newView, 0);
        //mContainerOptions.addView(newView, 0);

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("test", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


        //View mySubview = getLayoutInflater().inflate(R.layout.dialog_pick_condition, mContainerOptions);
        //mContainerOptions = LayoutInflater.from(this).inflate(R.id.container_dialog_options, this);
        //mContents = LayoutInflater.from(ctx).inflate(R.layout.mycustomview, this).
        //mContainerCondition = (ViewGroup) findViewById(R.id.condition_container);

        //final ViewGroup newOption = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
        //        R.layout.dialog_pick_single, mContainerOptions, false);
        //        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
        //          R.layout.condition_action_header, mContainerCondition, false);

        //((TextView) newOption.findViewById(android.R.id.text1)).setText("test");
        //((ImageButton) newOption.findViewById(R.id.dialog_icon)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_today));

        // container is now filled.
        //String a = "b";
        //mContainerOptions.addView(newOption, 0);


        //setContentView( R.layout.activity_event );
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_pick_condition, null))
                // Add action buttons
                .setPositiveButton("test", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

*/
        //ListAdapter adapter = new ArrayAdapter<String>(
         //       getApplicationContext(), R.layout.list_row, items)

        //ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.dialog_pick_single, opt_conditions);
/*
        // dialog now
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a tile set");
        builder.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int item) {
                        //Toast.makeText(MyApp.this, "You selected: " + items[item],Toast.LENGTH_LONG).show();
                        //dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);


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
                openConditions();
                Toast.makeText(getBaseContext(), "picking new condition", Toast.LENGTH_SHORT).show();
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
            Log.d("info", "number of conditions: "+ mContainerCondition.getChildCount());

            eventName = (TextView) findViewById(R.id.event_name);

            Main.getInstance().options.put("eventSave", "1");
            Main.getInstance().options.put("eventName", eventName.getText().toString());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
