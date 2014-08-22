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
public class DeleteListDialogFragment extends DialogFragment {

    public interface DeleteListDialogListener {
        public void OnPositiveButtonClick(DialogFragment dialog);
        public void OnNegativeButtonClick(DialogFragment dialog);
    }

    DeleteListDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_deleteList)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.OnPositiveButtonClick(DeleteListDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.OnNegativeButtonClick(DeleteListDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DeleteListDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement NoticeDialogListener!");
        }
    }
}
