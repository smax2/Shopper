package com.maxedapps.com.max.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.maxedapps.SharedShopping.R;

/**
 * Created by Max on 22.07.2014.
 */
public class ChooseSyncDialogFragment extends DialogFragment {

    public interface ChooseSyncDialogListener {
        public void OnItemChosen(int choice);
    }

    ChooseSyncDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_sync)
                .setItems(R.array.dialog_sync_choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.OnItemChosen(which);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ChooseSyncDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement NoticeDialogListener!");
        }
    }
}
