package com.maxedapps.shopper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.maxedapps.SharedShopping.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximilian on 10.08.14.
 */
public class SyncDatabasesAsyncTask extends AsyncTask<String, Void, Boolean> {
    public static final int SYNC_LOCAL = 0;
    public static final int SYNC_EXTERNAL = 1;
    public static final int SYNC_CONNECT_USER = 2;
    public static final int SYNC_UNCONNECT_USER = 3;

    private ISyncLocalDatabase mActivity;
    private boolean mTaskAccomplished = false;
    private ArrayList<ShoppingList> mShoppingLists;
    private ShoppingList mShoppingList;
    private Context mContext;
    private int mSyncMode;
    private long mListId;
    private String mListPassword;
    private String mErrorMessage;

    public SyncDatabasesAsyncTask(ISyncLocalDatabase activity, Context context, int syncMode) {
        mActivity = activity;
        mShoppingLists = DBHelper.getInstance(context).getLists();
        mContext = context;
        mSyncMode = syncMode;
    }

    public void provideJoinData(long listId, String listPassword) {
        mListId = listId;
        mListPassword = listPassword;
    }

    public void provideUnjoinData(ShoppingList shoppingList) {

        mShoppingList = shoppingList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mContext, mContext.getString(R.string.notification_start_update), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(String... url) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            mErrorMessage = mContext.getString(R.string.error_no_internet);
            return null;
        }
        long userId = checkOrCreateUser(url[0]);

