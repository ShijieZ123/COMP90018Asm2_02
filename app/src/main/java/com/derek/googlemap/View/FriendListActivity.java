package com.derek.googlemap.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.derek.googlemap.Adapter.FriendAdapter;
import com.derek.googlemap.Model.Friend;
import com.derek.googlemap.Model.User;
import com.derek.googlemap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FriendListActivity extends AppCompatActivity{

    ListView friendListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        friendListView = findViewById(R.id.friendlist);

        DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String[] friendUids = documentSnapshot.getString("friends").split(",");
                ArrayList<Friend> friends = new ArrayList<>();
                for (String uid: friendUids){
                    Log.d("TEST ON SUCCESS", uid);
                    Log.e("TEST ON SUCCESS", uid);

                    if(uid.length()==0){
                        continue;
                    }
                    DocumentReference friendDoc = FirebaseFirestore.getInstance().collection("users").document(uid);
                    friendDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        FriendAdapter friendAdapter = new FriendAdapter(FriendListActivity.this, friends);
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Friend friend = documentSnapshot.toObject(Friend.class);
                            Log.d("TEST ON friend name", friend.getfName());
                            friends.add(friend);

                            friendListView.setAdapter(friendAdapter);
                            // display friends
                            Log.d("TEST ON friend number", String.valueOf(friends.size()));
                        }
                    });
                }
            }
        });



    }
}
