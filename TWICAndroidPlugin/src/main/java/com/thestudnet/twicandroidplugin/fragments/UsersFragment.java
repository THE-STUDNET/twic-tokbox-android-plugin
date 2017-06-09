package com.thestudnet.twicandroidplugin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.R2;
import com.thestudnet.twicandroidplugin.adapters.UserAction;
import com.thestudnet.twicandroidplugin.adapters.UsersAdapter;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.UserManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 08/04/2017.
 */

public class UsersFragment extends CustomFragment implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {

    private TextView title;
    private ExpandableListView usersListView;

    List<JSONObject> listDataHeader;
    HashMap<String, List<UserAction>> listDataChild;

    /**
     * Returns a new instance of this fragment
     */
    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
        return fragment;
    }

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        ButterKnife.bind(this, rootView);

        this.title = (TextView) rootView.findViewById(R.id.title);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.prepareListData();

        this.title.setText(this.getContext().getString(R.string.fragment_users_title, String.valueOf(this.listDataHeader.size())));

        this.usersListView = (ExpandableListView) view.findViewById(R.id.users_listview);
        this.usersListView.setAdapter(new UsersAdapter(this.getContext(), listDataHeader, listDataChild));
        this.usersListView.setOnChildClickListener(this);
        this.usersListView.setOnGroupClickListener(this);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<JSONObject>();
        listDataChild = new HashMap<String, List<UserAction>>();
        int index = 0;

        for(String userId : UserManager.getInstance().getKeys()) {
            if(!userId.equals(UserManager.getInstance().getCurrentUserId())) {
                // Adding header data
                JSONObject user = UserManager.getInstance().getSettingsForKey(userId);
//                String displayName = user.optString(UserManager.USER_NICKNAMEKEY, "no nickname");
                listDataHeader.add(user);
                // Adding child data
                List<UserAction> userActions = new ArrayList<UserAction>();
                userActions.add(new UserAction(this.getContext().getString(R.string.action_request_camera), 1));
                userActions.add(new UserAction(this.getContext().getString(R.string.action_request_mic), 2));
                userActions.add(new UserAction(this.getContext().getString(R.string.action_request_screen), 3));
                userActions.add(new UserAction(this.getContext().getString(R.string.action_request_kick, user.optString(UserManager.USER_FIRSTNAMEKEY) + " " + user.optString(UserManager.USER_LASTNAMEKEY)), -1));
                // Adding the whole group of data
                listDataChild.put(userId, userActions);
            }
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        JSONObject user = this.listDataHeader.get(groupPosition);
        if(!"connected".equals(user.optString(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, "disconnected"))) {
            return true; // Consume the event
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_BACK, null);
        return false;
    }

    @OnClick(R2.id.button_close) void close() {
        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_BACK, null);
    }

    @Subscribe
    public void OnAPIInteraction(APIInteraction.OnAPIInteractionEvent event) {
        if(event.getType() == APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED) {
            this.prepareListData();
            UsersAdapter adapter = new UsersAdapter(this.getContext(), listDataHeader, listDataChild);
            this.usersListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

}
