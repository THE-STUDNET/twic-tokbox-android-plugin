package com.thestudnet.twicandroidplugin.libs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.activities.TWICAndroidPluginActivity;
import com.thestudnet.twicandroidplugin.communication.ErrorMessage;
import com.thestudnet.twicandroidplugin.events.EventBus;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 22/04/15.
 */
public class CustomFragment extends Fragment {

    public SweetAlertDialog progressDialog;
    public SweetAlertDialog alertDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int layout) {
        // Construct progress dialog
        this.progressDialog = new SweetAlertDialog(this.getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        this.progressDialog.setTitle(R.string.progress_title);
        this.progressDialog.setTitleText(this.getString(R.string.progress_message));
        /*if(Build.VERSION.SDK_INT >= 23) {
            this.progressDialog.getProgressHelper().setBarColor(this.getResources().getColor(R.color.loadingColor, null));
        }
        else {
            this.progressDialog.getProgressHelper().setBarColor(this.getResources().getColor(R.color.loadingColor));
        }*/
        this.progressDialog.getProgressHelper().setBarColor(this.getResources().getColor(R.color.loadingColor));
        this.progressDialog.setCancelable(true);

        // Construct alert dialog
        this.alertDialog = new SweetAlertDialog(this.getActivity(), SweetAlertDialog.ERROR_TYPE);
        this.alertDialog.setTitle(R.string.error_title);
        this.alertDialog.setCancelable(true);

        return inflater.inflate(layout, container, false);
    }

    public void enableHeaderAndFooter(boolean enabled) {
        if(this.getActivity() instanceof TWICAndroidPluginActivity) {
            ((TWICAndroidPluginActivity) this.getActivity()).enableHeaderAndFooter(enabled);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register bus events
        EventBus.getInstance().register(this);
    }

    protected void showErrors(ArrayList<ErrorMessage> errors) {
        if(errors != null) {
            for(ErrorMessage error : errors) {
                this.alertDialog.setTitle(error.getError());
                this.alertDialog.setTitleText(error.getMessage());
                this.alertDialog.show();
            }
        }
    }

    protected void showLoading() {
        this.progressDialog.show();
    }

    protected void hideLoading() {
        this.progressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister bus events
        EventBus.getInstance().unregister(this);
    }

}
