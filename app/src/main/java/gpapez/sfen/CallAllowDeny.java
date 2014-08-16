package gpapez.sfen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gregor on 14.8.2014.
 */
public class CallAllowDeny {

    protected enum TYPE {ALLOW, DENY}

    protected enum ENTRY_TYPE {CONTACT, GROUP, NUMBER}

    private TYPE callType;
    private ENTRY_TYPE entryType;

    private ArrayList<Long> contactId = new ArrayList<Long>();
    private ArrayList<Long> groupId = new ArrayList<Long>();
    private String phoneNumber;


    /**
     * index key from profileactivity>callAllowDeny array
     */
    protected static int editingKey = -1;

    /**
     *
     * CONSTRUCTOR
     *
     */
    public CallAllowDeny() {  }

    /**
     *
     * GETTERS/SETTERS
     *
     */
    public TYPE getCallType() {
        return callType;
    }

    public void setCallType(TYPE callType) {
        this.callType = callType;
    }

    public ENTRY_TYPE getEntryType() {
        return entryType;
    }

    public void setEntryType(ENTRY_TYPE entryType) {
        this.entryType = entryType;
    }

    public ArrayList<Long> getContactId() {
        return contactId;
    }

    public void setContactId(ArrayList<Long> contactId) {
        this.contactId = contactId;
    }

    public ArrayList<Long> getGroupId() {
        return groupId;
    }

    public void setGroupId(ArrayList<Long> groupId) {
        this.groupId = groupId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     *
     *
     * STATIC METHODS
     *
     *
     */
    protected static void openSelectionDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.getInstance());

        String[] options = new String[]{
                ProfileActivity.getInstance().getString(R.string.allow_single_contact),
                ProfileActivity.getInstance().getString(R.string.deny_single_contact),
                ProfileActivity.getInstance().getString(R.string.allow_group),
                ProfileActivity.getInstance().getString(R.string.deny_group),
                ProfileActivity.getInstance().getString(R.string.allow_phone_number),
                ProfileActivity.getInstance().getString(R.string.deny_phone_number)
        };

        builder
                .setTitle(ProfileActivity.getInstance().getString(R.string.select_option))
                .setIcon(R.drawable.ic_launcher)
                .setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        openSpecifiedSelection(i);
                    }
                })
                .setNegativeButton(ProfileActivity.getInstance().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
     * @param type:
     *            0 = allow person,
     *            1 = deny person,
     *            2 = allow group,
     *            3 = deny group,
     *            4 = allow single number,
     *            5 = deny single number
     */
    protected static void openSpecifiedSelection(int type) {

        switch (type) {

            // PERSON
            case 0:
                showDialogContacts(TYPE.ALLOW);
                break;

            case 1:
                showDialogContacts(TYPE.DENY);
                break;

            // GROUP
            case 2:
                showDialogGroups(TYPE.ALLOW);
                break;

            case 3:
                showDialogGroups(TYPE.DENY);
                break;

            // SINGLE NUMBER DIALOG
            case 4:
                showDialogNumber(TYPE.ALLOW);
                break;

            case 5:
                showDialogNumber(TYPE.DENY);
                break;

            default:

                break;


        }


    }


    /**
     *
     * CONTACTS DIALOG
     *
     */
    protected static void showDialogContacts(final TYPE type) {

        final int[] editKey = new int[1];
        if (editingKey != -1)
            editKey[0] = editingKey;

        else
            editKey[0] = editingKey;


        HashMap<Integer, String> contacts = ContactsPhone.sortHashMapByValuesD(
                ContactsPhone.getContactsWithNumbers()
        );

        /**
         * putting all contacts in String[] array
         */
        final String[] contactsString = new String[contacts.size()];
        boolean[] checkedContacts = new boolean[contacts.size()];

        final ArrayList<Integer> selectedContacts = new ArrayList<Integer>();

        int i = 0;

        for (int single : contacts.keySet()) {

            //System.out.println(single +" > "+ contacts.get(single));
            contactsString[i] = contacts.get(single);

            // if we are updating, lets find out if current contact is the one already stored
            if (editKey[0] != -1) {

                ArrayList<Long> savedContacts = ProfileActivity.getInstance().callAllowDeny
                        .get(editKey[0]).getContactId();

                // loop through stored contacts
                for (long cID : savedContacts) {

                    if (savedContacts.contains(Long.valueOf(single))) {
                        checkedContacts[i] = true;
                        selectedContacts.add(i);
                        break ;
                    }

                }


            }

            i++;
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.getInstance());

        builder
                .setIcon(R.drawable.ic_whitelist)
                .setTitle(ProfileActivity.getInstance().getString(R.string.single_contacts))
                .setMultiChoiceItems(contactsString, checkedContacts, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                        if (checked)
                            selectedContacts.add(i);
                        else
                            selectedContacts.remove(selectedContacts.indexOf(i));

//                        System.out.println("Touched "+ contactsString[i]);
                    }
                })
                .setPositiveButton(ProfileActivity.getInstance().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        /**
                         * selected contacts?
                         */
                        if (selectedContacts.size() == 0) {
                            Util.showMessageBox(ProfileActivity.getInstance().getString(R.string.select_contacts_to_continue), false);
                            return;
                        }

                        /**
                         * start doing things with selected contacts
                         *
                         * GET REAL ID's from Contacts
                         */
                        ArrayList<Long> contacts = new ArrayList<Long>();
                        for (i = 0; i < selectedContacts.size(); i++) {

//                            System.out.println("Real ID from "+ contactsString[selectedContacts.get(i)] +
//                            " is "+ ContactsPhone.getContactID(contactsString[selectedContacts.get(i)]));

                            contacts.add(ContactsPhone.getContactID(contactsString[selectedContacts.get(i)]));

                            //ProfileActivity.getInstance().mContainerCallAllowDeny

                        }

                        /**
                         * create new CallAllowDeny object
                         */
                        CallAllowDeny obj = new CallAllowDeny();

                        obj.callType = type;
                        obj.entryType = ENTRY_TYPE.CONTACT;
                        obj.contactId = contacts;


                        /**
                         * remove previous item if we're updating
                         */
                        if (editKey[0] != -1)
                            ProfileActivity.getInstance().callAllowDeny.remove(editKey[0]);


                        /**
                         * add them to list in profileactivity
                         */
                        ProfileActivity.getInstance().callAllowDeny.add(obj);


                        /**
                         * refresh profile activity view
                         */
                        ProfileActivity.getInstance().refreshView();


                    }
                })
                .setNegativeButton(ProfileActivity.getInstance().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })

                .show();

