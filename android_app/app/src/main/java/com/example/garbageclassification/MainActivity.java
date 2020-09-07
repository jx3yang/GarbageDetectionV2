package com.example.garbageclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.garbageclassification.model.IModel;
import com.example.garbageclassification.model.TfModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void test() {
        IModel model = new TfModel();
        model.predict();
    }
}