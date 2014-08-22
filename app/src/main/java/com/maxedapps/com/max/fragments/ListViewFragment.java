package com.maxedapps.com.max.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.maxedapps.shopper.DBHelper;
import com.maxedapps.shopper.ItemListAdapter;
import com.maxedapps.shopper.ListItem;
import com.maxedapps.SharedShopping.R;

import java.util.ArrayList;

public class ListViewFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SELECTED_LIST = "selected_list";

    private long mSelectedList;
    private ArrayList<ListItem> mListItems;
    private View mRootView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param selectedList Die ID (in der lokalen DB) der ausgewählten Liste.
     * @return A new instance of fragment ListViewFragment.
     */
    public static ListViewFragment newInstance(Long selectedList) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SELECTED_LIST, selectedList);
        fragment.setArguments(args);
        return fragment;
    }

    public ListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedList = getArguments().getLong(ARG_SELECTED_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_list_view, container, false);

        mListItems = DBHelper.getInstance(getActivity()).getItems(mSelectedList);
        ListView itemList = (android.widget.ListView) mRootView.findViewById(R.id.listItems);
        ItemListAdapter adapter = new ItemListAdapter(getActivity(), mListItems);
        itemList.setAdapter(adapter);
        return mRootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
