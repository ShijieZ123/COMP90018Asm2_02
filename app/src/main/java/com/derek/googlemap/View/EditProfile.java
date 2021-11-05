/**
 *
 * This class is an activity of editing profile activity
 *
 */

package com.derek.googlemap.View;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.derek.googlemap.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import com.derek.googlemap.FileUtils;

import static com.derek.googlemap.View.UploadActivity.getImageContentUri;

public class EditProfile extends TakePhotoActivity {

    CharSequence[] items = new CharSequence[]{"Take photo", "Album"};

    public static final String TAG = "TAG";
    EditText profileFullName, profileEmail,profileBirthday, profilePhone;
    Spinner profileGender;
    CircleImageView profileImageView;
    ImageView back;
    Button saveBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    private TakePhoto takePhoto;
    CropOptions cropOptions;

    private Uri imageUri; //global variable when taking picture

    private StorageTask mUploadTask; //global variable for upload task

    private static final String[] genders = {"Male", "Female", "Other"}; // options for gender dropdown list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        takePhoto = getTakePhoto();
        CompressConfig compressConfig = new CompressConfig.Builder().setMaxSize(70 * 70).setMaxPixel(100).create();

        cropOptions = new CropOptions.Builder().setAspectX(1).setAspectY(1).setWithOwnCrop(true).create();

        takePhoto.onEnableCompress(compressConfig, true);

        Intent data = getIntent();
        final String fullName = data.getStringExtra("fullName");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("teachers_uploads");


