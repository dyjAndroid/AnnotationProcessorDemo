package com.example.dyj.annotationprocessordemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "dyj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "CanonicalName:" + MainActivity.class.getCanonicalName() +
                ",SimpleName:" + MainActivity.class.getSimpleName());
    }
}
