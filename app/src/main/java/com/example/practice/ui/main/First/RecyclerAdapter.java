package com.example.practice.ui.main.First;

import android.app.AlertDialog;
import android.content.Context;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.List;

import com.example.practice.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {
    List<Dictionary> mData;
    List<Dictionary> filteredmData;
    Context context;
    CustomFilter filter;

    private FirstViewModel mViewModel;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        protected TextView name;
        protected ImageButton callbutton;
        protected ImageButton messagebutton;
        protected String group;
        protected String number;

        ViewHolder(View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조. (hold strong reference)
            name = itemView.findViewById(R.id.text2);
            callbutton = itemView.findViewById(R.id.callButton);
            messagebutton = itemView.findViewById(R.id.messageButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("연락처").setMessage("(" + group + ") " + number);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            itemView.setOnCreateContextMenuListener(this);

            callbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("tel:" + number));
                    context.startActivity(intent);
                }
            });
            messagebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("sms:" + number));
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            MenuItem Edit = menu.add(Menu.NONE, 1001, 1, "편집");
            MenuItem Delete = menu.add(Menu.NONE, 1002, 2, "삭제");
            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:  // 5. 편집 항목을 선택시
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        View v = LayoutInflater.from(context)
                                .inflate(R.layout.frist_edittext, null, false);
                        builder.setView(v);
                        final EditText editname = v.findViewById(R.id.editname); //view에는 callbutton 존재x
                        final EditText editgroup = v.findViewById(R.id.editgroup);
                        final EditText editnumber = v.findViewById(R.id.editnumber);
                        final Button buttonsubmit = v.findViewById(R.id.okbutton);
                        editname.setText(name.getText());
                        editgroup.setText(group);
                        editnumber.setText(number);
                        editname.setSelection(editname.length());//커서 위치 끝으로
                        editgroup.setSelection(editgroup.length());
                        editgroup.setSelection(editgroup.length());

                        final AlertDialog dialog = builder.create();
                        buttonsubmit.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                String strName = editname.getText().toString();
                                String strGroup = editgroup.getText().toString();
                                String strNumber = editnumber.getText().toString();
                                Dictionary dict = new Dictionary(strName, strGroup, strNumber);
                                mData.set(getAdapterPosition(), dict);
                                notifyItemChanged(getAdapterPosition());
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                    case 1002:
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                        alert_confirm.setMessage("Do you want to delete the contact?").setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int num = getAdapterPosition();
                                        mViewModel.delete(mData.get(num));
                                        //mData.remove(num);
                                        notifyItemRemoved(num);
                                        notifyItemRangeChanged(num, mData.size());
                                        dialog.dismiss();
                                        return;
                                    }
                                }).setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        return;
                                    }
                                });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                        break;
                }
                return true;
            }
        };
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public RecyclerAdapter(List<Dictionary> list, Context context, FirstViewModel firstViewModel) {
        this.mData = list;
        this.filteredmData = list;
        this.context = context;
        this.mViewModel=firstViewModel;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.first_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mData.get(position).getName());
        holder.group = mData.get(position).getGroup();
        holder.number = mData.get(position).getNumber();
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        if (mData != null) {
            Log.d("getitem",mData+"");
            return mData.size();}
        else return 0;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter(filteredmData, this);
        }
        return filter;
    }
}