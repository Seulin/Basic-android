package com.example.practice.ui.main.First;

import android.util.Log;
import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;

public class CustomFilter extends Filter{

    RecyclerAdapter adapter;
    List<Dictionary> filterList;

    public CustomFilter(List<Dictionary> filterList, RecyclerAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<Dictionary> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getName().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
                String number = convert(filterList.get(i).getNumber());
                Log.d("convert", number+"");
                if(number.toUpperCase().contains(constraint))
                {Log.d("ifnumbersame", number+"");
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }

            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;

        }

        return results;
    }

    private String convert(String input) {  //010-0000-0000 -> 010000000
        String output = null;
        String substr = input.substring(0, 3);
        output = substr;
        substr = input.substring(4, 8);
        output += substr;
        substr = input.substring(9, 13);
        output += substr;
        Log.d("convert", output+"");
        return output;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.mData= (ArrayList<Dictionary>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }
}