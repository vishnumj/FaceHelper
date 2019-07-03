package com.wonderkiln.camerakit;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.*;

@Retention(RetentionPolicy.SOURCE)
@IntDef({FLASH_OFF, FLASH_ON, FLASH_AUTO, FLASH_TORCH})
public @interface Flash {
}