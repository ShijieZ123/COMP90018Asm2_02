package com.derek.googlemap.View;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.derek.googlemap.Direction.FetchURL;
import com.derek.googlemap.Direction.TaskLoadedCallback;
import com.derek.googlemap.R;
import com.derek.googlemap.Utility.Login;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.derek.googlemap.BitmapFillet;

import com.derek.googlemap.Utility.*;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener, PopupMenu.OnMenuItemClickListener, LocationListener, TaskLoadedCallback {


    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.refresh)
    ImageView refresh;

    private ProgressBar mProgressBar;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private GoogleMap mMap;

    static FirebaseAuth fAuth;
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

    private TextView speed;

    //******************Navigation*******************
    DrawerLayout drawerLayout;

    //direction variable
    ArrayList<LatLng> listPoints;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        //******************Navigation*******************
        //Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        // show current speed
        speed = (TextView) findViewById(R.id.speed);
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {ACCESS_FINE_LOCATION}, 200);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {ACCESS_COARSE_LOCATION}, 200);
        }
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Error: app requires permission to use location", Toast.LENGTH_SHORT);
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        this.onLocationChanged(null);

        // click listener on menu button
        ImageButton menu_button = (ImageButton) findViewById(R.id.map_menu);
        menu_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPopup(v);
            }
        });

        //direction
        listPoints = new ArrayList<LatLng>();


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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
                if (listPoints.size()==2){
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if(listPoints.size()==1){
                    //Add first marker to map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else{
                    //Add second marker
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                //get direction
                if(listPoints.size()==2){
                    String url = getUrl(listPoints.get(0),listPoints.get(1), "driving");
                    new FetchURL(MainActivity.this).execute(url,"driving");
                }


            }
        });

    }


    //getUrl is provided in the Google Map Platform
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if(polyline != null){
            polyline.remove();
        }
        polyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private void addUserMarker(LatLng sydney, String imageUrl, String userName) {

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

    @OnClick({R.id.btn_add, R.id.iv_refresh, R.id.refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add:

                intent = new Intent(this, UploadActivity.class);
                intent.putExtra("friend", friend);
                startActivity(intent);
                break;

            case R.id.iv_refresh:

                intent = new Intent(this, ProfileActivity.class);

                startActivity(intent);

                break;

            case R.id.refresh:
                recreate();

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

        //Close drawer
        closeDrawer(drawerLayout);

    }


    //******************Navigation*******************

    public void ClickMenu(View view){
        //Open drawer
        openDrawer(drawerLayout);
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }

    public void CLickFriendsList(View view){
        Intent intent = new Intent(this, FriendListActivity.class);
        startActivity(intent);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //Check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //When drawer is open
            //Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        //Recreate activity
        recreate();
    }

    public void CLickProfile(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void ClickLogout(View view){
        //logout
        logout(this);
        // test search
//        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
//        intent.putExtra("uid", "AGmYx6VNGlPdVFDS12bGqTIn4Xz1");
//        startActivity(intent);
    }

    public void logout(Activity activity) {
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //Set title
        builder.setTitle("Logout");
        //Set message
        builder.setMessage("Are you sure you want to logout ?");
        //Positive yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //sign out the account
                fAuth.signOut();
                //Redirect activity to Login
                redirectActivity(activity, Login.class);
                finish();
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

    public static void redirectActivity(Activity activity, Class aClass) {
        //Initialize intent
        Intent intent = new Intent(activity, aClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Start activity
        activity.startActivity(intent);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.map_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.qr_scan:
                i = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(i);
                break;
            case R.id.qr_genr:
                i = new Intent(MainActivity.this, GeneratorActivity.class);
                startActivity(i);
                break;
            case R.id.add_friend:
                intent = new Intent(MainActivity.this, UploadActivity.class);
                intent.putExtra("friend", friend);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location==null){
            this.speed.setText("0 km/h");
        }
        else{
            int speed=(int) ((location.getSpeed()*3600)/1000);
            this.speed.setText(speed+" km/h");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

