package com.wonderkiln.camerakit;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.*;

@Retention(RetentionPolicy.SOURCE)
@IntDef({VIDEO_QUALITY_QVGA, VIDEO_QUALITY_480P, VIDEO_QUALITY_720P, VIDEO_QUALITY_1080P, VIDEO_QUALITY_2160P, VIDEO_QUALITY_HIGHEST, VIDEO_QUALITY_LOWEST})
public @interface VideoQuality {
}
