package com.assignment.comp90018asm2_02;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileFragment extends Fragment {

    private TextInputLayout tilFullname, tilPassword, tilPhone, tilEmail;
    private TextView tvProfileName;
    private String currFullname, currPassword, currPhone, currEmail;
    private Button btnUpdate;
    private DatabaseReference ref;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilFullname = getView().findViewById(R.id.tilFullname);
        tilPassword = getView().findViewById(R.id.tilPassword);
        tilPhone = getView().findViewById(R.id.tilPhone);
        tilEmail = getView().findViewById(R.id.tilEmail);
        tvProfileName = getView().findViewById(R.id.tvProfileName);
        btnUpdate = getView().findViewById(R.id.btnUpdate);

        ref = FirebaseDatabase.getInstance().getReference("users");
        userID = FirebaseAuth.getInstance().getUid();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(view);
            }
        });

        showUserData();
    }

    private void showUserData() {
        Intent i = getActivity().getIntent();
        currFullname = i.getStringExtra("fName");
        currPassword = i.getStringExtra("password");
        currPhone = i.getStringExtra("phone");
        currEmail = i.getStringExtra("email");

        Log.e("UserProfileFragment", FirebaseAuth.getInstance().getCurrentUser().toString());

        tilFullname.getEditText().setText(currFullname);
        tvProfileName.setText(currFullname);
        tilPassword.getEditText().setText(currPassword);
        tilPhone.getEditText().setText(currPhone);
        tilEmail.getEditText().setText(currEmail);
    }

    private void update(View view) {
        if (isNameChanged() || isEmailChanged() || isPasswordChanged() || isPhoneChanged()) {
            Toast.makeText(getActivity(), "Data updated...", Toast.LENGTH_SHORT);
        }
        else {
            Toast.makeText(getActivity(), "No data has been changed.", Toast.LENGTH_SHORT);
        }
    }

    private boolean isNameChanged() {
        String editedName = tilFullname.getEditText().getText().toString();
        if (currFullname.equals(editedName)) {
            return false;
        }
        else {
            ref.child(userID).child("fName").setValue(editedName);
            currFullname = editedName;
            return true;
        }
    }
    private boolean isEmailChanged() {
        String editedEmail = tilEmail.getEditText().getText().toString();
        if (currEmail.equals(editedEmail)) {
            return false;
        }
        else {
            ref.child(userID).child("email").setValue(editedEmail);
            currEmail = editedEmail;
            return true;
        }
    }
    private boolean isPhoneChanged() {
        String editedPhone = tilPhone.getEditText().getText().toString();
        if (currPhone.equals(editedPhone)) {
            return false;
        }
        else {
            ref.child(userID).child("phone").setValue(editedPhone);
            currPhone = editedPhone;
            return true;
        }
    }
    private boolean isPasswordChanged() {
        String editedPassword = tilPassword.getEditText().getText().toString();
        if (currPassword.equals(editedPassword)) {
            return false;
        }
        else {
            ref.child(userID).child("password").setValue(editedPassword);
            currPassword = editedPassword;
            return true;
        }
    }
}