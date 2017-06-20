package com.thestudnet.twicandroidplugin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
import com.thestudnet.twicandroidplugin.managers.UserManager;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 19/06/2017.
 */

public class UserDemandFragment extends CustomFragment implements View.OnClickListener {

    private TextView lblPage;

    public static UserDemandFragment newInstance(int page, boolean isLast, String userDemand) {
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putString("userDemand", userDemand);
        if (isLast)
            args.putBoolean("isLast", true);
        final UserDemandFragment fragment = new UserDemandFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.popup_demand, container, false);
        //lblPage = (TextView) view.findViewById(R.id.lbl_page);

        ImageView demand_icon = (ImageView) view.findViewById(R.id.demand_icon);
        TextView demand_text = (TextView) view.findViewById(R.id.demand_text);

        String[] userDemand = getArguments().getString("userDemand").split("_#_");
        JSONObject user = UserManager.getInstance().getSettingsForKey(userDemand[1]);
        if(user != null) {
            if(userDemand[0].equals(TokBoxClient.SIGNALTYPE_CAMERAAUTHORIZATION)) {
                demand_icon.setImageResource(R.drawable.user_action_camera);
                demand_text.setText(getResources().getString(R.string.demand_text_camera, user.optString(UserManager.USER_FIRSTNAMEKEY) + " " + user.optString(UserManager.USER_LASTNAMEKEY)));

                getArguments().putInt("demandType", 1); // 1 = camera, 2 = microphone
            }
            else if(userDemand[0].equals(TokBoxClient.SIGNALTYPE_MICROPHONEAUTHORIZATION)) {
                demand_icon.setImageResource(R.drawable.user_action_mic);
                demand_text.setText(getResources().getString(R.string.demand_text_microphone, user.optString(UserManager.USER_FIRSTNAMEKEY) + " " + user.optString(UserManager.USER_LASTNAMEKEY)));

                getArguments().putInt("demandType", 2); // 1 = camera, 2 = microphone
            }

            view.findViewById(R.id.button_deny).setOnClickListener(this);
            view.findViewById(R.id.button_accept).setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        String[] userDemand = getArguments().getString("userDemand").split("_#_");
        if(getArguments().getInt("demandType") == 1) { // 1 = camera, 2 = microphone
            if(v.getId() == R.id.button_deny) {
                // Signal "hgt_cancel_camera_authorization" event via tokbox for user ID
                TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CANCELCAMERAAUTHORIZATION, userDemand[1]);
                // Notify dataset changed
                ArrayList<String> list = new ArrayList<>(1);
                list.add(getArguments().getString("userDemand"));
                FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_USER_DEMAND_REMOVED, list);
            }
            else if(v.getId() == R.id.button_accept) {
                // Signal "hgt_camera_requested" event via tokbox for user ID
                TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CAMERAREQUESTED, userDemand[1]);
                // Notify dataset changed
                ArrayList<String> list = new ArrayList<>(1);
                list.add(getArguments().getString("userDemand"));
                FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_USER_DEMAND_REMOVED, list);
            }
        }
        else if(getArguments().getInt("demandType") == 2) { // 1 = camera, 2 = microphone
            if(v.getId() == R.id.button_deny) {
                // Signal "hgt_cancel_microphone_authorization" event via tokbox for user ID
                TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION, userDemand[1]);
                // Notify dataset changed
                ArrayList<String> list = new ArrayList<>(1);
                list.add(getArguments().getString("userDemand"));
                FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_USER_DEMAND_REMOVED, list);
            }
            else if(v.getId() == R.id.button_accept) {
                // Signal "hgt_microphone_requested" event via tokbox for user ID
                TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_MICROPHONEREQUESTED, userDemand[1]);
                // Notify dataset changed
                ArrayList<String> list = new ArrayList<>(1);
                list.add(getArguments().getString("userDemand"));
                FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_USER_DEMAND_REMOVED, list);
            }
        }
    }

}
