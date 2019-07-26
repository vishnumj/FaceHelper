package com.vishnumj.facehelper.sample

import android.app.Application
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.utils.constants.Constants

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FaceHelper.Builder(this).setMaxRetryCount(Constants.Settings.FACE_DETECTION_RETRY_COUNT)
            .setMinimumValidFrames(Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES)
            .setCacheDirectory(cacheDir).build()
    }
}