package gpapez.sfen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

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

        //final EditText input = new EditText(context);

        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(5); // 5 minutes
        numberPicker.setMaxValue(1440); // 24 hours
        numberPicker.getValue();

        final TextView info = new TextView(context);
        info.setText(context.getString(R.string.number_of_minutes_store_cell));
        info.setPadding(10, 10, 10, 10);
        //input.setInputType(InputType.TYPE_CLASS_NUMBER);

        // get number of minutes from preferences
        int recordMinutes = 10;
        try {
            recordMinutes = Preferences
                    .getSharedPreferences().getInt("CellRecordMinutes", 10);
        } catch (Exception e) {}

        //input.setText(String.valueOf(recordMinutes));
        numberPicker.setValue(recordMinutes);

        ScrollView scrollView = new ScrollView(Main.getInstance());
        LinearLayout newView = new LinearLayout(Main.getInstance());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);
        newView.addView(info);
        newView.addView(numberPicker);
        //newView.addView(input);

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
            infoCalendar.setText(context.getString(R.string.already_recording_until, Util.getDateLong(calendarUntil,context)));
            infoCalendar.setPadding(10, 15, 10, 10);

            newView.addView(infoCalendar, 2);
        }


        /**
         * add another checkbox for permanent recording
         */
        final CheckBox checkPermanent = new CheckBox(context);
        checkPermanent.setText(context.getString(R.string.record_as_long_as_sfen_running));
        checkPermanent.setPadding(10, 10, 10, 10);

        checkPermanent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermanent.isChecked())
                    numberPicker.setEnabled(false);
                else
                    numberPicker.setEnabled(true);
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

            numberPicker.setEnabled(false);
        }



        newView.addView(checkPermanent);

        //SharedPreferences msp = BackgroundService.getInstance().mPreferences.getSharedPreferencesObject();
        scrollView.addView(newView);

        builder
                .setView(scrollView)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(context.getString(R.string.cell_tower_ids))
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        /**
                         * no input or entered 0?
                         */
//                        if (input.getText().toString().equals("") ||
//                                input.getText().toString().equals("0")) {
//
//                            Util.showMessageBox(context.getString(R.string.cell_add_more_minutes), false);
//
//                        }

                        /**
                         * store current date + X minutes to preferences
                         */
//                        else {

//                            int minutes = Integer.parseInt(input.getText().toString());
                        int minutes = numberPicker.getValue();

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
                                    minutes).apply();

                            if (!checkPermanent.isChecked())
                                Util.showMessageBox(context.getString(R.string.cells_new_added_for_next_minutes, minutes), false);
                            else
                                Util.showMessageBox(context.getString(R.string.cells_running_until_sfen_running), false);

//                        }


                    }
                })
                        //.set
                .setNegativeButton(context.getString(R.string.clear_and_cancel), new DialogInterface.OnClickListener() {
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

        /**
         * sort the array
         */
        Collections.sort(cells, Collections.reverseOrder());

//        // dummy
//        cells.add(
//                new Cell(
//                        "13:1337:13371338",
//                        Calendar.getInstance()
//                )
//        );

        if (cells.size() == 0) {

            Util.showMessageBox(context.getString(R.string.cells_no_cells_to_show), false);
            return ;

        }


        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mCellTowers = (ViewGroup) dialogView.findViewById(R.id.condition_pick);



        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setIcon(R.drawable.ic_cell)
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })


                .setTitle(context.getString(R.string.cell_tower_history));

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
                    .setText(Util.getDateLong(single.getStoreDate(),context));

            ImageView imageView = (ImageView)newRow.findViewById(R.id.cellid_delete);

            mCellTowers.addView(newRow);

            /**
             * clicking single row
             */
//            newRow.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (newRow.isSelected()) {
//                        selectedCells.add(single);
//                        newRow.setSelected(false);
//                    }
//                    else {
//                        selectedCells.remove(single);
//                        newRow.setSelected(true);
//                    }
//                }
//            });

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
         * if cellid already exists, update it & skip adding
         */
        boolean cellAlreadyExist = false;
        for (Cell single : cells) {

            if (single.getCellId().equals(cellId)) {
                Log.d("sfen", "CellId "+ cellId +" already exists in array. Updating.");
                single.setStoreDate(Calendar.getInstance());

                cells.set(
                        cells.indexOf(single),
                        single
                );

                cellAlreadyExist = true;

                Log.d("sfen", "Updating Cell "+ cellId +" in history array.");

                break;
            }
        }

        /**
         * update cells array with new entry
         */
        if (!cellAlreadyExist) {
            cells.add(
                    new Cell(
                            cellId,
                            Calendar.getInstance()
                    )
            );

            Log.d("sfen", "Storing Cell "+ cellId +" into history array.");
        }

        /**
         * set new preferences
         */
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
