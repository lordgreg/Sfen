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

public class Cell implements Comparable<Cell> {

    //A collection of CellConnectionInfo info, stored in preferences
    private String cellId;
    private boolean isError;
    private Calendar storeDate;

    public Cell(CellConnectionInfo cellInfo) {
        this.cellId = cellInfo.getCellId();
        this.isError = cellInfo.isError();
        this.storeDate = Calendar.getInstance();
    }

    public String getCellId() {
        return cellId;
    }

    public boolean isError() {
        return isError;
    }

    public Calendar getStoreDate() {
        return storeDate;
    }

    /**
     *
     * returns arraylist of all cells from preferences
     *
     */
    protected static ArrayList<Cell> getSavedCellsFromPreferences() {

         /**
         * get cells from preferences
         */
        ArrayList<Cell> cells = null;
        if (BackgroundService.getInstance().mPreferences != null) {
            cells = (ArrayList<Cell>) BackgroundService.getInstance().mPreferences
                    .getPreferences("cells", Preferences.REQUEST_TYPE.CELLS);
        }

        if (cells == null) {
            cells = new ArrayList<Cell>();
        }
        return cells;
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


                        /**
                         * clear cell history
                         */
                        BackgroundService.getInstance().mPreferences.setPreferences(
                                "cells",
                                new ArrayList<Cell>()
                        );


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

        ArrayList<Cell> cells = getSavedCellsFromPreferences();

        /**
         * sort the array
         */
        Collections.sort(cells, Collections.reverseOrder());

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

            String errStr ="";
            if(single.isError()) {
                errStr=" (error)";
            }
            ((TextView) newRow.findViewById(android.R.id.text1)).setText(single.getCellId()+ errStr);
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
                    removeCellIdFromArray(single);
                }
            });

            //newRow = null;


            //dialog.show();

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
     * Add new Cell ID to history array if recording
     *
     */
    protected static void recordCellId(CellConnectionInfo cellInfo) {
        /**
         * retrieve permanent info
         */
        boolean isRecordingPermanent =
                Preferences
                        .getSharedPreferences().getBoolean("CellRecordPermanent", false);

        /**
         * permanent recording saves all cells
         */
        if (isRecordingPermanent) {
            Cell.addCellIdToArray(cellInfo);
        }

        /**
         * permanent is disabled, then check times
         */
        else {
            /**
             * saving new cell id to preferences?
             */
            Calendar calendarUntil = null;
            try {
                Gson gson = new Gson();
                calendarUntil = gson.fromJson(
                        Preferences
                                .getSharedPreferences().getString("CellRecordUntil", null),
                        Calendar.class
                );
            } catch (Exception e) {
            }

            /**
             * is saved date there?
             */
            if (calendarUntil != null) {
                Calendar calendar = Calendar.getInstance();

                /**
                 * save new id into array IF save time meets conditions
                 */
                if (calendarUntil.after(calendar)) {
                    Cell.addCellIdToArray(cellInfo);
                }

                /**
                 * if until date did already passed current date, clear it from settings
                 */
                else {
                    // update date in preferences with empty string
                    BackgroundService.getInstance().mPreferences.setPreferences(
                            "CellRecordUntil", new String()
                    );
                }
            }
        }
    }

    /**
     *
     * Add new Cell ID to history array
     * (update time if in list)
     *
     */
    protected static void addCellIdToArray(CellConnectionInfo cellInfo) {

        /**
         * get cells from preferences
         */
        ArrayList<Cell> cells = getSavedCellsFromPreferences();

        Cell cell = new Cell(cellInfo);

        //Add cell, unordered list
        int i = cells.indexOf(cell);
        if (i >= 0) {
            cells.set(i, cell);
        } else {
            cells.add(cell);
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
     * Remove Cell ID from history array
     * (if not there yet)
     *
     */
    protected static void removeCellIdFromArray(Cell cell) {

        ArrayList<Cell> cells = getSavedCellsFromPreferences();
        cells.remove(cell);
        /**
         * set new preferences
         */
        Log.d("sfen", "Cell "+ cell.cellId +" deleted from history array.");

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
    }
}
