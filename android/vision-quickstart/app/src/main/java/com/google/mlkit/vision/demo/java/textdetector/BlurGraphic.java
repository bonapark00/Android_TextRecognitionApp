package com.google.mlkit.vision.demo.java.textdetector;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RenderEffect;


import android.graphics.RectF;
import android.util.Log;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import java.util.Arrays;
import android.graphics.Bitmap;
// import android.support.v8.renderscript;
import android.content.Context;

public class BlurGraphic extends Graphic {

    private static final int TEXT_COLOR = Color.BLACK;
    private static final int MARKER_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;
    private final Bitmap originalCameraImage;

    private final Paint circlePaint;

    private float img_width;
    private float img_height;
    BlurGraphic(GraphicOverlay overlay, Bitmap originalCameraImage){
        super(overlay); // reusing parent constructor
        this.originalCameraImage = originalCameraImage;
        circlePaint = new Paint();

        img_width = translateX(originalCameraImage.getWidth());
        img_height = translateY(originalCameraImage.getHeight());

        circlePaint.setColor(MARKER_COLOR);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(STROKE_WIDTH);

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();

    }

    @Override
    public void draw(Canvas canvas){
        canvas.drawCircle(img_width/2, img_height/2, img_width/2 - 20, circlePaint);




    }

}



