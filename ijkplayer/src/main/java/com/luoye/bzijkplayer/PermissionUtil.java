package com.luoye.bzijkplayer;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Created by admin on 2016/2/17.
 * 动态权限请求的相关工具类
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
    public static final int CODE_REQ_PERMISSION = 1100;//权限请求
    public static final int CODE_REQ_AUDIO_PERMISSION = 601;
    public static final int CODE_REQ_CAMERA_PERMISSION = 602;

    /**
     * 权限请求
     *
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermission(Activity activity, String[] permissionArr, int requestCode) {
        if (permissionArr != null) {
            activity.requestPermissions(permissionArr, requestCode);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermission(Activity activity, String permissionArr, int requestCode) {
        if (permissionArr != null) {
            activity.requestPermissions(new String[]{permissionArr}, requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermissionIFNot(Activity activity, String permissionArr, int requestCode) {
        if (permissionArr != null && !isPermissionGranted(activity, permissionArr)) {
            requestPermission(activity, permissionArr, requestCode);
        }
    }

    /**
     * 判断是否拥有该权限
     *
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isPermissionGranted(Activity activity, String permission) {
        if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
