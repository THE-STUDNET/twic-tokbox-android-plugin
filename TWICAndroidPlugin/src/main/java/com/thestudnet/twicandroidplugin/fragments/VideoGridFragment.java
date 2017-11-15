package com.thestudnet.twicandroidplugin.fragments;

import android.content.ContentValues;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;
import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.adapters.StreamsAdapter;
import com.thestudnet.twicandroidplugin.events.FragmentInteraction;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.libs.CustomFragment;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;
import com.thestudnet.twicandroidplugin.models.GenericModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoGridFragment extends CustomFragment implements View.OnClickListener {

    private static final String TAG = "com.thestudnet.twicandroidplugin." + VideoGridFragment.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private GridView gridview;

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    private AtomicInteger currentStreams;
    private StreamsAdapter streamsAdapter;

    private ConstraintLayout mContainer;
    private AtomicInteger subscribers;
    private Publisher mPublisher;

    /**
     * Returns a new instance of this fragment
     */
    public static VideoGridFragment newInstance() {
        VideoGridFragment fragment = new VideoGridFragment();
        return fragment;
    }

    public VideoGridFragment() {
        // Required empty public constructor
        this.currentStreams = new AtomicInteger();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_grid, container, false);

        this.subscribers = new AtomicInteger(0);

//        this.gridview = (GridView) rootView.findViewById(R.id.gridview);

//        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPublisherViewContainer = (RelativeLayout) view.findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) view.findViewById(R.id.subscriberview);

        mContainer = (ConstraintLayout) view.findViewById(R.id.main_container);

        if(TokBoxClient.getInstance().getPublisher() != null) {
            mPublisher = TokBoxClient.getInstance().getPublisher();
            mPublisher.getView().setId(R.id.publisher_view_id);
            // Remove parent view
            ViewGroup publisherParent = (ViewGroup) mPublisher.getView().getParent();
            if(publisherParent != null) {
                publisherParent.removeView(mPublisher.getView());
            }
            mContainer.addView(mPublisher.getView());
            mPublisher.getView().setOnClickListener(this);
        }

        Iterator<LinkedHashMap<String, Subscriber>> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
        while (iterator.hasNext()) {
            LinkedHashMap<String, Subscriber> users = iterator.next();
            Iterator<Subscriber> subscribers = users.values().iterator();
            while (subscribers.hasNext()) {
                Subscriber subscriber = subscribers.next();
                subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
                subscriber.getView().setTag(subscriber.getStream().getStreamId());
                ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
                if(subscriberParent != null) {
                    subscriberParent.removeView(subscriber.getView());
                }
                mContainer.addView(subscriber.getView());
                subscriber.getView().setOnClickListener(this);
            }
        }

//        Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
//        while (iterator.hasNext()) {
//            Subscriber subscriber = iterator.next();
//            subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
//            ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
//            if(subscriberParent != null) {
//                subscriberParent.removeView(subscriber.getView());
//            }
//            mContainer.addView(subscriber.getView());
//            subscriber.getView().setOnClickListener(this);
//        }

        calculateLayout();

        /*
        this.streamsAdapter = new StreamsAdapter(this.getContext());
        this.gridview.setAdapter(streamsAdapter);
        streamsAdapter.reload();
        */

        /*

        if(TokBoxClient.getInstance().getPublisher() != null && TokBoxClient.getInstance().getPublisher().getView() != null) {
            mPublisherViewContainer.addView(TokBoxClient.getInstance().getPublisher().getView());
        }

        if(TokBoxClient.getInstance().getSubscribers() != null && TokBoxClient.getInstance().getSubscribers().size() > 0) {
            Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
            while (iterator.hasNext()) {
                Subscriber subscriber = iterator.next();
                mSubscriberViewContainer.addView(subscriber.getView());
                break;
            }
        }
        */

        this.updateUserDemandsPosition();
    }

