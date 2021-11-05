/**
 * This class is an activity of friend list view
 */

package com.derek.googlemap.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FriendListActivity extends AppCompatActivity{

    ListView friendListView;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        /* bind views */
        friendListView = findViewById(R.id.friendlist);
        back = findViewById(R.id.iv_back);

        // get current user's document reference
        DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String friends = documentSnapshot.getString("friends");

                if(friends.length()==0){
                    exitFriendList(FriendListActivity.this);
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
                        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Friend selectedFriend = friends.get(i);
                                Log.d("TEST selected friend", selectedFriend.getfName());

                                Intent intent = new Intent(FriendListActivity.this, FriendDetailActivity.class);
                                intent.putExtra("imageUrl", selectedFriend.getImageUrl());
                                intent.putExtra("fName", selectedFriend.getfName());
                                intent.putExtra("email", selectedFriend.getEmail());
                                intent.putExtra("gender", selectedFriend.getGender());
                                intent.putExtra("birthday", selectedFriend.getBirthday());
                                intent.putExtra("phone", selectedFriend.getPhone());
                                intent.putExtra("lati", selectedFriend.getLati());
                                intent.putExtra("loti", selectedFriend.getLoti());
                                intent.putExtra("mFriends",splitedFriends);
                                intent.putExtra("uidToDelete",docs.get(i).getId());

                                startActivityForResult(intent, 1);
                            }
                        });
                    }
                });
            }
        });

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            recreate();
        }
    }

    public void exitFriendList(Activity activity) {
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //Set title
        builder.setTitle("Empty list");
        //Set message
        builder.setMessage("You did not add any friend yet");

        //Negative no button
        builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        //Show dialog
        builder.show();
    }
}
