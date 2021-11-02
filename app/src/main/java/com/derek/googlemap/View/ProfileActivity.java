package com.derek.googlemap.View;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.derek.googlemap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

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
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        gender = findViewById(R.id.gender);
        birthday = findViewById(R.id.birthday);
        coordinate = findViewById(R.id.coordinate);
        phone = findViewById(R.id.phone);

        String uid = fAuth.getCurrentUser().getUid();

    }

}
