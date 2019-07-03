package com.vishnumj.facehelper.utils

abstract class TrainingError {
    abstract fun getMessage(): String
}

class TrainingFaceAlreadyExists : TrainingError() {
    override fun getMessage(): String {
        return "Face already exists."
    }

}

class TrainingVideoInsufficient : TrainingError() {
    override fun getMessage(): String {
        return "Insufficient details in training video"
    }

}

class TrainingVideoMoreFace : TrainingError() {
    override fun getMessage(): String {
        return "More than one face detected during training"
    }

}

class TrainingGenericError : TrainingError() {
    override fun getMessage(): String {
        return "Unable to complete training"
    }

}

class TrainingVideoNotFound:TrainingError(){
    override fun getMessage(): String {
        return "Training video not found."
    }

}