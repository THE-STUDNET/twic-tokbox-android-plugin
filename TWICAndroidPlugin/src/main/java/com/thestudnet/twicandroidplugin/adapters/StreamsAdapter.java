package com.thestudnet.twicandroidplugin.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.opentok.android.Subscriber;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 09/05/2017.
 */

public class StreamsAdapter extends BaseAdapter {

    private Context context;

    public StreamsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        int count = 0;
        count += TokBoxClient.getInstance().getSubscribers().size();
        if(TokBoxClient.getInstance().getPublisher() != null)
            count++;
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        convertView = inflater.inflate(R.layout.grid_stream, parent, false);
//        return convertView;

        /*
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        convertView = LayoutInflater.from(this.context).inflate(R.layout.grid_stream, parent, false);
        convertView = inflater.inflate(R.layout.grid_stream, parent, false);
        */

        /*
        if (convertView == null) {
            if(position < TokBoxClient.getInstance().getSubscribers().size()) {
                convertView = TokBoxClient.getInstance().getSubscribers().get(position).getView();
            }
            else if(TokBoxClient.getInstance().getPublisher() != null) {
                convertView = TokBoxClient.getInstance().getSubscribers().get(position).getView();
            }
        }
        else {
            if(position < TokBoxClient.getInstance().getSubscribers().size()) {
                ((RelativeLayout) convertView).removeAllViews();
                ((RelativeLayout) convertView).addView(TokBoxClient.getInstance().getSubscribers().get(position).getView());
            }
            else if(TokBoxClient.getInstance().getPublisher() != null) {
                ((RelativeLayout) convertView).removeAllViews();
                ((RelativeLayout) convertView).addView(TokBoxClient.getInstance().getPublisher().getView());
            }
        }
        */

        /*
        ViewGroup parentView = (ViewGroup)convertView.findViewById(R.id.renderer_view);

        if(TokBoxClient.getInstance().getPublisher() != null) {
            if(position == 0) {
                View child = TokBoxClient.getInstance().getPublisher().getView();
                if(child.getParent() != null) {
                    ((ViewGroup) child.getParent()).removeView(child);
                }
                parentView.addView(TokBoxClient.getInstance().getPublisher().getView(), 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//                ((RelativeLayout) convertView.findViewById(R.id.renderer_view)).addView(TokBoxClient.getInstance().getPublisher().getView());
            }
            else {
//                ((RelativeLayout) convertView.findViewById(R.id.renderer_view)).addView(((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position-1]).getView());
                View child = ((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position-1]).getView();
                if(child.getParent() != null) {
                    ((ViewGroup) child.getParent()).removeView(child);
                }
                parentView.addView(child, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            }
        }
        else {
//            ((RelativeLayout) convertView.findViewById(R.id.renderer_view)).addView(((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position]).getView());
            View child = ((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position]).getView();
            if(child.getParent() != null) {
                ((ViewGroup) child.getParent()).removeView(child);
            }
            parentView.addView(child, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }

        return convertView;
        */

        // Creating a new RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(this.context);

        // Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        View child;
        if(TokBoxClient.getInstance().getPublisher() != null) {
            if(position == 0) {
                child = TokBoxClient.getInstance().getPublisher().getView();
            }
            else {
//                ((RelativeLayout) convertView.findViewById(R.id.renderer_view)).addView(((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position-1]).getView());
                child = ((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position-1]).getView();
            }
        }
        else {
//            ((RelativeLayout) convertView.findViewById(R.id.renderer_view)).addView(((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position]).getView());
            child = ((Subscriber) TokBoxClient.getInstance().getSubscribers().values().toArray()[position]).getView();
        }
        if(child.getParent() != null) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        child.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(child);
        convertView = relativeLayout;
        return convertView;
    }

    public void reload() {
        this.getCount();
        this.notifyDataSetChanged();
    }
}
