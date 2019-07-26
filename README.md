# FaceHelper for Android

A project to make facial recognition integration for android easier and simple. The project acts as a wrapper for [Qualeams's Face Recognition Project](https://github.com/Qualeams/Android-Face-Recognition-with-Deep-Learning-Test-Framework). 

### Usage

Initialise the library in the application class as below. 

    class App : Application() {  
      
        override fun onCreate() {  
            super.onCreate()  
            FaceHelper.Builder(this).setMaxRetryCount(Constants.Settings.FACE_DETECTION_RETRY_COUNT)  
                .setMinimumValidFrames(Constants.Settings.VIDEO_MINIMUM_VALID_FRAMES)  
                .setCacheDirectory(cacheDir).build()  
        }  
    }

Initialise training session using a video capture of the face. The library will split the video into frames and use the images to train.

    FaceHelper.getInstance().initFaceTraining(mYourVideoFile, "FACE_LABEL_${System.currentTimeMillis()}", object : FaceHelper.TrainingListener {  
            override fun onTrainingFinished(mTrainingResult: RecognitionUtils.TrainingResult) {  
                  
            }  
      
            override fun onTrainingFailed(mTrainingError: TrainingError) {  
                 
            }  
      
            override fun onProcessingInProgress(mProgress: Float?) {  
                 
            }  
      
            override fun onTrainingInProgress(mProgress: Float?) {  
                 
            }  
        }  
    )
Supply a camera source preview and the graphic overlay in the layout and the library will attempt recognition if and when a human face is detected.


    FaceHelper.getInstance().createCameraSource(overlay_graphic,
	camera_source_preview  object : FaceHelper.RecognitionListener {  
            override fun onPermissionDenied() {  
                //Camera and/or storage permissions was denied  
      }  
      
            override fun onRecognitionInProgress() {  
                //Recognition is in progress  
      }  
      
            override fun onSuccess(mLabel: String?) {  
                //Recognition was successful, the $mLabel is the recognised label  
      }  
      
            override fun onFaceDone() {  
                //The detected face is gone  
      }  
      
            override fun onFaceMissing() {  
                //The detected face is missing  
      }  
      
            override fun onFailed(mFaceError: FaceError) {  
                //The recognition failed $mFaceError.getMessage() will have an understandable message  
      }  
        }  
    )

   


### Known Issues

 - Face recognition system is inherently flawed without additional hardware assists. It cannot distinguish between an image and an actual human face.
 - Detection and recognition speeds are dependant on the device capability.

### Future Milestones (Ordered as Priority)

 1. Maven Repo
 2. Configurable training and recognition algorithms
 3. Ability to train using a collection of frame file paths
 4. Recognition and training performance improvements
 5. Unit Tests and CI
 


### FaceHelper Configurations

Use FaceHelper Builder to customise configurations during initialisation

|Parameter                |Description                          |Optional                         |
|----------------|-------------------------------|-----------------------------|
|MaxRetryCount|The maximum retry limit for face recognition. When set to desired value, the library will attempt recognise again when it cannot match the face to any of the stored models.       |YES          |
|MinimumValidFrames|The minimum number of valid frames from the training video. Higher number of frames can lead to higher accuracy but performance will take a toll            |YES          |
|CacheDirectory        |The directory to store trained models, spitted frames from the training video|YES|


