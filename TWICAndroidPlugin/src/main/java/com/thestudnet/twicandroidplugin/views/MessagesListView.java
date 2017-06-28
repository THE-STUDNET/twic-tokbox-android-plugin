package com.thestudnet.twicandroidplugin.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.adapters.MessagesAdapter;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.events.MessageInteraction;
import com.thestudnet.twicandroidplugin.managers.APIClient;
import com.thestudnet.twicandroidplugin.managers.MessagesManager;
import com.thestudnet.twicandroidplugin.models.GenericModel;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 28/06/2017.
 */

public class MessagesListView extends ListView {

    private MessagesAdapter adapter;

    public MessagesListView(Context context) {
        super(context);
        this.init();
    }

    public MessagesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public MessagesListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        this.adapter = new MessagesAdapter(this.getContext(), MessagesManager.getInstance().getMessages());
        this.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        this.setAdapter(this.adapter);
        /*
        this.adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                MessagesListView.this.setSelection(adapter.getCount() - 1);
            }
        });
        */
        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    // check if we reached the top or bottom of the list
                    View v = MessagesListView.this.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        // reached the top:
                        if(adapter.getCount() > 0) {
                            GenericModel message = (GenericModel) adapter.getItem(0);
                            // Get old(er) messages
                            APIClient.getInstance().getMessagesBeforeMessageId(message.getContentValue("id"));
                        }
                        return;
                    }
                } else if (totalItemCount - visibleItemCount == firstVisibleItem){
                    View v =  MessagesListView.this.getChildAt(totalItemCount-1);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        // reached the top:
                        return;
                    }
                }
            }
        });
    }

    private boolean isVisible() {
        return (this.getVisibility() == VISIBLE ? true : false);
    }

    public void toggleVisibility() {
        if(this.isVisible()) {
            this.setVisibility(GONE);
        }
        else {
            this.setVisibility(VISIBLE);
        }
    }

    @Subscribe
    public void onMessageInteraction(final MessageInteraction.OnMessageInteractionEvent event) {
        if(event.getType() == MessageInteraction.Type.ON_MESSAGES_LOADED || event.getType() == MessageInteraction.Type.ON_LATEST_MESSAGES_LOADED  || event.getType() == MessageInteraction.Type.ON_HISTORICAL_MESSAGES_LOADED) {
            if(event.getData().size() > 0 && event.getData().get(0) instanceof Integer && ((Integer) event.getData().get(0)).intValue() > 0) {
                this.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        if(event.getType() != MessageInteraction.Type.ON_HISTORICAL_MESSAGES_LOADED) {
                            // Scroll to bottom
                            MessagesListView.this.setSelection(adapter.getCount() - 1);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getInstance().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getInstance().unregister(this);
    }
}
