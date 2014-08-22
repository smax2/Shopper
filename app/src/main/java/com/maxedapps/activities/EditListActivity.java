package com.maxedapps.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.maxedapps.com.max.fragments.ListEditFragment;
import com.maxedapps.com.max.fragments.ListInfoFragment;
import com.maxedapps.shopper.DBHelper;
import com.maxedapps.shopper.ListItem;
import com.maxedapps.SharedShopping.R;
import com.maxedapps.shopper.ShoppingList;

import java.util.ArrayList;

public class EditListActivity extends Activity implements ListInfoFragment.OnInfoFragmentListener, ListEditFragment.OnListEditFragmentListener {
    public static final int MODE_EDIT = 0;
    public static final int MODE_CREATE = 1;

    private int mEditMode;
    private long mSelectedListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);
        Intent intent = getIntent();
        mEditMode = intent.getIntExtra(ListOverviewActivity.EXTRA_EDIT_MODE, MODE_CREATE);
        if (mEditMode == MODE_CREATE) {
            setTitle(getString(R.string.title_activity_edit_list_2));
        }
        mSelectedListId = intent.getLongExtra(ListOverviewActivity.EXTRA_LIST_ID,0);
        ShoppingList selectedList = DBHelper.getInstance(this).getListByLocalId(mSelectedListId);
        if (selectedList == null ){
            getFragmentManager().beginTransaction().replace(R.id.container, ListInfoFragment.newInstance(mSelectedListId)).commit();
            return;
        }
        if (savedInstanceState == null && selectedList.getListState() == ShoppingList.LIST_STATE_ADDED) {
            getFragmentManager().beginTransaction().replace(R.id.container, ListInfoFragment.newInstance(mSelectedListId)).commit();
        } else if (savedInstanceState == null && selectedList.getListState() == ShoppingList.LIST_STATE_JOINED) {
            getFragmentManager().beginTransaction().replace(R.id.container, ListEditFragment.newInstance(mSelectedListId, selectedList.getName(), selectedList.getPassword())).commit();
        }
    }

    @Override
    public void onChangesCommitted(String listName, String listPassword, ArrayList<ListItem> listItems, boolean isNewList, long loadedListId) {
        // In AsyncTask
        if (isNewList) {
            ShoppingList newList = new ShoppingList(listName);
            newList.setPassword(listPassword);
            for (ListItem item : listItems) {
                newList.addItem(item);
            }
            DBHelper.getInstance(this).addList(newList);
        } else {
            ShoppingList updatedList = DBHelper.getInstance(this).getListByLocalId(loadedListId);
            updatedList.setName(listName);
            updatedList.setPassword(listPassword);
            updatedList.clearItems();
            for (ListItem item : listItems) {
                updatedList.addItem(item);
            }
            DBHelper.getInstance(this).clearItems(loadedListId);
            DBHelper.getInstance(this).updateList(updatedList);
        }
        Intent intent = new Intent(this,ListOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEditCanceled() {
        // Zurück zu ListOverView
        Intent intent = new Intent(this,ListOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEditContinued(String listName, String listPassword) {
        // Fragment ListEditFragment laden
        getFragmentManager().beginTransaction().replace(R.id.container, ListEditFragment.newInstance(mSelectedListId, listName, listPassword)).commit();
    }
}
