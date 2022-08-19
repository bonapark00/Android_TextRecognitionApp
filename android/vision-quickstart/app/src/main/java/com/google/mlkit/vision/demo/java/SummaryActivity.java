package com.google.mlkit.vision.demo.java;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import com.google.mlkit.vision.demo.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import android.widget.CheckBox;
public class SummaryActivity extends AppCompatActivity {
    String TAG = "SummaryActivity";
    private TextView textView_summary;
    private CheckBox checkBox;
    private LinearLayout linearLayout_summary;
    private ScrollView view_scroller;
    private Button button_finish_summary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        //textView_summary = findViewById(R.id.textView_summary);
        linearLayout_summary = findViewById(R.id.linear_layout_summary);
        view_scroller = findViewById(R.id.scroll);
        button_finish_summary = findViewById(R.id.button_finish_summary);
        FileInputStream fileIn= null;
        try {
            fileIn = openFileInput("mytextfile.txt");
            InputStreamReader inputStreamReader= new InputStreamReader(fileIn, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
             while (line != null) {
                    checkBox = new CheckBox(this);
                    checkBox.setText(line);
                    checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String msg = "You have " + (isChecked ? "checked" : "unchecked") + " this Check it Checkbox.";
                            // Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                     // Add Checkbox to LinearLayout
                     if (linearLayout_summary != null) {
                         linearLayout_summary.addView(checkBox);
                       //  view_scroller.addView(checkBox);

                     }

                 Log.i(TAG, line);
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            }catch(Exception e){
                e.printStackTrace();
            } finally {
                String contents = stringBuilder.toString();
               // textView_summary.setText(contents);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        button_finish_summary.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LivePreviewActivity.class);
            startActivity(intent);
        });



    }
}