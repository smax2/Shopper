package com.maxedapps.com.max.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.maxedapps.shopper.DBHelper;
import com.maxedapps.shopper.ItemListEditAdapter;
import com.maxedapps.shopper.ListItem;
import com.maxedapps.SharedShopping.R;
import com.maxedapps.shopper.ShoppingList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListEditFragment.OnListEditFragmentListener} interface
 * to handle interaction events.
 * Use the {@link ListEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListEditFragment extends Fragment {
    private static final String ARG_LOADED_LIST = "loaded_list";
    private static final String ARG_LIST_NAME = "list_name";
    private static final String ARG_LIST_PASSWORD = "list_password";

    private ShoppingList mSelectedList;
    private ListItem mSelectedItem;
    private String mListName;
    private String mListPassword;

    private View mRootView;
    private ArrayList<ListItem> mListItems;
    private ItemListEditAdapter mAdapter;

    private OnListEditFragmentListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param shoppingListId The ID of the ShoppingList to be edited. 0 if a new list is created.
     * @return A new instance of fragment ListEditFragment.
     */
    public static ListEditFragment newInstance(long shoppingListId, String listName, String listPassword) {
        ListEditFragment fragment = new ListEditFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_LOADED_LIST, shoppingListId);
        args.putString(ARG_LIST_NAME, listName);
        args.putSerializable(ARG_LIST_PASSWORD, listPassword);
        fragment.setArguments(args);
        return fragment;
    }

    public ListEditFragment() {
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
        mListName = getArguments().getString(ARG_LIST_NAME);
        mListPassword = getArguments().getString(ARG_LIST_PASSWORD);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_list_edit, container, false);

        ListView itemList = (ListView) mRootView.findViewById(R.id.listItems);
        if (mSelectedList != null) {
            mListItems = DBHelper.getInstance(getActivity()).getItems(mSelectedList.getLocalDBKey());
        } else {
            mListItems = new ArrayList<ListItem>();
        }
        mAdapter = new ItemListEditAdapter(getActivity(), mListItems);
        itemList.setAdapter(mAdapter);
        registerForContextMenu(itemList);

        ImageButton btnAddList = (ImageButton) mRootView.findViewById(R.id.btnAddItem);
        btnAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonAddItemClick();
            }
        });

        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.list_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            mListener.onChangesCommitted(mListName,mListPassword,mListItems,mSelectedList == null, getArguments().getLong(ARG_LOADED_LIST));
            return true;
        } else if (id == R.id.action_cancel) {
            mListener.onEditCanceled();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnListEditFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.list_edit_contextual, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                mSelectedItem = (ListItem) mAdapter.getItem(info.position);
                mListItems.remove(mSelectedItem);
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void onButtonAddItemClick() {
        EditText inputItemName = (EditText) mRootView.findViewById(R.id.inputItemName);
        String itemName = inputItemName.getText().toString().trim();
        if (!isItemNameValid(itemName))
            return;
        inputItemName.setText("");
        ListItem newItem = new ListItem(itemName);
        mListItems.add(newItem);
        mAdapter.notifyDataSetChanged();
    }

    private boolean isItemNameValid(String itemName) {
        if (itemName.length() > 30 || itemName.length() < 1 || itemName.contains(";")) {
            Toast.makeText(getActivity(), getString(R.string.warning_item_name), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListEditFragmentListener {
        public void onChangesCommitted(String listName, String listPassword, ArrayList<ListItem> listItems, boolean isNewList, long loadedListId);
        public void onEditCanceled();
    }

}
