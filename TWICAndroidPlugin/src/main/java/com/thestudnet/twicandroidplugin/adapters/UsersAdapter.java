package com.thestudnet.twicandroidplugin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thestudnet.twicandroidplugin.R;

import java.util.HashMap;
import java.util.List;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 08/04/2017.
 */

public class UsersAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<UserAction>> _listDataChild;

    public UsersAdapter(Context context, List<String> listDataHeader, HashMap<String, List<UserAction>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final UserAction child = (UserAction) getChild(groupPosition, childPosition);

        /*
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(childPosition < 3) {
                convertView = inflater.inflate(R.layout.list_user_action, null);
            }
            else {
                convertView = inflater.inflate(R.layout.button_deny_small, null);
            }
        }
        */

        LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(childPosition < 3) {
            convertView = inflater.inflate(R.layout.list_user_action, null);
        }
        else {
            convertView = inflater.inflate(R.layout.button_deny_small, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.user_action_text);
        txtListChild.setText(child.getText());

        ImageView icon = (ImageView) convertView.findViewById(R.id.user_action);
        if(child.getType() == 1) {
            icon.setImageResource(R.drawable.action_camera);
        }
        else if(child.getType() == 2) {
            icon.setImageResource(R.drawable.action_mic);
        }
        else if(child.getType() == 3) {
            icon.setImageResource(R.drawable.action_screen);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_user, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.user_name);
        lblListHeader.setText(headerTitle);

        ImageView expandablelistview_indicator = (ImageView) convertView.findViewById(R.id.expandablelistview_indicator);
        expandablelistview_indicator.setSelected(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
