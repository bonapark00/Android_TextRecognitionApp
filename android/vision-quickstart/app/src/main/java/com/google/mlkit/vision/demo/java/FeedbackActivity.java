package com.google.mlkit.vision.demo.java;

import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.demo.R;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity {

    private String current_detected_word;
    private Button button_feedback_0,button_feedback_1,button_feedback_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);

        current_detected_word = getIntent().getStringExtra("current_detected_word");

        TextView textView_detectedWord = findViewById(R.id.textView_detectedWord);
        textView_detectedWord.setText(current_detected_word);

        Context context = getApplicationContext();
        button_feedback_0 =findViewById(R.id.button_feedback_0);
        button_feedback_1 =findViewById(R.id.button_feedback_1);
        button_feedback_2 =findViewById(R.id.button_feedback_2);
        button_feedback_0.setOnClickListener(v->{
            CharSequence text = "Thanks for your feedback!";
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 600);
            toast.show();
            finish();
        });
        button_feedback_1.setOnClickListener(v->{
            CharSequence text = "Thanks for your feedback!";
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 600);

            toast.show();
            finish();

        });
        button_feedback_2.setOnClickListener(v->{
            CharSequence text = "Thanks for your feedback!";
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 600);

            toast.show();
            finish();

        });
    }
}