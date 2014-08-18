package gpapez.sfen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Profile Window
 */
public class FragmentProfile extends Fragment {

    /**
     * VARIABLES
     */

    // singleton
    private static FragmentProfile sInstance;

    // container for our profiles
    private ViewGroup mContainerView;

    // our fragment view
    private View mView;


    /**
     * CONSTRUCTOR
     */
    public FragmentProfile() {


    }


    //@Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * set singleton
         */
        sInstance = this;

        /**
         * set View
         */
        mView = inflater.inflate(R.layout.activity_main_profiles, container, false);


        /**
         * refresh profiles view
         */
        refreshProfilesView();


        return mView;
    }

    /**
     *
     * REFRESH PROFILE CONTAINER
     *
     */
    /**
     * refresh PROFILES view
     */
    protected void refreshProfilesView() {

        mContainerView = (ViewGroup) mView.findViewById(R.id.container_profiles);
        mContainerView.removeAllViews();


        // if events array is empty, show "add new event" textview
        if (BackgroundService.getInstance().profiles.size() == 0) {
            //Main.getInstance().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            mView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        } else {
            mView.findViewById(android.R.id.empty).setVisibility(View.GONE);
        }


        // fill the profiles from array
        for (final Profile p : BackgroundService.getInstance().profiles) {
            final ViewGroup newRow = (ViewGroup) LayoutInflater.from(Main.getInstance()).inflate(
                    R.layout.main_single_item, mContainerView, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(p.getName());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                    (p.isActive()) ? sInstance.getString(R.string.active) : sInstance.getString(R.string.ready));

            ((ImageButton) newRow.findViewById(R.id.single_edit))
                    .setImageDrawable(getResources().getDrawable(p.getIcon()));

            // change color depending on profile color
            if (p.isActive())
                ((TextView) newRow.findViewById(android.R.id.text1)).setTextColor(Color.BLUE);


            // add on long press event (text1, text2, event_container
            newRow.findViewById(android.R.id.text1).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickSingleProfile(p, newRow);
                    return true;
                }
            });

            newRow.findViewById(android.R.id.text2).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickSingleProfile(p, newRow);
                    return true;
                }
            });


            newRow.findViewById(R.id.event_container).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onLongClickSingleProfile(p, newRow);
                    return true;
                }
            });

            // EDIT EVENT > open Event activity and pass event object to it!
            newRow.findViewById(R.id.single_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleProfile(p);
                }
            });

            // same goes with text1, text2 and event_container
            newRow.findViewById(android.R.id.text1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleProfile(p);
                }
            });
            newRow.findViewById(android.R.id.text2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleProfile(p);
                }
            });
            newRow.findViewById(R.id.event_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSingleProfile(p);
                }
            });


            // add new row to container
            mContainerView.addView(newRow, 0);

            // update preferences
            BackgroundService.getInstance().mPreferences.setPreferences("profiles",
                    BackgroundService.getInstance().profiles);


        }

    }



    /**
     * CLICK ON SINGLE PROFILE OPENS PROFILE ACTIVITY
     *
     * @param p Profile
     */
    private void onClickSingleProfile(Profile p) {
        Intent i = new Intent(Main.getInstance(), ProfileActivity.class);
        i.putExtra("sProfile", (new Gson().toJson(p)));
        i.putExtra("sProfileIndexKey", BackgroundService.getInstance().profiles.indexOf(p));
        startActivity(i);
    }

    /**
     * LONG CLICK SINGLE PROFILE
     *
     * should open popup with options.
     */
    private void onLongClickSingleProfile(final Profile p, final ViewGroup newRow) {
        // array of options
        final String[] sOptions = {
                sInstance.getString(R.string.activate),
                sInstance.getString(R.string.edit),
                sInstance.getString(R.string.delete)};
        // show dialog with more options for single event
        final AlertDialog.Builder builder = new AlertDialog.Builder(Main.getInstance());
        builder
                .setTitle(p.getName())
                .setItems(sOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // The 'which' argument contains the index position
                        // of the selected item
                        // 0 activate, 1 edit, 2 delete
                        if (which == 0) {

                            activateProfile(p);

                        }
                        if (which == 1) {

                            // go to edit mode
                            onClickSingleProfile(p);

                        }

                        /**
                         * delete
                         */
                        if (which == 2) {

                            /**
                             * is profile active in any event?
                             */
                            // we're creating continueDeleting variable as array so we can change its 1st
                            // value, even if boolean set to static.
                            //final boolean[] continueDeleting = new boolean[]{true};


                            /**
                             * reset events profileID where specified Profile
                             * unique ID is given to -1
                             */
                            for (int i = 0; i < BackgroundService.getInstance().events.size(); i++) {

                                if (BackgroundService.getInstance().events.get(i).getProfileID()
                                        == p.getUniqueID())
                                    BackgroundService.getInstance().events.get(i).setProfileID(-1);

                            }


                            // delete row AND spot in events
                            mContainerView.removeView(newRow);
                            BackgroundService.getInstance().profiles.remove(p);

                            // update preferences
                            BackgroundService.getInstance().mPreferences.setPreferences("profiles",
                                    BackgroundService.getInstance().profiles);

                            // refresh view
                            refreshProfilesView();


                        }

                    }
                })

                .setNegativeButton(sInstance.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //return;
                    }
                });

        // open the dialog now :)
        builder.show();
    }


    /**
     *
     * Activate Specified Profile
     *
     */
    protected void activateProfile(Profile p) {

        /**
         * update list of profiles with active flags
         */
        Profile.updateActiveProfile(p.getUniqueID());


        /**
         * run profile settings update
         */
        BackgroundService.getInstance().runProfileSettings(p);


        /**
         * run profile actions
         */
        BackgroundService.getInstance().runProfileActions(p);

        /**
         * Profile set active
         */
        Log.i("sfen", "Profile "+ p.getName() +" activated.");


        /**
         * refresh view
         */
        refreshProfilesView();
    }






}
