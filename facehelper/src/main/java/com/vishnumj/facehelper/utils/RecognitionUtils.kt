package com.vishnumj.facehelper.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper.SVM_PATH
import ch.zhaw.facerecognitionlibrary.Helpers.MatName
import ch.zhaw.facerecognitionlibrary.Helpers.PreferencesHelper
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory
import ch.zhaw.facerecognitionlibrary.Recognition.Recognition
import ch.zhaw.facerecognitionlibrary.Recognition.RecognitionFactory
import com.vishnumj.facehelper.utils.constants.Constants
import com.vishnumj.facehelper.FaceHelper
import com.vishnumj.facehelper.R
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.*

@SuppressLint("StaticFieldLeak")
object RecognitionUtils {

    private var mPreProcessorFactory: PreProcessorFactory? = null;
    private var mRecognitionAlgorithmFactory: Recognition? = null;
    private var mTrainingAlgorithmFactory: Recognition? = null
    private var mFileHelper: FileHelper? = null
    private var isInitialised = false;
    private var mPreferencesHelper: PreferencesHelper? = null
    private var mContext: Context = FaceHelper.getInstance().getContext()


    init {
        initialiseLibraries()
    }

    private fun initialiseLibraries() {
        PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false)
        OpenCVLoader.initDebug()
        initSVNTrainData(mContext)
        mFileHelper = FileHelper()
        mPreferencesHelper = PreferencesHelper(mContext)
        mPreProcessorFactory = PreProcessorFactory(mContext)
        mRecognitionAlgorithmFactory = RecognitionFactory.getRecognitionAlgorithm(
            mContext,
            Recognition.RECOGNITION,
            "TensorFlow with SVM or KNN"
        )
        mTrainingAlgorithmFactory =
            RecognitionFactory.getRecognitionAlgorithm(
                mContext,
                Recognition.TRAINING,
                "TensorFlow with SVM or KNN"
            );
        isInitialised = true
    }


    private fun initSVNTrainData(instance: Context) {
        val path = File(SVM_PATH)
        if (!path.exists()) {
            path.mkdirs()
        }
        val file1 = File(SVM_PATH + "svm_train")
        try {
            if (!file1.exists()) {
                Log.e("inside", "savestringlist")
                val fw1 = FileWriter(file1, true)
                val br = BufferedReader(InputStreamReader(instance.getAssets().open("extra_data")))
                Log.e("Reading", "")
                br.forEachLine { lines ->
                    Log.e("Reading1", "")
                    Log.e(lines, "")
                    fw1.append(lines + "\n")
                }
                fw1.close()
                br.close()
            }
        } catch (e: IOException) {
            Log.e(e.toString(), "main")
        }

    }

    suspend fun initRecognition(
        mRecognitionRequest: RecognitionRequest?,
        mFaceID: Int?
    ): RecognitionResponse {
        if (!isInitialised) {
            initialiseLibraries()
        }
        val mRecognitionResult = RecognitionResponse(RecognitionUtils.Result.ERROR, mFaceID)
        try {
            if (mRecognitionRequest?.mImage != null) {
                val mImages = mPreProcessorFactory?.getProcessedImage(
                    convertBitmapMat(mRecognitionRequest.mImage!!),
                    PreProcessorFactory.PreprocessingMode.RECOGNITION
                )
                if (mImages != null) {
                    mRecognitionResult.mResultLabel =
                        mRecognitionAlgorithmFactory?.recognize(
                            mImages?.get(0),
                            mRecognitionRequest.mExpectedLabel
                        )
                    mRecognitionResult.mResult = RecognitionUtils.Result.SUCCESS
                    return (mRecognitionResult)
                }
            }
        } catch (e: Exception) {
        }
        return mRecognitionResult;
    }

    suspend fun initTrainRecognition(
        mFrames: List<String>?,
        mLabel: String?,
        mTrainingListener: FaceHelper.TrainingListener
    ) {
        if (!isInitialised) {
            initialiseLibraries()
        }
        val mAgeGenderList = arrayListOf<String?>()
        var mValidFaceFramesCount = 0


        if (mFrames != null) {
            for (index in 0..mFrames?.size - 1) {
                val mImagePath = mFrames?.get(index)
                val mBitmap = BitmapFactory.decodeFile(mImagePath);
                val imageMat = convertBitmapMat(mBitmap)
                val images = mPreProcessorFactory?.getCroppedImage(imageMat)
                if (images != null && images!!.size == 1) {
                    val img = images!!.get(0)
                    if (img != null) {
                        var faces = mPreProcessorFactory?.getFacesForRecognition()
                        //Only proceed if 1 face has been detected, ignore if 0 or more than 1 face have been detected
                        if (faces != null) {
                            if (faces!!.size == 1) {
                                if (mValidFaceFramesCount < Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES) {
                                    var mRecognitionResult =
                                        initRecognition(RecognitionRequest(mBitmap, ""), 0)
                                    if (mRecognitionResult.mResult == RecognitionUtils.Result.SUCCESS &&
                                        !TextUtils.isEmpty(mRecognitionResult.mResultLabel)
                                    ) {
                                        mTrainingListener.onTrainingFailed(TrainingFaceAlreadyExists())
                                        break
                                    } else {
                                        val m =
                                            MatName(mLabel + "_" + System.currentTimeMillis(), img)
                                        val wholeFolderPath = FileHelper.TRAINING_PATH + mLabel
                                        File(wholeFolderPath).mkdirs()
                                        mFileHelper?.saveMatToImage(m, "$wholeFolderPath/")
                                        mValidFaceFramesCount++
                                        mTrainingListener.onProcessingInProgress(
                                            (mValidFaceFramesCount * 100 / (Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES * 2)).toFloat()
                                        )
                                    }
                                } else {
                                    mTrainingListener.onTrainingFailed(TrainingVideoInsufficient())
                                    break
                                }
                            } else {
                                mTrainingListener.onTrainingFailed(TrainingVideoMoreFace())
                                break
                            }
                        }
                    }
                }
            }
        }


        var mCounter = Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES
        if (mValidFaceFramesCount.toLong() >= Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES) {
            mFileHelper?.createDataFolderIfNotExsiting()
            val persons = mFileHelper?.trainingList
            if (persons?.size!! > 0) {
                for (person in persons) {
                    Log.e("person", person.name)
                    if (person.isDirectory && person.name.equals(mLabel)) {
                        val files = person.listFiles()
                        //int counter = 1;
                        if (files!!.size >= Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES) {
                            for (index in 0 until Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES) {
                                val file = files[index]
                                if (FileHelper.isFileAnImage(file)) {
                                    Log.e("in", "isimage")
                                    val imgRgb = Imgcodecs.imread(file.absolutePath)
                                    Imgproc.cvtColor(imgRgb, imgRgb, Imgproc.COLOR_BGRA2RGBA)
                                    var processedImage = Mat()
                                    imgRgb.copyTo(processedImage)
                                    var images: MutableList<Mat>? = null
                                    try {
                                        images = mPreProcessorFactory?.getProcessedImage(
                                            processedImage,
                                            PreProcessorFactory.PreprocessingMode.RECOGNITION
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    if (images == null || images.size > 1) {
                                        if (images == null)
                                            Log.e("in", "null")
                                        //if(images.size()>1)
                                        Log.e("in", "or faces>1")
                                        // More than 1 face detected --> cannot use this file for training
                                        continue
                                    } else {
                                        Log.e("in", "imagesget")
                                        processedImage = images[0]
                                    }
                                    if (processedImage.empty()) {
                                        Log.e("in", "imageprocessedempty")
                                        continue
                                    }
                                    // The last token is the name --> Folder name = Person name
                                    val tokens = file.parent.split("/".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                    val name = tokens[tokens.size - 1]

                                    val m = MatName("processedImage_${mLabel}", processedImage)
                                    mFileHelper?.saveMatToImage(m, FileHelper.DATA_PATH)
                                    try {
                                        mTrainingAlgorithmFactory!!.addImage(
                                            processedImage,
                                            name,
                                            false
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                mCounter++


                                mTrainingListener.onTrainingInProgress(
                                    (mCounter * 100 / (Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES * 2)).toFloat()
                                )


                            }

                            if (mTrainingAlgorithmFactory!!.train()) {
                                initialiseLibraries()
                                mTrainingListener.onTrainingFinished(
                                    TrainingResult(
                                        RecognitionUtils.Result.SUCCESS,
                                        mTrainedLabel = mLabel,
                                        mAgeGenderResult = getAverageAgeGender(mAgeGenderList)
                                    )
                                )
                            } else {
                                mTrainingListener.onTrainingFailed(TrainingGenericError())
                            }

                        } else {
                            mTrainingListener.onTrainingFailed(TrainingVideoInsufficient())
                        }
                    } else {
                        mTrainingListener.onTrainingFailed(TrainingVideoNotFound())
                    }
                }
            } else {
                mTrainingListener.onTrainingFailed(TrainingVideoNotFound())
            }
        } else {
            mTrainingListener.onTrainingFailed(TrainingVideoInsufficient())
        }
        mFileHelper?.deleteTrainingDirectory()
    }

    private fun getAverageAgeGender(mAgeGenderList: ArrayList<String?>): AgeGenderResult? {
        //265ms dist=0.0000 age=28 male
        var mAverageAge: Int = 0
        var mFemaleCount = 0
        var mMaleCount = 0
        mAgeGenderList.forEach {
            it?.split(" ")?.let {
                var mResult =
                    AgeGenderResult(it[2].replace("age=", "").toInt(), it[3].replace("\n", ""))
                mAverageAge += mResult.mAge
                if (mResult.mGender.equals("male")) mMaleCount++ else mFemaleCount++
            }
        }
        return AgeGenderResult(
            if (mAverageAge != 0) mAverageAge / mAgeGenderList.size else 0,
            if (mMaleCount >= mFemaleCount) "male" else "female"
        )
    }

    fun convertBitmapMat(bmp: Bitmap?): Mat? {
        if (bmp != null) {
            val mat = Mat()
            val bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true)
            Utils.bitmapToMat(bmp32, mat)
            return mat
        }
        return null
    }


    class RecognitionResponse(
        var mResult: Result,
        var mFaceID: Int? = 0,
        var mResultLabel: String? = "",
        var mFromGallery: Boolean? = false
    )

    class RecognitionRequest(
        var mImage: Bitmap? = null,
        var mExpectedLabel: String? = ""
    )

    class TrainingResult(
        var mResult: Result,
        var mAgeGenderResultList: ArrayList<String?>? = null,
        var mAgeGenderResult: AgeGenderResult? = null,
        var mTrainedLabel: String? = "",
        var mProgress: Int = 0
    )

    class AgeGenderResult(var mAge: Int, var mGender: String)

    enum class Result {
        UNKNOWN,
        SUCCESS,
        FAILED,
        IN_PROGRESS,
        ERROR
    }
}