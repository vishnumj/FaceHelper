package com.vishnumj.facehelper.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.utils.FaceError

class MainActivity : AppCompatActivity(), FaceHelper.RecognitionListener {

    private val PERMISSION_REQUEST_CODE: Int = 10

    override fun onPermissionDenied() {

    }

    override fun onRecognitionInProgress() {
        runOnUiThread {

            Toast.makeText(this@MainActivity, "onRecognitionInProgress()", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onSuccess(mLabel: String?) {
        runOnUiThread {

            Toast.makeText(this@MainActivity, "onSuccess() $mLabel", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFaceDone() {
        runOnUiThread {

            Toast.makeText(this@MainActivity, "onFaceDone()", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFaceMissing() {
        runOnUiThread {

            Toast.makeText(this@MainActivity, "onFaceMissing()", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFailed(mFaceError: FaceError) {
        runOnUiThread {

            Toast.makeText(this@MainActivity, mFaceError.getMessage(), Toast.LENGTH_SHORT).show()
        }
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

    override fun onResume() {
        super.onResume()
        FaceHelper.getInstance().onResume()
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
            }else{
                onPermissionDenied()
            }
        }
    }
}
