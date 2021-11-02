package com.derek.googlemap.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.derek.googlemap.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.derek.googlemap.BitmapFillet;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;

    private ProgressBar mProgressBar;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private GoogleMap mMap;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    private ImageView iv_image;

    private String friend;

    //compass parameters
    private ImageView imageView;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;

    long lastUpdatedTime = 0;
    float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        ButterKnife.bind(this);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();


        iv_image = findViewById(R.id.iv_image);
        mProgressBar = findViewById(R.id.myDataLoaderProgressBar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mProgressBar.setVisibility(View.VISIBLE);

//        mStorage = FirebaseStorage.getInstance();
//        mDatabaseRef = FirebaseDatabase.getInstance().getReference("teachers_uploads");
//
//        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren()) {
//                    Teacher upload = teacherSnapshot.getValue(Teacher.class);
//                    upload.setKey(teacherSnapshot.getKey());
//
//                    LatLng sydney = new LatLng(Double.parseDouble(upload.getLati()), Double.parseDouble(upload.getLoti()));
//                    addUserMarker(sydney, upload.getImageURL(), upload.getName());
//
//                    Log.e("user", upload.getName() + "---" + upload.getImageURL());
//
////                    Glide.with(ItemsActivity.this).load(upload.getImageURL()).into(iv_image);
//                }
//
//                LatLng sydney = new LatLng(-25.344, 131.036);
//                addUserMarker(sydney, "https://img2.baidu.com/it/u=3243760465,2391088822&fm=26&fmt=auto", "user");
//
//                mProgressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ItemsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                mProgressBar.setVisibility(View.INVISIBLE);
//            }
//        });


        // Create a reference to the cities collection



        DocumentReference documentReference = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    Log.e("onEvent", documentSnapshot.getString("phone"));

                    friend = documentSnapshot.getString("friends");

                    LatLng sydney = new LatLng(documentSnapshot.getDouble("lati"), documentSnapshot.getDouble("loti"));
                    addUserMarker(sydney, documentSnapshot.getString("imageUrl"), documentSnapshot.getString("fName"));

                    mProgressBar.setVisibility(View.GONE);

                    findFriends();

                } else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });

        //compass
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        imageView = findViewById(R.id.imageView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    private void findFriends() {
        String[] fs = friend.split(",");

        fStore.collection("users")
                .whereIn("fName", Arrays.asList(fs))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.e("onComplete", document.getId() + " => " + document.getString("phone"));

//                    profilePhone.setText(documentSnapshot.getString("phone"));

                                LatLng sydney = new LatLng(document.getDouble("lati"), document.getDouble("loti"));
                                addUserMarker(sydney, document.getString("imageUrl"), document.getString("fName"));

                                mProgressBar.setVisibility(View.GONE);

                            }
                        } else {
                            Log.e("onComplete", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    protected void onDestroy() {
        super.onDestroy();
//        mDatabaseRef.removeEventListener(mDBListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        120.196303,33.437243
//        119.430007,33.755245

        mMap = googleMap;

//        LatLng sydney = new LatLng(-25.344, 131.036);
//        addUserMarker(sydney, "https://img2.baidu.com/it/u=3243760465,2391088822&fm=26&fmt=auto", "user");
//
//        LatLng sydney1 = new LatLng(33.424855, 120.222377);
//        addUserMarker(sydney1, "https://img0.baidu.com/it/u=1128572502,203785415&fm=26&fmt=auto", "yan");

    }


    private void addUserMarker(LatLng sydney, String imageUrl, String userName) {

//        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.googlemap_marker_layout, null);
//        ImageView numTxt = (ImageView) marker.findViewById(R.id.iv_head);
//
//        Picasso.with(this).load(imageUrl).into(iv_image);
//
//        Bitmap bitmap = createDrawableFromView(this, marker);
//
//        iv_image.setImageBitmap(bitmap);
//
//
//        mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title(userName));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        Picasso.with(this).load(imageUrl).placeholder(R.mipmap.default_head).error(R.mipmap.default_head).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                iv_image.setImageBitmap(bitmap);

                mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title(userName)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFillet.fillet(setBitmap(bitmap, 75, 75), 90, BitmapFillet.CORNER_ALL))));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 6f));
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

                mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title(userName));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 6f));

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });
    }

    public Bitmap setBitmap(Bitmap bm, int newWidth, int newHeight) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }


    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    Intent intent;

    @OnClick({R.id.btn_add, R.id.iv_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add:

                intent = new Intent(this, UploadActivity.class);
                intent.putExtra("friend", friend);
                startActivity(intent);
                break;

            case R.id.iv_refresh:

                intent = new Intent(this, EditProfile.class);
                startActivity(intent);

                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == accelerometerSensor){
            System.arraycopy(sensorEvent.values, 0, lastAccelerometer, 0, sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        } else if (sensorEvent.sensor == magnetometerSensor){
            System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;

        }

        if(isLastMagnetometerArrayCopied && isLastAccelerometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthInRadian = orientation[0];
            float azimuthInDegree = (float) Math.toDegrees(azimuthInRadian);

            RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, -azimuthInDegree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);

            currentDegree = -azimuthInDegree;
            lastUpdatedTime = System.currentTimeMillis();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, magnetometerSensor);

    }
}

