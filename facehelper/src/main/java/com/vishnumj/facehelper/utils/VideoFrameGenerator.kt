package com.vishnumj.facehelper.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import com.vishnumj.facehelper.FaceHelper
import java.io.*
import java.nio.ByteBuffer

object VideoFrameGenerator {

    const val FRAME_COUNT = 50L;

    suspend fun generateVideoFrames(mFile: File?): File? {
        var mFrameDirectory: File? = null
        if (mFile != null && mFile.exists()) {

            mFrameDirectory = VideoFileUtils.getFrameDirectory(FaceHelper.getInstance().mContext, mFile.nameWithoutExtension)
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(mFile.getAbsolutePath())
            val METADATA_KEY_DURATION = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            var bmpOriginal: Bitmap? = mediaMetadataRetriever.getFrameAtTime(0)
            var bmpVideoHeight = bmpOriginal?.height
            var bmpVideoWidth = bmpOriginal?.width

            var mFrameInterval = METADATA_KEY_DURATION.toLong() / FRAME_COUNT;

            for (index in 0 until METADATA_KEY_DURATION.toLong() step mFrameInterval) {

                bmpOriginal = mediaMetadataRetriever.getFrameAtTime((index * 1000))
                bmpVideoHeight = bmpOriginal?.height ?: -1
                bmpVideoWidth = bmpOriginal?.width ?: -1
                val byteCount = bmpOriginal!!.width * bmpOriginal.height * 4
                Log.e("time", "1")
                val tmpByteBuffer = ByteBuffer.allocate(byteCount)
                Log.e("time", "2")
                bmpOriginal.copyPixelsToBuffer(tmpByteBuffer)
                Log.e("time", "3")
                val tmpByteArray = tmpByteBuffer.array()
                Log.e("time", "4")
                /*if(!Arrays.equals(tmpByteArray, lastSavedByteArray))
            {*/
                Log.e("time", "5")
                val quality = 100

                val outputFile = File(
                    mFrameDirectory, "IMG_" + index
                            + "_" + "quality_" + quality + "_w" + bmpVideoWidth + "_h" + bmpVideoHeight + ".png"
                )


                Log.e("Output Files::>>", "" + outputFile)
                var outputStream: OutputStream? = null
                try {
                    outputStream = FileOutputStream(outputFile)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val bmpScaledSize = Bitmap.createScaledBitmap(bmpOriginal, bmpVideoWidth, bmpVideoHeight, false)

                bmpScaledSize.compress(Bitmap.CompressFormat.PNG, quality, outputStream)

                try {
                    outputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mediaMetadataRetriever.release()
        }

        return mFrameDirectory;
    }


}