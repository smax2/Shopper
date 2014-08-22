package com.maxedapps.shopper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 22.07.2014.
 */
public class ShoppingList {
    private String mListName;
    private List<ListItem> mListItems = new ArrayList<ListItem>();
    private long mWebId = 0;
    private long mLocalDBKey = 0;
    private String mPassword;
    private int mTotalUsers = 1;
    private int mListState;

    public static final int LIST_STATE_ADDED = 0;
    public static final int LIST_STATE_JOINED = 1;

    public ShoppingList(String listName) {
        mListName = listName;
    }

    public ShoppingList(String name, String password, long webId, int totalUsers, List<ListItem> listItems, int listState) {
        mListName = name;
        mPassword = password;
        mWebId = webId;
        mTotalUsers = totalUsers;
        mListItems = listItems;
        mListState = listState;
    }

    public void addItem(ListItem item) {
        mListItems.add(item);
    }

    public void deleteItem(int itemPosition) {
        mListItems.remove(itemPosition);
    }

    public void checkItem(int itemPosition) {
        mListItems.get(itemPosition).setChecked(true);
    }

    public void uncheckItem(int itemPosition) {
        mListItems.get(itemPosition).setChecked(false);
    }

    public void setName(String newName) {
        mListName = newName;
    }

    public void clearItems() {
        mListItems.clear();
    }

    public void setItems(List<ListItem> listItems) {
        mListItems = listItems;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setTotalUsers(int totalUsers) {
        mTotalUsers = totalUsers;
    }

    public void setWebId(long id) {
        mWebId = id;
    }

    public void setLocalDBKey(long id) {
        mLocalDBKey = id;
    }

    public void setListState(int listState) {
        mListState = listState;
    }

    public String getName() {
        return mListName;
    }

    public List<ListItem> getItems() {
        return mListItems;
    }

    public long getWebId() {
        return mWebId;
    }

    public long getLocalDBKey() {
        return mLocalDBKey;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getTotalUsers() {
        return mTotalUsers;
    }

    public int getListState() {
        return mListState;
    }
}
