package gpapez.sfen;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;

/**
 * Created by Gregor on 2.8.2014.
 *
 * Class that will send commands and check if root is enabled
 */
public class Sudo {

    /**
     * variable that will allow us to check if we called sudo before
     */
    private boolean isSudoGranted = false;

    /**
     * array of string for commands
     */
    private String[] mCommands;

    /**
     * Constructor (empty)
     */
    public Sudo() {}


    /**
     *
     * SUDO ASYNC TASK!
     *
     */
    // All the methods in the following class are
    // executed in the same order as they are defined.
    private class sudoAsyncTask extends AsyncTask<Void, Void, Void> {

        private String mTaskName;

        sudoAsyncTask(String taskName) {
            //tv = (TextView)findViewById(R.id.tv);
            mTaskName = taskName;

        }

        // Executed on the UI thread before the
        // time taking task begins
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //System.out.println("Async: preexecute");
        }

        // Executed on a special thread and all your
        // time taking tasks should be inside this method
        @Override
        protected Void doInBackground(Void... arg0) {
            //System.out.println("Async: background");
//            isRootEnabledAsync();

            if (mTaskName.equals("ROOT_CHECK")) {
                isRootEnabledAsync();
            }
            else if (mTaskName.equals("RUN_COMMANDS"))
                callRootCommandsAsync();
            else {
                Log.e("sfen", "Wrong Task called for Sudo ("+ mTaskName +")!");
            }


            return null;
        }

        // Executed on the UI thread after the
        // time taking process is completed
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //System.out.println("Async: task completed");


            /**
             * sudo was granted, lets call for a broadcast that something happened!
             */
            if (isSudoGranted)
                BackgroundService.getInstance().sendBroadcast("ROOT_GRANTED");

            // if we were successful, lets send a broadcast

        }
    }

    /**
     * START ASYNC TASK for Root checking
     */
    protected void isRootEnabled() {
        sudoAsyncTask task = new sudoAsyncTask("ROOT_CHECK");

        //task.execute();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

    }

    protected void callRootCommands(String[] commands) {
        mCommands = commands;

        sudoAsyncTask task = new sudoAsyncTask("RUN_COMMANDS");

        //task.execute();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);

    }


    /**
     * General method to check if root is available
     */
    private boolean isRootEnabledAsync() {

        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();

            p.waitFor();

            /**
             * root gets granted only if process returns code 0
             */
            if (p.exitValue() == 0) {
                Log.d("sfen", "Sudo access was granted ("+ p.exitValue() +").");
                isSudoGranted = true;
                return true;
            }
            else {
                Log.d("sfen", "Sudo access was NOT granted ("+ p.exitValue() +").");
            }


        }
        catch (Exception e) {
            Log.e("sfen", "Root Exception handled!");
            e.getStackTrace();
        }

        return false;

    }


    /**
     * Call command(s) in sudo mode
     *
     * Extreme caution!
     */
    private boolean callRootCommandsAsync() {

        if (!isSudoGranted) {
            Log.e("sfen", "Cannot execute sudo commands. Sudo was not granted.");
            return false;
        }

        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());


            for (String tmpCmd : mCommands) {
                //System.out.println("+++++++ sending command: "+ tmpCmd);
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();

            return true;

        }
        catch (Exception e) {
            Log.e("sfen", "Root Exception handled!");
            e.getStackTrace();

        }

        return false;

    }


    /**
     * call root command just creates an array of one command
     * @param command
     * @return
     */
    public void callRootCommand(String command) {
        callRootCommands(new String[] {command});
    }


}
