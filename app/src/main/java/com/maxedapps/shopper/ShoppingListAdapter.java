package com.maxedapps.shopper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maxedapps.SharedShopping.R;

import java.util.ArrayList;

/**
 * Created by Max on 22.07.2014.
 */
public class ShoppingListAdapter extends ArrayAdapter {
    private Context mContext;
    private ArrayList<ShoppingList> mShoppingLists;

    public ShoppingListAdapter(Context context, ArrayList<ShoppingList> shoppingLists) {
        super(context, R.layout.shopping_list_row);
        mContext = context;
        mShoppingLists = shoppingLists;
    }

    @Override
    public int getCount() {
        return mShoppingLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mShoppingLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mShoppingLists.get(position).getLocalDBKey();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.shopping_list_row, parent, false);
        TextView listTitle = (TextView) rowView.findViewById(R.id.textTitle);
        listTitle.setText(mShoppingLists.get(position).getName());
        TextView listUsers = (TextView) rowView.findViewById(R.id.textUsers);
        listUsers.setText(String.valueOf(mShoppingLists.get(position).getTotalUsers()) + " " + mContext.getString(R.string.txt_list_users_annotation));
        TextView listItems = (TextView) rowView.findViewById(R.id.textNumberOfItems);
        listItems.setText(String.valueOf(mShoppingLists.get(position).getItems().size()) + " " + mContext.getResources().getQuantityString(R.plurals.txt_list_items_annotation, mShoppingLists.get(position).getItems().size()));
        return rowView;
    }
}
