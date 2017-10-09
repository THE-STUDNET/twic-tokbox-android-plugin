package com.thestudnet.twicandroidplugin.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.opentok.android.Publisher;
import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.R2;
import com.thestudnet.twicandroidplugin.TWICAndroidPlugin;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.events.MessageInteraction;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.fragments.UsersDemandsDialogFragment;
import com.thestudnet.twicandroidplugin.fragments.UsersFragment;
import com.thestudnet.twicandroidplugin.fragments.VideoDetailFragment;
import com.thestudnet.twicandroidplugin.fragments.VideoGridFragment;
import com.thestudnet.twicandroidplugin.managers.APIClient;
import com.thestudnet.twicandroidplugin.managers.HangoutManager;
import com.thestudnet.twicandroidplugin.managers.MessagesManager;
import com.thestudnet.twicandroidplugin.managers.SettingsManager;
import com.thestudnet.twicandroidplugin.managers.SocketIoClient;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
import com.thestudnet.twicandroidplugin.managers.UserManager;
import com.thestudnet.twicandroidplugin.models.GenericModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class TWICAndroidPluginActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, EasyPermissions.PermissionCallbacks, View.OnKeyListener {

    private static final String TAG = "com.thestudnet.twicandroidplugin " + TWICAndroidPluginActivity.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private AlertDialog userDialog;
    private AlertDialog confirmDialog;
    private UsersDemandsDialogFragment demandDialog;
    private AlertDialog cancelDialog;

    private ImageView publish_camera;
    private ImageView publish_mic;

    private ImageView button_record;

    private ArrayList<String> usersDemands;

    private LinearLayout messages_panel;
    private EditText type_message;
    private ImageView new_message_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_twic_android_plugin);

        this.usersDemands = new ArrayList<>();

        this.publish_camera = (ImageView) this.findViewById(R.id.publish_camera);
        this.publish_mic = (ImageView) this.findViewById(R.id.publish_mic);

        this.button_record = (ImageView) this.findViewById(R.id.button_record);

        this.messages_panel = (LinearLayout) this.findViewById(R.id.messages_panel);
        this.type_message = (EditText) this.findViewById(R.id.type_message);
        this.new_message_state = (ImageView) this.findViewById(R.id.new_message_state);

        this.updateUsersCount();

        EventBus.getInstance().register(this);

        ButterKnife.bind(this);

        this.getSupportFragmentManager().addOnBackStackChangedListener(this);

        this.buildUserDialog();
        this.buildConfirmDialog();
        this.buildUsersDemandsDialog();
        this.buildCancelDialog();

        this.requestPermissions();
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            // Show header
            this.findViewById(R.id.header).setVisibility(View.VISIBLE);
            // Show footer
            this.findViewById(R.id.footer).setVisibility(View.VISIBLE);
        }
    }

    /**************** EASY PERMISSIONS ****************/

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            MessagesManager.getInstance().registerMessageManager();
            TokBoxClient.getInstance().connectSession();
            SocketIoClient.getInstance().registerIoSocket();
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, VideoGridFragment.newInstance())
                    .commit();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    /**************** END EASY PERMISSIONS ****************/

    @OnClick(R2.id.button_exit) void onButtonExitClicked() {
        // Register "hangout.leave" event with API
        APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_LEAVE);

        // TODO - Remove Firebase "disconnect" rule.

        // TODO - Remove user from Firebase hangout connected list

        TokBoxClient.getInstance().disconnectSession();
    }

    @OnClick(R2.id.button_record) void onButtonRecordClicked() {
        // Check Archive permission
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONARCHIVE) == true) {
            // Check recording state
            if(TokBoxClient.getInstance().isArchiving.get()) {
                // Recording ON
                // Stop hangout recording by calling API
                APIClient.getInstance().stopArchiving();
                // Register "hangout.stoprecord" event with API
                APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_STOPRECORD);
                // Immediately update button state
                this.button_record.setImageResource(R.drawable.record_off);
            }
            else {
                // Recording OFF
                // Start hangout recording by calling API
                APIClient.getInstance().startArchiving();
                // Register "hangout.startrecord" event with API
                APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_STARTRECORD);
                // Immediately update button state
                this.button_record.setImageResource(R.drawable.record_on);
            }
        }
    }

    @OnClick(R2.id.button_users) void onButtonUsersClicked() {
        this.showUsersActivity();
    }

    @OnClick(R2.id.button_messages) void onButtonMessagesClicked() {
        if(this.messages_panel.getVisibility() == View.GONE) {
            // Show the message panel
            this.messages_panel.setVisibility(View.VISIBLE);
            this.new_message_state.setVisibility(View.INVISIBLE);
            // Call API "conversation.read" (the whole conversation, message by message)
            APIClient.getInstance().sendConversationRead();
        }
        else {
            // Hide the message panel
            this.messages_panel.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v.getId() == R.id.type_message) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                this.sendMessage();
                return true;
            }
        }
        return false;
    }

    @OnClick(R2.id.button_send) void onButtonSendClicked() {
        this.sendMessage();
    }

    @OnClick(R2.id.publish_camera) void onButtonPublishCameraClicked() {
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            TokBoxClient.getInstance().publish(true, true);
            this.publish_camera.setVisibility(View.GONE);
            this.publish_mic.setVisibility(View.GONE);
        }
        else {
            // Update user local attribute
            UserManager.getInstance().setAskPermission(UserManager.USER_LOCAL_ASKCAMERA, true, UserManager.getInstance().getCurrentUserId());
            // Signal "hgt_camera_authorization" via tokbox
            TokBoxClient.getInstance().broadcastSignal(TokBoxClient.SIGNALTYPE_CAMERAAUTHORIZATION);
            // Register "hangout.ask_camera_auth" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_ASK_CAMERA_AUTH);
        }
    }

    @OnClick(R2.id.publish_mic) void onButtonPublishMicClicked() {
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), true);
            this.publish_mic.setVisibility(View.GONE);
        }
        else {
            // Update user local attribute
            UserManager.getInstance().setAskPermission(UserManager.USER_LOCAL_ASKMICROPHONE, true, UserManager.getInstance().getCurrentUserId());
            // Signal "hgt_microphone_authorization" via tokbox
            TokBoxClient.getInstance().broadcastSignal(TokBoxClient.SIGNALTYPE_MICROPHONEAUTHORIZATION);
            // Register "hangout.ask_microphone_auth" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_ASK_MICROPHONE_AUTH);
        }
    }

    @OnClick(R2.id.user_demand) void onUsersDemandsClicked() {
        this.demandDialog.updateData(this.usersDemands);
        this.demandDialog.show(getSupportFragmentManager(), "");
    }

    private void sendMessage() {
        if(!TextUtils.isEmpty(this.type_message.getText())) {
            APIClient.getInstance().sendMessage(this.type_message.getText().toString());
            this.type_message.setText("");
        }
    }

    private void updateUserDialog() {
        if(this.userDialog != null) {
            TextView user_action_mic_text = (TextView) this.userDialog.findViewById(R.id.user_action_mic_text);
            if(user_action_mic_text != null && UserManager.getInstance().isCurrentUserSharingAudio()) {
                user_action_mic_text.setText(R.string.user_action_mic_turn_off);
            }

            TextView user_action_camera_text = (TextView) this.userDialog.findViewById(R.id.user_action_camera_text);
            if(user_action_camera_text != null && UserManager.getInstance().isCurrentUserSharingCamera()) {
                user_action_camera_text.setText(R.string.user_action_cam_turn_off);
            }
        }
    }

    private void buildUserDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        View userDialogView = factory.inflate(R.layout.popup_user, null);
        this.userDialog = new AlertDialog.Builder(this).create();
        this.userDialog.setView(userDialogView);

        userDialogView.findViewById(R.id.user_action_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) v.findViewById(R.id.user_action_mic_text);
                if(text.getText().toString().equals(getResources().getString(R.string.user_action_mic_turn_on))) {
                    TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), true);
                    text.setText(R.string.user_action_mic_turn_off);
                }
                else {
                    TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), false);
                    text.setText(R.string.user_action_mic_turn_on);
                }
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) v.findViewById(R.id.user_action_camera_text);
                if(text.getText().toString().equals(getResources().getString(R.string.user_action_cam_turn_on))) {
                    TokBoxClient.getInstance().publish(true, UserManager.getInstance().isCurrentUserSharingAudio());
                    text.setText(R.string.user_action_cam_turn_off);
                }
                else {
                    TokBoxClient.getInstance().publish(false, UserManager.getInstance().isCurrentUserSharingAudio());
                    text.setText(R.string.user_action_cam_turn_on);
                }
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                TextView text = (TextView) v.findViewById(R.id.user_action_rotate_text);
                Publisher publisher = TokBoxClient.getInstance().getPublisher();
                if(publisher != null) {
                    if(text.getText().toString().equals(getResources().getString(R.string.user_action_rotate_front_cam))) {
                        // TODO Rotate camera ?
                        TokBoxClient.getInstance().getPublisher().swapCamera();
                    }
                    else {
                        // TODO Rotate camera ?

                    }
                }
                */
                Publisher publisher = TokBoxClient.getInstance().getPublisher();
                if(publisher != null) {
                    publisher.cycleCamera();
                }
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) v.findViewById(R.id.user_action_stop_text);
                if(text.getText().toString().equals(getResources().getString(R.string.user_action_stop_your_stream))) {
                    TokBoxClient.getInstance().unpublish();
                }
                else {

                }
                userDialog.dismiss();
            }
        });
    }

    /**
     * 1 = camera, 2 = mic
     * @param type
     */
    private void updateConfirmDialog(int type) {
        if(type == 1) {
            // Publish YOUR camera via tokbox
            ((ImageView) this.confirmDialog.findViewById(R.id.demand_icon)).setImageResource(R.drawable.user_action_camera);
            ((TextView) this.confirmDialog.findViewById(R.id.demand_text)).setText(R.string.confirm_text_camera);
            this.confirmDialog.findViewById(R.id.demand_popup).setTag(type);
        }
        else if(type == 2) {
            // Publish YOUR microphone via tokbox
            ((ImageView) this.confirmDialog.findViewById(R.id.demand_icon)).setImageResource(R.drawable.user_action_mic);
            ((TextView) this.confirmDialog.findViewById(R.id.demand_text)).setText(R.string.confirm_text_microphone);
            this.confirmDialog.findViewById(R.id.demand_popup).setTag(type);
        }
    }

    private void buildConfirmDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        View confirmDialogView = factory.inflate(R.layout.popup_demand, null);
        this.confirmDialog = new AlertDialog.Builder(this).create();
        this.confirmDialog.setView(confirmDialogView);

        confirmDialogView.findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((int) confirmDialog.findViewById(R.id.demand_popup).getTag() == 1) {
                    // Publish YOUR camera via tokbox
                    TokBoxClient.getInstance().publish(true, UserManager.getInstance().isCurrentUserSharingAudio());
                }
                else if((int) confirmDialog.findViewById(R.id.demand_popup).getTag() == 2) {
                    // Publish YOUR microphone via tokbox
                    TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), true);
                }
                confirmDialog.dismiss();
            }
        });
        confirmDialogView.findViewById(R.id.button_deny).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
    }

    private void updateUsersDemands() {
        if(this.usersDemands.size() == 0) {
            this.findViewById(R.id.user_demand).setVisibility(View.INVISIBLE);
        }
        else if(this.usersDemands.size() == 1) {
            this.findViewById(R.id.user_demand).setVisibility(View.VISIBLE);

            ImageView demand_type = (ImageView) this.findViewById(R.id.demand_type);
            if(this.usersDemands.get(0).split("_#_")[0].contains("camera")) {
                demand_type.setImageResource(R.drawable.demand_camera);
            }
            else {
                demand_type.setImageResource(R.drawable.demand_mic);
            }
            demand_type.setVisibility(View.VISIBLE);

            this.findViewById(R.id.demand_count).setVisibility(View.INVISIBLE);

            JSONObject user = UserManager.getInstance().getSettingsForKey(this.usersDemands.get(0).split("_#_")[1]);
            ImageView demand_avatar = (ImageView) this.findViewById(R.id.demand_avatar);
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
                    Glide.with(this).load(url).fitCenter().into(demand_avatar);
                }
            }
        }
        else if(this.usersDemands.size() > 1) {
            this.findViewById(R.id.user_demand).setVisibility(View.VISIBLE);
            this.findViewById(R.id.demand_type).setVisibility(View.INVISIBLE);
            TextView demand_count = (TextView) this.findViewById(R.id.demand_count);
            demand_count.setVisibility(View.VISIBLE);
            demand_count.setText(String.valueOf(this.usersDemands.size()));

            ImageView demand_avatar = (ImageView) this.findViewById(R.id.demand_avatar);
            Glide.with(this).load("").fitCenter().placeholder(new ColorDrawable(ContextCompat.getColor(this, R.color.background_white))).into(demand_avatar);
        }
    }

    private void buildUsersDemandsDialog() {

        this.demandDialog = new UsersDemandsDialogFragment();


        /*
        LayoutInflater factory = LayoutInflater.from(this);

        View demandDialogView = factory.inflate(R.layout.popup_demand_container, null);

        this.demandDialog = new AlertDialog.Builder(this).create();
        this.demandDialog.setView(demandDialogView);

        this.demandDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

            }
        });
        */



        /*
        LinearLayout container = (LinearLayout) demandDialogView.findViewById(R.id.scrollview_container);

        container.addView(factory.inflate(R.layout.popup_demand, null));
        container.addView(factory.inflate(R.layout.popup_demand, null));
        container.addView(factory.inflate(R.layout.popup_demand, null));

        this.demandDialog = new AlertDialog.Builder(this).create();
        this.demandDialog.setView(demandDialogView);

        demandDialogView.findViewById(R.id.button_deny).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demandDialog.dismiss();
            }
        });

        demandDialogView.findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demandDialog.dismiss();
            }
        });
        */
    }

    private void buildCancelDialog() {
        this.cancelDialog = new AlertDialog.Builder(this).create();
        this.cancelDialog.setMessage(getResources().getString(R.string.cancel_text));
        this.cancelDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelDialog.dismiss();
            }
        });
    }

    private void showUsersActivity() {
        // Hide header
        this.findViewById(R.id.header).setVisibility(View.GONE);
        // Hide footer
        this.findViewById(R.id.footer).setVisibility(View.GONE);
        // Show users fragment
        this.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_up, R.anim.slide_out_down)
                .replace(R.id.container, UsersFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    private void updateUsersCount() {
        ((TextView) this.findViewById(R.id.users_count)).setText(getResources().getString(R.string.users_count_text, String.valueOf(UserManager.getInstance().getTotalConnectedUsersCount()), String.valueOf(UserManager.getInstance().getTotalUsersCount())));
    }

    private void checkPublishPermission() {
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            if(UserManager.getInstance().isCurrentUserSharingCamera()) {
                this.publish_camera.setVisibility(View.GONE);
                this.publish_mic.setVisibility(View.GONE);
            }
            else if(UserManager.getInstance().isCurrentUserSharingAudio()) {
                this.publish_camera.setVisibility(View.VISIBLE);
                this.publish_mic.setVisibility(View.GONE);
            }
            else {
                this.publish_camera.setVisibility(View.VISIBLE);
                this.publish_mic.setVisibility(View.VISIBLE);
            }
        }
        else {
            this.publish_camera.setImageResource(R.drawable.ask_publish_camera);
            this.publish_camera.setVisibility(View.VISIBLE);
            this.publish_mic.setImageResource(R.drawable.ask_publish_mic);
            this.publish_mic.setVisibility(View.VISIBLE);
        }
    }

    private void checkArchivePermission() {
        // Check Archive permission
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONARCHIVE) == true) {
            // TRUE
            // Display "Record" state
            this.updateRecordButton();
        }
        else {
            // FALSE
            // Display "Record" state
            this.updateRecordButton();
        }
    }

    private void updateRecordButton() {
        if(TokBoxClient.getInstance().isArchiving.get()) {
            // Archiving
            this.button_record.setImageResource(R.drawable.record_on);
        }
        else {
            this.button_record.setImageResource(R.drawable.record_off);
        }
    }

    @Subscribe
    public void onFragmentInteraction(FragmentInteraction.OnFragmentInteractionEvent event) {
        if(event.getType() == FragmentInteraction.Type.ON_BACK) {
            // Back
            this.getSupportFragmentManager().popBackStack();
        }
        else if(event.getType() == FragmentInteraction.Type.ON_SHOW_USER_DIALOG) {
            this.userDialog.show();
        }
        else if(event.getType() == FragmentInteraction.Type.ON_SHOW_VIDEO_DETAILS_FRAGMENT) {
            this.getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                    .replace(R.id.container, VideoDetailFragment.newInstance(((GenericModel) event.getData().get(0)).getContentValue("stream_id")))
                    .addToBackStack(null)
                    .commit();
        }
        else if(event.getType() == FragmentInteraction.Type.ON_USER_DEMAND_REMOVED) {
            String demand = (String) event.getData().get(0);
            if(this.usersDemands.contains(demand)) {
                this.usersDemands.remove(demand);
                // Update the top right icon
                this.updateUsersDemands();
                // Update the demandDialog data
                this.demandDialog.updateData(this.usersDemands);
                // Refresh the demandDialog
                this.demandDialog.refreshData();
                // Close if empty !
                if(this.usersDemands.size() == 0) {
                    this.demandDialog.dismiss();
                }
            }
        }
    }

    @Subscribe
    public void OnTokBoxInteraction(TokBoxInteraction.OnTokBoxInteractionEvent event) {
        if(event.getType() == TokBoxInteraction.Type.ON_SESSION_CONNECTED) {
            Log.d(TAG, "ON_SESSION_CONNECTED");

            this.findViewById(R.id.button_exit).setVisibility(View.VISIBLE);

            if(!HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHCAMERA) || !HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHMICROPHONE)) {
                this.checkPublishPermission();
            }
            // else wait for the (publisher) stream created event

            // Check Archive permission
            this.checkArchivePermission();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SESSION_DISCONNECTED) {
            Log.d(TAG, "ON_SESSION_DISCONNECTED");

            this.finish();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_ADDED || event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_REMOVED) { // the (publisher) stream created/destroyed events
            this.checkPublishPermission();
            this.updateUserDialog();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_ARCHIVE_STARTED) {
            Log.d(TAG, "ON_ARCHIVE_STARTED");

            // Set "Record" icon state to "Recording"
            this.updateRecordButton();

            // TODO Add "Recording started" notification message in conversation panel
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_ARCHIVE_STOPPED) {
            Log.d(TAG, "ON_ARCHIVE_STOPPED");

            // Set "Record" icon state to "Not recording"
            this.updateRecordButton();

            // TODO Add "Recording stopped" notification message in conversation panel
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SIGNAL_RECEIVED) {
            Log.d(TAG, "ON_SIGNAL_RECEIVED");

            if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_CAMERAAUTHORIZATION)) {
                // check if YOU have "askDevice" permission
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKDEVICE)) {
                    // TRUE
                    String demand = TokBoxClient.SIGNALTYPE_CAMERAAUTHORIZATION + "_#_" + ((String) event.getData().get(1));
                    if(!this.usersDemands.contains(demand)) {
                        this.usersDemands.add(demand);
                        // Show on interface User camera sharing demand
                        this.updateUsersDemands();
                    }
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_CANCELCAMERAAUTHORIZATION)) {
                // check if YOU have "askDevice" permission
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKDEVICE)) {
                    // TRUE
                    String demand = TokBoxClient.SIGNALTYPE_CANCELCAMERAAUTHORIZATION + "_#_" + ((String) event.getData().get(1));
                    if(this.usersDemands.contains(demand)) {
                        this.usersDemands.remove(demand);
                        // Hide User camera sharing demand
                        this.updateUsersDemands();
                    }
                }
                // check if YOU were asking for permission
                if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKCAMERA, UserManager.getInstance().getCurrentUserId())) {
                    // TRUE
                    // Notify user that his request has been declined
                    this.cancelDialog.show();
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION)) {
                // check if YOU have "askDevice" permission
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKDEVICE)) {
                    // TRUE
                    String demand = TokBoxClient.SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION + "_#_" + ((String) event.getData().get(1));
                    if(this.usersDemands.contains(demand)) {
                        this.usersDemands.remove(demand);
                        // Hide User microphone sharing demand
                        this.updateUsersDemands();
                    }
                }
                // check if YOU were asking for permission
                if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKMICROPHONE, UserManager.getInstance().getCurrentUserId())) {
                    // TRUE
                    // Notify user that his request has been declined
                    this.cancelDialog.show();
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_CANCELSCREENAUTHORIZATION)) {
                // check if YOU have "askScreen" permission
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKSCREEN)) {
                    // TRUE
                    String demand = TokBoxClient.SIGNALTYPE_CANCELSCREENAUTHORIZATION + "_#_" + ((String) event.getData().get(1));
                    if(this.usersDemands.contains(demand)) {
                        this.usersDemands.remove(demand);
                        // Hide User screen sharing demand
                        this.updateUsersDemands();
                    }
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_MICROPHONEAUTHORIZATION)) {
                // check if YOU have "askDevice" permission
                if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONASKDEVICE)) {
                    // TRUE
                    String demand = TokBoxClient.SIGNALTYPE_MICROPHONEAUTHORIZATION + "_#_" + ((String) event.getData().get(1));
                    if(!this.usersDemands.contains(demand)) {
                        this.usersDemands.add(demand);
                        // Show on interface User microphone sharing demand
                        this.updateUsersDemands();
                    }
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_CAMERAREQUESTED)) {
                // check if YOU were asking for permission
                if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKCAMERA, UserManager.getInstance().getCurrentUserId())) {
                    // TRUE
                    UserManager.getInstance().setAskPermission(UserManager.USER_LOCAL_ASKCAMERA, false, UserManager.getInstance().getCurrentUserId());
                    // Publish User camera via tokbox
                    TokBoxClient.getInstance().publish(true, UserManager.getInstance().isCurrentUserSharingAudio());
                    // Signal "hgt_cancel_camera_authorization" event via tokbox for user ID
                    TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CANCELCAMERAAUTHORIZATION, UserManager.getInstance().getCurrentUserId());
                }
                else {
                    // FALSE
                    // Show popup to Accept / Decline sharing camera demand
                    this.confirmDialog.show();
                    this.updateConfirmDialog(1);
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_MICROPHONEREQUESTED)) {
                // check if YOU were asking for permission
                if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKMICROPHONE, UserManager.getInstance().getCurrentUserId())) {
                    // TRUE
                    UserManager.getInstance().setAskPermission(UserManager.USER_LOCAL_ASKMICROPHONE, false, UserManager.getInstance().getCurrentUserId());
                    // Publish User microphone via tokbox
                    TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), true);
                    // Signal "hgt_cancel_microphone_authorization" event via tokbox for user ID
                    TokBoxClient.getInstance().sendSignal(TokBoxClient.SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION, UserManager.getInstance().getCurrentUserId());
                }
                else {
                    // FALSE
                    // Show popup to Accept / Decline sharing microphone demand
                    this.confirmDialog.show();
                    this.updateConfirmDialog(2);
                }
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_FORCEMUTESTREAM)) {
                // Mute stream corresponding to event streamId
                TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), false);
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_FORCEUNMUTESTREAM)) {
                // Unmute stream corresponding to event streamId
                TokBoxClient.getInstance().publish(UserManager.getInstance().isCurrentUserSharingCamera(), true);
            }
            else if(((String) event.getData().get(0)).equals(TokBoxClient.SIGNALTYPE_FORCEUNPUBLISHSTREAM)) {
                TokBoxClient.getInstance().publish(false, UserManager.getInstance().isCurrentUserSharingAudio());
            }
        }
    }

    @Subscribe
    public void OnAPIInteraction(APIInteraction.OnAPIInteractionEvent event) {
        if(event.getType() == APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED) {
            Log.d(TAG, "ON_USER_CONNECTION_STATE_CHANGED");

            // Update Interface: - Increase user total count - Increase user connected count
            this.updateUsersCount();

            // TODO Add "User joined" notification message in conversation panel
        }
    }

    @Subscribe
    public void onMessageInteraction(MessageInteraction.OnMessageInteractionEvent event) {
        if(event.getType() == MessageInteraction.Type.ON_MESSAGES_LOADED || event.getType() == MessageInteraction.Type.ON_LATEST_MESSAGES_LOADED) {
            if(event.getData().size() > 0 && event.getData().get(0) instanceof Integer && ((Integer) event.getData().get(0)).intValue() > 0) {
                if(this.messages_panel.getVisibility() == View.GONE) {
                    // Show the new message status
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new_message_state.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        }
        // else disable back button
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        TWICAndroidPlugin.getInstance().onResume();

//        if (mSession == null) {
//            return;
//        }
//        mSession.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        TWICAndroidPlugin.getInstance().onPause();

//        if (mSession == null) {
//            return;
//        }
//        mSession.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TWICAndroidPlugin.getInstance().onDestroy();

        EventBus.getInstance().unregister(this);
    }

}
