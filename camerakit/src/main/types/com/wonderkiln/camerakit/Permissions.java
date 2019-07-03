package com.wonderkiln.camerakit;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.*;

@Retention(RetentionPolicy.SOURCE)
@IntDef({PERMISSIONS_STRICT, PERMISSIONS_LAZY, PERMISSIONS_PICTURE})
public @interface Permissions {
}