//
//
//        //ContactsPhone.getContactNumbers("****");
//        System.out.println("all groups");
//        HashMap<Long, String> groupsAll = ContactsPhone.getContactsGroups();
//
//        for (Long key : groupsAll.keySet()) {
//
//            System.out.println("Group ID: "+ key +", Name: "+ groupsAll.get(key));
//
//
//
//        }
//
//        System.out.println("contact MP groups");
//        ArrayList<String> groupsContact = ContactsPhone.getContactGroups("****");
//
//        System.out.println("Group with ID 1 is "+ ContactsPhone.getGroupName(1));
//        System.out.println("Group with ID 6 is "+ ContactsPhone.getGroupName(6));
//
//        System.out.println("ID of contact ****? "+ ContactsPhone.getContactID("****"));
//        System.out.println("Is **** in group 1 "+ ContactsPhone.getGroupName(1) +"?"+
//            ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 1L));
//
//        System.out.println("Is **** in group 3 "+ ContactsPhone.getGroupName(3) +"?"+
//                ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 3L));
//
//        System.out.println("Is **** in group 6 "+ ContactsPhone.getGroupName(6) +"?"+
//                ContactsPhone.isContactInGroup(ContactsPhone.getContactID("****"), 6L));
//
//        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(1231232L, 79));
//        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(516566L, 79));
//        System.out.println("is TEL in ****? "+ ContactsPhone.isNumberInContact(38651523523556682L, 79));
//
//        System.out.println("is TEL in group 1 ("+ ContactsPhone.getGroupName(1) +")? "+ ContactsPhone.isNumberInGroup(516566L, 1L));
//        System.out.println("is TEL in group 2 ("+ ContactsPhone.getGroupName(2) +")? "+ ContactsPhone.isNumberInGroup(516566L, 2L));
//        System.out.println("is TEL in group 6 ("+ ContactsPhone.getGroupName(6) +")? "+ ContactsPhone.isNumberInGroup(516566L, 6L));
//
//        int a = 1;



        editingKey = -1;

    }

    protected static void showDialogGroups(final TYPE type) {


        final int[] editKey = new int[1];
        if (editingKey != -1)
            editKey[0] = editingKey;

        else
            editKey[0] = editingKey;


        final HashMap<Integer, String> groups = ContactsPhone.sortHashMapByValuesD(
                ContactsPhone.getContactsGroups()
        );

        /**
         * putting all contacts in String[] array
         */
        final String[] groupsString = new String[groups.size()];
        boolean[] checkedGroups = new boolean[groups.size()];

        final ArrayList<Integer> selectedGroups = new ArrayList<Integer>();

        int i = 0;

        for (int single : groups.keySet()) {

            //System.out.println(single +" > "+ contacts.get(single));
            groupsString[i] = groups.get(single);

            // if we are updating, lets find out if current contact is the one already stored
            if (editKey[0] != -1) {

                ArrayList<Long> savedGroups = ProfileActivity.getInstance().callAllowDeny
                        .get(editKey[0]).getGroupId();

                // loop through stored contacts
                for (long gID : savedGroups) {

                    if (savedGroups.contains(Long.valueOf(single))) {
                        checkedGroups[i] = true;
                        selectedGroups.add(i);
                        break ;
                    }

                }


            }

            i++;
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.getInstance());

        builder
                .setIcon(R.drawable.ic_whitelist)
                .setTitle(ProfileActivity.getInstance().getString(R.string.groups))
                .setMultiChoiceItems(groupsString, checkedGroups, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                        if (checked)
                            selectedGroups.add(i);
                        else
                            selectedGroups.remove(selectedGroups.indexOf(i));

//                        System.out.println("Touched "+ contactsString[i]);
                    }
                })
                .setPositiveButton(ProfileActivity.getInstance().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        /**
                         * selected groups?
                         */
                        if (selectedGroups.size() == 0) {
                            Util.showMessageBox(ProfileActivity.getInstance().getString(R.string.select_groups_to_continue), false);
                            return ;
                        }

                        /**
                         * start doing things with selected groups
                         *
                         * GET REAL ID's from groups
                         */
                        ArrayList<Long> groups = new ArrayList<Long>();
                        for (i = 0; i < selectedGroups.size(); i++) {

//                            System.out.println("Real ID from "+ contactsString[selectedContacts.get(i)] +
//                            " is "+ ContactsPhone.getContactID(contactsString[selectedContacts.get(i)]));

                            groups.add(ContactsPhone.getGroupID(groupsString[selectedGroups.get(i)]));

                            //ProfileActivity.getInstance().mContainerCallAllowDeny

                        }

                        /**
                         * create new CallAllowDeny object
                         */
                        CallAllowDeny obj = new CallAllowDeny();

                        obj.callType = type;
                        obj.entryType = ENTRY_TYPE.GROUP;
                        obj.groupId = groups;


                        /**
                         * remove previous item if we're updating
                         */
                        if (editKey[0] != -1)
                            ProfileActivity.getInstance().callAllowDeny.remove(editKey[0]);



                        /**
                         * add them to list in profileactivity
                         */
                        ProfileActivity.getInstance().callAllowDeny.add(obj);


                        /**
                         * refresh profile activity view
                         */
                        ProfileActivity.getInstance().refreshView();


                    }
                })
                .setNegativeButton(ProfileActivity.getInstance().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })

                .show();





        editingKey = -1;

    }

    protected static void showDialogNumber(final TYPE type) {

        final int[] editKey = new int[1];
        String phoneNumber = "";

        if (editingKey != -1) {
            editKey[0] = editingKey;
            phoneNumber = ProfileActivity.getInstance().callAllowDeny
                    .get(editKey[0]).getPhoneNumber();
        }

        else {
            editKey[0] = editingKey;
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.getInstance());


        /**
         * create dialog
         */
        final EditText input = new EditText(ProfileActivity.getInstance());
        final TextView info = new TextView(ProfileActivity.getInstance());
        info.setText(ProfileActivity.getInstance().getString(R.string.enter_phone_number));
        info.setPadding(10, 10, 10, 10);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(
                ((editKey[0] != -1) ? String.valueOf(phoneNumber) : "")
        );

        LinearLayout newView = new LinearLayout(ProfileActivity.getInstance());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);

        newView.addView(info);
        newView.addView(input);


        builder
                .setIcon(R.drawable.ic_whitelist)
                .setTitle(ProfileActivity.getInstance().getString(R.string.phone_number))
                .setView(newView)
                .setPositiveButton(ProfileActivity.getInstance().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        if (input.getText().length() < 3) {

                            Util.showMessageBox(ProfileActivity.getInstance().getString(R.string.phone_number_must_contain), true);
                            return;

                        }


                        /**
                         * do the job with phone number
                         */
                        CallAllowDeny obj = new CallAllowDeny();

                        obj.callType = type;
                        obj.entryType = ENTRY_TYPE.NUMBER;
                        obj.phoneNumber = input.getText().toString();


                        /**
                         * remove previous item if we're updating
                         */
                        if (editKey[0] != -1)
                            ProfileActivity.getInstance().callAllowDeny.remove(editKey[0]);


                        /**
                         * add them to list in profileactivity
                         */
                        ProfileActivity.getInstance().callAllowDeny.add(obj);


                        /**
                         * refresh profile activity view
                         */
                        ProfileActivity.getInstance().refreshView();


                    }
                })
                .setNegativeButton(ProfileActivity.getInstance().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })

                .show();


        editingKey = -1;

    }


    protected static boolean isNumberOnAllowOrDenyList(String incomingNumber, TYPE type) {

        /**
         * get currently active profile first
         */
        Profile p = Profile.getActiveProfile();

        if (p == null)
            return false;

        /**
         * loop through all allowdenylists, check if allow, etc
         */
        for (CallAllowDeny single : p.getCallAllowDenies()) {

            if (single.getCallType() == type) {

                // persons, group or number?
                if (single.getEntryType() == ENTRY_TYPE.CONTACT) {
                    // loop all contacts
                    for (Long contact : single.getContactId()) {

                        if (ContactsPhone.isNumberInContact(incomingNumber, contact))
                            return true;

                    }

                }
                else if (single.getEntryType() == ENTRY_TYPE.GROUP) {
                    // loop all groups
                    for (Long group : single.getGroupId()) {

                        if (ContactsPhone.isNumberInGroup(incomingNumber, group))
                            return true;

                    }
                }
                else {
                    // just check number, if same, return true
                    if (incomingNumber.contains(single.getPhoneNumber()))
                        return true;

                }

            }


        }

        return false;
    }



}
