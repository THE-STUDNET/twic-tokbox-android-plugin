package com.thestudnet.twicandroidplugin.fragments;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.opentok.android.Subscriber;
import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
import com.thestudnet.twicandroidplugin.models.GenericModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoDetailFragment extends CustomFragment implements View.OnClickListener {

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
            layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mCurrentPublisherView = TokBoxClient.getInstance().getPublisher().getView();
            mCurrentPublisherView.setOnClickListener(this);
//            mPublisherViewContainer.setVisibility(View.VISIBLE);
            mPublisherViewContainer.addView(mCurrentPublisherView, layoutParamsPublisher);
        }
        if(TokBoxClient.getInstance().getSubscribers() != null && TokBoxClient.getInstance().getSubscribers().size() > 0) {
            boolean foundId = false;
            Iterator<LinkedHashMap<String, Subscriber>> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Subscriber> users = iterator.next();
                Iterator<Subscriber> subscribers = users.values().iterator();
                while (subscribers.hasNext()) {
                    Subscriber subscriber = subscribers.next();
                    if(subscriber.getStream().getStreamId().equals(this.streamId)) {
                        // Remove parent views
                        ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
                        if(subscriberParent != null) {
                            subscriberParent.removeView(subscriber.getView());
                        }
                        // Add views
                        RelativeLayout.LayoutParams layoutParamsSubscriber = new RelativeLayout.LayoutParams(
                                getResources().getDisplayMetrics().widthPixels,
                                getResources().getDisplayMetrics().heightPixels
                        );
                        mSubscriberViewContainer.addView(subscriber.getView(), layoutParamsSubscriber);
                        foundId = true;
                        break;
                    }
                    if(foundId) {
                        break;
                    }
                }
            }
        }

        this.updateUserDemandsPosition();
    }

    @Subscribe
    public void OnTokBoxInteraction(TokBoxInteraction.OnTokBoxInteractionEvent event) {
        if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_ADDED) {
            Log.d(TAG, "ON_SUBSCRIBER_ADDED");

        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_ADDED) {
            Log.d(TAG, "ON_PUBLISHER_ADDED");
//            if(TokBoxClient.getInstance().getPublisher() != null) {
//                // Remove parent views
//                ViewGroup publisherParent = (ViewGroup) TokBoxClient.getInstance().getPublisher().getView().getParent();
//                if(publisherParent != null) {
//                    publisherParent.removeView(TokBoxClient.getInstance().getPublisher().getView());
//                }
//                // Add view
//                RelativeLayout.LayoutParams layoutParamsPublisher = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.publisherview_width), (int) getResources().getDimension(R.dimen.publisherview_height));
//                layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                layoutParamsPublisher.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                mCurrentPublisherView = TokBoxClient.getInstance().getPublisher().getView();
//                mPublisherViewContainer.addView(mCurrentPublisherView, layoutParamsPublisher);
//            }
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED) {
            Log.d(TAG, "ON_SUBSCRIBER_REMOVED");

        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_REMOVED) {
            Log.d(TAG, "ON_PUBLISHER_REMOVED");

//            if(TokBoxClient.getInstance().getPublisher() != null) {
//                // Remove parent views
//                ViewGroup publisherParent = (ViewGroup) TokBoxClient.getInstance().getPublisher().getView().getParent();
//                if(publisherParent != null) {
//                    publisherParent.removeView(TokBoxClient.getInstance().getPublisher().getView());
//                }
//            }

            if(mCurrentPublisherView != null) {
                mCurrentPublisherView.setOnClickListener(null);
                mPublisherViewContainer.removeAllViews();
//                mPublisherViewContainer.setVisibility(View.INVISIBLE);
//                ViewGroup publisherParent = (ViewGroup) mCurrentPublisherView.getParent();
//                if(publisherParent != null) {
//                    publisherParent.removeView(mCurrentPublisherView);
//                }
            }
        }
    }

    private void updateUserDemandsPosition() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.publisherview_width), 0, 0);
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        this.getActivity().findViewById(R.id.user_demand).setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_USER_DIALOG, null);
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
