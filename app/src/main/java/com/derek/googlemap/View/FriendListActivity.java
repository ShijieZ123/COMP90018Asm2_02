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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
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

                String friends = documentSnapshot.getString("friends");

                if(friends.length()==0){
                    return;
                }

                String[] splitedFriends = friends.split(",");
                CollectionReference citiesRef = FirebaseFirestore.getInstance().collection("users");

                List<String> friendids = new ArrayList<>(Arrays.asList(splitedFriends));

                friendids.remove(""); // remove the empty element
                citiesRef.whereIn(FieldPath.documentId(), friendids).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Friend> friends = new ArrayList<>();
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        Log.d("TEST ON friend docs", String.valueOf(docs.size()));
                        for (DocumentSnapshot doc : docs) {
                            Friend friend = doc.toObject(Friend.class);
                            friends.add(friend);
                            Log.d("TEST ON friend name", friend.getfName());
                        }
                        Log.d("TEST ON friend number", String.valueOf(friends.size()));

                        double mlati = documentSnapshot.getDouble("lati");
                        double mloti = documentSnapshot.getDouble("loti");
                        FriendAdapter friendAdapter = new FriendAdapter(FriendListActivity.this, friends,mlati,mloti);
                        friendListView.setAdapter(friendAdapter);

                    }
                });

            }
        });



    }
}
