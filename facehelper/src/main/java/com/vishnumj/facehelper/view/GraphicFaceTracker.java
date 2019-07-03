package com.vishnumj.facehelper.view;

import android.util.Log;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.vishnumj.facehelper.view.camera.FaceGraphic;
import com.vishnumj.facehelper.view.camera.GraphicOverlay;


public class GraphicFaceTracker extends Tracker<Face> {

    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private long onnewItemMilliSecond;
    private OnFaceDetectionListener mOnFaceDetectionListener;
    private boolean isFaceDetected;

    public GraphicFaceTracker(GraphicOverlay overlay, OnFaceDetectionListener mOnFaceDetectionListener) {
        this.mOverlay = overlay;
        this.mOnFaceDetectionListener = mOnFaceDetectionListener;
        this.mFaceGraphic = new FaceGraphic(overlay);
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face face) {
        Log.e("new", "id");
        mFaceGraphic.setId(faceId);
        if (!isFaceDetected) {
            onnewItemMilliSecond = System.currentTimeMillis();
        } else {
            onnewItemMilliSecond = 0;
        }
        isFaceDetected = false;
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(final FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mFaceGraphic);
        mFaceGraphic.updateFace(face);
        long onUpdateMilliSecond = System.currentTimeMillis();
        int faceCount = detectionResults.getDetectedItems().size();     //Log.d("OnUpdate","faceCount = "+faceCount+" ");
        if (faceCount > 0 && !isFaceDetected) {
            Log.e("training", "3");
            Log.d("FACE_STABLE_TIME", (onUpdateMilliSecond - onnewItemMilliSecond) + " ");
            long FACE_STABLE_TIME = 1500;
            if ((onUpdateMilliSecond - onnewItemMilliSecond) > FACE_STABLE_TIME) {
                isFaceDetected = true;
                mOnFaceDetectionListener.onFaceDetected();
            } else {
                Log.d("onUpdate", "skipped");
            }
        }
    }


    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        Log.e("on", "missing");
        mOverlay.remove(mFaceGraphic);
        mOnFaceDetectionListener.onFaceMissing();

    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mFaceGraphic);
        mOnFaceDetectionListener.onFaceDone();
    }

    public void reset() {
        isFaceDetected = false;
        onnewItemMilliSecond = System.currentTimeMillis();
    }

    public interface OnFaceDetectionListener {
        void onFaceDetected();

        void onFaceDone();

        void onFaceMissing();
    }
}