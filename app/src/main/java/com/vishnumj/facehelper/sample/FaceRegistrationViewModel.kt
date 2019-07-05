package com.vishnumj.facehelper.sample

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnumj.facehelper.utils.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class FaceRegistrationViewModel : ViewModel() {

    private var mCountDownTimer: CountDownTimer? = null;
    var mPreElapsedTimeLiveData = MutableLiveData<Long>()
    var mElapsedTimeLiveData = MutableLiveData<Long>()

    fun initPreCountDownTimer() {
        startCountDownTimer(Constants.Settings.VIDEO_COUNTDOWN_LIMIT, true)
    }

    private fun startCountDownTimer(mTimer: Long, isPreCounter: Boolean) {
        mCountDownTimer = object : CountDownTimer(mTimer, 1000) {
            override fun onFinish() {
                (if (isPreCounter) mPreElapsedTimeLiveData else mElapsedTimeLiveData).postValue(
                    mTimer
                )
            }

            override fun onTick(millisUntilFinished: Long) {
                (if (isPreCounter) mPreElapsedTimeLiveData else mElapsedTimeLiveData).postValue(
                    if (isPreCounter) {
                        millisUntilFinished
                    } else getInvertedCountDown(millisUntilFinished, mTimer)
                )
            }

        }.start()
    }

    private fun getInvertedCountDown(millisUntilFinished: Long, mTimer: Long): Long {
        return mTimer - millisUntilFinished;
    }

    fun initCountDownTimer() {
        startCountDownTimer(Constants.Settings.VIDEO_LENGTH_LIMIT, false)
    }


    override fun onCleared() {
        super.onCleared()
        mCountDownTimer?.cancel()
    }


}
