package com.example.joshua.koreancards;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FilterReader;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Cell> implements Filterable {

    private final String DEBUG_TAG = "DEBUG_TAG";
    private int resourceLayout;
    private Context context;
    private ArrayList<Cell> items;
    private ArrayList<Cell> oldItems;


    public CustomAdapter(Context context, int resourceLayout, ArrayList<Cell> items) {
        super(context, resourceLayout, items);
        this.items = items;
        this.resourceLayout = resourceLayout;
        this.context = context;
        this.oldItems = (ArrayList<Cell>)items.clone();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            view = vi.inflate(resourceLayout, null);
        }
        Cell cell = getItem(position);

        if (cell != null) {
            TextView foreignWord = (TextView) view.findViewById(R.id.foreignText);
            TextView nativeWord = (TextView) view.findViewById(R.id.nativeText);
            TextView eFactor = (TextView) view.findViewById(R.id.eFactorText);

            if (foreignWord != null) {
                foreignWord.setText(cell.getKorean());
            }
            if (nativeWord != null) {
                nativeWord.setText(cell.getEnglish());
            }
            if (eFactor != null) {
                eFactor.setText(Double.toString(cell.getDifficulty()));
            }
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(DEBUG_TAG, "PublishResults called.");
                items.clear();
                for(Cell c : (ArrayList<Cell>) results.values) {
                    items.add(c);
                }
                notifyDataSetChanged();
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(DEBUG_TAG, "performFiltering called: " + constraint.toString() + "\toldItem size: " + Integer.toString(oldItems.size()));
                FilterResults results = new FilterResults();
                ArrayList<Cell> FilteredArrayNames = new ArrayList<>();

//                items = oldItems;

                constraint = constraint.toString().toLowerCase();
                String tempString = constraint.toString().trim().toLowerCase();
                int counter = 0;
                Log.d(DEBUG_TAG, "Constraint String value: " + tempString);
                for(int i =1; i < oldItems.size(); i++) {
//                    Log.d(DEBUG_TAG, oldItems.get(i).getEnglish().toLowerCase());
                    if(oldItems.get(i).getEnglish().toLowerCase().contains(tempString)) {
                        FilteredArrayNames.add(oldItems.get(i));
//                        Log.d(DEBUG_TAG, oldItems.get(i).getEnglish() + " added.");
                        counter+=1;
                    }
                }
                Log.d(DEBUG_TAG, Integer.toString(counter));
                results.values = FilteredArrayNames;
                results.count = FilteredArrayNames.size();
                return results;
            }
        };
        return filter;
    }

}