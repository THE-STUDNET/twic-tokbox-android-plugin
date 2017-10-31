package com.thestudnet.twicandroidplugin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.opentok.android.Subscriber;
import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;

import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoDetailFragment extends CustomFragment {

    private static final String TAG = "com.thestudnet.twicandroidplugin" + VideoDetailFragment.class.getSimpleName();

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    private View mCurrentPublisherView = null;

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

        if(TokBoxClient.getInstance().getPublisher() != null) {
            // Remove parent views
            ViewGroup publisherParent = (ViewGroup) TokBoxClient.getInstance().getPublisher().getView().getParent();
            if(publisherParent != null) {
                publisherParent.removeView(TokBoxClient.getInstance().getPublisher().getView());
            }
            // Add view
            RelativeLayout.LayoutParams layoutParamsPublisher = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.publisherview_width), (int) getResources().getDimension(R.dimen.publisherview_height));
            mCurrentPublisherView = TokBoxClient.getInstance().getPublisher().getView();
            mPublisherViewContainer.addView(mCurrentPublisherView, layoutParamsPublisher);
        }
        if(TokBoxClient.getInstance().getSubscribers() != null && TokBoxClient.getInstance().getSubscribers().size() > 0 && TokBoxClient.getInstance().getSubscribers().get(this.streamId) != null) {
            // Remove parent views
            ViewGroup subscriberParent = (ViewGroup) TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView().getParent();
            if(subscriberParent != null) {
                subscriberParent.removeView(TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView());
            }
            // Add views
            RelativeLayout.LayoutParams layoutParamsSubscriber = new RelativeLayout.LayoutParams(
                    getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels
            );
            mSubscriberViewContainer.addView(TokBoxClient.getInstance().getSubscribers().get(this.streamId).getView(), layoutParamsSubscriber);
        }
    }

    @Subscribe
    public void OnTokBoxInteraction(TokBoxInteraction.OnTokBoxInteractionEvent event) {
        if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_ADDED) {
            Log.d(TAG, "ON_SUBSCRIBER_ADDED");

        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_ADDED) {
            Log.d(TAG, "ON_PUBLISHER_ADDED");
            if(TokBoxClient.getInstance().getPublisher() != null) {
                // Remove parent views
                ViewGroup publisherParent = (ViewGroup) TokBoxClient.getInstance().getPublisher().getView().getParent();
                if(publisherParent != null) {
                    publisherParent.removeView(TokBoxClient.getInstance().getPublisher().getView());
                }
                // Add view
                RelativeLayout.LayoutParams layoutParamsPublisher = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.publisherview_width), (int) getResources().getDimension(R.dimen.publisherview_height));
                layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                mCurrentPublisherView = TokBoxClient.getInstance().getPublisher().getView();
                mPublisherViewContainer.addView(mCurrentPublisherView, layoutParamsPublisher);
            }
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED) {
            Log.d(TAG, "ON_SUBSCRIBER_REMOVED");

        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_REMOVED) {
            Log.d(TAG, "ON_PUBLISHER_REMOVED");
            if(mCurrentPublisherView != null) {
//                ViewGroup publisherParent = (ViewGroup) mCurrentPublisherView.getParent();
//                if(publisherParent != null) {
//                    publisherParent.removeView(mCurrentPublisherView);
//                }
            }
        }
    }


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
