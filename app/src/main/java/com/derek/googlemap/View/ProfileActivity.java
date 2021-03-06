package com.derek.googlemap.View;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.derek.googlemap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    ImageView icon, back;
    TextView name, email, gender,birthday,coordinate,phone;
    Button editProfile, addFriend, copyUid;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    private boolean isMyProfile = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /* bind views */
        icon = findViewById(R.id.icon);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        gender = findViewById(R.id.gender);
        birthday = findViewById(R.id.birthday);
        coordinate = findViewById(R.id.coordinate);
        phone = findViewById(R.id.phone);
        editProfile = findViewById(R.id.editProfile);
        back = findViewById(R.id.iv_back);
        copyUid = findViewById(R.id.uidButton);

        /* initialise firebase variables and get current user ID */
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        String Uid = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        /* check for a user ID in provided parameters */
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String argUid = b.getString("uid");
            /* if a user ID is found and it is not the same as the current users, we are viewing
            *  another user's profile */
            if (!argUid.equals(Uid)) {
                isMyProfile = false;
                Uid = argUid;
            }
        }

        Glide.with(this).load(R.drawable.loading).into(icon);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(Uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    /* if user information is retrieved correctly, display in the app */
                    if (doc.exists()) {

                        // if doc exits add on click listener to copy uid
                        copyUid.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userId = doc.getId();
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("uid", userId);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(ProfileActivity.this, "Copy uid: "+userId, Toast.LENGTH_SHORT).show();
                            }
                        });

                        Glide.with(ProfileActivity.this).load(doc.getString("imageUrl")).into(icon);
                        name.setText(doc.getString("fName"));
                        phone.setText(doc.getString("phone"));
                        email.setText(doc.getString("email"));
                        gender.setText(doc.getString("gender"));
                        birthday.setText(doc.getString("birthday"));

                        String coord = String.format("%.2f", doc.getDouble("lati"))+" "+String.format("%.2f", doc.getDouble("loti"));
                        coordinate.setText(coord);
                        Log.d("Profile", "DocumentSnapshot data: " + doc.getData());
                    } else {
                        Log.d("Profile", "No such document "+fAuth.getCurrentUser().getUid());
                        /* result is for scanner if an invalid user ID is provided */
                        setResult(1);
                        finish();
                    }
                } else {
                    Log.d("Profile", "get failed with ", task.getException());
                }
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfile.class);
                startActivityForResult(intent, 1);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /* if not current user's profile, prevent ability to edit */
        if (!isMyProfile) {
            editProfile.setVisibility(View.GONE);
            addFriend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            recreate();
        }
    }
}
