package com.thestudnet.twicandroidplugin.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.R2;
import com.thestudnet.twicandroidplugin.TWICAndroidPlugin;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.events.SocketIoInteraction;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.fragments.UsersFragment;
import com.thestudnet.twicandroidplugin.fragments.VideoDetailFragment;
import com.thestudnet.twicandroidplugin.fragments.VideoGridFragment;
import com.thestudnet.twicandroidplugin.managers.APIClient;
import com.thestudnet.twicandroidplugin.managers.HangoutManager;
import com.thestudnet.twicandroidplugin.managers.SocketIoClient;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
import com.thestudnet.twicandroidplugin.managers.UserManager;
import com.thestudnet.twicandroidplugin.models.GenericModel;

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

public class TWICAndroidPluginActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "com.thestudnet.twicandroidplugin " + TWICAndroidPluginActivity.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private AlertDialog userDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_twic_android_plugin);

        this.updateUsersCount();

        EventBus.getInstance().register(this);

        ButterKnife.bind(this);

        this.getSupportFragmentManager().addOnBackStackChangedListener(this);

        this.buildUserDialog();

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
        APIClient.getInstance().sendUserLeave();

        // TODO - Remove Firebase "disconnect" rule.

        // TODO - Remove user from Firebase hangout connected list

        TokBoxClient.getInstance().disconnectSession();
    }

    @OnClick(R2.id.button_users) void onButtonUsersClicked() {
        this.showUsersActivity();
    }

    @OnClick(R2.id.publish_camera) void onButtonPublishCameraClicked() {
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            TokBoxClient.getInstance().publish(true, false);
        }
        else {
            // TODO : Signal "hgt_camera_authorization" via tokbox
        }
    }

    @OnClick(R2.id.publish_mic) void onButtonPublishMicClicked() {
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            TokBoxClient.getInstance().publish(false, true);
        }
        else {
            // TODO : Signal "hgt_microphone_authorization" via tokbox
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
                //your business logic
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });
        userDialogView.findViewById(R.id.user_action_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
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
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up)
                .replace(R.id.container, UsersFragment.newInstance())
                .addToBackStack(null)
                .commit();
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
    }

    @Subscribe
    public void onSocketIoInteraction(SocketIoInteraction.OnSocketIoInteractionEvent event) {
        if(event.getType() == SocketIoInteraction.Type.ON_BACK_PRESSED) {
            // Back
            this.getSupportFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void OnTokBoxInteraction(TokBoxInteraction.OnTokBoxInteractionEvent event) {
        if(event.getType() == TokBoxInteraction.Type.ON_SESSION_CONNECTED) {
            Log.d(TAG, "ON_SESSION_CONNECTED");

            this.findViewById(R.id.button_exit).setVisibility(View.VISIBLE);

            this.checkPublishPermission();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SESSION_DISCONNECTED) {
            Log.d(TAG, "ON_SESSION_DISCONNECTED");

            this.finish();
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

    private void updateUsersCount() {
        ((TextView) this.findViewById(R.id.users_count)).setText(getResources().getString(R.string.users_count_text, String.valueOf(UserManager.getInstance().getTotalConnectedUsersCount()), String.valueOf(UserManager.getInstance().getTotalUsersCount())));
    }

    private void checkPublishPermission() {
        // Show the buttons
        this.findViewById(R.id.publish_camera).setVisibility(View.VISIBLE);
        this.findViewById(R.id.publish_mic).setVisibility(View.VISIBLE);
        // Check permission
        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONPUBLISH) == true) {
            if(UserManager.getInstance().isSharingCamera(UserManager.getInstance().getCurrentUserId())) {
                this.findViewById(R.id.publish_camera).setVisibility(View.GONE);
            }
            if(UserManager.getInstance().isSharingAudio(UserManager.getInstance().getCurrentUserId())) {
                this.findViewById(R.id.publish_mic).setVisibility(View.GONE);
            }
        }
        else {
            ImageView publish_camera = (ImageView) this.findViewById(R.id.publish_camera);
            publish_camera.setImageResource(R.drawable.ask_publish_camera);
            ImageView publish_mic = (ImageView) this.findViewById(R.id.publish_mic);
            publish_mic.setImageResource(R.drawable.ask_publish_mic);
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
