package com.derek.googlemap.Utility;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import eu.livotov.labs.android.camview.ScannerLiveView;
import eu.livotov.labs.android.camview.scanner.decoder.zxing.ZXDecoder;

import static android.Manifest.permission.CAMERA;

import com.derek.googlemap.R;
import com.derek.googlemap.View.ProfileActivity;

public class ScannerFragment extends Fragment {

    /* QR code scanner */
    private ScannerLiveView scanner;
    //private TextView tvScanner;
    private ImageButton back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scanner = getView().findViewById(R.id.codeScanner);
        //tvScanner = getView().findViewById(R.id.tvScanner);

        /* request camera permission if not already granted */
        while (true) {
            if (!permissionGranted()) {
                requestPermission();
            }
            else {
                break;
            }
        }

        /* define scanner behaviour; we only really want particular behaviour
        *  for when something is scanned */
        scanner.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scanner) {
                //Toast.makeText(getActivity(), "Scanner Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScannerStopped(ScannerLiveView scanner) {
                //Toast.makeText(getActivity(), "Scanner Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScannerError(Throwable err) {

            }

            @Override
            public void onCodeScanned(String data) {

                //tvScanner.setText(data);

                /* QR code scanned should be an encrypted user ID
                *  pass the user ID into the profile activity to view */
                Intent i = new Intent(getActivity(), ProfileActivity.class);
                Bundle b = new Bundle();
                b.putString("uid", data);
                i.putExtras(b);
                startActivityForResult(i, 1);
            }
        });

        back = (ImageButton) getActivity().findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().recreate();
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    /* occurs if the scanned data is not a valid user ID and no corresponding
    *  document was found in the firebase database */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("Scanner", "result returned, resultCode: " + resultCode);
        int ERROR_CODE = 1;
        if (resultCode == ERROR_CODE) {
            Toast.makeText(getActivity(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
        }
    }

    /* setup the decoder to read data encrypted in QR code and start scanning */
    @Override
    public void onResume() {
        super.onResume();
        ZXDecoder decoder = new ZXDecoder();
        decoder.setScanAreaPercent(0.8);

        scanner.setDecoder(decoder);
        scanner.startScanner();
    }

    @Override
    public void onPause() {
        scanner.stopScanner();
        super.onPause();
    }

    /* only camera permission is required for scanning so check if it has been granted */
    private boolean permissionGranted() {
        int cameraPermission = ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(),
                CAMERA
        );
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraAccepted) {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}