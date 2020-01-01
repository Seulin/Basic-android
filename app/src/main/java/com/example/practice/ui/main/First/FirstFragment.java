package com.example.practice.ui.main.First;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

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

public class FirstFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    RecyclerView rv;
    RecyclerAdapter ra;
    SearchView sv;
    FloatingActionButton fab, add, sync;
    Animation fabopen, fabclose, fabrclock, fabranticlock;
    boolean isOpen = false;

    private FirstViewModel mViewModel;

    public static FirstFragment newInstance() {
        return new FirstFragment(); //
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(FirstViewModel.class);

/*        final Observer<ArrayList<Dictionary>> listObserver = new Observer<ArrayList<Dictionary>>() {
            @Override
            public void onChanged(ArrayList<Dictionary> dictionaries) {
                //not used in this code,
            }
        };
        mViewModel.getLiveList().observe(this, listObserver);*/
        mViewModel.getList();
        mViewModel.jsonProcess(getResources().getAssets());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.first_fragment, container, false);
        rv = view.findViewById(R.id.recycler);
        sv = view.findViewById(R.id.search_view);
        rv.addItemDecoration(new DividerItemDecoration(view.getContext(), 1));

        ra = new RecyclerAdapter(mViewModel.getList(), getActivity());
        rv.setAdapter(ra);

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

        sv.setOnQueryTextListener(this);

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


    @Override
    public boolean onQueryTextSubmit(String queryString) {
        ra.getFilter().filter(queryString);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryString) {
        ra.getFilter().filter(queryString);
        return false;
    }

}
