package com.vishnumj.facehelper.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.utils.FaceError
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FaceHelper.RecognitionListener {

    private val PERMISSION_REQUEST_CODE: Int = 10

    override fun onPermissionDenied() {

    }

    override fun onRecognitionInProgress() {
        appendToLog("onRecognitionInProgress()\n")
    }

    private fun appendToLog(mLogMessage: String) {
        runOnUiThread {
            tv_status_log.append(mLogMessage)
            sv_status_log.post {
                sv_status_log.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    override fun onSuccess(mLabel: String?) {
        appendToLog("onSuccess() $mLabel\n")


    }

    override fun onFaceDone() {
        appendToLog("onFaceDone()\n")


    }

    override fun onFaceMissing() {
        appendToLog("onFaceMissing()\n")
    }

    override fun onFailed(mFaceError: FaceError) {
        appendToLog("${mFaceError.getMessage()}\n")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FaceHelper.getInstance().createCameraSource(
                findViewById(R.id.overlay_graphic),
                findViewById(R.id.camera_source_preview),
                this
            )
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        FaceHelper.getInstance().onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        FaceHelper.getInstance().onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            FaceHelper.getInstance().onResume()
        }, 1000)
    }

    fun addFace(view: View) {
        startActivity(TrainingActivity.getStartIntent(this@MainActivity))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FaceHelper.getInstance().createCameraSource(
                    findViewById(R.id.overlay_graphic),
                    findViewById(R.id.camera_source_preview),
                    this
                )
            } else {
                onPermissionDenied()
            }
        }
    }
}
