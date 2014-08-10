package gpapez.sfen;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Gregor on 5.8.2014.
 *
 * http://developer.android.com/guide/topics/ui/layout/gridview.html
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

            imageView.setLayoutParams(new GridView.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, parent.getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, parent.getResources().getDisplayMetrics())
            ));

            imageView.setTag(mThumbIds[position]);

            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    protected static Integer[] mThumbIds = {
            R.drawable.ic_launcher,
            R.drawable.ic_profile,
            R.drawable.ic_add,
            R.drawable.ic_notification,
            R.drawable.ic_time,
            R.drawable.ic_alarm,
            R.drawable.ic_battery,
            R.drawable.ic_phone,
            R.drawable.ic_sound,
            R.drawable.ic_screen,
            R.drawable.ic_date,
            R.drawable.ic_whitelist,
            R.drawable.ic_home,
            R.drawable.ic_work,
            /*
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_day,
            android.R.drawable.ic_menu_delete,
            android.R.drawable.ic_menu_directions,
            android.R.drawable.ic_menu_month,
            android.R.drawable.ic_menu_agenda,
            android.R.drawable.presence_online,
            android.R.drawable.presence_away,
            android.R.drawable.presence_busy,
            android.R.drawable.presence_invisible
*/
    };
}