package com.vishnumj.facehelper.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.vishnumj.facehelper.utils.VideoFileUtils
import com.vishnumj.facehelper.utils.constants.Constants
import kotlinx.android.synthetic.main.activity_training.*
import kotlinx.android.synthetic.main.layout_count_down_timer.*
import kotlinx.android.synthetic.main.layout_progress_circle.*

class TrainingActivity : AppCompatActivity() {


    private var isVideoCapturing: Boolean = false
    private lateinit var mViewModel: FaceRegistrationViewModel

    companion object {
        fun getStartIntent(mActivity: AppCompatActivity): Intent {
            return Intent(mActivity, TrainingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        mViewModel = ViewModelProviders.of(this).get(FaceRegistrationViewModel::class.java)
        mViewModel.initPreCountDownTimer()

        mViewModel.mPreElapsedTimeLiveData.observe(this, Observer<Long> {
            if (it == Constants.Settings.VIDEO_COUNTDOWN_LIMIT) {
                startVideoRecording()
            }
            updatePreCountDownTimer(it)
        })


        mViewModel.mElapsedTimeLiveData.observe(this, Observer<Long> {
            if (it == Constants.Settings.VIDEO_LENGTH_LIMIT) {
                stopVideoRecording()
            }
            updateElapsedTimer(it)
        })





        progress_circular.setOnIndeterminateModeChangeListener {
            Log.d("setOnIndeterminate", "$it")
        }
    }

    private fun updatePreCountDownTimer(it: Long?) {
        if (it == null || it == Constants.Settings.VIDEO_COUNTDOWN_LIMIT) {
            tv_pre_count_down_timer.visibility = View.GONE
        } else {
            tv_pre_count_down_timer.text = "${it / 1000}"
        }
    }

    private fun updateElapsedTimer(it: Long?) {
        if (it == null || it == Constants.Settings.VIDEO_LENGTH_LIMIT) {
            enableIndeterminateMode()
        } else {
            setProgress((it.toInt() * 100 / Constants.Settings.VIDEO_LENGTH_LIMIT.toInt()).toFloat())
        }
    }

    private fun startVideoRecording() {
        isVideoCapturing = true
        mViewModel.initCountDownTimer()
        progress_circular.visibility = View.VISIBLE
        view_camera_kit_scanner.captureVideo(VideoFileUtils.generateVideoFile(this@TrainingActivity)) {
            if (it.videoFile.exists()) {
                showStatusMessage("Processing")
            }
            isVideoCapturing = false
        }
    }

    private fun stopVideoRecording() {
        view_camera_kit_scanner.stopVideo()
    }

    private fun showStatusMessage(mStatusMessaage: String) {
        if (tv_status_message.visibility == View.GONE) {
            tv_status_message.visibility = View.VISIBLE
        }
        tv_status_message.text = mStatusMessaage
    }

    private fun setProgress(mProgress: Float) {
        runOnUiThread {
            progress_circular.progress = mProgress
        }
    }

    private fun enableIndeterminateMode() {
        runOnUiThread {
            progress_circular.enableIndeterminateMode(true)
        }
    }


}
