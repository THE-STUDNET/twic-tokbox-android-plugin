package com.thestudnet.twicandroidplugin.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.fragments.VideoGridFragment;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class VideoGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_video_grid);

        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, VideoGridFragment.newInstance())
                .commit();
    }
}
