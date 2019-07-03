package com.vishnumj.facehelper.utils;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.vishnumj.facehelper.view.GraphicFaceTracker;
import com.vishnumj.facehelper.view.camera.GraphicOverlay;

public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

    private GraphicOverlay mGraphicOverlay;
    private GraphicFaceTracker.OnFaceDetectionListener mOnFaceDetectionListener;
    private GraphicFaceTracker mGraphicFaceTracker;

    public GraphicFaceTrackerFactory(GraphicOverlay mGraphicOverlay, GraphicFaceTracker.OnFaceDetectionListener mOnFaceDetectionListener) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mOnFaceDetectionListener = mOnFaceDetectionListener;
        this.mGraphicFaceTracker = new GraphicFaceTracker(mGraphicOverlay, mOnFaceDetectionListener);
    }

    @Override
    public Tracker<Face> create(Face face) {
        return mGraphicFaceTracker;
    }

    public void resetFaceTracker() {
        mGraphicFaceTracker.reset();
    }
}