        switch (mSyncMode) {
            case (SYNC_EXTERNAL):
                mTaskAccomplished = syncExternalDB(userId, url[1]);
                break;
            case (SYNC_LOCAL):
                mTaskAccomplished = syncLocalDB(userId, url[1]);
                break;
            case (SYNC_CONNECT_USER):
                if (joinList(userId, mListId, mListPassword, url[2]))
                    mTaskAccomplished = syncLocalDB(userId, url[1]);
                break;
            case (SYNC_UNCONNECT_USER):
                mTaskAccomplished = unjoinList(userId, mShoppingList.getWebId(), url[1]);
                break;
            default:
                break;
        }
        return null;
    }

    private boolean syncLocalDB(long user_id, String uri) {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpPost httppost = new HttpPost(uri);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
        HttpResponse response = null;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        int responseCode = 404;
        if (response != null) {
            responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 404) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                return false;
            }
        } else {
            return false;
        }
        if (responseCode == 200) {
            InputStream is = null;
            try {
                is = response.getEntity().getContent();
            } catch (IOException e) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
            } catch (Exception e) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            try {
                JSONObject jsonObj = new JSONObject(sb.toString());

                // Getting JSON Array node
                JSONArray lists = jsonObj.getJSONArray("lists");

                //DBHelper.getInstance(mContext).deleteAllLists();
                mShoppingLists.clear();
                long[] exceptionIds = new long[lists.length()];
                for (int i = 0; i < lists.length(); i++) {
                    List<ListItem> listItems = new ArrayList<ListItem>();
                    JSONObject list = lists.getJSONObject(i);
                    JSONArray listItemsJSON = null;
                    try {
                        listItemsJSON = list.getJSONArray("list_items");
                        for (int j = 0; j < listItemsJSON.length(); j++) {
                            JSONObject listItem = listItemsJSON.getJSONObject(j);
                            listItems.add(new ListItem(listItem.getString("item_name"), listItem.getInt("item_checked") == 0 ? false : true));
                        }
                    } catch (JSONException e) {
                        listItemsJSON = null;
                    }
                    exceptionIds[i] = list.getLong("list_id");
                    ShoppingList syncList = DBHelper.getInstance(mContext).getListByWebId(list.getLong("list_id"));
                    if (syncList != null) {
                        syncList.setListState(list.getInt("list_state"));
                        syncList.setName(list.getString("list_name"));
                        syncList.setTotalUsers(list.getInt("list_users"));
                        if (syncList.getListState() == ShoppingList.LIST_STATE_JOINED)
                            syncList.setPassword(list.getString("list_password"));
                        syncList.setItems(listItems);
                    } else {
                        syncList = new ShoppingList(list.getString("list_name"), list.getString("list_password"), list.getLong("list_id"), list.getInt("list_users"), listItems, list.getInt("list_state"));
                    }
                    //DBHelper.getInstance(mContext).addList(shoppingList);
                    DBHelper.getInstance(mContext).updateOrAddList(syncList);
                    mShoppingLists.add(syncList);
                }
                DBHelper.getInstance(mContext).deleteAllLists(exceptionIds);
            } catch (JSONException e) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                e.printStackTrace();
            }
            return true;
        } else {
            // Keine Daten vorhanden
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            return true;
        }
    }

    private boolean syncExternalDB(long user_id, String uri) {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpPost httppost = new HttpPost(uri);
        String json = "";

        JSONObject jObject = new JSONObject();
        JSONArray jLists = new JSONArray();
        try {

            for (ShoppingList list : mShoppingLists) {
                JSONObject jList = new JSONObject();
                jList.put("list_id", list.getLocalDBKey());
                jList.put("list_name", list.getName());
                try {
                    String transmitPassword = list.getPassword();
                    if (list.getListState() == ShoppingList.LIST_STATE_ADDED)
                        transmitPassword = EncryptionHelper.SHA1(list.getPassword());
                    jList.put("list_password", transmitPassword);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                jList.put("web_id", list.getWebId());
                JSONArray jListItems = new JSONArray();
                for (ListItem item : list.getItems()) {
                    JSONObject jListItem = new JSONObject();
                    jListItem.put("item_name", item.getName());
                    jListItem.put("item_checked", item.isChecked() == true ? 1 : 0);
                    jListItems.put(jListItem);
                }
                jList.put("list_items", jListItems);
                jLists.put(jList);
            }
            jObject.put("user_id", user_id);
            jObject.put("lists", jLists);
        } catch (JSONException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        json = jObject.toString();
        try {
            StringEntity se = new StringEntity(json);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            httppost.setEntity(se);
        } catch (UnsupportedEncodingException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");

        int responseCode = 404;
        try {
            HttpResponse httpResponse = httpclient.execute(httppost);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            InputStream is = httpResponse.getEntity().getContent();
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            if (responseCode == 200) {
                try {
                    JSONObject jsonObj = new JSONObject(sb.toString());

                    // Getting JSON Array node
                    JSONArray local_ids = jsonObj.getJSONArray("local_ids");
                    JSONArray web_ids = jsonObj.getJSONArray("web_ids");
                    for (int i = 0; i < local_ids.length(); i++) {
                        for (ShoppingList list : mShoppingLists) {
                            if (list.getLocalDBKey() == local_ids.getLong(i)) {
                                list.setWebId(web_ids.getLong(i));
                                DBHelper.getInstance(mContext).setWebId(list.getLocalDBKey(), web_ids.getLong(i));
                            }
                        }
                    }
                } catch (JSONException e) {
                    mErrorMessage = mContext.getString(R.string.error_no_data_received);
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        if (responseCode == 200 || responseCode == 201) {
            return true;
        } else {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            return false;
        }
    }

    private boolean joinList(long userId, long listId, String listPassword, String uri) {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpPost httppost = new HttpPost(uri);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(userId)));
        nameValuePairs.add(new BasicNameValuePair("list_id", String.valueOf(listId)));
        try {
            nameValuePairs.add(new BasicNameValuePair("list_password", EncryptionHelper.SHA1(listPassword)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        int responseCode = 404;
        if (response != null) {
            responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 404 || responseCode == 201) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                return false;
            }
        } else {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            return false;
        }
        return true;
    }

    private boolean unjoinList(long userId, long listId, String uri) {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpPost httppost = new HttpPost(uri);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(userId)));
        nameValuePairs.add(new BasicNameValuePair("list_id", String.valueOf(listId)));
        HttpResponse response = null;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            e.printStackTrace();
        }
        int responseCode = 404;
        if (response != null) {
            responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 404 || responseCode == 201) {
                mErrorMessage = mContext.getString(R.string.error_no_data_received);
                return false;
            }
        } else {
            mErrorMessage = mContext.getString(R.string.error_no_data_received);
            return false;
        }
        DBHelper.getInstance(mContext).deleteList(mShoppingList.getLocalDBKey());
        return true;
    }

    private long checkOrCreateUser(String url) {
        SharedPreferences userMgmt = mContext.getSharedPreferences("userMgmt", 0);
        long user_id = userMgmt.getLong("user_id", 0);
        if (user_id == 0) {
            AccountManager accountManager = AccountManager.get(mContext);
            Account[] accounts = accountManager.getAccountsByType(
                    GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String mail;
            if (accounts.length != 0)
                mail = accounts[0].name;
            else {
                return 0;
                // FEHLER ==> Google Account wird benötigt
            }
            InputStream is = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                try {
                    nameValuePairs.add(new BasicNameValuePair("user_mail", EncryptionHelper.SHA1(mail)));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                // Get response code + user_id
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    is = response.getEntity().getContent();
                } else
                    return 0;

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                user_id = Long.parseLong(sb.toString().replace("\"", ""));
                SharedPreferences.Editor editor = userMgmt.edit();
                editor.putLong("user_id", user_id);

            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
        }
        return user_id;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (!mTaskAccomplished) {
            Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(mContext, mContext.getString(R.string.notification_successfull_update), Toast.LENGTH_SHORT).show();
        mActivity.update();
    }

    public interface ISyncLocalDatabase {
        public void update();
    }
}
