package com.derek.googlemap.Utility;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.derek.googlemap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.WINDOW_SERVICE;

public class GeneratorFragment extends Fragment {

    private TextView generatorTV;
    private ImageView generatorIV;
    private Button generateCodeBtn;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private ImageButton back;
    FirebaseAuth fAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        generatorTV = getView().findViewById(R.id.tvGenerator);
        generatorIV = getView().findViewById(R.id.ivGenerator);
        generateCodeBtn = getView().findViewById(R.id.GenerateCodeBtn);
        generateCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "AddFriend#"+getMyID();
                if (data.isEmpty()) {
                    Toast.makeText(getActivity(),
                            "Error retrieving user ID",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);

                    Display display = manager.getDefaultDisplay();

                    Point point = new Point();
                    display.getSize(point);

                    int width = point.x;
                    int height = point.y;

                    int dimen = width < height ? width : height;
                    dimen = dimen * 3 / 4;

                    qrgEncoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, dimen);
                    try {
                        generatorTV.setText("");
                        bitmap = qrgEncoder.encodeAsBitmap();
                        generatorIV.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        Log.e("GeneratorActivity", e.toString());
                    }
                }
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

    private String getMyID() {
        return fAuth.getCurrentUser().getUid();
    }
}