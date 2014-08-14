package gpapez.sfen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gregor on 14.8.2014.
 */
public class CallAllowDeny {

    private enum TYPE {ALLOW, DENY};

    private TYPE callType;

    private Uri contact;



    protected static void openSelectionDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.getInstance());

        String[] options = new String[]{"Single contact", "Group", "Phone number"};

        builder
                .setTitle("Select option")
                .setIcon(R.drawable.ic_launcher)
                .setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        openSpecifiedSelection(i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })

                .show();

    }


    /**
     * OPENS SPECIFIED system selection or our dialog
     *
     * @param type: 0 = person, 1 = group, 2 = single number
     */
    protected static void openSpecifiedSelection(int type) {

        switch (type) {

            // PERSON
            case 0:

                //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

                // 5 = REQUEST_CONTACT_RESULT from ProfileActivity
                //ProfileActivity.getInstance().startActivityForResult(intent, 5);
                showDialogContacts();


                break;

            // GROUP
            case 1:

                showDialogGroups();

                break;

            // SINGLE NUMBER DIALOG
            case 2:

                showDialogNumber();



                break;


            default:

                break;


        }


    }





    protected static void showDialogContacts() {

//        System.out.println("*** SHWOING NUMBERS FROM **** ***");
//        for (String num : ContactsPhone.getContactNumbers("****")) {
//
//            System.out.println(">>> "+ num);
//
//        }
//
//        System.out.println("ID from ****: "+ ContactsPhone.getContactID("****"));


        //ContactsPhone.getContactNumbers("****");
        System.out.println("all groups");
        HashMap<Long, String> groupsAll = ContactsPhone.getContactsGroups();

        for (Long key : groupsAll.keySet()) {

            System.out.println("Group ID: "+ key +", Name: "+ groupsAll.get(key));



        }

        System.out.println("contact MP groups");
        ArrayList<String> groupsContact = ContactsPhone.getContactGroups("****");

        System.out.println("Group with ID 1 is "+ ContactsPhone.getGroupName(1));
        System.out.println("Group with ID 6 is "+ ContactsPhone.getGroupName(6));

        System.out.println("ID of contact ****? "+ ContactsPhone.getContactID("****"));
        System.out.println("Is **** in group 1 "+ ContactsPhone.getGroupName(1) +"?"+
            ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 1L));

        System.out.println("Is **** in group 3 "+ ContactsPhone.getGroupName(3) +"?"+
                ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 3L));

        System.out.println("Is **** in group 6 "+ ContactsPhone.getGroupName(6) +"?"+
                ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 6L));

        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(1231232L, 79));
        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(516566L, 79));
        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(38651523523556682L, 79));

        System.out.println("is TEL in group 1 ("+ ContactsPhone.getGroupName(1) +")? "+ ContactsPhone.isNumberInGroup(516566L, 1L));
        System.out.println("is TEL in group 2 ("+ ContactsPhone.getGroupName(2) +")? "+ ContactsPhone.isNumberInGroup(516566L, 2L));
        System.out.println("is TEL in group 6 ("+ ContactsPhone.getGroupName(6) +")? "+ ContactsPhone.isNumberInGroup(516566L, 6L));

        int a = 1;




//        System.out.println("*** SHOWING ALL CONTACTS WITH PHONE NUMBERS ***");
//
//        // sort them by key
//        Map<String, ArrayList<String>> map = new TreeMap<String, ArrayList<String>>(ContactsPhone.getContactsNumbers());
//
//
//        for (String key : map.keySet()) {
//
//            System.out.println(key);
//
//            // phone numbers
//            ArrayList<String> numbers = map.get(key);
//
//            for (String number : numbers) {
//                System.out.println(">>> number: "+ number);
//            }
//
//        }



//        Cursor cursor = ProfileActivity.getInstance()
//                .getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        if(cursor.moveToFirst())
//        {
//            ArrayList<String> alContacts = new ArrayList<String>();
//            do
//            {
//                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//
//                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
//                {
//                    Cursor pCur = ProfileActivity.getInstance()
//                            .getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
//                    while (pCur.moveToNext())
//                    {
//                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                        System.out.println("contact: "+ contactName +" number: "+ contactNumber);
//                        //alContacts.add(contactNumber);
//                        break;
//                    }
//                    pCur.close();
//                }
//
//            } while (cursor.moveToNext()) ;
//        }




    }

    protected static void showDialogGroups() {

        /**
         * used: http://developer.android.com/reference/android/provider/ContactsContract.Groups.html
         */


    }

    protected static void showDialogNumber() {

    }

//
//    protected static void openSubDialogWithSettings(Uri contactData) {
//        Cursor c =  ProfileActivity.getInstance().getContentResolver()
//                .query(contactData, null, null, null, null);
//
//        if (c.moveToFirst()) {
//            String contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//            //Util.showMessageBox("Picked contact: "+ contactName, false);
//
//            System.out.println("opening subdialog with contact: "+ contactName);
//        }
//
//    }


}
