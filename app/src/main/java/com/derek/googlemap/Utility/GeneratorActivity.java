package com.derek.googlemap.Utility;

import androidx.appcompat.app.AppCompatActivity;

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

public class GeneratorActivity extends AppCompatActivity {

    private TextView generatorTV;
    private ImageView generatorIV;
    private Button generateCodeBtn;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private ImageButton back;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        fAuth = FirebaseAuth.getInstance();
        generatorTV = findViewById(R.id.tvGenerator);
        generatorIV = findViewById(R.id.ivGenerator);
        generateCodeBtn = findViewById(R.id.GenerateCodeBtn);
        generateCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = getMyID();
                if (data.isEmpty()) {
                    Toast.makeText(GeneratorActivity.this,
                            "Error retrieving user ID",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

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

        back = findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    /* data to be encoded into QR code */
    private String getMyID() {
        return fAuth.getCurrentUser().getUid();
    }
}