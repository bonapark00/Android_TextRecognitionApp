package com.google.mlkit.vision.demo.java;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import java.lang.Math;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.exifinterface.media.ExifInterface;
// ContentResolver dependency
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutioncore.VideoInput;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.demo.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import android.os.AsyncTask;
import android.widget.Toast;

public class HandTestingActivity extends AppCompatActivity {

    private static final String TAG = "HandTestingActivity";

    private Hands hands;
    // Run the pipeline and the model inference on GPU or CPU.
    private static boolean RUN_ON_GPU = true; // was 'final'


    private enum InputSource {
        UNKNOWN,
        IMAGE,
        VIDEO,
        CAMERA,
    }

    private InputSource inputSource = InputSource.UNKNOWN;

    // Image demo UI and image loader components.
    private ActivityResultLauncher<Intent> imageGetter;
    private HandsResultImageView imageView;
    // Video demo UI and video loader components.
    private VideoInput videoInput;
    private ActivityResultLauncher<Intent> videoGetter;
    // Live camera demo UI and camera components.
    private CameraInput cameraInput;

    private TextView textView4, textView5;
    private ImageView imageView_pointer;
    private SolutionGlSurfaceView<HandsResult> glSurfaceView;

    private Button button_next_page, button_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_testing); //---
        setupLiveDemoUiComponents();
        textView4 = findViewById(R.id.textview4);
        textView5 = findViewById(R.id.textview5);
        imageView_pointer = findViewById(R.id.imageView_pointer);
        button_start = findViewById(R.id.button_start_camera);
        button_next_page = findViewById(R.id.button_next_page);

        textView4.setVisibility(View.INVISIBLE);
        textView5.setVisibility(View.INVISIBLE);
        imageView_pointer.setVisibility(View.INVISIBLE);
        button_start.setVisibility(View.INVISIBLE);
        button_next_page.setVisibility(View.INVISIBLE);

        // show first instruction
        // Mission Complete Message after 7 seconds
        new CountDownTimer(1 * 500, 1000) {
            public void onTick(long millisUntilFinished) {
                return;
            }
            public void onFinish() {
                textView4.setVisibility(View.VISIBLE);
            }
        }.start();
        // show second instruction
        new CountDownTimer(2 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                return;
            }
            public void onFinish() {
                textView5.setVisibility(View.VISIBLE);
            }
        }.start();
        new CountDownTimer(4 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                return;
            }
            public void onFinish() {
                imageView_pointer.setVisibility(View.VISIBLE);
                button_start.setVisibility(View.VISIBLE);
                button_next_page.setVisibility(View.VISIBLE);
            }
        }.start();




        button_next_page = findViewById(R.id.button_next_page);
        button_next_page.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), LivePreviewActivity.class);
            startActivity(intent);
        });
        Context context = getApplicationContext();



    }

    @Override
    protected void onResume() {
    super.onResume();
    if (inputSource == InputSource.CAMERA) {
        // Restarts the camera and the opengl surface rendering.
        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
        glSurfaceView.post(this::startCamera);
        glSurfaceView.setVisibility(View.VISIBLE);
    } else if (inputSource == InputSource.VIDEO) {
        videoInput.resume();
    }
}

    @Override
    protected void onPause() {
    super.onPause();
    if (inputSource == InputSource.CAMERA) {
        glSurfaceView.setVisibility(View.GONE);
        cameraInput.close();
    } else if (inputSource == InputSource.VIDEO) {
        videoInput.pause();
    }
}

    //-------------------------------------------------------------------------------------------------


    /**
     * Sets up the UI components for the live demo with camera input.
     */
    private void setupLiveDemoUiComponents() {
    // mSelectedImage =
    Button startCameraButton = findViewById(R.id.button_start_camera);
    startCameraButton.setOnClickListener(
            v -> {
                if (inputSource == InputSource.CAMERA) { // static mode
                    // runTextRecognition();
                    return;
                }
                stopCurrentPipeline();

                ImageView img_animation = (ImageView) findViewById(R.id.imageView_pointer);

                TranslateAnimation animation = new TranslateAnimation(0.0f, 800.0f,
                        0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
                animation.setDuration(4000);  // animation duration
                animation.setRepeatCount(5);  // animation repeat count
                animation.setRepeatMode(2);   // repeat animation (left to right, right to left )
                //animation.setFillAfter(true);

                img_animation.startAnimation(animation);  // start animation


                // Mission Complete Message after 7 seconds
                new CountDownTimer(7 * 1000+1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        int seconds = (int) (millisUntilFinished / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                    }
                    public void onFinish() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Hand recognizing Finished!", Toast.LENGTH_SHORT);
                        toast.show();
                        img_animation.clearAnimation();
                    }
                }.start();

                setupStreamingModePipeline(InputSource.CAMERA);
            });
}

    /**
     * Sets up core workflow for streaming mode.
     */
    private void setupStreamingModePipeline(InputSource inputSource) {
    this.inputSource = inputSource;
    // Initializes a new MediaPipe Hands solution instance in the streaming mode.
    hands =
            new Hands(
                    this,
                    HandsOptions.builder()
                            .setStaticImageMode(false)
                            .setMaxNumHands(2)
                            .setRunOnGpu(RUN_ON_GPU)
                            .build());
    hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));

    if (inputSource == InputSource.CAMERA) {
        cameraInput = new CameraInput(this);
        cameraInput.setNewFrameListener(
                textureFrame -> {
                    hands.send(textureFrame); //---
                }
        );
    } else if (inputSource == InputSource.VIDEO) {
        videoInput = new VideoInput(this);
        videoInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
    }

    // Initializes a new Gl surface view with a user-defined HandsResultGlRenderer.
    glSurfaceView =
            new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());
    glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
    glSurfaceView.setRenderInputImage(true);
    hands.setResultListener(
            handsResult -> {
                try{
                    glSurfaceView.setRenderData(handsResult);
                    glSurfaceView.requestRender();
                }catch(Exception e){
                    Log.e(TAG, "Error after hand setResultListener==> " + e);
                }

            });

    // The runnable to start camera after the gl surface view is attached.
    // For video input source, videoInput.start() will be called when the video uri is available.
    if (inputSource == InputSource.CAMERA) {
        glSurfaceView.post(this::startCamera);
    }

    // Updates the preview layout.
    FrameLayout frameLayout = findViewById(R.id.preview_display_layout); //--------
//    imageView.setVisibility(View.GONE);
    frameLayout.removeAllViewsInLayout();
    frameLayout.addView(glSurfaceView);
    glSurfaceView.setVisibility(View.VISIBLE);
    frameLayout.requestLayout();
}

    private void startCamera() {
    cameraInput.start(
            this,
            hands.getGlContext(), //
            CameraInput.CameraFacing.BACK,
            glSurfaceView.getWidth(),
            glSurfaceView.getHeight());
}

    private void stopCurrentPipeline() {
    if (cameraInput != null) {
        cameraInput.setNewFrameListener(null);
        cameraInput.close();
    }
    if (videoInput != null) {
        videoInput.setNewFrameListener(null);
        videoInput.close();
    }
    if (glSurfaceView != null) {
        glSurfaceView.setVisibility(View.GONE);
    }
    if (hands != null) { //
        hands.close(); //
    }
    }
//-------------------------------------------------------------------------------------------
// Timer
//start timer function


}








