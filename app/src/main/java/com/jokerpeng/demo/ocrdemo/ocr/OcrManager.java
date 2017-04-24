package com.jokerpeng.demo.ocrdemo.ocr;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.hanvon.HWCloudManager;
import com.hanvon.utils.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/19.
 */
public class OCRManager {
    /**
     * 身份证
     * */
    public static final int ID_CARD = 1;
    /**
     * 银行卡
     * */
    public static final int BANK_CARD = 2;
    /**
     * 存放拍照路径
     * */
    private final String STR_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "ShootImg";

//    private OCRManager mOcrManager;
    private HWCloudManager hwCloudManager;
    /**
     * 拍照URI
     * */
    private Uri cameraUri;
    /**
     * 照片类型选择
     * */
    private int type;
    /**
     * 选择图片的路径
     * */
    private String picPath;


    public OCRManager(Context context,String key) {
//        this.mOcrManager = mOcrManager;
        type = 0;
        Init(context,key);
    }


    private void Init(Context context, String key){
        hwCloudManager = new HWCloudManager(context, key);
    }

    public String getPicPath() {
        return picPath;
    }

    /**
     * 选择身份证照片
     * */
    public Intent selectIdPic(){
        type = ID_CARD;
        return getSelectIntent();
    }

    /**
     * 选择银行卡照片
     * */
    public Intent selectBankPic(){
        type = BANK_CARD;
        return getSelectIntent();
    }

    private Intent getSelectIntent(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        return intent;
    }

    public Intent shootIdPhoto(Context context) throws IOException {
        type = ID_CARD;
        return getShootIntent(context);
    }

    public Intent shootBankPhoto(Context context) throws IOException {
        type = BANK_CARD;
        return getShootIntent(context);
    }
    /**
     * 获得拍摄
     * */
    private Intent getShootIntent(Context context) throws IOException {
        if(isStorageState(context)){
            File dir = new File(STR_DIR);
            if(!dir.exists()){
                dir.mkdir();
            }
            SimpleDateFormat t = new SimpleDateFormat("yyyyMMddHHMMSS");
            String imageName = "ID_" + (t.format(new Date()));

            File imageFile = null;
            imageFile = File.createTempFile(imageName,".jpg",dir);
            cameraUri = Uri.fromFile(imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);
            return intent;
        }
        return null;
    }
    /**
     * 判断是否有SD卡
     * */
    private boolean isStorageState(Context context){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(context, "请插入存储卡", Toast.LENGTH_LONG).show();
            return false;
        }else{
            return  true;
        }
    }

    /**
     *照片图片处理
     * */
    public Bitmap photoImageProcess(Context context,Intent data){
        Uri uri = data.getData();
        // 通过uri获取图片路径
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null,
                null);
        picPath = null;
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            picPath = cursor.getString(column_index);
        }
        else
        {
            picPath = data.getData().getPath();
        }
        System.out.println(picPath);
        Log.e("main",picPath);
        return imageProcess();
    }
    /**
     * 拍照图片处理
     * */
    public Bitmap shootImageProcess(){
        picPath = cameraUri.getPath();
        System.out.println(picPath);
        return imageProcess();
    }

    private Bitmap imageProcess(){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        options.inSampleSize = BitmapUtil.calculateInSampleSize(options,1280, 720);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
        return bitmap;
    }

    /**
     * 识别图片
     * param1:图片路径
     * */
    public String discriminate(String picPath){
        String result = null;
        switch (type){
            case ID_CARD:
                result = hwCloudManager.idCardCropLanguage(picPath);
                break;
            case BANK_CARD:
                result = hwCloudManager.bankCardCropLanguage(picPath);
                break;
        }
        return result;
    }
    /**
     *识别图片
     *param1：图片路径
     * param2：类型
     * */
    public String discriminate(String picPath,int type){
        String result = null;
        switch (type){
            case ID_CARD:
                result = hwCloudManager.idCardCropLanguage(picPath);
                break;
            case BANK_CARD:
                result = hwCloudManager.bankCardCropLanguage(picPath);
                break;
        }
        return result;
    }
}
