package com.example.practice.ui.main.First;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.practice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment implements View.OnClickListener {
    RecyclerView rv;
    RecyclerAdapter ra;
    SearchView sv;
    FloatingActionButton fab, add, sync;
    Animation fabopen, fabclose, fabrclock, fabranticlock;
    boolean isOpen = false;

    public List<Dictionary> dictList = new ArrayList<>();
    private FirstViewModel mViewModel;


    public static FirstFragment newInstance() {
        return new FirstFragment(); //
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(FirstViewModel.class);

       final Observer<List<Dictionary>> listObserver = new Observer<List<Dictionary>>() {
           @Override
           public void onChanged(List<Dictionary> dictionaries) {
               dictList = dictionaries;
               try {
               sv.setQueryHint(ra.getItemCount()+" contacts in your device"); }
               catch (NullPointerException e) { }
               Log.d("aaobeserv", dictList+"");
           }
       };
        mViewModel.getLiveList().observe(getActivity(), listObserver);
        mViewModel.jsonProcess(getResources().getAssets());
        Log.d("create", dictList+"");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("jsonprosse", "done");
        View view = inflater.inflate(R.layout.first_fragment, container, false);
        rv = view.findViewById(R.id.recycler);
        sv = view.findViewById(R.id.search_view);
        rv.addItemDecoration(new DividerItemDecoration(view.getContext(), 1));

        Log.d("aarecyclerad", dictList+"");
        Log.d("aaviewmodel", mViewModel.getList()+"");
        ra = new RecyclerAdapter(dictList, getActivity(), mViewModel);
        rv.setAdapter(ra);
        sv.setQueryHint(ra.getItemCount()+" contacts in your device");

        fab = view.findViewById(R.id.fab);
        add = view.findViewById(R.id.add);
        sync = view.findViewById(R.id.sync);

        fabopen = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);
        fabrclock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_clockwise);
        fabranticlock = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_anticlockwise);

        fab.setOnClickListener(this);
        add.setOnClickListener(this);
        sync.setOnClickListener(this);

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
                        isOpen = false;
                    }
                    fab.hide();
                    fab.setClickable(false);
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                    fab.setClickable(true);
                }
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                ra.getFilter().filter(queryString);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

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
                }
                break;
            case R.id.add: { //same code in the function dialogSendMessage in ThirdFragment.java
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.frist_edittext, null, false);
                builder.setView(v);

                final EditText editname = v.findViewById(R.id.editname); //view에는 callbutton 존재x
                final EditText editgroup = v.findViewById(R.id.editgroup);
                final EditText editnumber = v.findViewById(R.id.editnumber);
                final Button buttonsubmit = v.findViewById(R.id.okbutton);

                final AlertDialog dialog = builder.create();
                buttonsubmit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        String strName = editname.getText().toString();
                        String strGroup = editgroup.getText().toString();
                        String strNumber = editnumber.getText().toString();

                        Dictionary dict = new Dictionary(strName, strGroup, strNumber);
                        mViewModel.add(dict); // RecyclerView의 마지막 줄에 삽입
                        ra.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            }
            case R.id.sync: {
                try {
                    mViewModel.getContactList(getActivity());
                    ra.notifyDataSetChanged();
                } catch (SecurityException e) {
                    Toast.makeText(getActivity(), "Permission is not allowed. Please change your setting.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
