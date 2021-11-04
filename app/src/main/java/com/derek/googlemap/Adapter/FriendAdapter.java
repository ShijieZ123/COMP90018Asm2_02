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
import java.util.List;

public class FriendAdapter extends ArrayAdapter<Friend> {

    private double mlati;
    private double mloti;

    public FriendAdapter(Activity context, ArrayList<Friend> friends,double mlati, double mloti) {

        super(context, 0, friends);
        this.mloti = mloti;
        this.mlati = mlati;
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
        double distance = this.calculateDistance(mlati,mloti,currentFriend.getLati(),currentFriend.getLoti());

        numberTextView.setText(String.valueOf((int)distance)+" km away");

        ImageView iconView = (ImageView) listItemView.findViewById(R.id.friendlist_item_icon);
        Glide.with(this.getContext()).load(currentFriend.getImageUrl()).into(iconView);

        return listItemView;
    }

    /**
     * By given two points, this function calculate the distance between two points on google map
     * This function is referenced from https://blog.csdn.net/qq_31332467/article/details/79222165
     *
     * @param lati1 user's latitude
     * @param loti1 user's longtitude
     * @param lati2 friend's latitude
     * @param loti2 friend's longtitude
     * @return
     */
    private double calculateDistance(double lati1, double loti1, double lati2, double loti2){
        double x1 = (Math.PI/180)*lati1;
        double x2 = (Math.PI/180)*lati2;
        double y1 = (Math.PI/180)*loti1;
        double y2 = (Math.PI/180)*loti2;

        double R = 6371;// radius of the Earth
        double distance = Math.acos(Math.sin(x1)*Math.sin(x2)+Math.cos(x1)*Math.cos(x2)
                * Math.cos(y2-y1))
                * R;
        return distance;
    }
}
