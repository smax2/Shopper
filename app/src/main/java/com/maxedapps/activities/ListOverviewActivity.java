package com.maxedapps.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.maxedapps.SharedShopping.R;
import com.maxedapps.com.max.fragments.AddListDialogFragment;
import com.maxedapps.com.max.fragments.ChooseSyncDialogFragment;
import com.maxedapps.com.max.fragments.DeleteItemDialogFragment;
import com.maxedapps.com.max.fragments.DeleteListDialogFragment;
import com.maxedapps.com.max.fragments.JoinListDialogFragment;
import com.maxedapps.shopper.DBHelper;
import com.maxedapps.shopper.ShoppingList;
import com.maxedapps.shopper.ShoppingListAdapter;
import com.maxedapps.shopper.SyncDatabasesAsyncTask;

import java.util.ArrayList;


public class ListOverviewActivity extends Activity implements AdapterView.OnItemClickListener, DeleteItemDialogFragment.DeleteItemDialogListener, ChooseSyncDialogFragment.ChooseSyncDialogListener, SyncDatabasesAsyncTask.ISyncLocalDatabase, JoinListDialogFragment.JoinListDialogListener, AddListDialogFragment.AddListDialogListener, DeleteListDialogFragment.DeleteListDialogListener {

    public static final String EXTRA_LIST_ID = "list_id";
    public static final String EXTRA_EDIT_MODE = "edit_mode";
    public static final String EXTRA_LIST_POSITION = "list_position";

    private static final int SYNC_CHOICE_OVERWRITE_LOCAL = 0;
    private static final int SYNC_CHOICE_OVERWRITE_EXTERNAL = 1;

    private static final int ADD_CHOICE_ADD_LOCAL = 0;
    private static final int ADD_CHOICE_JOIN_LIST = 1;

    private ShoppingList mSelectedList;
    private DBHelper mDBHelper;
    private ShoppingListAdapter mAdapter;

    private ArrayList<ShoppingList> mShoppingLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_overview);

        mDBHelper = DBHelper.getInstance(this);

        mShoppingLists = mDBHelper.getLists();

        ListView shoppingListView = (android.widget.ListView) findViewById(R.id.listShoppingLists);
        mAdapter = new ShoppingListAdapter(this, mShoppingLists);
        shoppingListView.setAdapter(mAdapter);
        shoppingListView.setOnItemClickListener(this);
        registerForContextMenu(shoppingListView);

        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_overview_contextual, menu);
        ListView lv = (ListView) v;
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ShoppingList list = (ShoppingList) lv.getItemAtPosition(acmi.position);
        if (list.getListState() == ShoppingList.LIST_STATE_JOINED) {
            menu.findItem(R.id.action_delete).setTitle(getString(R.string.action_unjoinList));
            menu.removeItem(R.id.action_share);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mSelectedList = (ShoppingList) mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.action_view:
                onItemClick((AdapterView<?>) info.targetView.getParent(), info.targetView, info.position, info.id);
                return super.onContextItemSelected(item);
            case R.id.action_edit:
                Intent intent = new Intent(this, EditListActivity.class);
                intent.putExtra(EXTRA_EDIT_MODE, EditListActivity.MODE_EDIT);
                intent.putExtra(EXTRA_LIST_ID, ((ShoppingList) mAdapter.getItem(info.position)).getLocalDBKey());
                startActivity(intent);
                return super.onContextItemSelected(item);
            case R.id.action_delete:
                if (mSelectedList.getListState() == ShoppingList.LIST_STATE_ADDED) {
                    DialogFragment deleteDialog = new DeleteListDialogFragment();
                    deleteDialog.show(getFragmentManager(), "DeleteList");
                } else {
                    AsyncTask<String, Void, Boolean> dbUpdater = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_UNCONNECT_USER);
                    ((SyncDatabasesAsyncTask) dbUpdater).provideUnjoinData(mSelectedList);
                    dbUpdater.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/unjoinList.php");
                }
                return super.onContextItemSelected(item);
            case R.id.action_share:
                if (mSelectedList.getWebId() == 0) {
                    Toast.makeText(this,getString(R.string.warning_sync_necessary),Toast.LENGTH_SHORT).show();
                    SyncDatabasesAsyncTask syncDB = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_EXTERNAL);
                    syncDB.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncWeb.php");
                    return super.onContextItemSelected(item);
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
                return super.onContextItemSelected(item);
            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_addList):
                AddListDialogFragment addDialogFragment = new AddListDialogFragment();
                addDialogFragment.show(getFragmentManager(), "choose_add_dialog");
                return true;
            case (R.id.action_sync):
                ChooseSyncDialogFragment dialogFragment = new ChooseSyncDialogFragment();
                dialogFragment.show(getFragmentManager(), "choose_sync_dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Hier wird dann die Activity mit den Items der Liste und den entsprechenden Optionen aufgerufen
        Intent intent = new Intent(this, ListViewActivity.class);
        intent.putExtra(EXTRA_LIST_ID, ((ShoppingList) parent.getAdapter().getItem(position)).getLocalDBKey());
        intent.putExtra(EXTRA_LIST_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void OnPositiveButtonClick(DialogFragment dialog) {
        // Der DB-Zugriff besser in AsyncTask bzw. Thread
        mDBHelper.deleteList(mSelectedList.getLocalDBKey());
        mShoppingLists.remove(mSelectedList);
        mAdapter.notifyDataSetChanged();
//        SyncDatabasesAsyncTask syncDB = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_EXTERNAL);
//        syncDB.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncWeb.php");
    }

    @Override
    public void OnNegativeButtonClick(DialogFragment dialog) {

    }

    @Override
    public void OnItemChosen(int choice) {
        if (choice == SYNC_CHOICE_OVERWRITE_EXTERNAL) {
            AsyncTask<String, Void, Boolean> dbUpdater = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_EXTERNAL).execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncWeb.php");
        } else {
            AsyncTask<String, Void, Boolean> dbUpdater = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_LOCAL).execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncLocal.php");
        }
    }

    @Override
    public void update() {
        mShoppingLists.clear();
        mShoppingLists.addAll(DBHelper.getInstance(this).getLists());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnJoinConfirmed(long listId, String password) {
        AsyncTask<String, Void, Boolean> dbUpdater = new SyncDatabasesAsyncTask(this, this, SyncDatabasesAsyncTask.SYNC_CONNECT_USER);
        ((SyncDatabasesAsyncTask) dbUpdater).provideJoinData(listId, password);
        dbUpdater.execute("http://maxedapps.com/SharedShopping/checkUser.php", "http://maxedapps.com/SharedShopping/syncLocal.php", "http://maxedapps.com/SharedShopping/joinList.php");
    }


    @Override
    public void OnAddModeChosen(int choice) {
        if (choice == ADD_CHOICE_ADD_LOCAL) {
            Intent intent = new Intent(this, EditListActivity.class);
            intent.putExtra(EXTRA_EDIT_MODE, EditListActivity.MODE_CREATE);
            intent.putExtra(EXTRA_LIST_ID, 0);
            startActivity(intent);
        } else {
            DialogFragment joinListDialog = new JoinListDialogFragment();
            joinListDialog.show(getFragmentManager(), "JoinList");
        }
    }

}
