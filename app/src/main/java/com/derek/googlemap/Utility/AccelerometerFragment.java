package com.derek.googlemap.Utility;

import static com.derek.googlemap.View.MainActivity.logout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.derek.googlemap.R;

public class AccelerometerFragment extends Fragment implements SensorEventListener {
    private TextView xView;
    private TextView yView;
    private TextView zView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_accelerometer, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        xView = getView().findViewById(R.id.accX);
        yView =  getView().findViewById(R.id.accY);
        zView =  getView().findViewById(R.id.accZ);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            xView.setText("X acceleration: " );
            yView.setText("Y acceleration: " );
            zView.setText("Z acceleration: " );
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float[] values = sensorEvent.values;
        String accX = Float.toString(values[0]);
        String accY = Float.toString(values[1]);
        String accZ = Float.toString(values[2]);

        getActivity().runOnUiThread(() -> {
            xView.setText("X acceleration: " + accX + " m/s");
            yView.setText("Y acceleration: " + accY + " m/s");
            zView.setText("Z acceleration: " + accZ + " m/s");
        });


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }
}
