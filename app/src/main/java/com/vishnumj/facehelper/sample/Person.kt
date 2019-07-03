package com.vishnumj.facehelper.sample

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Person(var mName: String) : Parcelable {

}