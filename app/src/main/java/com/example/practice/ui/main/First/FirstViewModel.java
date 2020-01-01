package com.example.practice.ui.main.First;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;

public class FirstViewModel extends ViewModel {
    private MutableLiveData<List<Dictionary>> contactList;
    private List<Dictionary> items;

    public MutableLiveData<List<Dictionary>> getLiveList() {
        if (contactList == null) {
            items = new ArrayList<>();
            contactList = new MutableLiveData<>();
        }
        return contactList;
    }

    public List<Dictionary> getList() {
        if (items == null) {
            items = new ArrayList<>();
            //contactList = new MutableLiveData<>();
        }
        return items;
    }

    public int getSize() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public void add(Dictionary dict) {
        items.add(dict);
        contactList.setValue(items);
    }

    public void delete(Dictionary dict) {
        items.remove(dict);
        contactList.setValue(items);
    }

    public void jsonProcess(AssetManager am) {
        String json = getJsonString(am);
        jsonParsing(json, items);
    }

    private String getJsonString(AssetManager am) {
        String json = "";

        try {
            InputStream is = am.open("data.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private void jsonParsing(String json, List<Dictionary> items) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            String ContactAddress = jsonObject.getString("ContactAddress");
            JSONArray dictionaryArray = new JSONArray(ContactAddress);

            for (int i = 0; i < dictionaryArray.length(); i++) {
                JSONObject dictObject = dictionaryArray.getJSONObject(i);
                Dictionary dict = new Dictionary();
                dict.setName(dictObject.getString("name"));
                dict.setGroup(dictObject.getString("group"));
                dict.setNumber(dictObject.getString("number"));
                if (hasdict(dict, items)) {
                    break;
                } else {
                    add(dict);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getContactList(FragmentActivity fa) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };

        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = fa.getContentResolver().query(uri, projection, null,
                selectionArgs, sortOrder);

        LinkedHashSet<Dictionary> hashlist = new LinkedHashSet<>();
        List<Dictionary> contactsList;

        if (cursor.moveToFirst()) {
            do {

                Dictionary myContact = new Dictionary();
                myContact.setNumber(cursor.getString(0));
                myContact.setName(cursor.getString(1));

                hashlist.add(myContact);
            } while (cursor.moveToNext());
        }

        contactsList = new ArrayList<Dictionary>(hashlist);
        for (int i = 0; i < contactsList.size(); i++) {
            contactsList.get(i).setGroup("none");
        }
        if (cursor != null) {
            cursor.close();
        }
        List<Dictionary> copy = new ArrayList<>(items);
        for (Dictionary dict : contactsList) {
            if (hasdict(dict, copy)) {
                break;
            } else {
                add(dict);
            }
        }
    }

    private boolean hasdict(Dictionary dict, List<Dictionary> items) {
        for (Dictionary item : items) {
            if (item.getNumber().equals(dict.getNumber())) {
                return true;
            }
        }
        return false;
    }
}