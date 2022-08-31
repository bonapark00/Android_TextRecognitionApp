/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.demo.CameraSource;
import com.google.mlkit.vision.demo.CameraSourcePreview;


import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.barcodescanner.BarcodeScannerProcessor;
import com.google.mlkit.vision.demo.java.facedetector.FaceDetectorProcessor;
import com.google.mlkit.vision.demo.java.labeldetector.LabelDetectorProcessor;
import com.google.mlkit.vision.demo.java.objectdetector.ObjectDetectorProcessor;
import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.google.mlkit.vision.demo.java.segmenter.SegmenterProcessor;
import com.google.mlkit.vision.demo.java.textdetector.TextRecognitionProcessor;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.demo.preference.SettingsActivity;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import io.alterac.blurkit.BlurLayout;
import java.util.Timer;
import java.util.TimerTask;

import android.view.animation.Animation;
/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
    implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
  private static final String OBJECT_DETECTION = "Object Detection";
  private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
  private static final String CUSTOM_AUTOML_OBJECT_DETECTION =
      "Custom AutoML Object Detection (Flower)";
  private static final String FACE_DETECTION = "Face Detection";
  private static final String BARCODE_SCANNING = "Barcode Scanning";
  private static final String IMAGE_LABELING = "Image Labeling";
  private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
  private static final String CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)";
  private static final String POSE_DETECTION = "Pose Detection";
  private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
  private static final String TEXT_RECOGNITION_LATIN = "Text Recognition Latin";
  private static final String TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese (Beta)";
  private static final String TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari (Beta)";
  private static final String TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese (Beta)";
  private static final String TEXT_RECOGNITION_KOREAN = "Text Recognition Korean (Beta)";

  private static final String TAG = "LivePreviewActivity";

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = OBJECT_DETECTION;

  //------------------------------------------------------------------------------------------------
  private BlurLayout blurLayout_top, blurLayout_bottom;
  private ImageView icon_record;
  private View layout_setting;
  private TextView textview_minimum_text_size, textview_move_closer_further;
  private Button button_feedback, button_pause, button_summary;
  private VisionProcessorBase current_vision_processor;
  private TextRecognitionProcessor current_TextRecognition_processor;

  // file IO
  private FileOutputStream fileOut;
  private OutputStreamWriter outputStreamWriter;
  private FileInputStream fileIn;
  private InputStreamReader inputStreamReader;


  private Timer tShow = new Timer();
  private Timer tHide = new Timer();
  private AlertDialog helpDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_vision_live_preview);

    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    // Create Blur effect
    blurLayout_top = findViewById(R.id.blurLayout_top);
    blurLayout_bottom = findViewById(R.id.blurLayout_bottom);
    layout_setting = findViewById(R.id.settings_button);
    layout_setting.bringToFront();
    textview_minimum_text_size = findViewById(R.id.minimum_text_size);
    textview_minimum_text_size.bringToFront();

    // Blinking camera image
    icon_record = findViewById(R.id.icon_record);
    icon_record.bringToFront();
    final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
    animation.setDuration(2000); // duration - half a second
    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
    icon_record.startAnimation(animation);

    // Pause button
    button_pause = findViewById(R.id.button_pause);
    button_pause.bringToFront();
    button_pause.setText("Pause");
    button_pause.setOnClickListener(
            v->{ // Pause -> Start: make recording icon visible & stop camera
              if(button_pause.getText()=="Pause"){
                button_pause.setText("Resume");
                icon_record.clearAnimation();
                icon_record.setVisibility(View.INVISIBLE);
                onPause();  // stop camera
              }
              else{ // Start -> Pause
                button_pause.setText("Pause");
                icon_record.startAnimation(animation);
                icon_record.setVisibility(View.VISIBLE);
                onResume(); // resume camera

              }

            }
    );

    // Feedback button
    button_feedback = findViewById(R.id.button_feedback);
    button_feedback.bringToFront();
    button_feedback.setOnClickListener(
            v -> {
              Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class );
              String current_detected_word = current_TextRecognition_processor.getCurrentDetectedWord();
              intent.putExtra("current_detected_word", current_detected_word);
              startActivity(intent);
            }
    );

    // Summary Button
    button_summary = findViewById(R.id.button_summary);
    button_summary.bringToFront();
    button_summary.setOnClickListener(
            v -> {
              Intent intent = new Intent(getApplicationContext(), SummaryActivity.class );
              startActivity(intent);
            }
    );

    // Saving Words
    // add-write text into file
    try {
      fileOut = openFileOutput("mytextfile.txt", MODE_PRIVATE);
      outputStreamWriter =new OutputStreamWriter(fileOut);
      outputStreamWriter.write("");
      outputStreamWriter.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
    saveDetectedText();


    // Draw "Sample text size"
    textview_minimum_text_size.setVisibility(View.INVISIBLE);

    // "Come closer or Come further"
    textview_move_closer_further = findViewById(R.id.move_closer_further);
    popUpComeCloserComeFurther();

    //----------------------------------------------------------------------------------------------
    Spinner spinner = findViewById(R.id.spinner);
    List<String> options = new ArrayList<>();
    options.add(TEXT_RECOGNITION_LATIN);
    options.add(OBJECT_DETECTION);
    options.add(OBJECT_DETECTION_CUSTOM);
    options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
    options.add(FACE_DETECTION);
    options.add(BARCODE_SCANNING);
    options.add(IMAGE_LABELING);
    options.add(IMAGE_LABELING_CUSTOM);
    options.add(CUSTOM_AUTOML_LABELING);
    options.add(POSE_DETECTION);
    options.add(SELFIE_SEGMENTATION);
    options.add(TEXT_RECOGNITION_CHINESE);
    options.add(TEXT_RECOGNITION_DEVANAGARI);
    options.add(TEXT_RECOGNITION_JAPANESE);
    options.add(TEXT_RECOGNITION_KOREAN);

    // Creating adapter for spinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    spinner.setAdapter(dataAdapter);
    spinner.setOnItemSelectedListener(this);

    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    ImageView settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
          intent.putExtra(
              SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
          startActivity(intent);
        });

    createCameraSource(selectedModel);
  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedModel = parent.getItemAtPosition(pos).toString();
    Log.d(TAG, "Selected model: " + selectedModel);
    preview.stop();
    createCameraSource(selectedModel);
    startCameraSource();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    try {
      switch (model) {
        case OBJECT_DETECTION:
          Log.i(TAG, "Using Object Detector Processor");
          ObjectDetectorOptions objectDetectorOptions =
              PreferenceUtils.getObjectDetectorOptionsForLivePreview(this);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, objectDetectorOptions));
          break;
        case OBJECT_DETECTION_CUSTOM:
          Log.i(TAG, "Using Custom Object Detector Processor");
          LocalModel localModel =
              new LocalModel.Builder()
                  .setAssetFilePath("custom_models/object_labeler.tflite")
                  .build();
          CustomObjectDetectorOptions customObjectDetectorOptions =
              PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, customObjectDetectorOptions));
          break;
        case CUSTOM_AUTOML_OBJECT_DETECTION:
          Log.i(TAG, "Using Custom AutoML Object Detector Processor");
          LocalModel customAutoMLODTLocalModel =
              new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
          CustomObjectDetectorOptions customAutoMLODTOptions =
              PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(
                  this, customAutoMLODTLocalModel);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, customAutoMLODTOptions));
          break;



        case TEXT_RECOGNITION_LATIN:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin.");
          //------------------------------------------------------------------------------
          current_TextRecognition_processor = new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build());
          cameraSource.setMachineLearningFrameProcessor(current_TextRecognition_processor);
          break;




        case TEXT_RECOGNITION_CHINESE:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Chinese.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new ChineseTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_DEVANAGARI:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Devanagari.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new DevanagariTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_JAPANESE:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Japanese.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new JapaneseTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_KOREAN:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Korean.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new KoreanTextRecognizerOptions.Builder().build()));
          break;
        case FACE_DETECTION:
          Log.i(TAG, "Using Face Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));
          break;
        case BARCODE_SCANNING:
          Log.i(TAG, "Using Barcode Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(new BarcodeScannerProcessor(this));
          break;
        case IMAGE_LABELING:
          Log.i(TAG, "Using Image Label Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, ImageLabelerOptions.DEFAULT_OPTIONS));
          break;
        case IMAGE_LABELING_CUSTOM:
          Log.i(TAG, "Using Custom Image Label Detector Processor");
          LocalModel localClassifier =
              new LocalModel.Builder()
                  .setAssetFilePath("custom_models/bird_classifier.tflite")
                  .build();
          CustomImageLabelerOptions customImageLabelerOptions =
              new CustomImageLabelerOptions.Builder(localClassifier).build();
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, customImageLabelerOptions));
          break;
        case CUSTOM_AUTOML_LABELING:
          Log.i(TAG, "Using Custom AutoML Image Label Detector Processor");
          LocalModel customAutoMLLabelLocalModel =
              new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
          CustomImageLabelerOptions customAutoMLLabelOptions =
              new CustomImageLabelerOptions.Builder(customAutoMLLabelLocalModel)
                  .setConfidenceThreshold(0)
                  .build();
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, customAutoMLLabelOptions));
          break;
        case POSE_DETECTION:
          PoseDetectorOptionsBase poseDetectorOptions =
              PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
          Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
          boolean shouldShowInFrameLikelihood =
              PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
          cameraSource.setMachineLearningFrameProcessor(
              new PoseDetectorProcessor(
                  this,
                  poseDetectorOptions,
                  shouldShowInFrameLikelihood,
                  visualizeZ,
                  rescaleZ,
                  runClassification,
                  /* isStreamMode = */ true));
          break;
        case SELFIE_SEGMENTATION:
          cameraSource.setMachineLearningFrameProcessor(new SegmenterProcessor(this));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    // start blur
    blurLayout_top.startBlur();
    blurLayout_bottom.startBlur();


    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }

        preview.start(cameraSource, graphicOverlay);


      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();

    preview.stop();
    blurLayout_top.pauseBlur();
    blurLayout_bottom.pauseBlur();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
      try {
        outputStreamWriter.close();
        inputStreamReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  //---------------------------------------------------------
  private String findCurrentDetectedWord(){
      if(current_TextRecognition_processor != null){
        String current_detected_word = current_TextRecognition_processor.getCurrentDetectedWord();
        Log.i(TAG, "LIVE PREVIEW ACTIVITY: ==>> " + current_detected_word);
        return current_detected_word;
      }else{
        return "";
      }
  }

  private void saveDetectedText(){

      Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
            // find current detected word
            String current_detected_word = findCurrentDetectedWord();
            if(current_detected_word != null){
              current_detected_word.replaceAll(" ", "");
              String previous_word = "";
              try{
                // read word from file
                String sCurrentline;
                String all_words="";
                // StringBuilder stringBuilder = new StringBuilder();
                FileInputStream fileIn=openFileInput("mytextfile.txt");
                InputStreamReader inputStreamReader= new InputStreamReader(fileIn);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                while((sCurrentline = reader.readLine()) != null){
                  all_words = all_words + sCurrentline + "\n" ;
                  previous_word = sCurrentline;
                }
                Log.i(TAG, all_words.replaceAll("\n", "+"));
                inputStreamReader.close();
                Log.i(TAG, "Previous word: ==>> +" + previous_word + "+\n" +
                        "Current word:  -->> *" + current_detected_word + "*");
                if(!(previous_word.equalsIgnoreCase(current_detected_word)) && !(current_detected_word.equals("")) ){
                  // write text into file
                  FileOutputStream fileout = openFileOutput("mytextfile.txt", MODE_PRIVATE);
                  OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileout);
                  outputStreamWriter.write(all_words + current_detected_word + "\n");

                  outputStreamWriter.close();
                }
              } catch (Exception e){
                e.printStackTrace();
              }
            }

        }
      }, 0, 1000); // every second

  }

  private void popUpComeCloserComeFurther(){
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        if(findCurrentDetectedWord() != null){
          // find current word's height
          Float height_detected_word = current_TextRecognition_processor.getCurrentDetectedRectHeight();

          if(height_detected_word <= 100){
            textview_move_closer_further.setText("Please move closer");
          }
          else if(100 < height_detected_word && height_detected_word <= 200){
            textview_move_closer_further.setText("Great! Please continue with this distance.");
          }
          else{
            textview_move_closer_further.setText("Please move further.");
          }
        }
      }
    }, 0, 1000); // every second
  }

}
