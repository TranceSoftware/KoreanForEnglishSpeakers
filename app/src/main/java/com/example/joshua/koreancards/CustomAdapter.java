package com.example.joshua.koreancards;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FilterReader;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<CardTable> implements Filterable {

    private final String DEBUG_TAG = "DEBUG_TAG";
    private int resourceLayout;
    private Context context;
    private ArrayList<CardTable> items;
    private ArrayList<CardTable> oldItems;
    private int sdk;

    public CustomAdapter(Context context, int resourceLayout, ArrayList<CardTable> items, int sdk) {
        super(context, resourceLayout, items);
        this.items = items;
        this.resourceLayout = resourceLayout;
        this.context = context;
        this.oldItems = (ArrayList<CardTable>)items.clone();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            view = vi.inflate(resourceLayout, null);
        }
        CardTable cardTable = getItem(position);

        if (cardTable != null) {
            TextView foreignWord = (TextView) view.findViewById(R.id.foreignText);
            TextView nativeWord = (TextView) view.findViewById(R.id.nativeText);
            ImageView posButton = (ImageView) view.findViewById(R.id.button1);
//            TextView eFactor = (TextView) view.findViewById(R.id.eFactorText);

            if (foreignWord != null) {
                foreignWord.setText(cardTable.getForeignWord());
            }
            if (nativeWord != null) {
                nativeWord.setText(" - " + cardTable.getNativeWord());
            }
            if(posButton != null) {
                switch (cardTable.getPos()) {
                    case "n":
                        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                            posButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_btn_blue));
                        } else {
                            posButton.setBackground(context.getResources().getDrawable(R.drawable.bg_btn_blue));
                        }
                        break;
                    case "v":
                        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                            posButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_btn_outline_green));
                        } else {
                            posButton.setBackground(context.getResources().getDrawable(R.drawable.bg_btn_outline_green));
                        }
                        break;
                    case "j":
                        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                            posButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_btn_outline_red));
                        } else {
                            posButton.setBackground(context.getResources().getDrawable(R.drawable.bg_btn_outline_red));
                        }
                        break;
                    case "r":
                        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                            posButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_btn_outline_pink));
                        } else {
                            posButton.setBackground(context.getResources().getDrawable(R.drawable.bg_btn_outline_pink));
                        }
                        break;
                }
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
                for(CardTable c : (ArrayList<CardTable>) results.values) {
                    items.add(c);
                }
                notifyDataSetChanged();
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(DEBUG_TAG, "performFiltering called: " + constraint.toString() + "\toldItem size: " + Integer.toString(oldItems.size()));
                FilterResults results = new FilterResults();
                ArrayList<CardTable> FilteredArrayNames = new ArrayList<>();

//                items = oldItems;

                constraint = constraint.toString().toLowerCase();
                String tempString = constraint.toString().trim().toLowerCase();
                int counter = 0;
                Log.d(DEBUG_TAG, "Constraint String value: " + tempString);
                for(int i =0; i < oldItems.size(); i++) { //i was initiall set to 1 todo
//                    Log.d(DEBUG_TAG, oldItems.get(i).getEnglish().toLowerCase());
                    if(oldItems.get(i).getNativeWord().toLowerCase().contains(tempString)) {
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
    @Override
    public long getItemId(int position) {
        return items.get(position).getIndexKey();
    }
}