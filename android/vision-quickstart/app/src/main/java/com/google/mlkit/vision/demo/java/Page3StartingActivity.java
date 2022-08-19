package com.google.mlkit.vision.demo.java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.demo.R;

public final class Page3StartingActivity extends AppCompatActivity
{

    private static final String TAG = "ThirdPageStartingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        // should be changed to going page_3 not later!

        setContentView(R.layout.page_3);
        Button ThirdPage_to_FourthPage_Button = findViewById(R.id.button_Third_to_FourthPage);
        ThirdPage_to_FourthPage_Button.setOnClickListener(view-> {
            Intent intent = new Intent(getApplicationContext(), HandTestingActivity.class);
            startActivity(intent);
        });




        /*
        *

        TextView TextView_java_entry_point = findViewById(R.id.java_entry_point);
        TextView_java_entry_point.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChooserActivity.class);
            startActivity( intent);
        });

        // let's just delete here
        TextView TextView_kotlin_entry_point = findViewById(R.id.kotlin_entry_point);
        TextView_kotlin_entry_point.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChooserActivity.class);
            startActivity(intent);

        });



        **/





    }
}
