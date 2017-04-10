package com.thestudnet.twicandroidplugin.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.fragments.UsersFragment;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class UsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_users);

        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, UsersFragment.newInstance())
                .commit();
    }

    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();
        // Show nice animation
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

}
