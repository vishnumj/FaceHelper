package com.vishnumj.facehelper.utils

abstract class FaceError {
    abstract fun getMessage(): String
}

class EmptyFaceDataError : FaceError() {
    override fun getMessage(): String {
        return "Empty face data. Please add train some faces first"
    }
}

class InvalidFaceError : FaceError() {
    override fun getMessage(): String {
        return "Unable to recognise face."
    }
}

class DeviceNotSupportedError : FaceError() {
    override fun getMessage(): String {
        return "Device does not support face detection"
    }
}

class CameraNotAvailableError : FaceError(){
    override fun getMessage(): String {
        return "Camera not available"
    }

}