package com.thestudnet.twicandroidplugin.adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.thestudnet.twicandroidplugin.fragments.UserDemandFragment;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 19/06/2017.
 */

public class UsersDemandsAdapter extends FragmentStatePagerAdapter {

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

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) { // See https://stackoverflow.com/questions/18642890/fragmentstatepageradapter-with-childfragmentmanager-fragmentmanagerimpl-getfra
        try{
            super.restoreState(state, loader);
        }catch (NullPointerException e){
            // null caught
        }
    }
}
