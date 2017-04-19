package com.jokerpeng.demo.ocrdemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Administrator on 2017/4/18.
 */
public class RequestPermissions {
    /**
     * SD卡读写权限,开启摄像头权限
     * */
    private static final String[] PERMISSION_OCR = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    private static final int REQUEST_OCR = 100;


    /**
     * 用于OCR识别
     * SD卡读写权限,开启摄像头权限
     * */
    public static void verifyOCRPermissions(Activity activity) {
        int permissionWrite = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_OCR,
                    REQUEST_OCR);
        }
    }

}
