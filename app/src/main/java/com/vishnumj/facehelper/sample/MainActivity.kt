package com.vishnumj.facehelper.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.utils.FaceError

class MainActivity : AppCompatActivity(), FaceHelper.RecognitionListener {

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

        FaceHelper.getInstance().createCameraSource(
            findViewById(R.id.overlay_graphic),
            findViewById(R.id.camera_source_preview),
            this
        )

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
}