//    @OnClick(R2.id.publisherview) void onPublisherviewClicked() {
//        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_USER_DIALOG, null);
//        /*
//        this.userDialog.show();
//        Window window = this.userDialog.getWindow();
//        window.setLayout(this.getContext().getResources().getDimensionPixelSize(R.dimen.popup_all), WindowManager.LayoutParams.WRAP_CONTENT);
//        */
//    }
//
//    @OnClick(R2.id.subscriberview) void onSubscriberviewClicked() {
//        FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_VIDEO_DETAILS_FRAGMENT, null);
//        /*
//        this.getActivity().getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out)
//                .replace(R.id.container, VideoDetailFragmentOld.newInstance())
//                .addToBackStack(null)
//                .commit();
//                */
//    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.publisher_view_id) {
            FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_USER_DIALOG, null);
        }
        else {

            view.setOnClickListener(null);
            /*
            if (Arrays.asList(getResources().getIntArray(R.array.subscriber_view_ids)).contains(id)) {
                // found a match
                ArrayList<GenericModel> list = new ArrayList<>(1);
                ContentValues contentValues = new ContentValues();
                contentValues.addOrReplace("", "");
                list.add(new GenericModel(contentValues));
                FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_USER_DIALOG, list);
            }
            */

            if(TokBoxClient.getInstance().getPublisher() != null) {
                mPublisher = TokBoxClient.getInstance().getPublisher();
                mPublisher.getView().setOnClickListener(null);
            }

            ArrayList<GenericModel> list = new ArrayList<>(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("stream_id", (String) view.getTag());
            list.add(new GenericModel(contentValues));
            FragmentInteraction.getInstance().FireEvent(FragmentInteraction.Type.ON_SHOW_VIDEO_DETAILS_FRAGMENT, list);
        }
    }

    @Subscribe
    public void OnTokBoxInteraction(TokBoxInteraction.OnTokBoxInteractionEvent event) {
        if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_ADDED) {
            Log.d(TAG, "ON_SUBSCRIBER_ADDED");

//            streamsAdapter.reload();
            /*
            this.currentStreams.getAndAdd(1);

            if(TokBoxClient.getInstance().getSubscribers() != null && TokBoxClient.getInstance().getSubscribers().size() > 0) {
                Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
                while (iterator.hasNext()) {
                    Subscriber subscriber = iterator.next();
                    mSubscriberViewContainer.addView(subscriber.getView());
                    break;
                }
            }
            */

            int i = 0;
            Iterator<LinkedHashMap<String, Subscriber>> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Subscriber> users = iterator.next();
                Iterator<Subscriber> subscribers = users.values().iterator();
                while (subscribers.hasNext()) {
                    Subscriber subscriber = subscribers.next();
                    if(this.subscribers.get() == i) {
                        subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
                        subscriber.getView().setTag(subscriber.getStream().getStreamId());
                        // Remove parent view
                        ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
                        if(subscriberParent != null) {
                            subscriberParent.removeView(subscriber.getView());
                        }
                        mContainer.addView(subscriber.getView());
                        subscriber.getView().setOnClickListener(this);
                        break;
                    }
                    i++;
                }
            }

//            Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
//            int i = 0;
//            while (iterator.hasNext()) {
//                Subscriber subscriber = iterator.next();
//                if(this.subscribers.get() == i) {
//                    subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
//                    // Remove parent view
//                    ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
//                    if(subscriberParent != null) {
//                        subscriberParent.removeView(subscriber.getView());
//                    }
//                    mContainer.addView(subscriber.getView());
//                    subscriber.getView().setOnClickListener(this);
//                    break;
//                }
//                i++;
//            }

            calculateLayout();
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_ADDED) { // the (publisher) stream created event
            Log.d(TAG, "ON_PUBLISHER_ADDED");

//            streamsAdapter.reload();

            /*

            this.currentStreams.getAndAdd(1);

            mPublisherViewContainer.addView(TokBoxClient.getInstance().getPublisher().getView());
            */

            if(mPublisher == null) {
                mPublisher = TokBoxClient.getInstance().getPublisher();
                mPublisher.getView().setId(R.id.publisher_view_id);
                // Remove parent view
                ViewGroup publisherParent = (ViewGroup) mPublisher.getView().getParent();
                if(publisherParent != null) {
                    publisherParent.removeView(mPublisher.getView());
                }
                mContainer.addView(mPublisher.getView());
                mPublisher.getView().setOnClickListener(this);
                calculateLayout();
            }
        }
        else if(event.getType() == TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED) {
            mContainer.removeAllViews();
            this.subscribers.set(0);

            if(TokBoxClient.getInstance().getPublisher() != null) {
                mPublisher = TokBoxClient.getInstance().getPublisher();
                mPublisher.getView().setId(R.id.publisher_view_id);
                // Remove parent view
                ViewGroup publisherParent = (ViewGroup) mPublisher.getView().getParent();
                if(publisherParent != null) {
                    publisherParent.removeView(mPublisher.getView());
                }
                mContainer.addView(mPublisher.getView());
                mPublisher.getView().setOnClickListener(this);
            }

            Iterator<LinkedHashMap<String, Subscriber>> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Subscriber> users = iterator.next();
                Iterator<Subscriber> subscribers = users.values().iterator();
                while (subscribers.hasNext()) {
                    Subscriber subscriber = subscribers.next();
                    subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
                    ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
                    if(subscriberParent != null) {
                        subscriberParent.removeView(subscriber.getView());
                    }
                    mContainer.addView(subscriber.getView());
                    subscriber.getView().setOnClickListener(this);
                }
            }

