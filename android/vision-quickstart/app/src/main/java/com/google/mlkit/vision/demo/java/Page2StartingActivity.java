package com.google.mlkit.vision.demo.java;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

import com.google.mlkit.vision.demo.R;

public final class Page2StartingActivity extends AppCompatActivity
{

    private static final String TAG = "SecondPageStartingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){


        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_2);

        Button SecondPage_to_ThirdPage_Button = findViewById(R.id.SecondPage_to_ThirdPage_Button);
        SecondPage_to_ThirdPage_Button.setOnClickListener(view-> {
                Intent intent = new Intent(getApplicationContext(), Page3StartingActivity.class);
                startActivity(intent);
        });





    }
}
