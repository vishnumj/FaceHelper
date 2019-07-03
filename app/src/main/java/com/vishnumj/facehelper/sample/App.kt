package com.vishnumj.facehelper.sample

import android.app.Application
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.utils.constants.Constants

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val mRecogniserConfig: FaceHelper.() -> Unit = {
            isFaceTrackerGraphicEnabled = true
            mContext = this@App
            mMaxRetryCount = Constants.Settings.FACE_DETECTION_RETRY_COUNT
        }
        FaceHelper.Builder().setInstance(mRecogniserConfig).build()
    }
}