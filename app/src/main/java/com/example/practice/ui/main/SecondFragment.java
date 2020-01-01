package com.example.practice.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.practice.R;

public class SecondFragment extends Fragment {

    private SecondViewModel mViewModel;

    public static SecondFragment newInstance() {
        return new SecondFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.second_fragment, container, false);

        final GridView gridView = (GridView) view.findViewById(R.id.gridView);
        MyGridAdapter gAdapter = new MyGridAdapter(view.getContext());
        gridView.setAdapter(gAdapter);

        return view;
    }

    public class MyGridAdapter extends BaseAdapter {
        Context context;
        Integer[] imgID = {R.drawable.img01, R.drawable.img02, R.drawable.img03, R.drawable.img04,R.drawable.img05, R.drawable.img06, R.drawable.img07, R.drawable.img08, R.drawable.img09, R.drawable.img10, R.drawable.img11, R.drawable.img12, R.drawable.img13, R.drawable.img14, R.drawable.img15, R.drawable.img16, R.drawable.img17, R.drawable.img18, R.drawable.img19, R.drawable.img20, R.drawable.img21};

        public MyGridAdapter(Context C) { context = C;}

        public int getCount() { return imgID.length; }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(350, 240));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(imgID[position]);

            final int pos  = position;

            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    View scaledView = (View) View.inflate(v.getContext(), R.layout.scaled, null);
                    AlertDialog.Builder s = new AlertDialog.Builder(v.getContext());
                    ImageView scaledImg = (ImageView) scaledView.findViewById(R.id.ScaledImage);
                    scaledImg.setImageResource(imgID[pos]);
                    s.setView(scaledView);
                    s.setNegativeButton("Close",null);
                    s.show();

                }
            });


            return imageView;
        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SecondViewModel.class);
        // TODO: Use the ViewModel
    }

}
