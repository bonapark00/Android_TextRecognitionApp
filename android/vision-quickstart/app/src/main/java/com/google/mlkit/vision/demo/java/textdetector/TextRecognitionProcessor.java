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

package com.google.mlkit.vision.demo.java.textdetector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.FeedbackActivity;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import androidx.appcompat.app.AppCompatActivity;


/** Processor for the text detector demo. */
public class TextRecognitionProcessor extends VisionProcessorBase<Text> {

  private static final String TAG = "TextRecProcessor";

  private final TextRecognizer textRecognizer;
  private final Boolean shouldGroupRecognizedTextInBlocks;
  private final Boolean showLanguageTag;

  //---------------------------------
  public Text texts;
  private String str_detected_word;

  public HandsOptions handsOptions =
          HandsOptions.builder()
                  .setStaticImageMode(true)
                  .setMaxNumHands(2)
                  .setRunOnGpu(true).build();
  public Hands hands;

  public float x_index_tip = -100;
  public float y_index_tip = -100;
  private TextView foundWord;

  private Button button_feedback;

  //---------------------------------

  public TextRecognitionProcessor(
      Context context, TextRecognizerOptionsInterface textRecognizerOptions) {
    super(context);
    shouldGroupRecognizedTextInBlocks = PreferenceUtils.shouldGroupRecognizedTextInBlocks(context);
    showLanguageTag = PreferenceUtils.showLanguageTag(context);
    textRecognizer = TextRecognition.getClient(textRecognizerOptions);

    hands = new Hands(context, handsOptions);
    hands.setResultListener(
            handsResult -> {
              if (handsResult.multiHandLandmarks().isEmpty()) {
                Log.e(TAG, "handsResult empty");



                return;
              }
              int width = handsResult.inputBitmap().getWidth();
              int height = handsResult.inputBitmap().getHeight();

              float[] coordinate_index_tip;
              coordinate_index_tip = getIndexTipLandmark(handsResult, true);
              x_index_tip = coordinate_index_tip[0];
              y_index_tip = coordinate_index_tip[1];
            });
    hands.setErrorListener(
            (message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));





  }

  @Override
  public void stop() {
    super.stop();
    textRecognizer.close();
  }


  @Override
  protected Task<Text> detectInImage(InputImage image) {
    hands.send(originalCameraImage);
    return textRecognizer.process(image);
  }

  @Override
  protected void onSuccess(@NonNull Text texts, @NonNull GraphicOverlay graphicOverlay) {
    Log.d(TAG, "On-device Text detection successful");


    processTextRecognitionResult(texts, graphicOverlay);


    logExtrasForTesting(texts);
    // graphicOverlay.add(
    //     new TextGraphic(graphicOverlay, texts, shouldGroupRecognizedTextInBlocks, showLanguageTag));
  }

  private static void logExtrasForTesting(Text texts) {
    if (texts != null) {
      Log.v(MANUAL_TESTING_LOG, "Detected text has : " + texts.getTextBlocks().size() + " blocks");
      for (int i = 0; i < texts.getTextBlocks().size(); ++i) {
        List<Line> lines = texts.getTextBlocks().get(i).getLines();
        Log.v(
            MANUAL_TESTING_LOG,
            String.format("Detected text block %d has %d lines", i, lines.size()));
        for (int j = 0; j < lines.size(); ++j) {
          List<Element> elements = lines.get(j).getElements();
          Log.v(
              MANUAL_TESTING_LOG,
              String.format("Detected text line %d has %d elements", j, elements.size()));
          for (int k = 0; k < elements.size(); ++k) {
            Element element = elements.get(k);
            Log.v(
                MANUAL_TESTING_LOG,
                String.format("Detected text element %d says: %s", k, element.getText()));
            Log.v(
                MANUAL_TESTING_LOG,
                String.format(
                    "Detected text element %d has a bounding box: %s",
                    k, element.getBoundingBox().flattenToString()));
            Log.v(
                MANUAL_TESTING_LOG,
                String.format(
                    "Expected corner point size is 4, get %d", element.getCornerPoints().length));
            for (Point point : element.getCornerPoints()) {
              Log.v(
                  MANUAL_TESTING_LOG,
                  String.format(
                      "Corner point for element %d is located at: x - %d, y = %d",
                      k, point.x, point.y));
            }
          }
        }
      }
    }
  }

