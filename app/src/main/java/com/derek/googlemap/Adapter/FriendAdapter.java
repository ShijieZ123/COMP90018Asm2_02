/**
 *
 * Code is refereced from the following site
 * https://anna-scott.medium.com/clickable-listview-items-with-clickable-buttons-e52fa6030d36
 */

package com.derek.googlemap.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.derek.googlemap.Model.Friend;
import com.derek.googlemap.R;

import java.util.ArrayList;

public class FriendAdapter extends ArrayAdapter<Friend> {

    public FriendAdapter(Activity context, ArrayList<Friend> friends) {

        super(context, 0, friends);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.friendlist_item, parent, false);
        }

        Friend currentFriend = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.friendlist_item_name);
        nameTextView.setText(currentFriend.getfName());

        TextView numberTextView = (TextView) listItemView.findViewById(R.id.friendlist_item_distance);
        numberTextView.setText(0);

        ImageView iconView = (ImageView) listItemView.findViewById(R.id.friendlist_item_icon);
        Glide.with(this.getContext()).load(currentFriend.getImageUrl()).into(iconView);

        return listItemView;
    }
}
