package com.thestudnet.twicandroidplugin.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.R2;
import com.thestudnet.twicandroidplugin.TWICAndroidPlugin;
import com.thestudnet.twicandroidplugin.config.IoSocketConfig;
import com.thestudnet.twicandroidplugin.config.OpenTokConfig;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.utils.DeviceUuidFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoGridFragment extends CustomFragment implements EasyPermissions.PermissionCallbacks, Session.SessionListener, Publisher.PublisherListener, Subscriber.VideoListener, Emitter.Listener {

    private static final String TAG = "com.thestudnet.twicandroidplugin " + VideoGridFragment.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;
    private AlertDialog userDialog;

    /**
     * Returns a new instance of this fragment
     */
    public static VideoGridFragment newInstance() {
        VideoGridFragment fragment = new VideoGridFragment();
        return fragment;
    }

    public VideoGridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_grid, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPublisherViewContainer = (RelativeLayout) view.findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) view.findViewById(R.id.subscriberview);

        this.registerIoSocket();

        this.requestPermissions();

        this.buildUserDialog();
    }

    private void buildUserDialog() {
        LayoutInflater factory = LayoutInflater.from(this.getContext());
        View userDialogView = factory.inflate(R.layout.popup_user, null);
        this.userDialog = new AlertDialog.Builder(this.getContext()).create();
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

    @OnClick(R2.id.publisherview) void onPublisherviewClicked() {
        this.userDialog.show();
        Window window = this.userDialog.getWindow();
        window.setLayout(this.getContext().getResources().getDimensionPixelSize(R.dimen.popup_all), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R2.id.subscriberview) void onSubscriberviewClicked() {
        this.getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
                .replace(R.id.container, VideoDetailFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }
    
    private void registerIoSocket() {
        TWICAndroidPlugin.getInstance().getIoSocket().on(Socket.EVENT_CONNECT, onConnect);
        TWICAndroidPlugin.getInstance().getIoSocket().on(Socket.EVENT_DISCONNECT, onDisconnect);
        TWICAndroidPlugin.getInstance().getIoSocket().on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        TWICAndroidPlugin.getInstance().getIoSocket().on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        TWICAndroidPlugin.getInstance().getIoSocket().on("authenticated", onAuthenticated);
        TWICAndroidPlugin.getInstance().getIoSocket().on("ch.message", onMessage);
        TWICAndroidPlugin.getInstance().getIoSocket().connect();
    }
    
    private void unregisterIoSocket() {
        TWICAndroidPlugin.getInstance().getIoSocket().disconnect();
        TWICAndroidPlugin.getInstance().getIoSocket().off(Socket.EVENT_CONNECT, onConnect);
        TWICAndroidPlugin.getInstance().getIoSocket().off(Socket.EVENT_DISCONNECT, onDisconnect);
        TWICAndroidPlugin.getInstance().getIoSocket().off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        TWICAndroidPlugin.getInstance().getIoSocket().off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        TWICAndroidPlugin.getInstance().getIoSocket().off("authenticated", onAuthenticated);
        TWICAndroidPlugin.getInstance().getIoSocket().off("ch.message", onMessage);
    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(this.getContext(), stream);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }

        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSession.unsubscribe(mSubscriber);
            mSubscriber.destroy();
            mSubscriber = null;
        }

        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }

    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());

        mPublisher = new Publisher(this.getContext(), "publisher");

        mPublisher.setPublisherListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

        mPublisherViewContainer.addView(mPublisher.getView());

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        mSession = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());

        Toast.makeText(this.getContext(), "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        this.getActivity().finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());

        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
            return;
        }
        if (mSubscriber != null) {
            return;
        }

        subscribeToStream(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
            return;
        }
        if (mSubscriber == null) {
            return;
        }

        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber.destroy();
            mSubscriber = null;
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");

        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
            return;
        }

        subscribeToStream(stream);
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");

        Toast.makeText(this.getContext(), "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        this.getActivity().finish();
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        if(mSubscriber != null) {
            mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this.getContext(), perms)) {
            mSession = new Session(this.getContext(), OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(OpenTokConfig.TOKEN_2);
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

    @Override
    public void call(Object... args) {

    }

    /************* IO SOCKET PART **************/

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : CONNECTED");
                    try {
                        JSONObject params = new JSONObject();
                        params.put("id", 1);
                        params.put("authentification", IoSocketConfig.AUTH_TOKEN);
                        params.put("connection_token", new DeviceUuidFactory(getActivity()).getDeviceUuid().toString());
                        TWICAndroidPlugin.getInstance().getIoSocket().emit("authentify", params);
                    }
                    catch (JSONException error) {
                        Log.d(TAG, "IOSOCKET CONNECT : JSONException");
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : DISCONNECTED");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : CONNECT ERROR");
                }
            });
        }
    };

    private Emitter.Listener onAuthenticated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : AUTHENTICATED");
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : MESSAGE " + args.toString());
                }
            });
        }
    };

    /************* END IO SOCKET PART **************/

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        if (mSession == null) {
            return;
        }
        mSession.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        if (mSession == null) {
            return;
        }
        mSession.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        unregisterIoSocket();

        disconnectSession();

        super.onDestroy();
    }
}
