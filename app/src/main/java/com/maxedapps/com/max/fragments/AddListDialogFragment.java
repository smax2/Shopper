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
public class AddListDialogFragment extends DialogFragment {

    public interface AddListDialogListener {
        public void OnAddModeChosen(int choice);
    }

    AddListDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_add)
                .setItems(R.array.dialog_add_choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.OnAddModeChosen(which);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AddListDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement NoticeDialogListener!");
        }
    }
}
