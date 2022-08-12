package com.google.mlkit.vision.demo.java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.mlkit.vision.demo.R;

public class Page4StartingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_4);


        Button button_Third_to_FourthPage = findViewById(R.id.button_Third_to_FourthPage);
        button_Third_to_FourthPage.setOnClickListener(view-> {
            //Intent intent = new Intent(getApplicationContext(), Page5StartingActivity.class);
           // startActivity(intent);
        });

        // No Intent for skip button





    }
}