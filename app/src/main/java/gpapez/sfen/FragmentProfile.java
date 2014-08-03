package gpapez.sfen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Profile Window
 */
public class FragmentProfile extends Fragment {

    /**
     * VARIABLES
     */

    // singleton
    private static FragmentProfile sInstance;

    // placeholder for current Event
    protected Profile profile = null;

    // arrays for conditions and actions
    protected ArrayList<Profile> profiles = new ArrayList<Profile>();

    // container for our profiles
    private ViewGroup mContainerView;


    //@Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.activity_main_profiles, container, false);
        //TextView textview = (TextView) view.findViewById(R.id.tabtextview);
        //textview.setText(R.string.One);
        return view;
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
    }


}
