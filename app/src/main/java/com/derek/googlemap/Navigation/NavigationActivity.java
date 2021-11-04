package com.derek.googlemap.Navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.derek.googlemap.R;
import com.derek.googlemap.Utility.AccelerometerFragment;
import com.derek.googlemap.Utility.EnvironmentsFragment;
import com.derek.googlemap.Utility.GeneratorFragment;
import com.derek.googlemap.Utility.ScannerFragment;
import com.derek.googlemap.View.FriendListActivity;

public class NavigationActivity extends AppCompatActivity {
    //Initialize variable
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        //Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

    }

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

//    public void ClickQrGenerate(View view){
//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.map_layout,
//                new GeneratorFragment()
//        ).commit();
//    }
//
//    public void ClickQrScanner(View view){
//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.map_layout,
//                new ScannerFragment()
//        ).commit();
//    }
//
//    public void ClickEnvironment(View view){
//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.map_layout,
//                new EnvironmentsFragment()
//        ).commit();
//    }
//
//    public void ClickAcceleration(View view){
//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.map_layout,
//                new AccelerometerFragment()
//        ).commit();
//    }

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

    public void CLickFriendsList(View view){
        Intent intent = new Intent(this, FriendListActivity.class);
    }




//    public void ClickDashboard(View view){
//        //Redirect activity to dashboard
//        redirectActivity(this, );
//    }
//
//    public void ClickAboutUs(View view){
//        //Redirect activity to about us
//        redirectActivity(this, );
//    }

    public void ClickLogout(View view){
        //logout
        logout(this);
    }

    public static void logout(Activity activity) {
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
                //Finish activity
                activity.finishAffinity();
                //Exist app
                System.exit(0);
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

    @Override
    protected void onPause() {
        super.onPause();
        //Close drawer
        closeDrawer(drawerLayout);
    }
}
