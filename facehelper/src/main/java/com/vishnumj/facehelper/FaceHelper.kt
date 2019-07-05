package com.vishnumj.facehelper

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.vishnumj.facehelper.utils.*
import com.vishnumj.facehelper.utils.constants.Constants
import com.vishnumj.facehelper.view.GraphicFaceTracker
import com.vishnumj.facehelper.view.camera.CameraSourcePreview
import com.vishnumj.facehelper.view.camera.Exif
import com.vishnumj.facehelper.view.camera.GraphicOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class FaceHelper() : GraphicFaceTracker.OnFaceDetectionListener, CoroutineScope {

    data class Builder(var mApplicationContext: Application? = null) {
        private var mCacheDirectory: File? = null
        private var mMaxRetryCount: Int? = 0
        private var mMinimumValidFrames: Int? = 0

        fun build(): FaceHelper {
            return init(mCacheDirectory, mMaxRetryCount, mMinimumValidFrames)
        }

        fun setCacheDirectory(mCacheDirectory: File) = apply {
            this.mCacheDirectory = mCacheDirectory
        }

        fun setMaxRetryCount(mCount: Int) = apply {
            this.mMaxRetryCount = mCount
        }

        fun setMinimumValidFrames(mMinimumValidFrames: Int) = apply {
            this.mMinimumValidFrames = mMinimumValidFrames
        }

    }


    private var mAttemptCount = 0
    var mMaxRetryCount = Constants.Settings.FACE_DETECTION_RETRY_COUNT
    private var mCameraSourcePreview: CameraSourcePreview? = null
    private var mOverLay: GraphicOverlay? = null

    private var mCoroutineJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + mCoroutineJob

    override fun onFaceDetected() {
        mCameraSource?.takePicture(null, {
            if (!isFaceRecognitionInProgress) {
                mRecognitionListener.onRecognitionInProgress()
                Log.e("Byte Size", "" + it.size)
                val file = File(FileHelper.SVM_PATH + "labelMap_train")
                if (file.exists()) {
                    isFaceRecognitionInProgress = true
                    launch {
                        val mResult = RecognitionUtils.initRecognition(
                            RecognitionUtils.RecognitionRequest(rotateBitmap(it)),
                            0
                        )
                        isFaceRecognitionInProgress = false
                        if (mResult.mResult == RecognitionUtils.Result.SUCCESS) {
                            mRecognitionListener.onSuccess(mResult.mResultLabel)
                        } else {
                            retryRecogniser()
                        }
                    }
                } else {
                    isFaceRecognitionInProgress = false
                    mRecognitionListener.onFailed(EmptyFaceDataError())
                }
            }
        })
    }

    private fun retryRecogniser() {
        if (mAttemptCount >= mMaxRetryCount) {
            mRecognitionListener.onFailed(InvalidFaceError())
        } else {
            mAttemptCount++
            onFaceDetected()
        }
    }

    override fun onFaceDone() {
        mRecognitionListener.onFaceDone()
    }

    override fun onFaceMissing() {
        mRecognitionListener.onFaceMissing()
    }

    private lateinit var mRecognitionListener: RecognitionListener
    private var isFaceRecognitionInProgress = false

    private var mCameraSource: CameraSource? = null

    /**
     * Show face graphic around the face in camera preview
     */
    var isFaceTrackerGraphicEnabled: Boolean = true

    /**
     * Cache directory for storing models and trained data
     */
    var mCacheDirectory: File? = null


    /**
     * Minimum valid frames in from a training video
     */
    var mMinimumValidFrames = Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES


    public lateinit var mContext: Application

    fun getContext(): Application {
        return mContext
    }

    companion object {

        private var mInstance: FaceHelper? = null

        private fun
                init(
            mCacheDirectory: File?,
            mMaxRetryCount: Int?,
            mMinimumValidFrames: Int?
        ): FaceHelper {
            this.mInstance = FaceHelper()
            if (mCacheDirectory == null) {
                mInstance?.mCacheDirectory = mInstance?.getContext()?.cacheDir
            }

            FileHelper.FOLDER_PATH = "${mInstance?.mCacheDirectory}/facerecognition"
            FileHelper.initDirectory()

            if (mMaxRetryCount == null) {
                mInstance?.mMaxRetryCount = Constants.Settings.FACE_DETECTION_RETRY_COUNT
            }

            if (mMinimumValidFrames == null) {
                mInstance?.mMinimumValidFrames = Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES
            }

            return this.mInstance!!
        }

        fun clearInstance() {
            mInstance = null
        }

        fun getInstance(): FaceHelper {
            return mInstance!!
        }
    }

    public fun createCameraSource(
        mOverlay: GraphicOverlay,
        mCameraSourcePreview: CameraSourcePreview,
        mListener: RecognitionListener
    ) {
        this.mOverLay = mOverlay
        this.mCameraSourcePreview = mCameraSourcePreview
        this.mRecognitionListener = mListener
        if (checkCameraPermission(getContext()) && checkStoragePermission(getContext())) {

            val detector = FaceDetector.Builder(mContext)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()

            if (detector.isOperational) {

                var mGraphicFaceTrackerInstance = GraphicFaceTrackerFactory(mOverlay, this);

                detector.setProcessor(
                    MultiProcessor.Builder<Face>(mGraphicFaceTrackerInstance)
                        .build()
                )

                when {
                    checkCameraFront(getContext()) -> {
                        mCameraSource = CameraSource.Builder(getContext(), detector)
                            .setFacing(CameraSource.CAMERA_FACING_FRONT)
                            .setRequestedFps(30.0f)
                            .build()
                        startCameraSource(mCameraSourcePreview, mOverlay)
                    }
                    checkCameraBack(getContext()) -> {
                        mCameraSource = CameraSource.Builder(getContext(), detector)
                            .setFacing(CameraSource.CAMERA_FACING_BACK)
                            .setRequestedFps(30.0f)
                            .build()
                        startCameraSource(mCameraSourcePreview, mOverlay)
                    }
                    else -> {
                        mListener.onFailed(CameraNotAvailableError())
                    }
                }
            } else {
                mListener.onFailed(DeviceNotSupportedError())
            }
        } else {
            mListener.onPermissionDenied()
        }

    }

    private fun checkStoragePermission(context: Application): Boolean {
        val pm = context.packageManager
        val hasPerm = pm.checkPermission(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            context.packageName
        )
        return (hasPerm == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkCameraPermission(context: Application): Boolean {
        val pm = context.packageManager
        val hasPerm = pm.checkPermission(
            android.Manifest.permission.CAMERA,
            context.packageName
        )
        return (hasPerm == PackageManager.PERMISSION_GRANTED)
    }

    fun onResume() {
        startCameraSource(mCameraSourcePreview, mOverLay)
    }

    fun onPause() {
        mCameraSourcePreview?.stop()
    }

    fun onDestroy() {
        if (mCameraSource != null) {
            mCameraSource?.release()
        }
        mCoroutineJob.cancel()
    }

    private fun startCameraSource(
        mCameraSourcePreview: CameraSourcePreview?,
        mGraphicOverlay: GraphicOverlay?
    ) {

        if (mCameraSource != null) {
            try {
                mCameraSourcePreview?.start(mCameraSource, mGraphicOverlay)
            } catch (e: IOException) {
                mCameraSource?.release()
                mCameraSource = null
            }

        }
    }

    private fun rotateBitmap(bytes: ByteArray?): Bitmap? {
        if (bytes != null) {
            val orientation = Exif.getOrientation(bytes)
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            when (orientation) {
                90 -> bitmap = TransformationUtils.rotateImage(bitmap, 90)
                180 -> bitmap = TransformationUtils.rotateImage(bitmap, 180)
                270 -> bitmap = TransformationUtils.rotateImage(bitmap, 270)
                0 -> bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                else -> {
                }
            }
            return bitmap
        } else {
            return null;
        }
    }

    private fun checkCameraFront(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    private fun checkCameraBack(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    public fun initFaceTraining(
        mVideoFile: File,
        mFaceLabel: String,
        mTrainingListener: TrainingListener
    ) {
        launch {
            var mFramesDirectory = VideoFrameGenerator.generateVideoFrames(mVideoFile)
            var mFrameList = VideoFileUtils.getFramePathList(mFramesDirectory)
            RecognitionUtils.initTrainRecognition(mFrameList, mFaceLabel, mTrainingListener)
        }
    }


    interface RecognitionListener {
        fun onPermissionDenied()
        fun onRecognitionInProgress()
        fun onSuccess(mLabel: String?)
        fun onFaceDone()
        fun onFaceMissing()
        fun onFailed(mFaceError: FaceError)
    }

    interface TrainingListener {
        fun onTrainingFinished(mTrainingResult: RecognitionUtils.TrainingResult)
        fun onTrainingFailed(mTrainingError: TrainingError)
        fun onProcessingInProgress(mProgress: Float?)
        fun onTrainingInProgress(mProgress: Float?)
    }
}