package com.thestudnet.twicandroidplugin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.R2;
import com.thestudnet.twicandroidplugin.adapters.UserAction;
import com.thestudnet.twicandroidplugin.adapters.UsersAdapter;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 08/04/2017.
 */

public class UsersFragment extends CustomFragment implements ExpandableListView.OnChildClickListener {

    private TextView title;
    private ExpandableListView usersListView;

    List<String> listDataHeader;
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
    }

    /*
         * Preparing the list data
         */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<UserAction>>();

        // Adding child data
        listDataHeader.add("Jeremy Jones");
        listDataHeader.add("Minerva Hardy");
        listDataHeader.add("Maurice Morris");

        // Adding child data
        List<UserAction> top250 = new ArrayList<UserAction>();
        top250.add(new UserAction(this.getContext().getString(R.string.action_request_camera), 1));
        top250.add(new UserAction(this.getContext().getString(R.string.action_request_mic), 2));
        top250.add(new UserAction(this.getContext().getString(R.string.action_request_screen), 3));
        top250.add(new UserAction(this.getContext().getString(R.string.action_request_kick, listDataHeader.get(0)), -1));

        List<UserAction> nowShowing = new ArrayList<UserAction>();
        nowShowing.add(new UserAction(this.getContext().getString(R.string.action_request_camera), 1));
        nowShowing.add(new UserAction(this.getContext().getString(R.string.action_request_mic), 2));
        nowShowing.add(new UserAction(this.getContext().getString(R.string.action_request_screen), 3));
        nowShowing.add(new UserAction(this.getContext().getString(R.string.action_request_kick, listDataHeader.get(1)), -1));

        List<UserAction> comingSoon = new ArrayList<UserAction>();
        comingSoon.add(new UserAction(this.getContext().getString(R.string.action_request_camera), 1));
        comingSoon.add(new UserAction(this.getContext().getString(R.string.action_request_mic), 2));
        comingSoon.add(new UserAction(this.getContext().getString(R.string.action_request_screen), 3));
        comingSoon.add(new UserAction(this.getContext().getString(R.string.action_request_kick, listDataHeader.get(2)), -1));

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_BACK, null);
        return false;
    }

    @OnClick(R2.id.button_close) void close() {
        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_BACK, null);
    }

}
