package gpapez.sfen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Gregor on 11.8.2014.
 */
public class Cell {

    private String cellId;
    private Calendar storeDate;

    public Cell(String cellId, Calendar storeDate) {
        this.cellId = cellId;
        this.storeDate = storeDate;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public Calendar getStoreDate() {
        return storeDate;
    }

    public void setStoreDate(Calendar storeDate) {
        this.storeDate = storeDate;
    }

    /**
     *
     * returns arraylist of all cells from preferences
     *
     */
    protected static ArrayList<Cell> getSavedCellsFromPreferences() {

        /**
         * get profiles from preferences
         */
        Preferences mPreferences = new Preferences(Main.getInstance());


        if (mPreferences == null) {

            return null;

        }


        return (ArrayList<Cell>) mPreferences
                .getPreferences("cells", Preferences.REQUEST_TYPE.CELLS);

    }


    /**
     * show record dialog
     */
    protected static void openCellTowerRecordDialog(final Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText input = new EditText(context);
        final TextView info = new TextView(context);
        info.setText("Number of minutes to store cell tower ID's:");
        info.setPadding(10, 10, 10, 10);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText("10");

        LinearLayout newView = new LinearLayout(Main.getInstance());
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
                .setTitle("Cell tower ID's")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        /**
                         * no input or entered 0?
                         */
                        if (input.getText().toString().equals("") ||
                                input.getText().toString().equals("0")) {

                            Util.showMessageBox("Next time, add more minutes, okay?", false);

                        }

                        /**
                         * store current date + X minutes to preferences
                         */
                        else {

                            int minutes = Integer.parseInt(input.getText().toString());

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.MINUTE, minutes);

                            //System.out.println("we are saving ids until "+ calendar.getTime().toString());

                            // store all to preferences again
                            BackgroundService.getInstance().mPreferences.setPreferences(
                                    "CellRecordUntil", calendar
                            );


                            /*
                            SharedPreferences.Editor prefsEditor = Main.getInstance()
                                    .getPreferences(Context.MODE_PRIVATE).edit();

                            Gson gson = new Gson();

                            //cond.setSetting("selectedDays", (new Gson().toJson(mSelectedDays)));

                            prefsEditor.putString("CellRecordUntil", (new Gson().toJson(calendar)));
                            prefsEditor.commit();
*/
                            Util.showMessageBox("New cells will be added to the list for the next " +
                                    minutes +" minutes.", false);

                        }


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

    }


    protected static void openCellTowersHistoryDialog(Context context) {
        System.out.println("huey");

        CheckBox checkBox = new CheckBox(context);

        checkBox.setText("cell id #1\nmore info");
        //checkBox.set

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setView(checkBox)
//                .setItems(profileNames, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        dialogInterface.dismiss();
//
//                        Intent.ShortcutIconResource icon =
//                                Intent.ShortcutIconResource.fromContext(sInstance, profiles.get(i).getIcon());
//
//                        Intent intent = new Intent();
//
//                        Intent launchIntent = new Intent(sInstance, ShortcutActivity.class);
//                        launchIntent.putExtra("PROFILE_TO_RUN", profiles.get(i).getUniqueID());
//
//                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
//                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profiles.get(i).getName());
//                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
//
//                        setResult(RESULT_OK, intent);
//                        finish();
//
//                    }
//                })
                .setTitle("Select profile")
                .show();

//
//        final AlertDialog dialog = builder.create();
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                dialogInterface.dismiss();
//            }
//        });
//        dialog.show();

    }
}
