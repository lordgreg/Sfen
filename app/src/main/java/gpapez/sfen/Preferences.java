package gpapez.sfen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregor on 23.7.2014.
 */
public class Preferences {

    private Gson mGson;
    private SharedPreferences mPreferences;
    private Activity mContext;

    public enum REQUEST_TYPE {EVENTS, ALARMS, PROFILES, CELLS, SETTINGS};
    private REQUEST_TYPE mRequestType;




    public Preferences(Activity context) {
        mGson = new Gson();

        if (context == null) {
            Log.e("sfen", "Preferences aren't accessible! Context is null!");
        }
        else {
            mContext = context;
            mPreferences = context.getPreferences(Context.MODE_PRIVATE);
        }
    }

    public void setPreferences(String prefName, Object obj) {

        String json = mPreferences.getString(prefName, "");

        // store all to preferences again
        SharedPreferences.Editor prefsEditor = mPreferences.edit();
        json = mGson.toJson(obj, obj.getClass());
        prefsEditor.putString(prefName, json);
        prefsEditor.commit();

    }

    //public ArrayList<?> getPreferences(String prefName, REQUEST_TYPE reqType, Object obj) {
    public ArrayList<?> getPreferences(String prefName, REQUEST_TYPE reqType) {

        // return object
        ArrayList<?> returnObj = null;
        //Object returnObj = null;

        // retrieve object from preferences
        String json = mPreferences.getString(prefName, "");

        // define type
        switch (reqType) {
            case EVENTS:
                returnObj = mGson.fromJson(json, new TypeToken<List<Event>>(){}.getType());
                break;
            case ALARMS:
                returnObj = mGson.fromJson(json, new TypeToken<List<Alarm>>(){}.getType());
                //returnObj = mGson.fromJson(json, obj.getClass());
                break;
            case PROFILES:
                returnObj = mGson.fromJson(json, new TypeToken<List<Profile>>(){}.getType());
                break;
            case CELLS:
                returnObj = mGson.fromJson(json, new TypeToken<List<Cell>>(){}.getType());
                break;

            default:
                Log.e("sfen", "Type of "+ reqType +" is invalid.");
                break;
        }


        // check retrieved object size
        if (returnObj == null) {
            Log.e("sfen", "Preferences " + prefName + " of type " + reqType + " is null.");
        }
        else {
            if (returnObj.size() == 0) {
                Log.d("sfen", "Preferences " + prefName + " of type " + reqType + " is empty.");
            }
//            else
//                System.out.println("object from preferences: "+ returnObj.toString());
        }


        return returnObj;
    }

    /**
     * retrieve shared preferences from created object
     */
    protected SharedPreferences getSharedPreferencesObject() {

        return mPreferences;

    }


    /**
     * Retrieve shared preference
     */
    protected static SharedPreferences getSharedPreferences() {

        return PreferenceManager
                .getDefaultSharedPreferences(BackgroundService.getInstance());

    }

    protected static SharedPreferences getSharedPreferences(Context context) {

        return PreferenceManager
                .getDefaultSharedPreferences(context);

    }

}
