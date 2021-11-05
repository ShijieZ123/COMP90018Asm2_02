package com.derek.googlemap.View;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.derek.googlemap.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.derek.googlemap.FileUtils;
import com.derek.googlemap.Model.User;

public class UploadActivity extends AppCompatActivity {

    CharSequence[] items = new CharSequence[]{"Take photo", "Album"};

    private LinearLayout chooseImageBtn;
    private Button uploadBtn;
    private EditText nameEditText;
    private EditText etJingdu;
    private EditText etWeidu;
    private ImageView chosenImageView;
    private ImageView back;
    private ProgressBar uploadProgressBar;

    private Uri mImageUri;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        chooseImageBtn = findViewById(R.id.ll_select_head);
        back = findViewById(R.id.iv_back);
        uploadBtn = findViewById(R.id.uploadBtn);
        nameEditText = findViewById(R.id.nameEditText);
        etJingdu = findViewById(R.id.et_jingdu);
        etWeidu = findViewById(R.id.et_weidu);
        chosenImageView = findViewById(R.id.chosenImageView);
        uploadProgressBar = findViewById(R.id.progress_bar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        String friend = getIntent().getStringExtra("friend");

        stringBuilder.append(friend);

        mStorageRef = FirebaseStorage.getInstance().getReference("teachers_uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("teachers_uploads");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(UploadActivity.this, "An Upload is Still in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("CHECK UID", String.valueOf(nameEditText.getText()));
                    Intent intent = new Intent(UploadActivity.this, SearchActivity.class);
                    intent.putExtra("uid", nameEditText.getText().toString());
                    startActivity(intent);
                    finish();
                }

//                if (mUploadTask != null && mUploadTask.isInProgress()) {
//                    Toast.makeText(UploadActivity.this, "An Upload is Still in Progress", Toast.LENGTH_SHORT).show();
//                } else {
////                    uploadFile();
//
//                    stringBuilder.append("," + nameEditText.getText().toString());
//
//                    DocumentReference docRef = fStore.collection("users").document(user.getUid());
//                    Map<String, Object> edited = new HashMap<>();
//                    edited.put("friends", stringBuilder.toString());
//                    docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(UploadActivity.this, "Add success", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    });
//
//                }
            }
        });
    }

    private void showLocation(Location location) {
        etJingdu.setText(location.getLatitude() + "");
        etWeidu.setText(location.getLongitude() + "");
    }

    private void openFileChooser() {
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //choose take picture
                                //create file to save picture
                                String takePictureName = System.currentTimeMillis() + ".jpg";
                                File takePhotoImage = new File(
                                        getExternalFilesDir(Environment.DIRECTORY_PICTURES), takePictureName);
                                try {
                                    takePhotoImage.createNewFile();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //get uri of image file
//                                imageUri = Uri.fromFile(takePhotoImage);
                                imageUri = getImageContentUri(UploadActivity.this, takePhotoImage);

                                //create Intent to call camera of phone
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                //set input to imageUri
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                //start intent and return picture
                                startActivityForResult(intent, TAKE_PHOTO);
                                Log.i("Tag 2031", "imageUri==null?  " + (imageUri == null));
                                break;
                            case 1: //choose call gallery of phone
                                //create Intent to call gallery of phone
                                Intent intent1 = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                //start intent and return result
                                startActivityForResult(intent1, LOCAL_CROP);
                                break;
                        }
                    }
                }).show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadProgressBar.setVisibility(View.VISIBLE);
            uploadProgressBar.setIndeterminate(true);

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    uploadProgressBar.setVisibility(View.VISIBLE);
                                    uploadProgressBar.setIndeterminate(false);
                                    uploadProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(UploadActivity.this, "Saved successfully", Toast.LENGTH_LONG).show();


                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    //createNewPost(imageUrl);

                                    User upload = new User(nameEditText.getText().toString().trim(),
                                            imageUrl,
                                            etJingdu.getText().toString(), etWeidu.getText().toString());

                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);

                                    uploadProgressBar.setVisibility(View.INVISIBLE);
                                    openImagesActivity();
                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            uploadProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "You haven't Selected Any file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagesActivity() {
        finish();
    }


    private static final int TAKE_PHOTO = 1;
    private static final int LOCAL_CROP = 2;
    private static final int IDENTIFICATION = 3;
    private Uri imageUri; //global variable when taking picture
    private Uri galleryCropUri;
    private Uri takePhotoCropUri;

    public static Uri getImageContentUri(Context context, java.io.File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    //must rewrite onActivityResult method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        takePhotoCropUri = getImageContentUri(UploadActivity.this, cropImage);
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
                    intent1.putExtra("outputX", 299 * 4);
                    intent1.putExtra("outputY", 299 * 4);
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
                        galleryCropUri = getImageContentUri(UploadActivity.this, galleryImage);
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

                            chosenImageView.setImageBitmap(bitmap);

                            Log.e("path", FileUtils.getRealPathFromUri(UploadActivity.this, takePhotoCropUri));

                            mImageUri = takePhotoCropUri;

                        }
                        //if choose gallery
                        else if (galleryCropUri != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryCropUri);

                            chosenImageView.setImageBitmap(bitmap);

                            Log.e("path", FileUtils.getRealPathFromUri(UploadActivity.this, galleryCropUri));

                            mImageUri = galleryCropUri;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
