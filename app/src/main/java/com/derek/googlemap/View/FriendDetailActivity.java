package com.derek.googlemap.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.derek.googlemap.R;
import com.derek.googlemap.Utility.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String[] mFriends = intent.getExtras().getStringArray("mFriends");
        String uidToDelete = intent.getExtras().getString("uidToDelete");

        deleteFriend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteFriend(FriendDetailActivity.this, mFriends,uidToDelete);
            }
        });
    }

    public void deleteFriend(Activity activity, String[] mFriends, String uidToDelete) {
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //Set title
        builder.setTitle("Delete");
        //Set message
        builder.setMessage("Are you sure you want to delete this guy?");
        //Positive yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //delete friend
                String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(mUid);

                List<String> friendids = new ArrayList<>(Arrays.asList(mFriends));
                friendids.remove(uidToDelete);
                friendids.remove("");
                String friends="";
                for(String s: friendids){
                    friends+=","+s;
                }
                Map<String, Object> edited = new HashMap<>();
                edited.put("friends", friends);

                docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FriendDetailActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
                        getParent().recreate();
                        finish();
                    }
                });
            }
        });

        //Negative no button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Dismiss dialog
                dialogInterface.dismiss();
            }
        });
        //Show dialog
        builder.show();
    }
}
