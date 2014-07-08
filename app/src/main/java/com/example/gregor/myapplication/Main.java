package com.example.gregor.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Main extends Activity {
    private static Main sInstance = null;
    private ViewGroup mContainerView;

    // Map with options. Instead of creating more variables to use around
    // activities, I've created HashMap; putting settings here if needed.
    Map options = new HashMap();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (sInstance == null) {
            sInstance = this;
        }

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
        return super.onOptionsItemSelected(item);
    }


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
}
