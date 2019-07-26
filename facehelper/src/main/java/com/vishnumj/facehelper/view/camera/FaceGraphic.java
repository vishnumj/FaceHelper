/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vishnumj.facehelper.view.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 80.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 15.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = Color.WHITE;

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }


        //   Log.d("Face Graphic",name+"");

        String TAG = "CAMERA";
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        //    Log.e(TAG,"face.getPosition().x = "+face.getPosition().x);
        //    Log.e(TAG,"face.getWidth() = "+face.getWidth());
        //    Log.e(TAG,"x = "+x);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        //    Log.e(TAG,"face.getPosition().x = "+face.getPosition().y);
        //    Log.e(TAG,"face.getHeight() = "+face.getHeight());
        //     Log.e(TAG,"y = "+y);
       /* canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);
*/

        //  Log.e(TAG,"x = "+x+" y = "+y);
        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        //   Log.e(TAG,"xOffset = "+xOffset);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        //     Log.e(TAG,"yOffset = "+yOffset);
        float left = x - xOffset;
        //     Log.e(TAG,"left = "+left);
        float top = y - yOffset;
        //     Log.e(TAG,"top = "+top);
        float right = x + xOffset;
        //   Log.e(TAG,"right = "+right);
        float bottom = y + yOffset;
        //    Log.e(TAG,"bottom = "+bottom);

        /*Paint paint2 = new Paint();
        paint2.setColor(Color.GREEN);
        paint2.setStrokeWidth(BOX_STROKE_WIDTH);
        paint2.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left, top, right, bottom, paint2);*/


        float width = face.getWidth();
        //     Log.e(TAG,"width = "+width);
        float height = face.getHeight();
        //    Log.e(TAG,"height = "+height);
        float average = (width + height) / 2;

        //   Log.e(TAG,"average = "+average);

        canvas.drawPoint(left, top, mBoxPaint);
        canvas.drawLine(left, top, left + (average / 4), top, mBoxPaint);
        canvas.drawLine(right - (average / 4), top, right, top, mBoxPaint);

        canvas.drawPoint(right, top, mBoxPaint);
        canvas.drawLine(left, top, left, top + (average / 4), mBoxPaint);
        canvas.drawLine(right, bottom, right, bottom - (average / 4), mBoxPaint);

        canvas.drawPoint(left, bottom, mBoxPaint);
        canvas.drawLine(left, bottom, left, bottom - (average / 4), mBoxPaint);
        canvas.drawLine(left, bottom, left + (average / 4), bottom, mBoxPaint);

        canvas.drawPoint(right, bottom, mBoxPaint);
        canvas.drawLine(right, top, right, top + (average / 4), mBoxPaint);
        canvas.drawLine(right - (average / 4), bottom, right, bottom, mBoxPaint);

       /* canvas.drawLine(x,y, x+width,y,mBoxPaint);
        canvas.drawLine(x,y, x-width,y,mBoxPaint);
        canvas.drawLine(x,y, x,y+height,mBoxPaint);
        canvas.drawLine(x,y, x,y-height,mBoxPaint);*/

        /* canvas.drawLine(left, bottom, left+(width/4), bottom, mBoxPaint);
        canvas.drawLine(left+((3*width)/4), bottom, left+width, bottom, mBoxPaint);

        canvas.drawLine(left, top, left, top+(height/4), mBoxPaint);
        canvas.drawLine(left, bottom, left, bottom-(height/4), mBoxPaint);

        canvas.drawLine(left+width, bottom, left+width, bottom+(height/4), mBoxPaint);*/

//        canvas.drawLine(left, bottom, left, bottom+(height/4), mBoxPaint);
//        canvas.drawLine(left, bottom+((3*height)/4), left+height, bottom, mBoxPaint);


        //  canvas.drawLine(200, 0, 0, 200, mBoxPaint);
        /*canvas.drawLine(left, top, right, bottom, mBoxPaint);
        canvas.drawLine(right, top, left, bottom, mBoxPaint);
        canvas.drawLine(left, right, left, right, mBoxPaint);*/

        /*Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(BOX_STROKE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(xOffset,top , xOffset,bottom , paint);*/

      /*  Paint paint2 = new Paint();
        paint2.setColor(Color.GREEN);
        paint2.setStrokeWidth(BOX_STROKE_WIDTH);
        canvas.drawRect(left+xOffset, top, right,bottom-xOffset , paint2);*/
    }


}
