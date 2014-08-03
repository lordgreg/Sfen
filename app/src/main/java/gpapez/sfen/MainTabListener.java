package gpapez.sfen;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by Gregor on 2.8.2014.
 */
public class MainTabListener implements ActionBar.TabListener {

    private Fragment mFragmentSelected;


    public MainTabListener(Fragment fragment) {
        mFragmentSelected = fragment;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.replace(R.id.fragment_container, mFragmentSelected);

        /**
         * update current tab
         */
        Main.getInstance().mTabPosition = tab.getPosition();

        /**
         * we moved to another tab,
         * let's update menus
         */
        Main.getInstance().invalidateOptionsMenu();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(mFragmentSelected);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
