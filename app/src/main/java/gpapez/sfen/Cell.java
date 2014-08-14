package gpapez.sfen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Gregor on 11.8.2014.
 */
public class Cell implements Comparable<Cell> {

    private String cellId;
    private Calendar storeDate;

    protected static ArrayList<Cell> selectedCells = new ArrayList<Cell>();

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

        // get number of minutes from preferences
        int recordMinutes = 10;
        try {
            recordMinutes = Preferences
                    .getSharedPreferences().getInt("CellRecordMinutes", 10);
        } catch (Exception e) {}

        input.setText(String.valueOf(recordMinutes));

        LinearLayout newView = new LinearLayout(Main.getInstance());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);
        newView.addView(info, 0);
        newView.addView(input, 1);

        /**
         * if time stored, lets show info until when are we recording this
         */
        Calendar calendarUntil = null;
        try {
            Gson gson = new Gson();
            calendarUntil = gson.fromJson(
                    Preferences
                            .getSharedPreferences().getString("CellRecordUntil", null),
                    Calendar.class
            );
        }
        catch (Exception e) {}


        /**
         * calendar time was stored, add more info to dialog
         */
        if (calendarUntil != null) {
            //System.out.println("current saved preference? " + calendarUntil.getTime().toString());
            final TextView infoCalendar = new TextView(context);
            infoCalendar.setText("Already recording until: "+ Util.getDateLong(calendarUntil));
            infoCalendar.setPadding(10, 15, 10, 10);

            newView.addView(infoCalendar, 2);
        }


        /**
         * add another checkbox for permanent recording
         */
        final CheckBox checkPermanent = new CheckBox(context);
        checkPermanent.setText("Record cell tower ID's as long as Sfen is running?");
        checkPermanent.setPadding(10, 10, 10, 10);

        checkPermanent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermanent.isChecked())
                    input.setEnabled(false);
                else
                    input.setEnabled(true);
            }
        });

        /**
         * retrieve permanent info
         */
        boolean isRecordingPermanent =
                Preferences
                        .getSharedPreferences().getBoolean("CellRecordPermanent", false);


        // tick checkbox if permanent is enabled
        if (isRecordingPermanent) {
            checkPermanent.setChecked(isRecordingPermanent);

            input.setEnabled(false);
        }



        newView.addView(checkPermanent);

        //SharedPreferences msp = BackgroundService.getInstance().mPreferences.getSharedPreferencesObject();

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
                            Preferences.getSharedPreferences().edit().putString(
                                    "CellRecordUntil",
                                    new Gson().toJson(calendar)).apply();


                            // permanent setting
                            Preferences.getSharedPreferences().edit().putBoolean(
                                    "CellRecordPermanent",
                                    checkPermanent.isChecked()).apply();

                            // input setting
                            Preferences.getSharedPreferences().edit().putInt(
                                    "CellRecordMinutes",
                                    Integer.parseInt(input.getText().toString())).apply();

                            if (!checkPermanent.isChecked())
                                Util.showMessageBox("New cells will be added to the list for the next " +
                                    minutes +" minutes.", false);
                            else
                                Util.showMessageBox("New cells will be added to the list for the whole " +
                                        "time of Sfen running.", false);

                        }


                    }
                })
                        //.set
                .setNegativeButton("Clear & Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        /**
                         * clear preference entry
                         */
                        Preferences.getSharedPreferences().edit().putString(
                                "CellRecordUntil",
                                new String()).apply();

                        Preferences.getSharedPreferences().edit().putBoolean(
                                "CellRecordPermanent",
                                false).apply();

                        // input setting
                        Preferences.getSharedPreferences().edit().putInt(
                                "CellRecordMinutes",
                                10).apply();


                    }
                });


        builder.show();

    }


    /**
     *
     * shows all cell id's that cellchange listener saved into preferences
     *
     */
    protected static void openCellTowersHistoryDialog(Context context) {

        /**
         * get cells from preferences
         */
        ArrayList<Cell> cells = (ArrayList<Cell>)BackgroundService.getInstance().mPreferences
                .getPreferences("cells", Preferences.REQUEST_TYPE.CELLS);

        if (cells == null) {
            cells = new ArrayList<Cell>();
        }

//        // dummy
//        cells.add(
//                new Cell(
//                        "13:1337:13371338",
//                        Calendar.getInstance()
//                )
//        );

        if (cells.size() == 0) {

            Util.showMessageBox("No cells to show. Try to record few first", false);
            return ;

        }


        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mCellTowers = (ViewGroup) dialogView.findViewById(R.id.condition_pick);



        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setIcon(R.drawable.ic_cell)
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })


                .setTitle("Cell tower history");

        /**
         * create dialog
         */
        final AlertDialog dialog = builder.create();

        /**
         * fill dialog with cells
         */
        //newRow;
        for (final Cell single : cells) {
            final ViewGroup newRow = (ViewGroup) inflater.inflate(R.layout.dialog_cellid_single, mCellTowers, false);


            ((TextView) newRow.findViewById(android.R.id.text1)).setText(single.getCellId());
            ((TextView) newRow.findViewById(android.R.id.text2))
                    .setText(Util.getDateLong(single.getStoreDate()));

            ImageView imageView = (ImageView)newRow.findViewById(R.id.cellid_delete);

            mCellTowers.addView(newRow);

            /**
             * clicking single row
             */
            newRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newRow.isSelected()) {
                        selectedCells.add(single);
                        newRow.setSelected(false);
                    }
                    else {
                        selectedCells.remove(single);
                        newRow.setSelected(true);
                    }
                }
            });

            /**
             * clicking trash bin, deletes stored cell
             */
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCellTowers.removeView(newRow);

                    /**
                     * remove cellID from history array
                     */
                    removeCellIdFromArray(single.getCellId());
                }
            });

            //newRow = null;


            dialog.show();

        }



        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }


    /**
     *
     * Add new Cell ID into history array
     * (if not there yet)
     *
     */
    protected static void addCellIdToArray(String cellId) {

        /**
         * get cells from preferences
         */
        ArrayList<Cell> cells = (ArrayList<Cell>)BackgroundService.getInstance().mPreferences
                .getPreferences("cells", Preferences.REQUEST_TYPE.CELLS);

        if (cells == null) {
            cells = new ArrayList<Cell>();
        }

        /**
         * if cellid already exists, skip adding
         */
        for (Cell single : cells) {

            if (single.getCellId().equals(cellId)) {
                Log.d("sfen", "Cellid "+ cellId +" already exists in array.");
                return;
            }
        }

        /**
         * update cells array with new entry
         */
        cells.add(
                new Cell(
                        cellId,
                        Calendar.getInstance()
                        )
        );

        /**
         * set new preferences
         */
        Log.d("sfen", "Storing Cell "+ cellId +" into history array.");

        BackgroundService.getInstance().mPreferences.setPreferences(
                "cells", cells
        );



    }



    /**
     *
     * Add new Cell ID into history array
     * (if not there yet)
     *
     */
    protected static void removeCellIdFromArray(String cellId) {

        /**
         * get cells from preferences
         */
        ArrayList<Cell> cells = (ArrayList<Cell>)BackgroundService.getInstance().mPreferences
                .getPreferences("cells", Preferences.REQUEST_TYPE.CELLS);

        if (cells == null) {
            cells = new ArrayList<Cell>();
        }

        /**
         * create arraylist of items to be removed
         */
        ArrayList<Cell> cellsToBeRemoved = new ArrayList<Cell>();

        for (Cell single : cells) {

            if (single.getCellId().equals(cellId))
                cellsToBeRemoved.add(single);
        }

        /**
         * update cells array with new entry
         */
        cells.removeAll(cellsToBeRemoved);

        /**
         * set new preferences
         */
        Log.d("sfen", "Cell "+ cellId +" deleted from history array.");

        BackgroundService.getInstance().mPreferences.setPreferences(
                "cells", cells
        );


    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (cellId.equals(((Cell) o).getCellId()))
            return true;

        else
            return false;

    }

    @Override
    public int hashCode() {
        return cellId.hashCode();
    }


    /**
     * compareTo for Cell will check only dates and return their compareTo value
     */
    @Override
    public int compareTo(Cell another) {
        return storeDate.compareTo(another.getStoreDate());

        //return 0;
    }
}
