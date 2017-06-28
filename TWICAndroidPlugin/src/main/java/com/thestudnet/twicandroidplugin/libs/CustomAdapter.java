package com.thestudnet.twicandroidplugin.libs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.thestudnet.twicandroidplugin.models.GenericModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CustomAdapter extends BaseAdapter {

    protected final Context context;
    protected ArrayList<? extends GenericModel> values = new ArrayList<GenericModel>();
    protected LinkedHashMap<Integer, View> indexView = new LinkedHashMap<Integer, View>();

    public CustomAdapter(Context context, ArrayList<? extends GenericModel> values) {
        this.context = context;
        this.values = values;
    }

    public int getCount() {
        return this.values.size();
    }

    @Override
    public Object getItem(int position) {
        if(this.values.size() >= position) {
            return this.values.get(position);
        }
        else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean areAllItemsSelectable() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = this.indexView.get(position);

        return rowView;
    }

}