        /* bind views */
        back = findViewById(R.id.iv_back);
        profileFullName = findViewById(R.id.profileFullName);
        profileEmail = findViewById(R.id.profileEmailAddress);
        profilePhone = findViewById(R.id.profilePhone);
        profileGender = findViewById(R.id.profileGender);
        profileGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders));
        profileBirthday = findViewById(R.id.profileBirthday);
        profileImageView = findViewById(R.id.profileImageView);
        saveBtn = findViewById(R.id.saveProfileInfo);


        String userId = fAuth.getCurrentUser().getUid();

        // get the current user's information
        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    profilePhone.setText(documentSnapshot.getString("phone"));
                    profileFullName.setText(documentSnapshot.getString("fName"));
                    profileEmail.setText(documentSnapshot.getString("email"));
                    profileBirthday.setText(documentSnapshot.getString("birthday"));
                    int genderSelection = Arrays.asList(genders).indexOf(documentSnapshot.getString("gender"));
                    profileGender.setSelection(genderSelection);
                    Picasso.with(EditProfile.this).load(documentSnapshot.getString("imageUrl")).placeholder(R.mipmap.default_head).error(R.mipmap.default_head).into(profileImageView);
                } else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(EditProfile.this)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //choose take picture
                                        String takePictureName = System.currentTimeMillis() + ".png";
                                        File takePhotoImage = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + takePictureName);

                                        takePhoto.onPickFromCaptureWithCrop(Uri.fromFile(takePhotoImage), cropOptions);

                                        break;
                                    case 1: //choose call gallery of phone
                                        String takePictureName1 = System.currentTimeMillis() + ".jpg";
                                        File takePhotoImage1 = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + takePictureName1);
                                        takePhoto.onPickFromGalleryWithCrop(Uri.fromFile(takePhotoImage1), cropOptions);
                                        break;
                                }
                            }
                        }).show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileFullName.getText().toString().isEmpty() || profileEmail.getText().toString().isEmpty() || profilePhone.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfile.this, "One or Many fields are empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.updateEmail(profileEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("users").document(user.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("email", profileEmail.getText().toString());
                        edited.put("fName", profileFullName.getText().toString());
                        edited.put("phone", profilePhone.getText().toString());
                        edited.put("birthday",profileBirthday.getText().toString());
                        edited.put("gender",profileGender.getSelectedItem().toString());
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                setResult(0);
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }


    private static final int TAKE_PHOTO = 1;
    private static final int LOCAL_CROP = 2;
    private static final int IDENTIFICATION = 3;
    private Uri galleryCropUri;
    private Uri takePhotoCropUri;
    private Uri mImageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PHOTO: //choose take picture
                //if operation was successful
                if (resultCode == RESULT_OK) {
                    //create intent to crop picture
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    //allow to scale
                    //intent.putExtra("scale", true);
                    //set output width and height
                    intent.putExtra("outputX", 299 * 4);
                    intent.putExtra("outputY", 299 * 4);
                    //set crop to square
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);

                    String cropPictureName = "CROP" + System.currentTimeMillis() + ".jpg";
                    File cropImage = new File(
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES), cropPictureName);
                    try {
                        cropImage.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        takePhotoCropUri = getImageContentUri(EditProfile.this, cropImage);
                    } else {
                        takePhotoCropUri = Uri.fromFile(cropImage);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoCropUri);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("noFaceDetection", true);
                    intent.putExtra("return-data", false);
                    //start intent , return result and jump to case IDENTIFICATION
                    startActivityForResult(intent, IDENTIFICATION);
                }
                break;
            case LOCAL_CROP: //choose call gallery of phone
                if (resultCode == RESULT_OK) {
                    //create intent to crop picture
                    Intent intent1 = new Intent("com.android.camera.action.CROP");
                    //get the uri of the chosen picture from gallery of phone
                    Uri uri = data.getData();
                    intent1.setDataAndType(uri, "image/*");
                    //set output width and height
                    intent1.putExtra("outputX", 50 * 50);
                    intent1.putExtra("outputY", 50 * 50);
                    //set crop to square
                    intent1.putExtra("crop", "true");
                    intent1.putExtra("aspectX", 1);
                    intent1.putExtra("aspectY", 1);

                    String galleryPictureName = "CROP" + System.currentTimeMillis() + ".jpg";
                    File galleryImage = new File(
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES), galleryPictureName);
                    try {
                        galleryImage.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //get uri of image file
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        galleryCropUri = getImageContentUri(EditProfile.this, galleryImage);
                    } else {
                        galleryCropUri = Uri.fromFile(galleryImage);
                    }
                    intent1.putExtra(MediaStore.EXTRA_OUTPUT, galleryCropUri);
                    intent1.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent1.putExtra("noFaceDetection", true);
                    intent1.putExtra("return-data", false);

                    //start intent , return result and jump to case IDENTIFICATION
                    startActivityForResult(intent1, IDENTIFICATION);
                }
                break;
            case IDENTIFICATION:
                if (resultCode == RESULT_OK) {
                    try {
                        //if take picture
                        if (takePhotoCropUri != null) {

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), takePhotoCropUri);

                            profileImageView.setImageBitmap(bitmap);

                            Log.e("path", FileUtils.getRealPathFromUri(EditProfile.this, takePhotoCropUri));

                            mImageUri = takePhotoCropUri;

                        }
                        //if choose gallery
                        else if (galleryCropUri != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryCropUri);

                            profileImageView.setImageBitmap(bitmap);

                            Log.e("path", FileUtils.getRealPathFromUri(EditProfile.this, galleryCropUri));

                            mImageUri = galleryCropUri;

                        }

                        uploadImage(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private StorageReference mStorageRef;

    private void uploadImage(Uri uri) {
        StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                + ".png");

        mUploadTask = fileReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                DocumentReference docRef = fStore.collection("users").document(user.getUid());
                                Map<String, Object> edited = new HashMap<>();
                                edited.put("imageUrl", imageUrl);
                                docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                    }
                                });
                            }
                        });
                    }
                });
    }


    private void uploadImageToFirebase(Uri imageUri) {
        // uplaod image to firebase storage
        final StorageReference fileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
//                        Picasso.with(EditProfile.this).load(uri).into(profileImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);

        mImageUri = Uri.fromFile(new File(result.getImage().getCompressPath()));
        profileImageView.setImageBitmap(BitmapFactory.decodeFile(result.getImage().getCompressPath()));

        uploadImage(mImageUri);
    }
}
