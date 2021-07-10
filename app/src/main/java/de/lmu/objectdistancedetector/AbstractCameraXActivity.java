// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package de.lmu.objectdistancedetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

public abstract class AbstractCameraXActivity<R> extends de.lmu.objectdistancedetector.BaseModuleActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

    private static final int VOICE_OUTPUT_INTERVAL = 5000;

    private long mLastAnalysisResultTime;
    private long mLastOutputTime;

    protected abstract int getContentViewLayoutId();

    protected abstract TextureView getCameraPreviewTextureView();

    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutId());

        startBackgroundThread();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            setupCameraX();
        }

        mTTS=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                }
            }
        });

        mLastOutputTime = SystemClock.elapsedRealtime();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this,
                    "You can't use object detection example without granting CAMERA permission",
                    Toast.LENGTH_LONG)
                    .show();
                finish();
            } else {
                setupCameraX();
            }
        }
    }

    private void setupCameraX() {
        final AutoFitTextureView textureView = (AutoFitTextureView) getCameraPreviewTextureView();
        textureView.setAspectRatio(3, 4);
        final PreviewConfig previewConfig = new PreviewConfig.Builder().build(); //.setTargetAspectRatio(new Rational(9, 16))

        final Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(output -> textureView.setSurfaceTexture(output.getSurfaceTexture()));

        final ImageAnalysisConfig imageAnalysisConfig =
            new ImageAnalysisConfig.Builder()
                .setTargetResolution(new Size(480, 640))
                .setCallbackHandler(mBackgroundHandler)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        final ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer((image, rotationDegrees) -> {
            if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
                return;
            }

            final R result = analyzeImage(image, rotationDegrees);
            if (result != null) {
                mLastAnalysisResultTime = SystemClock.elapsedRealtime();
                runOnUiThread(() -> applyToUiAnalyzeImageResult(result));

                //tts test
                ObjectDetectionActivity.AnalysisResult analysisResult = (ObjectDetectionActivity.AnalysisResult) result;
                ArrayList<Result> mResults = analysisResult.getResults();
                Result nearby = null;
                if (mResults.size() >= 1) {
                    nearby = mResults.get(0);
                    for (Result res : mResults) {
                        if (res.dist < nearby.dist) {
                            nearby = res;
                        }
                    }
                }
                
                if (SystemClock.elapsedRealtime() - mLastOutputTime > VOICE_OUTPUT_INTERVAL && nearby != null) {
                    mLastOutputTime = SystemClock.elapsedRealtime();
                    int distCm = (int) Math.round(nearby.dist);
                    String outputText = PrePostProcessor.mClasses[nearby.classIndex] + " at " + distCm + " centimeter";
                    Toast.makeText(getApplicationContext(), outputText, Toast.LENGTH_SHORT).show();
                    mTTS.speak(outputText, TextToSpeech.QUEUE_FLUSH, null);
                }
                
            }
        });

        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    @WorkerThread
    @Nullable
    protected abstract R analyzeImage(ImageProxy image, int rotationDegrees);

    @UiThread
    protected abstract void applyToUiAnalyzeImageResult(R result);
}
