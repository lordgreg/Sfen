package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventActivity extends Activity {
    private ViewGroup mContainerCondition, mContainerAction, mContainerOptions;
    private TextView eventName;

    // list of possible Conditions in Options
    ArrayList<DialogOptions> optConditions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions("Location", "Entering/leaving location", R.drawable.ic_map, DialogOptions.type.CONDITION));
        add(new DialogOptions("Time", "Time range", R.drawable.ic_time, DialogOptions.type.CONDITION));
        add(new DialogOptions("Days", "Day(s) of week.", R.drawable.ic_date, DialogOptions.type.CONDITION));
        add(new DialogOptions("Wifi", "Connected/disconnected from Wifi", R.drawable.ic_wifi, DialogOptions.type.CONDITION));
    }};

    private void openConditions() {
        // container for condition in dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);

        // fill all options in container
        ViewGroup newRow;
        DialogOptions opt;
        for (int i = 0; i < optConditions.size(); i++)
        //for (String key : opt_conditions.keySet())
        {
            //opt = optConditions[i];
            opt = optConditions.get(i);

            newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, mContainerOptions, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(opt.getTitle());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(opt.getDescription());
            ((ImageButton) newRow.findViewById(R.id.dialog_icon))
                    .setImageDrawable(getResources().getDrawable(opt.getIcon()));
            //((TextView) newRow.findViewById(android.R.id.text2)).setText(getString(R.string.condition_new_sub));
            mContainerOptions.addView(newRow);

            newRow = null;
        }

        /*mContainerOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("id: "+ v.getId());
                System.out.println("tag: "+ v.getTag());
            }
        });
        */


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



        AlertDialog alert = builder.create();
        alert.show();


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);


        //DialogOptions[] dialogOptionses = new DialogOptions[];
        //dialogOptionses[0] = new DialogOptions("test", "test", 1);


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
            Log.d("info", "number of dialogOptionses: "+ mContainerCondition.getChildCount());

            eventName = (TextView) findViewById(R.id.event_name);

            Main.getInstance().options.put("eventSave", "1");
            Main.getInstance().options.put("eventName", eventName.getText().toString());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
