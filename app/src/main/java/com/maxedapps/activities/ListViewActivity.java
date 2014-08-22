package com.maxedapps.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.maxedapps.SharedShopping.R;
import com.maxedapps.com.max.fragments.DeleteItemDialogFragment;
import com.maxedapps.com.max.fragments.DeleteListDialogFragment;
import com.maxedapps.com.max.fragments.ListViewFragment;
import com.maxedapps.shopper.DBHelper;
import com.maxedapps.shopper.ShoppingList;
import com.maxedapps.shopper.SyncDatabasesAsyncTask;

import java.util.ArrayList;

public class ListViewActivity extends Activity implements DeleteItemDialogFragment.DeleteItemDialogListener, SyncDatabasesAsyncTask.ISyncLocalDatabase, DeleteListDialogFragment.DeleteListDialogListener {
    public static final String EXTRA_LIST_ID = "list_id";
    public static final String EXTRA_EDIT_MODE = "edit_mode";

    private long mSelectedListId;
    private ShoppingList mSelectedList;
    private int mCalls;
    private int mListPosition;
    private InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        // ID von Intent
        Intent intent = getIntent();
        mSelectedListId = intent.getLongExtra(ListOverviewActivity.EXTRA_LIST_ID, 1);
        mSelectedList = DBHelper.getInstance(this).getListByLocalId(mSelectedListId);
        mListPosition = intent.getIntExtra(ListOverviewActivity.EXTRA_LIST_POSITION, 0);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.placeholder, ListViewFragment.newInstance(mSelectedListId)).commit();
        setTitle(mSelectedList.getName());
        // Für transitional Ad
        SharedPreferences userMgmt = getSharedPreferences("userMgmt", 0);
        mCalls = userMgmt.getInt("list_view_calls", 0);
        SharedPreferences.Editor editor = userMgmt.edit();
        mCalls++;
        editor.putInt("list_view_calls", mCalls);
        editor.commit();
        if (mCalls >= 3) {
            // Create the mInterstitial.
            mInterstitial = new InterstitialAd(this);
            mInterstitial.setAdUnitId("ca-app-pub-2071006942209821/8449284997");

            // Create ad request.
            AdRequest adRequest = new AdRequest.Builder().build();

            // Begin loading your mInterstitial.
            mInterstitial.loadAd(adRequest);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view, menu);
        if (mSelectedList.getListState() == ShoppingList.LIST_STATE_JOINED) {
            menu.findItem(R.id.action_delete).setTitle(getString(R.string.action_unjoinList));
            menu.removeItem(R.id.action_share);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_edit):
                displayInterstitial();
                Intent intent = new Intent(this, EditListActivity.class);
                intent.putExtra(EXTRA_EDIT_MODE, EditListActivity.MODE_EDIT);
                intent.putExtra(EXTRA_LIST_ID, mSelectedListId);
                startActivity(intent);
                break;
            case (R.id.action_delete):
                if (mSelectedList.getListState() == ShoppingList.LIST_STATE_ADDED) {
                    DialogFragment deleteDialog = new DeleteListDialogFragment();
                    deleteDialog.show(getFragmentManager(), "DeleteList");
                } else {
                    AsyncTask<String, Void, Boolean> dbUpdater = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_UNCONNECT_USER);
                    ((SyncDatabasesAsyncTask) dbUpdater).provideUnjoinData(mSelectedList);
                    dbUpdater.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/unjoinList.php");
                }
                break;
            case R.id.action_share:
                if (mSelectedList.getWebId() == 0) {
                    Toast.makeText(this, getString(R.string.warning_sync_necessary), Toast.LENGTH_SHORT).show();
                    SyncDatabasesAsyncTask syncDB = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_EXTERNAL);
                    syncDB.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncWeb.php");
                }
                String shareText = getString(R.string.share_list_information_info)
                        + getString(R.string.share_list_information_id)
                        + mSelectedList.getWebId()
                        + getString(R.string.share_list_information_password)
                        + mSelectedList.getPassword()
                        + getString(R.string.share_list_information_get_app);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_share)));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnPositiveButtonClick(DialogFragment dialog) {
        // Der DB-Zugriff besser in AsyncTask bzw. Thread
        DBHelper.getInstance(this).deleteList(mSelectedListId);
        displayInterstitial();
        Intent intent = new Intent(this, ListOverviewActivity.class);
        startActivity(intent);
//        SyncDatabasesAsyncTask syncDB = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_EXTERNAL);
//        syncDB.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncWeb.php");
    }

    @Override
    public void OnNegativeButtonClick(DialogFragment dialog) {

    }

    @Override
    public void update() {
        Intent intent = new Intent(this, ListOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
            displayInterstitial();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void displayInterstitial() {
        if (mInterstitial != null && mInterstitial.isLoaded()) {
            SharedPreferences userMgmt = getSharedPreferences("userMgmt", 0);
            SharedPreferences.Editor editor = userMgmt.edit();
            editor.putInt("list_view_calls", 0);
            editor.commit();
            mInterstitial.show();
        }
    }
}
