package com.vishnumj.facehelper.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.vishnumj.facehelper.R
import java.io.File

object VideoFileUtils {

    fun generateVideoFile(mContext: Fragment?): File? {
        return if (mContext != null) generateVideoFile(mContext.activity) else null
    }

    fun generateVideoFile(mContext: Activity?): File? {
        return if (mContext != null) File(
            mContext.cacheDir?.absolutePath + File.separator +
                    "FaceHelperVideo" + "${System.currentTimeMillis()}.mp4"
        ) else null
    }

    fun getVideoTitleFromFile(mVideoFileName: File?): String {
        return if (mVideoFileName != null && mVideoFileName.isFile) mVideoFileName.nameWithoutExtension else ""
    }

    fun getFrameDirectory(mContext: Context?, mVideoFileName: String): File {
        var mFile = File(
            mContext?.cacheDir?.absolutePath + File.separator +
                    mVideoFileName + File.separator
        )
        if (mFile.exists()) {
            mFile.deleteRecursively()
        }
        mFile.mkdirs()
        return mFile
    }

    fun getFramePathList(mGeneratedFramesDirectory: File?): List<String>? {
        if (mGeneratedFramesDirectory != null && mGeneratedFramesDirectory.exists()) {
            return mGeneratedFramesDirectory.listFiles().map {
                it.absolutePath
            } as ArrayList<String>
        }
        return null;
    }
}