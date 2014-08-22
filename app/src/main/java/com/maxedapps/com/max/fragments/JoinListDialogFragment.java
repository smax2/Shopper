package com.maxedapps.com.max.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.maxedapps.SharedShopping.R;

/**
 * Created by Max on 22.07.2014.
 */
public class JoinListDialogFragment extends DialogFragment {

    public interface JoinListDialogListener {
        public void OnJoinConfirmed(long listId, String password);
    }

    JoinListDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_join_list)
                .setView(inflater.inflate(R.layout.dialog_join_list, null))
                .setPositiveButton(R.string.btn_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText inputListId = (EditText) getDialog().findViewById(R.id.inputListId);
                        EditText inputListPassword = (EditText) getDialog().findViewById(R.id.inputListPassword);
                        long listId = Long.parseLong(inputListId.getText().toString());
                        String listPassword = inputListPassword.getText().toString();
                        mListener.OnJoinConfirmed(listId, listPassword);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (JoinListDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement JoinListDialogListener!");
        }
    }
}