//            Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
//            while (iterator.hasNext()) {
//                Subscriber subscriber = iterator.next();
//                subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
//                ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
//                if(subscriberParent != null) {
//                    subscriberParent.removeView(subscriber.getView());
//                }
//                mContainer.addView(subscriber.getView());
//                subscriber.getView().setOnClickListener(this);
//            }

            calculateLayout();

        }
        else if(event.getType() == TokBoxInteraction.Type.ON_PUBLISHER_REMOVED) {
            mContainer.removeAllViews();
            this.subscribers.set(0);

            if(mPublisher != null) {
                if(mPublisher.getView() != null) {
                    mPublisher.getView().setOnClickListener(null);
                }
                mPublisher = null;
            }

            Iterator<LinkedHashMap<String, Subscriber>> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Subscriber> users = iterator.next();
                Iterator<Subscriber> subscribers = users.values().iterator();
                while (subscribers.hasNext()) {
                    Subscriber subscriber = subscribers.next();
                    subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
                    ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
                    if(subscriberParent != null) {
                        subscriberParent.removeView(subscriber.getView());
                    }
                    mContainer.addView(subscriber.getView());
                    subscriber.getView().setOnClickListener(this);
                }
            }

//            Iterator<Subscriber> iterator = TokBoxClient.getInstance().getSubscribers().values().iterator();
//            while (iterator.hasNext()) {
//                Subscriber subscriber = iterator.next();
//                subscriber.getView().setId(getResIdForSubscriberIndex(this.subscribers.getAndAdd(1)));
//                ViewGroup subscriberParent = (ViewGroup) subscriber.getView().getParent();
//                if(subscriberParent != null) {
//                    subscriberParent.removeView(subscriber.getView());
//                }
//                mContainer.addView(subscriber.getView());
//                subscriber.getView().setOnClickListener(this);
//            }

            calculateLayout();
        }
    }

    private int getResIdForSubscriberIndex(int index) {
        TypedArray arr = getResources().obtainTypedArray(R.array.subscriber_view_ids);
        int subId = arr.getResourceId(index, 0);
        arr.recycle();
        return subId;
    }

    private void calculateLayout() {
        ConstraintSetHelper set = new ConstraintSetHelper(R.id.main_container);

        int size = this.subscribers.get();

        if(size > 10) {
            return;
        }

        if(TokBoxClient.getInstance().getPublisher() != null) { // TODO and user has right to publish
            if (size == 0) {
                // Publisher full screen
                set.layoutViewFullScreen(R.id.publisher_view_id);
            }
            else if (size == 1) {
                // Publisher
                // Subscriber
                set.layoutViewAboveView(R.id.publisher_view_id, getResIdForSubscriberIndex(0));
                set.layoutViewWithTopBound(R.id.publisher_view_id, R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewAllContainerWide(R.id.publisher_view_id, R.id.main_container);
                set.layoutViewAllContainerWide(getResIdForSubscriberIndex(0), R.id.main_container);

            }
            else if (size > 1 && size % 2 == 0) {
                //  Publisher
                // Sub1 | Sub2
                // Sub3 | Sub4
                //    .....
                set.layoutViewWithTopBound(R.id.publisher_view_id, R.id.main_container);
                set.layoutViewAllContainerWide(R.id.publisher_view_id, R.id.main_container);

                for (int i = 0; i < size; i += 2) {
                    if (i == 0) {
                        set.layoutViewAboveView(R.id.publisher_view_id, getResIdForSubscriberIndex(i));
                        set.layoutViewAboveView(R.id.publisher_view_id, getResIdForSubscriberIndex(i + 1));
                    } else {
                        set.layoutViewAboveView(getResIdForSubscriberIndex(i - 2), getResIdForSubscriberIndex(i));
                        set.layoutViewAboveView(getResIdForSubscriberIndex(i - 1), getResIdForSubscriberIndex(i + 1));
                    }

                    set.layoutTwoViewsOccupyingAllRow(getResIdForSubscriberIndex(i), getResIdForSubscriberIndex(i + 1));
                }

                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 2), R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 1), R.id.main_container);
            }
            else if (size > 1) {
                // Pub  | Sub1
                // Sub2 | Sub3
                // Sub3 | Sub4
                //    .....

                set.layoutViewWithTopBound(R.id.publisher_view_id, R.id.main_container);
                set.layoutViewWithTopBound(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutTwoViewsOccupyingAllRow(R.id.publisher_view_id, getResIdForSubscriberIndex(0));

                for (int i = 1; i < size; i += 2) {
                    if (i == 1) {
                        set.layoutViewAboveView(R.id.publisher_view_id, getResIdForSubscriberIndex(i));
                        set.layoutViewAboveView(getResIdForSubscriberIndex(0), getResIdForSubscriberIndex(i + 1));
                    } else {
                        set.layoutViewAboveView(getResIdForSubscriberIndex(i - 2), getResIdForSubscriberIndex(i));
                        set.layoutViewAboveView(getResIdForSubscriberIndex(i - 1), getResIdForSubscriberIndex(i + 1));
                    }
                    set.layoutTwoViewsOccupyingAllRow(getResIdForSubscriberIndex(i), getResIdForSubscriberIndex(i + 1));
                }

                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 2), R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 1), R.id.main_container);
            }
        }
        else {
            if (size == 1) {
                // 1st subscriber full screen
                set.layoutViewFullScreen(getResIdForSubscriberIndex(0));
            }
            else if (size == 2) {
                // Sub1
                // Sub2
                set.layoutViewAboveView(getResIdForSubscriberIndex(0), getResIdForSubscriberIndex(1));
                set.layoutViewWithTopBound(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(1), R.id.main_container);
                set.layoutViewAllContainerWide(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewAllContainerWide(getResIdForSubscriberIndex(1), R.id.main_container);
            }
            else if (size > 2 && size % 2 != 0) {
                //  Sub1
                // Sub2 | Sub3
                // Sub4 | Sub5
                //    .....
                set.layoutViewWithTopBound(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewAllContainerWide(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewAboveView(getResIdForSubscriberIndex(0), getResIdForSubscriberIndex(1));
                set.layoutViewAboveView(getResIdForSubscriberIndex(0), getResIdForSubscriberIndex(2));

                for (int i = 2; i < size; i += 2) {
                    set.layoutViewAboveView(getResIdForSubscriberIndex(i - 1), getResIdForSubscriberIndex(i + 1));
                    set.layoutViewAboveView(getResIdForSubscriberIndex(i), getResIdForSubscriberIndex(i + 2));

                    set.layoutTwoViewsOccupyingAllRow(getResIdForSubscriberIndex(i - 1), getResIdForSubscriberIndex(i));
                }

                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 2), R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 1), R.id.main_container);
            }
            else if (size > 2) {
                // Sub1 | Sub2
                // Sub3 | Sub4
                // Sub5 | Sub6
                //    .....

                set.layoutViewWithTopBound(getResIdForSubscriberIndex(0), R.id.main_container);
                set.layoutViewWithTopBound(getResIdForSubscriberIndex(1), R.id.main_container);

                for (int i = 2; i <= size; i += 2) {
                    set.layoutViewAboveView(getResIdForSubscriberIndex(i - 2), getResIdForSubscriberIndex(i));
                    set.layoutViewAboveView(getResIdForSubscriberIndex(i - 1), getResIdForSubscriberIndex(i + 1));

                    set.layoutTwoViewsOccupyingAllRow(getResIdForSubscriberIndex(i - 2), getResIdForSubscriberIndex(i - 1));
                }

                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 2), R.id.main_container);
                set.layoutViewWithBottomBound(getResIdForSubscriberIndex(size - 1), R.id.main_container);
            }
        }

        set.applyToLayout(mContainer, true);
    }

    private void updateUserDemandsPosition() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 0);
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        this.getActivity().findViewById(R.id.user_demand).setLayoutParams(layoutParams);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        //TokBoxClient.getInstance().resumeSession();

//        if (mSession == null) {
//            return;
//        }
//        mSession.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        //TokBoxClient.getInstance().pauseSession();

//        if (mSession == null) {
//            return;
//        }
//        mSession.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

//        unregisterIoSocket();

//        disconnectSession();

        super.onDestroy();
    }
}
