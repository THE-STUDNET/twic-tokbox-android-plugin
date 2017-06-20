package com.thestudnet.twicandroidplugin.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.thestudnet.twicandroidplugin.fragments.UserDemandFragment;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 19/06/2017.
 */

public class UsersDemandsAdapter extends FragmentPagerAdapter {

    private ArrayList<String> usersDemands;

    public void updateData(ArrayList<String> usersDemands) {
        this.usersDemands = usersDemands;
    }

    public UsersDemandsAdapter(FragmentManager fm, ArrayList<String> usersDemands) {
        super(fm);
        this.usersDemands = usersDemands;
    }

    @Override
    public Fragment getItem(int position) {
        return UserDemandFragment.newInstance(position + 1, position == getCount() - 1, this.usersDemands.get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return this.usersDemands.size();
    }
}
