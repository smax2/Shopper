package com.maxedapps.shopper;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.maxedapps.SharedShopping.R;

import java.util.ArrayList;

/**
 * Created by Max on 22.07.2014.
 */
public class ItemListAdapter extends ArrayAdapter {
    private Context mContext;
    private ArrayList<ListItem> mListItems;

    public ItemListAdapter(Context context, ArrayList<ListItem> listItems) {
        super(context, R.layout.shopping_list_row);
        mContext = context;
        mListItems = listItems;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mListItems.get(position).getLocalDBKey();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.shopping_list_item_row, parent, false);
        TextView listTitle = (TextView) rowView.findViewById(R.id.textTitle);
        listTitle.setText(mListItems.get(position).getName());
        CheckBox checkedItem = (CheckBox) rowView.findViewById(R.id.checkItemChecked);
        checkedItem.setChecked(mListItems.get(position).isChecked());
        checkedItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mListItems.get(position).setChecked(b);
                AsyncTask<Void, Void, Boolean> pdb = new PushDataToDatabaseTask().execute();
            }
        });
        return rowView;
    }

    private class PushDataToDatabaseTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            DBHelper db = DBHelper.getInstance(mContext);
            db.checkItems(mListItems);
            return null;
        }
    }
}
