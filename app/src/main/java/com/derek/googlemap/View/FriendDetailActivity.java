package com.derek.googlemap.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.derek.googlemap.R;

public class FriendDetailActivity extends AppCompatActivity {

    ImageView icon;
    TextView name, email, gender,birthday,coordinate,phone;
    Button deleteFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        icon = findViewById(R.id.friend_icon);
        name = findViewById(R.id.friend_name);
        email = findViewById(R.id.friend_email);
        gender = findViewById(R.id.friend_gender);
        birthday = findViewById(R.id.friend_birthday);
        coordinate = findViewById(R.id.friend_coordinate);
        phone = findViewById(R.id.friend_phone);
        deleteFriend = findViewById(R.id.friend_delete);

        Intent intent = getIntent();
        Glide.with(this).load(intent.getExtras().getString("imageUrl")).into(icon);
        name.setText(intent.getExtras().getString("fName"));
        email.setText(intent.getExtras().getString("email"));
        gender.setText(intent.getExtras().getString("gender"));
        birthday.setText(intent.getExtras().getString("birthday"));
        phone.setText(intent.getExtras().getString("phone"));

        // set coordinate
        double lati = intent.getExtras().getDouble("lati");
        double loti = intent.getExtras().getDouble("loti");
        String coord = String.format("%.2f",lati) +" "+String.format("%.2f", loti);
        coordinate.setText(coord);


    }
}
