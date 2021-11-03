package com.derek.googlemap.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    ImageView icon;
    TextView name, email, gender,birthday,coordinate,phone;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        icon = findViewById(R.id.icon);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        gender = findViewById(R.id.gender);
        birthday = findViewById(R.id.birthday);
        coordinate = findViewById(R.id.coordinate);
        phone = findViewById(R.id.phone);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        Glide.with(this).load(R.drawable.loading).into(icon);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(fAuth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {

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
                        Log.d("Profile", "No such document"+fAuth.getCurrentUser().getUid());
                    }
                } else {
                    Log.d("Profile", "get failed with ", task.getException());
                }
            }
        });
    }

}
