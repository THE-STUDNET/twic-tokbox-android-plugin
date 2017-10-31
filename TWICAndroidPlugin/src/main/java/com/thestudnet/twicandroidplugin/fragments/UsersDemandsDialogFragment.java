package com.thestudnet.twicandroidplugin.fragments;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.rd.PageIndicatorView;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.adapters.UsersDemandsAdapter;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 19/06/2017.
 */

public class UsersDemandsDialogFragment extends android.support.v4.app.DialogFragment {

    private ViewPager pager;
    private PageIndicatorView pageIndicatorView;
    private UsersDemandsAdapter usersDemandsAdapter;
    private ArrayList<String> usersDemands = new ArrayList<>();

    public void updateData(ArrayList<String> usersDemands) {
        this.usersDemands = usersDemands;
    }

    public void refreshData() {
        if(this.usersDemandsAdapter != null) {
            this.usersDemandsAdapter.updateData(this.usersDemands);
            this.usersDemandsAdapter.notifyDataSetChanged();
            this.pageIndicatorView.setCount(this.usersDemands.size());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_demand_container, container);

        this.pager = (ViewPager) view.findViewById(R.id.pager);

        this.pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.pageIndicatorView);
        this.pageIndicatorView.setViewPager(pager);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.80), (int) (width * 0.80));
        window.setGravity(Gravity.CENTER);

        /*
        if(this.usersDemandsAdapter == null) {
            this.usersDemandsAdapter = new UsersDemandsAdapter(getChildFragmentManager(), this.usersDemands);
            this.pager.setAdapter(this.usersDemandsAdapter);
        }
        else {
            this.usersDemandsAdapter.updateData(this.usersDemands);
            this.usersDemandsAdapter.notifyDataSetChanged();
        }
        */
        this.usersDemandsAdapter = new UsersDemandsAdapter(getChildFragmentManager(), this.usersDemands);
        this.pager.setAdapter(this.usersDemandsAdapter);
        this.pageIndicatorView.setCount(this.usersDemands.size());
    }
}
