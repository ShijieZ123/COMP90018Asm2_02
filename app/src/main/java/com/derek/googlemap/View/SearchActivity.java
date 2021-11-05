package com.derek.googlemap.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.derek.googlemap.R;
import com.derek.googlemap.Utility.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    ImageView user_image, back;
    TextView user_name, user_gender, user_email, user_phone;
    Button user_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        back = findViewById(R.id.iv_back);
        user_image = findViewById(R.id.user_image);
        user_name = findViewById(R.id.user_name);
        user_gender = findViewById(R.id.user_gender);
        user_email = findViewById(R.id.user_email);
        user_phone = findViewById(R.id.user_phone);
        user_add = findViewById(R.id.user_add);

        // get the uid of user
        String user_uid = getIntent().getStringExtra("uid");

        // make firebase document reference through user id
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user_uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {

                        // pull data from cloud to local
                        Glide.with(SearchActivity.this).load(doc.getString("imageUrl")).into(user_image);
                        user_name.setText(doc.getString("fName"));
                        user_gender.setText(doc.getString("gender"));
                        user_email.setText(doc.getString("email"));
                        user_phone.setText(doc.getString("phone"));
                        Log.d("Search", "DocumentSnapshot data: " + doc.getData());

                        // listen on click to add friend
                        user_add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Add Button", "on click");
                                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                                //Set title
                                builder.setTitle("Add");
                                //Set message
                                builder.setMessage("Are you sure you to add this person to friend list?");
                                Log.d("builder", "set up");
                                //Positive yes button
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // update friend list in firebase
                                        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        Map<String, Object> edited = new HashMap<>();
                                        Log.d("Uid", user_uid);
                                        edited.put("friends", "," + user_uid);
                                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(SearchActivity.this, "Add success", Toast.LENGTH_SHORT).show();
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
                        });

                        back.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                finish();
                            }
                        });

                    } else {
                        Log.d("Search", "No such document "+ user_uid);
                        setResult(1);
                        finish();
                    }
                } else {
                    Log.d("Search", "get failed with ", task.getException());
                }
            }
        });


    }



}
