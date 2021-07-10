// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package de.lmu.objectdistancedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ResultView extends View {

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 370;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private ArrayList<de.lmu.objectdistancedetector.Result> mResults;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private Map<String, Integer> mapClassHeight;

    // Focal for Galaxy S10+ calculated per callibration process
    private final static double FOCAL = 1142.85;

    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();

        mapClassHeight = new HashMap<String, Integer>();
        mapClassHeight.put("Waste container", 75);
        mapClassHeight.put("Street Light", 250 );
        mapClassHeight.put("Tree", 250);
        mapClassHeight.put("Bench", 83);
        mapClassHeight.put("Fire hydrant", 075);
        mapClassHeight.put("Traffic light",060);
        mapClassHeight.put("Traffic sign", 80);
        mapClassHeight.put("Chair", 86);
        mapClassHeight.put("Bicycle", 80);
        mapClassHeight.put("Table", 76);
        mapClassHeight.put("Ladder", 200);
        mapClassHeight.put("Parkin meter", 121);
        mapClassHeight.put("Flowerpot", 35);
        mapClassHeight.put("Car", 155);
        mapClassHeight.put("Bus", 301);
        mapClassHeight.put("Motorcycle", 109);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mResults == null) return;
        for (de.lmu.objectdistancedetector.Result result : mResults) {
            mPaintRectangle.setStrokeWidth(5);
            mPaintRectangle.setStyle(Paint.Style.STROKE);
            canvas.drawRect(result.rect, mPaintRectangle);

            Path mPath = new Path();
            RectF mRectF = new RectF(result.rect.left, result.rect.top, result.rect.left + TEXT_WIDTH,  result.rect.top + TEXT_HEIGHT);
            mPath.addRect(mRectF, Path.Direction.CW);
            mPaintText.setColor(Color.MAGENTA);
            canvas.drawPath(mPath, mPaintText);

            mPaintText.setColor(Color.WHITE);
            mPaintText.setStrokeWidth(0);
            mPaintText.setStyle(Paint.Style.FILL);
            mPaintText.setTextSize(32);


            double objectHeight = mapClassHeight.get(PrePostProcessor.mClasses[result.classIndex]);
            double distance = (objectHeight * FOCAL) / result.rect.height();

            canvas.drawText(String.format("%s %.2f d:%.1f cm", PrePostProcessor.mClasses[result.classIndex], result.score, distance), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
        }
    }

    public void setResults(ArrayList<de.lmu.objectdistancedetector.Result> results) {
        mResults = results;
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
