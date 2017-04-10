package com.thestudnet.twicandroidplugin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.fragments.VideoGridFragment;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class VideoGridActivity extends AppCompatActivity implements View.OnClickListener, FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_video_grid);

        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, VideoGridFragment.newInstance())
                .commit();

        this.getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_exit) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                // Exit
                this.finish();
            }
            else {
                this.getSupportFragmentManager().popBackStackImmediate();
            }
        }
        else if(id == R.id.button_users) {
            this.showUsersActivity();
        }
    }

    private void showUsersActivity() {
        Intent i = new Intent(this, UsersActivity.class);
        startActivityForResult(i, RESULT_OK);
        //Set the transition -> method available from Android 2.0 and beyond
        this.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
}