  private void processTextRecognitionResult(Text texts, @NonNull GraphicOverlay graphicOverlay) {
    List<Text.TextBlock> blocks = texts.getTextBlocks();

    if (blocks.size() == 0) {
      Log.e(TAG, "No text found");
      // showToast("No text found");
      return;
    }

    if(x_index_tip == -100 || y_index_tip == -100){
      Log.e(TAG, "No hand found");
      return;
    }


    List<Double> distances = new ArrayList<>();
    List<List<Integer>> indexes = new ArrayList<>();
    for (int i = 0; i < blocks.size(); i++) {
      List<Text.Line> lines = blocks.get(i).getLines();
      for (int j = 0; j < lines.size(); j++) {
        List<Text.Element> elements = lines.get(j).getElements();
        for (int k = 0; k < elements.size(); k++) {
          Text.Element element = elements.get(k);
          Rect rect = element.getBoundingBox();
          float x_rect_center = rect.exactCenterX();
          float y_rect_center = rect.exactCenterY();

          if (y_rect_center <= y_index_tip){ // only care about the words above the finger
            double dist = Math.pow(x_index_tip-x_rect_center, 2) + Math.pow(y_index_tip-y_rect_center, 2);
            distances.add(dist);
            List<Integer> index = Arrays.asList(i, j, k);
            indexes.add(index);
            Log.i(TAG, String.format(
                    "\'Distance\': %f, x_index_tip: %f, x_rect_center: %f, y_index_tip: %f, y_rect_center: %f", dist, x_index_tip, x_rect_center, y_index_tip, y_rect_center
            ));
            Log.i(TAG, String.format(
                    "Here word: %s", texts.getTextBlocks().get(i).getLines().get(j).getElements().get(k).getText()
            ));
          }
        }
      }
    }

    if(! distances.isEmpty() ){

      // "distances" stores all distances between the bounding box and the index tip
      int minIndex = distances.indexOf(Collections.min(distances));
      int i = indexes.get(minIndex).get(0);
      int j = indexes.get(minIndex).get(1);
      int k = indexes.get(minIndex).get(2);
      Text.Element elem_targetWord = texts.getTextBlocks().get(i).getLines().get(j).getElements().get(k);
      String str_targetWord = elem_targetWord.getText();
      str_detected_word = str_targetWord;
      Log.i(TAG, "Here FOUND WORD=====>>>>>>" + str_targetWord);

      //foundWord = (TextView) findViewById(R.id.found_word);
      //foundWord.setText(str_targetWord);

      graphicOverlay.add(
              new TextGraphic(graphicOverlay, elem_targetWord, shouldGroupRecognizedTextInBlocks, showLanguageTag));


    }
  }


  private float[] getIndexTipLandmark(HandsResult result, boolean showPixelValues) {
    if (result.multiHandLandmarks().isEmpty()) {
      return null;
    }
    NormalizedLandmark indexTipLandmark =
            result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.INDEX_FINGER_TIP);
    // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
    if (showPixelValues) {
      int width = result.inputBitmap().getWidth();
      int height = result.inputBitmap().getHeight();

      // MediaPipe Hand index tip coordinates (pixel values)
      float[] coordinate = new float[2];
      coordinate[0] = indexTipLandmark.getX() * width;
      coordinate[1] = indexTipLandmark.getY() * height;
      Log.i(TAG, String.format("Inside getIndexTipLandmark's pixel values of  x_y index tip: ",coordinate[0], coordinate[1]));
      return coordinate;

    } else {
      // ratio
      float[] coordinate = new float[2];
      coordinate[0] = indexTipLandmark.getX();
      coordinate[1] = indexTipLandmark.getY();
      Log.i(TAG, String.format("Inside  x_y index tip: %f, %f",coordinate[0], coordinate[1]));
      return coordinate;
    }

  }

  public String getCurrentDetectedWord(){
    return str_detected_word;
  }





  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Text detection failed." + e);
  }
}
