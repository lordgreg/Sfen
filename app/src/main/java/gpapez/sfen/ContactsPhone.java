package gpapez.sfen;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gregor on 14.8.2014.
 */
public class ContactsPhone {

    private long contactID;
    private String contactName;
    private ArrayList<ContactNumbers> numbers = new ArrayList<ContactNumbers>();
    private ArrayList<ContactGroups> groups = new ArrayList<ContactGroups>();

    class ContactGroups {
        protected long id;
        protected String title;
    }

    class ContactNumbers {
        protected long id;
        protected String number;
    }



    /**
     * get ALL groups from phone
     *
     * @return arraylist of strings
     */
    protected static HashMap<Long, String> getContactsGroups() {

        HashMap<Long, String> groups = new HashMap<Long, String>();
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor pGroup = cr.query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE
                },
                null,
                null, null
        );

        while (pGroup.moveToNext()) {

            String group = pGroup.getString(
                    pGroup.getColumnIndex(ContactsContract.Groups.TITLE));

            long _id = pGroup.getLong(
                    pGroup.getColumnIndex(ContactsContract.Groups._ID)
            );

            //groups.add(group);
            groups.put(_id, group);




        }
        pGroup.close();

        return groups;

    }

    protected static boolean isNumberInGroup(long number, long groupId) {

        long contactId = getContactByPhoneNumber(number);


        return isContactInGroup(contactId, groupId);
    }




    protected static long getContactByPhoneNumber(long number) {

        long contactId = 0;
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor cur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                },
                ContactsContract.CommonDataKinds.Phone.NUMBER+" like'%" + number +"%'",
                null, null
        );

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                contactId = cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                break;

            }
        }
        cur.close();


        return contactId;
    }



    protected static boolean isNumberInContact(long number, long contactId) {

        boolean ret = false;
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor cur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                },
                ContactsContract.CommonDataKinds.Phone.NUMBER+" like'%" + number +"%' AND "+
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" like'%" + contactId +"%'",
                null, null
        );

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {

                ret = true;
                break;

            }
        }
        cur.close();


        return ret;
    }


    protected static boolean isContactInGroup(long contactId, long groupId) {

        boolean ret = false;
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor cur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID
                },
                ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID+"=" + contactId +" AND "+
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID +"="+ groupId,
                null, null
        );

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ret = true;
                break ;
            }
        }
        cur.close();

        return ret;

    }



    protected static ArrayList<String> getContactNumbers(String name) {

        ArrayList<String> numbers = new ArrayList<String>();
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        //public final Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        Cursor cur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'",
                null, null
        );

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                numbers.add(number);
            }
        }
        cur.close();


        return numbers;
    }

    protected static long getContactID(String name) {

        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();
        long ret = 0;

        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME
                },
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'",
                null, null
        );

        if (cur.getCount() > 0) {

            if(cur.moveToFirst()) {
                long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));

                ret = id;
            }

        }
        cur.close();

        return ret;

    }


    protected static String getContactName(long id) {

        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();
        String ret = "";

        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME
                },
                ContactsContract.CommonDataKinds.Phone._ID+" like'%" + id +"%'",
                null, null
        );

        if (cur.getCount() > 0) {

            if(cur.moveToFirst()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                ret = name;

            }

        }
        cur.close();

        return ret;
    }


    protected static long getGroupID(String name) {

        long ret = 0;
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor cur = cr.query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE
                },
                ContactsContract.Groups.TITLE +" like'%" + name +"%'",
                null, null
        );

        if (cur.getCount() > 0) {

            if(cur.moveToFirst()) {

//                String group = cur.getString(
//                        cur.getColumnIndex(ContactsContract.Groups.TITLE));

                long _id = cur.getLong(
                        cur.getColumnIndex(ContactsContract.Groups._ID)
                );

                ret = _id;

            }

        }
        cur.close();

        return ret;

    }

    protected static String getGroupName(long id) {

        String name = "";
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        Cursor cur = cr.query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE
                },
                ContactsContract.Groups._ID +" like'%" + id +"%'",
                null, null
        );

        if (cur.getCount() > 0) {

            if(cur.moveToFirst()) {

                String group = cur.getString(
                        cur.getColumnIndex(ContactsContract.Groups.TITLE));

                long _id = cur.getLong(
                        cur.getColumnIndex(ContactsContract.Groups._ID)
                );

                name = group;

            }

        }
        cur.close();

        return name;

    }


    protected static ArrayList<String> getContactGroups(String name) {

        ArrayList<String> groups = new ArrayList<String>();
        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        //public final Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        Cursor cur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.DATA1
                },
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" like'%" + name +"%' AND "+
                ContactsContract.Data.MIMETYPE + "=?",
                new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE}, null
        );

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String contactID = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                String groupID = cur.getString(cur.getColumnIndex(ContactsContract.Data.DATA1));

                System.out.println(name +" has group "+ groupID);

                //groups.add(number);
            }
        }


        return groups;
    }





    protected static HashMap<String, ArrayList<String>> getContactsNumbers() {

        /**
         * http://www.coderzheaven.com/2011/06/13/get-all-details-from-contacts-in-android/
         */
        HashMap<String, ArrayList<String>> contactsNumbers = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> contactsGroups = new HashMap<String, ArrayList<String>>();

        ContentResolver cr = ProfileActivity.getInstance().getContentResolver();

        /**
         * cursor to get ALL contacts
         */
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {

            /**
             * loop through all contacts
             */
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));



                /**
                 * if contact has phone number
                 */
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //System.out.println("name : " + name + ", ID : " + id);

                    ArrayList<String> phoneNumbers = new ArrayList<String>();
                    ArrayList<String> groups = new ArrayList<String>();

                    // get phone numbers
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id},
                            null);

                    while (pCur.moveToNext()) {

                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //System.out.println("phone" + phone);


                        // add number to array of numbers
                        phoneNumbers.add(phone);

                    }
                    pCur.close();


                    // now add everything into hashmap
                    contactsNumbers.put(name, phoneNumbers);
                    contactsGroups.put(name, groups);


                }
            }
        }

        return contactsNumbers;

    }

}
