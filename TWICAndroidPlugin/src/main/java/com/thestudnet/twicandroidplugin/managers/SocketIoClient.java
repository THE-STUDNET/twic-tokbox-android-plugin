package com.thestudnet.twicandroidplugin.managers;

import android.util.Log;

import com.thestudnet.twicandroidplugin.TWICAndroidPlugin;
import com.thestudnet.twicandroidplugin.config.IoSocketConfig;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.utils.DeviceUuidFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class SocketIoClient {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + SocketIoClient.class.getSimpleName();

    private Socket ioSocket;
    public Socket getIoSocket() {
        return ioSocket;
    }
    public void setIoSocket(Socket ioSocket) {
        this.ioSocket = ioSocket;
    }

    private static SocketIoClient instance;
    public SocketIoClient() {
    }
    public static SocketIoClient getInstance() {
        if(instance == null) {
            SocketIoClient minstance = new SocketIoClient();
            EventBus.getInstance().register(minstance);
            instance = minstance;
            try {
                instance.ioSocket = IO.socket(IoSocketConfig.SERVER_URL);
            }
            catch (URISyntaxException error) {

            }
            return instance;
        } else {
            return instance;
        }
    }

    public void registerIoSocket() {
        if(this.ioSocket != null) {
            this.ioSocket.on(Socket.EVENT_CONNECT, onConnect);
            this.ioSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            this.ioSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            this.ioSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            this.ioSocket.on("authenticated", onAuthenticated);
            this.ioSocket.on("ch.message", onMessage);
            this.ioSocket.connect();
        }
    }

    public void unregisterIoSocket() {
        if(this.ioSocket != null) {
            this.ioSocket.disconnect();
            this.ioSocket.off(Socket.EVENT_CONNECT, onConnect);
            this.ioSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            this.ioSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            this.ioSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            this.ioSocket.off("authenticated", onAuthenticated);
            this.ioSocket.off("ch.message", onMessage);
        }
        EventBus.getInstance().unregister(this);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "IOSOCKET : CONNECTED");

            try {
                JSONObject params = new JSONObject();
                params.put("id", 1);
                params.put("authentification", IoSocketConfig.AUTH_TOKEN);
                params.put("connection_token", new DeviceUuidFactory(TWICAndroidPlugin.getInstance().getContext()).getDeviceUuid().toString());
                getIoSocket().emit("authentify", params);
            }
            catch (JSONException error) {
                Log.d(TAG, "IOSOCKET CONNECT : JSONException");
            }

            /*
            SocketIoInteraction.getInstance().FireEvent(SocketIoInteraction.Type.ON_BACK_PRESSED, null);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : CONNECTED");
                    try {
                        JSONObject params = new JSONObject();
                        params.addOrReplace("id", 1);
                        params.addOrReplace("authentification", IoSocketConfig.AUTH_TOKEN);
                        params.addOrReplace("connection_token", new DeviceUuidFactory(getActivity()).getDeviceUuid().toString());
                        TWICAndroidPlugin.getInstance().getIoSocket().emit("authentify", params);
                    }
                    catch (JSONException error) {
                        Log.d(TAG, "IOSOCKET CONNECT : JSONException");
                    }
                }
            });
            */
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "IOSOCKET : DISCONNECTED");
            /*
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : DISCONNECTED");
                }
            });
            */
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "IOSOCKET : CONNECT ERROR");
            /*
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : CONNECT ERROR");
                }
            });
            */
        }
    };

    private Emitter.Listener onAuthenticated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "IOSOCKET : AUTHENTICATED");
        /*
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : AUTHENTICATED");
                }
            });
            */
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "IOSOCKET : MESSAGE " + args.toString());

            if(args.length > 0 && args[0] instanceof JSONObject) {
                try {
                    JSONObject message = (JSONObject) args[0];
                    if(message.optString("conversation_id", "").equals(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY))) {
                        // TODO : check the message is not of type 2 (private message)
                        if(MessagesManager.getInstance().getMessages().size() > 0) {
                            APIClient.getInstance().getMessagesFromMessageId(MessagesManager.getInstance().getMessages().get(MessagesManager.getInstance().getMessages().size() - 1).getContentValue("id"));
//                            APIClient.getInstance().getMessagesFromMessageId(MessagesManager.getInstance().getMessages().get(0).getContentValue("id"));
                        }
                        else {
                            APIClient.getInstance().getMessages();
                        }

                    }
                    // else : Message does not belong to this hangout : Do nothing
                } catch (Exception e) {
                    Log.e(TAG, "IOSOCKET : MESSAGE : EXCEPTION : " + e.getLocalizedMessage());
                }
            }

        /*
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IOSOCKET : MESSAGE " + args.toString());
                }
            });
            */
        }
    };

}
