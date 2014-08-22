package com.maxedapps.shopper;

/**
 * Created by Max on 22.07.2014.
 */
public class ListItem {
    private String mItemName;
    private boolean mIsChecked = false;
    private long mDatabaseId;

    public ListItem(String itemName) {
        mItemName = itemName;
    }

    public ListItem(String itemName, int id) {
        mItemName = itemName;
        mDatabaseId = id;
    }

    public ListItem(String name, boolean isChecked) {
        mItemName = name;
        mIsChecked = isChecked;
    }

    public void setChecked(boolean checkState) {
        mIsChecked = checkState;
    }

    public void setLocalDBKey(long id) {
        mDatabaseId = id;
    }

    public long getLocalDBKey() {
        return mDatabaseId;
    }

    public String getName() {
        return mItemName;
    }

    public Boolean isChecked() {
        return mIsChecked;
    }
}
