package com.maxedapps.com.max.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maxedapps.shopper.DBHelper;
import com.maxedapps.SharedShopping.R;
import com.maxedapps.shopper.ShoppingList;


public class ListInfoFragment extends Fragment {
    private static final String ARG_LOADED_LIST = "loaded_list";

    private ShoppingList mSelectedList;

    private View mRootView;
    private EditText mInputListName;
    private EditText mInputListPassword;

    private OnInfoFragmentListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shoppingListId The ID of the ShoppingList to be edited. 0 if a new list is created.
     * @return A new instance of fragment ListInfoFragment.
     */
    public static ListInfoFragment newInstance(long shoppingListId) {
        ListInfoFragment fragment = new ListInfoFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_LOADED_LIST, shoppingListId);
        fragment.setArguments(args);
        return fragment;
    }
    public ListInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getLong(ARG_LOADED_LIST) > 0) {
            mSelectedList = DBHelper.getInstance(getActivity()).getListByLocalId(getArguments().getLong(ARG_LOADED_LIST));
        } else {
            mSelectedList = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_list_info, container, false);
        mInputListName = (EditText) mRootView.findViewById(R.id.inputListName);
        mInputListPassword = (EditText) mRootView.findViewById(R.id.inputListPassword);
        if (mSelectedList != null) {
            mInputListName.setText(mSelectedList.getName());
            mInputListPassword.setText(mSelectedList.getPassword());
        }
        Button btnCancel = (Button) mRootView.findViewById(R.id.btnCancel);
        final Button btnContinue = (Button) mRootView.findViewById(R.id.btnContinue);
        mInputListName.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_ENTER:
                            mInputListPassword.requestFocus();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        mInputListPassword.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_ENTER:
                            btnContinue.callOnClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancelPressed();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputIsValid()) {
                    Toast.makeText(getActivity(), getString(R.string.warning_name_and_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                onButtonContinuePressed();
            }
        });

        return mRootView;
    }

    private boolean inputIsValid() {
        String inputName = mInputListName.getText().toString();
        String inputPassword = mInputListPassword.getText().toString();

        if (inputName == "" || inputPassword == "" || inputName.length() > 30 || inputName.length() < 1 ||inputPassword.length() < 7 || inputPassword.length() > 10 || inputPassword.contains(" ") || inputPassword.contains(";") || inputPassword.contains(",") || inputPassword.contains("."))
            return false;
        return true;
    }

    private void onButtonCancelPressed() {
        if (mListener != null) {
            mListener.onEditCanceled();
        }
    }

    private void onButtonContinuePressed() {
        if (mListener != null) {
            mListener.onEditContinued(mInputListName.getText().toString().trim(),mInputListPassword.getText().toString());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnInfoFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnInfoFragmentListener {
        public void onEditCanceled();
        public void onEditContinued(String listName, String listPassword);
    }

}
