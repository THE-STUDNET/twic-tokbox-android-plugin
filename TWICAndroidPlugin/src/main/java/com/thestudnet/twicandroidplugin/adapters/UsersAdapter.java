package com.thestudnet.twicandroidplugin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.managers.SettingsManager;
import com.thestudnet.twicandroidplugin.managers.UserManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 08/04/2017.
 */

public class UsersAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<JSONObject> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<UserAction>> _listDataChild;

    public UsersAdapter(Context context, List<JSONObject> listDataHeader, HashMap<String, List<UserAction>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        JSONObject user = (JSONObject) getGroup(groupPosition);
        return this._listDataChild.get(user.optString(UserManager.USER_IDKEY)).get(childPosititon);
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
        if(child.getType() != UserAction.Type.KICK_USER) {
            convertView = inflater.inflate(R.layout.list_user_action, null);
        }
        else {
            convertView = inflater.inflate(R.layout.button_deny, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.user_action_text);
        txtListChild.setText(child.getText());

        ImageView icon = (ImageView) convertView.findViewById(R.id.user_action);
        if(child.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_CAMERA || child.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_CAMERA || child.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_CAMERA) {
            icon.setImageResource(R.drawable.action_camera);
        }
        else if(child.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_MICROPHONE || child.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_MICROPHONE || child.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_MICROPHONE) {
            icon.setImageResource(R.drawable.action_mic);
        }
        else if(child.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_SCREEN || child.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_SCREEN || child.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_SCREEN) {
            icon.setImageResource(R.drawable.action_screen);
        }

        convertView.setTag(child);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        JSONObject user = getGroup(groupPosition);
        return this._listDataChild.get(user.optString(UserManager.USER_IDKEY)).size();
    }

    @Override
    public JSONObject getGroup(int groupPosition) {
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
        JSONObject user = (JSONObject) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_user, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.user_name);
        lblListHeader.setText(UserManager.getInstance().getDisplayName(user.optString("id")));

        com.makeramen.roundedimageview.RoundedImageView user_connection_state = (com.makeramen.roundedimageview.RoundedImageView) convertView.findViewById(R.id.user_connection_state);
        ImageView sharing_camera = (ImageView) convertView.findViewById(R.id.sharing_camera);
        ImageView sharing_mic = (ImageView) convertView.findViewById(R.id.sharing_mic);
        ImageView sharing_screen = (ImageView) convertView.findViewById(R.id.sharing_screen);
        ImageView expandablelistview_indicator = (ImageView) convertView.findViewById(R.id.expandablelistview_indicator);
        // Check user connection state
        if(user.optBoolean(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, false)) {
            user_connection_state.setImageResource(R.color.action_green);
            // Check user streaming states
            if(UserManager.getInstance().isSharingAudio(user.optString(UserManager.USER_IDKEY, ""))) {
                sharing_mic.setVisibility(View.VISIBLE);
            }
            else {
                sharing_mic.setVisibility(View.INVISIBLE);
            }
            if(UserManager.getInstance().isSharingCamera(user.optString(UserManager.USER_IDKEY, ""))) {
                sharing_camera.setVisibility(View.VISIBLE);
            }
            else {
                sharing_camera.setVisibility(View.INVISIBLE);
            }
            if(UserManager.getInstance().isSharingScreen(user.optString(UserManager.USER_IDKEY, ""))) {
                sharing_screen.setVisibility(View.VISIBLE);
            }
            else {
                sharing_screen.setVisibility(View.INVISIBLE);
            }
            expandablelistview_indicator.setVisibility(View.VISIBLE);
        }
        else {
            user_connection_state.setImageResource(R.color.action_red);
            sharing_mic.setVisibility(View.INVISIBLE);
            sharing_camera.setVisibility(View.INVISIBLE);
            sharing_screen.setVisibility(View.INVISIBLE);
            expandablelistview_indicator.setVisibility(View.INVISIBLE);
        }

        ImageView user_avatar_image = (ImageView) convertView.findViewById(R.id.user_avatar_image);
        JSONObject dsmSettings = SettingsManager.getInstance().getSettingsForKey(SettingsManager.SETTINGS_DMSKEY);
        if(dsmSettings != null) {
            JSONObject pathKeySettings = dsmSettings.optJSONObject(SettingsManager.SETTINGS_PATHSKEY);
            if(pathKeySettings != null) {
                String url = dsmSettings.optString(SettingsManager.SETTINGS_PROTOCOLKEY, "")
                        + "://"
                        + dsmSettings.optString(SettingsManager.SETTINGS_DOMAINKEY, "")
                        + "/"
                        + pathKeySettings.optString("datas", "")
                        + "/"
                        + user.optString(UserManager.USER_AVATARKEY, "");
                Glide.with(this._context).load(url).error(R.drawable.users).centerCrop().into(user_avatar_image);
            }
        }

        expandablelistview_indicator.setSelected(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        JSONObject user = (JSONObject) getGroup(groupPosition);
        if(!user.optBoolean(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, false)) {
            return false;
        }
        else {
            return true;
        }
    }

}
