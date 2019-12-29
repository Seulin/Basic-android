package com.example.practice.ui.main.First;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practice.MainActivity;
import com.example.practice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class FirstFragment extends Fragment {
    RecyclerView rv;
    RecyclerAdapter ra;
    FloatingActionButton fab, add, sync;
    Animation fabopen, fabclose, fabrclock, fabranticlock;
    boolean isOpen = false;

    private FirstViewModel mViewModel;
    private ArrayList<Dictionary> Items;

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Items = new ArrayList<>();
        String json = getJsonString();
        jsonParsing(json, Items);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.first_fragment, container, false);
        rv = view.findViewById(R.id.recycler);
        rv.addItemDecoration(new DividerItemDecoration(view.getContext(), 1));

        ra = new RecyclerAdapter(Items, getActivity());
        rv.setAdapter(ra);

        fab = view.findViewById(R.id.fab);
        add = view.findViewById(R.id.add);
        sync = view.findViewById(R.id.sync);

        fabopen = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);
        fabrclock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_clockwise);
        fabranticlock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_anticlockwise);

        fab.setOnClickListener(clickListener);
        add.setOnClickListener(clickListener);
        sync.setOnClickListener(clickListener);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    if (isOpen) {
                        add.startAnimation(fabclose);
                        sync.startAnimation(fabclose);
                        fab.startAnimation(fabranticlock);
                        add.setClickable(false);
                        sync.setClickable(false);
                        isOpen = false;}
                    fab.hide();
                    fab.setClickable(false);
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                    fab.setClickable(true);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(FirstViewModel.class);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab:
                    if (!isOpen) {
                        add.startAnimation(fabopen);
                        sync.startAnimation(fabopen);
                        fab.startAnimation(fabrclock);
                        add.setClickable(true);
                        sync.setClickable(true);
                        isOpen = true;
                    } else {
                        add.startAnimation(fabclose);
                        sync.startAnimation(fabclose);
                        fab.startAnimation(fabranticlock);
                        add.setClickable(false);
                        sync.setClickable(false);
                        isOpen = false;
                    } break;
                case R.id.add: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View v = LayoutInflater.from(getActivity()).inflate(R.layout.edittext, null, false);
                    builder.setView(v);

                    final EditText editname = v.findViewById(R.id.editname); //view에는 callbutton 존재x
                    final EditText editgroup = v.findViewById(R.id.editgroup);
                    final EditText editnumber = v.findViewById(R.id.editnumber);
                    final Button buttonsubmit = v.findViewById(R.id.button);

                    final AlertDialog dialog = builder.create();
                    buttonsubmit.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            String strName = editname.getText().toString();
                            String strGroup = editgroup.getText().toString();
                            String strNumber = editnumber.getText().toString();

                            Dictionary dict = new Dictionary(strName, strGroup, strNumber);
                            Items.add(dict); // RecyclerView의 마지막 줄에 삽입
                            ra.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    break;
                }
                case R.id.sync: {
                   try{
                        getContactList();
                        ra.notifyDataSetChanged();
                    } catch(SecurityException e) {
                        Snackbar.make(rv, "Accessing to contact is not allowed. Change your setting.", Snackbar.LENGTH_SHORT).show();
                    }
                break; }
            }
        }
    };

    private void getContactList() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        };

        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null,
                selectionArgs, sortOrder);

        LinkedHashSet<Dictionary> hashlist = new LinkedHashSet<>();
        ArrayList<Dictionary> contactsList;

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
        ArrayList<Dictionary> copy = new ArrayList<>(Items);
        for (Dictionary dict : contactsList) {
            if (hasdict(dict, copy)) {
                break;}
            else { Items.add(dict); }
        }
    }

    private boolean hasdict(Dictionary dict, ArrayList<Dictionary> items) {
        for (Dictionary item : items) {
            if (item.getNumber().equals(dict.getNumber())) { return true; }
        } return false;
    }


    private String getJsonString() {
        String json = "";

        try {
            InputStream is = getResources().getAssets().open("data.json");
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

    private void jsonParsing(String json, ArrayList<Dictionary> items) {
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
                items.add(dict);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
