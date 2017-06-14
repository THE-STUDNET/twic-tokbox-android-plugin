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
import com.thestudnet.twicandroidplugin.managers.APIClient;
import com.thestudnet.twicandroidplugin.managers.HangoutManager;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
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

                // Check if YOU have askDevice rights
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKDEVICE)) {
                    // Check if user is streaming camera OR/AND microphone
                    if(!UserManager.getInstance().isSharingCamera(userId)) {
                        // check if tab User was asking for camera permission
                        if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKCAMERA, userId)) {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_allow_user_to_share_his_camera), UserAction.Type.ALLOW_USER_TO_SHARE_HIS_CAMERA));
                        }
                        else {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_ask_user_to_share_his_camera), UserAction.Type.ASK_USER_TO_SHARE_HIS_CAMERA));
                        }
                    }
                    if(!UserManager.getInstance().isSharingAudio(userId)) {
                        // check if User was asking for microphone permission
                        if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKMICROPHONE, userId)) {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_allow_user_to_share_his_microphone), UserAction.Type.ALLOW_USER_TO_SHARE_HIS_MICROPHONE));
                        }
                        else {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_ask_user_to_share_his_microphone), UserAction.Type.ASK_USER_TO_SHARE_HIS_MICROPHONE));
                        }
                    }
                }

                // Check if YOU have askScreen rights
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKSCREEN)) {
                    // Check if user is streaming screen(s)
                    if(!UserManager.getInstance().isSharingScreen(userId)) {
                        // check if tab User was asking for screensharing permission
                        if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKSCREEN, userId)) {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_allow_user_to_share_his_screen), UserAction.Type.ALLOW_USER_TO_SHARE_HIS_SCREEN));
                        }
                        else {
                            userActions.add(new UserAction(this.getContext().getString(R.string.action_ask_user_to_share_his_screen), UserAction.Type.ASK_USER_TO_SHARE_HIS_SCREEN));
                        }
                    }
                }

                // Check if YOU have forceUnpublish rights
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONFORCEUNPUSBLISH)) {
                    // Check if user is streaming screen(s)
                    if(UserManager.getInstance().isSharingScreen(userId)) {
                        // Display "Force user to unpublish screen(s)"
                        userActions.add(new UserAction(this.getContext().getString(R.string.action_force_user_to_unpublish_screen), UserAction.Type.FORCE_USER_TO_UNPUBLISH_SCREEN));
                    }
                    // Check if user is streaming camera
                    if(UserManager.getInstance().isSharingCamera(userId)) {
                        // Display "Force user to unpublish camera"
                        userActions.add(new UserAction(this.getContext().getString(R.string.action_force_user_to_unpublish_camera), UserAction.Type.FORCE_USER_TO_UNPUBLISH_CAMERA));
                    }
                    // Check if user is streaming microphone
                    if(UserManager.getInstance().isSharingAudio(userId)) {
                        // Display "Force user to unpublish audio"
                        userActions.add(new UserAction(this.getContext().getString(R.string.action_force_user_to_unpublish_microphone), UserAction.Type.FORCE_USER_TO_UNPUBLISH_MICROPHONE));
                    }
                }

                // Check if YOU has kick rights
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONKICK)) {
                    userActions.add(new UserAction(this.getContext().getString(R.string.action_kick_user, user.optString(UserManager.USER_FIRSTNAMEKEY) + " " + user.optString(UserManager.USER_LASTNAMEKEY)), UserAction.Type.KICK_USER));
                }

                // Adding the whole group of data
                listDataChild.put(userId, userActions);
            }
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        JSONObject user = this.listDataHeader.get(groupPosition);
        if(!user.optBoolean(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, false)) {
            return true; // Consume the event
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
        JSONObject user = this.listDataHeader.get(groupPosition);
        String userId = user.optString(UserManager.USER_IDKEY, "");
        UserAction action = (UserAction) view.getTag();
        if(action.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_CAMERA || action.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_CAMERA) {
            // Signal "hgt_camera_requested" via tokbox to user
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CAMERAREQUESTED, userId);
            // Register "hangout.launchusercamera" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_LAUNCH_USERCAMERA);
        }
        else if(action.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_MICROPHONE || action.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_MICROPHONE) {
            // Signal "hgt_microphone_requested" via tokbox to user
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_MICROPHONEREQUESTED, userId);
            // Register "hangout.launchusermicrophone" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_LAUNCH_USERMICROPHONE);
        }
        else if(action.getType() == UserAction.Type.ALLOW_USER_TO_SHARE_HIS_SCREEN || action.getType() == UserAction.Type.ASK_USER_TO_SHARE_HIS_SCREEN) {
            // Signal "hgt_screen_requested" via tokbox to user
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_SCREENREQUESTED, userId);
            // Register "hangout.launchuserscreen" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_LAUNC_HUSERSCREEN);
        }
        else if(action.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_SCREEN) {
            // ForceUnpublish all user's screensharing streams via tokbox
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_FORCEUNPUBLISHSCREEN, userId);
        }
        else if(action.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_CAMERA) {
            // ForceUnpublish all user's camera stream via tokbox
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_FORCEUNPUBLISHSTREAM, userId);
        }
        else if(action.getType() == UserAction.Type.FORCE_USER_TO_UNPUBLISH_MICROPHONE) {
            // ForceUnpublish all user's microphone stream via tokbox
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_FORCEMUTESTREAM, userId);
        }
        else if(action.getType() == UserAction.Type.KICK_USER) {
            // Force user to disconnect via tokbox
            TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_KICKUSER, userId);
            // Register "hangout.kickuser" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_KICK_USER);
        }
        return true;
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
