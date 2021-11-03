package com.derek.googlemap.Utility;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.derek.googlemap.R;

public class EnvironmentsFragment extends Fragment implements SensorEventListener {
    private TextView temperatureView;
    private TextView pressureView;
    private TextView humidityView;
    private ImageButton back;
    private SensorManager sensorManager;
    private Sensor temperature;
    private Sensor pressure;
    private Sensor humidity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_environments, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        temperatureView = getView().findViewById(R.id.temperature);
        pressureView =  getView().findViewById(R.id.pressure);
        humidityView =  getView().findViewById(R.id.humidity);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        if (temperature == null) temperatureView.setText("Temperature sensor unavailable");
        if (pressure == null) pressureView.setText("Pressure sensor unavailable");
        if (humidity == null) humidityView.setText("Humidity sensor unavailable");

        back = (ImageButton) getActivity().findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        final float[] values = sensorEvent.values;
        String env = Float.toString(values[0]);
        getActivity().runOnUiThread(() -> {
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)
                temperatureView.setText("temperature: " + env + "°C");
            if (sensor.getType() == Sensor.TYPE_PRESSURE)
                pressureView.setText("pressure: " + env + "hPa");
            if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY)
                humidityView.setText("humidity: " + env + "%");
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
