package com.thestudnet.twicandroidplugin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoDetailFragment extends CustomFragment {

    private static final String TAG = "com.thestudnet.twicandroidplugin" + VideoDetailFragment.class.getSimpleName();

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    private String streamId;

    /**
     * Returns a new instance of this fragment
     */
    public static VideoDetailFragment newInstance(String streamId) {
        VideoDetailFragment fragment = new VideoDetailFragment();
        fragment.streamId = streamId;
        return fragment;
    }

    public VideoDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPublisherViewContainer = (RelativeLayout) view.findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) view.findViewById(R.id.subscriberview);

        // Remove parent views
        ViewGroup publisherParent = (ViewGroup) TokBoxClient.getInstance().getPublisher().getView().getParent();
        ViewGroup subscriberParent = (ViewGroup) TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView().getParent();
        if(publisherParent != null) {
            publisherParent.removeView(TokBoxClient.getInstance().getPublisher().getView());
        }
        if(subscriberParent != null) {
            subscriberParent.removeView(TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView());
        }

        // Add views
        RelativeLayout.LayoutParams layoutParamsPublisher = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.publisherview_width), (int) getResources().getDimension(R.dimen.publisherview_height));
        mPublisherViewContainer.addView(TokBoxClient.getInstance().getPublisher().getView(), layoutParamsPublisher);
        // Subscriber
        RelativeLayout.LayoutParams layoutParamsSubscriber = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels
        );
        mSubscriberViewContainer.addView(TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView(), layoutParamsSubscriber);
    }

//    @Override
//    public void onStreamDropped(Session session, Stream stream) {
//        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());
//
//        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            return;
//        }
//        if (mSubscriber == null) {
//            return;
//        }
//
//        if (mSubscriber.getStream().equals(stream)) {
//            mSubscriberViewContainer.removeView(mSubscriber.getView());
//            mSubscriber.destroy();
//            mSubscriber = null;
//        }
//    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        //TokBoxClient.getInstance().resumeSession();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        //TokBoxClient.getInstance().pauseSession();
    }
}
