package com.jokerpeng.demo.ocrdemo.view;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.HWCloudManager;
import com.hanvon.utils.BitmapUtil;
import com.jokerpeng.demo.ocrdemo.R;
import com.jokerpeng.demo.ocrdemo.ocr.OCRManager;
import com.jokerpeng.demo.ocrdemo.utils.RequestPermissions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private final String KEY = "d6a66f61-7cf0-4213-8564-db1f38116cf8";
//    private final int ID_CARD = 1;
//    private final int BANK_CARD = 2;
    private final int CAMERA = 10;
    private final int PHOTO = 20;
    /**
     * 存放拍照路径
     * */
    private final String STR_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "ShootImg";
    /**
     * 存放银行卡拍照路径
     * */
//    private final String BANK_DIR = Environment.getExternalStorageDirectory().getParent() + File.separator + "BankCarkImg";


    private Button mBtn_selectIdCard,mBtn_selectBankCard,mBtn_discriminate;
    private int flag;

    private ImageView iv_image;
    private TextView testView;
    private ProgressDialog pd;
    private DiscernHandler discernHandler;
    private OCRManager mOcrManager;
    /**
     * 选择图片的路径
     * */
    private String picPath;
    private String result;


    private Uri cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mOcrManager = new OCRManager(this,KEY);
//        mOcrManager.Init(this,KEY);

        discernHandler = new DiscernHandler();
        iv_image = (ImageView) findViewById(R.id.iv_image);
        testView = (TextView) findViewById(R.id.result);
        findViewById(R.id.btn_selectid).setOnClickListener(this);
        findViewById(R.id.btn_selectbank).setOnClickListener(this);
        findViewById(R.id.btn_discriminate).setOnClickListener(this);
        findViewById(R.id.btn_camera_id).setOnClickListener(this);
        findViewById(R.id.btn_camera_bank).setOnClickListener(this);
        RequestPermissions.verifyOCRPermissions(this,RequestPermissions.verifySdkVersion());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_selectid://选择身份证照片
                Intent intentId = mOcrManager.selectIdPic();
                startActivityForResult(intentId, PHOTO);
//                flag = ID_CARD;
//                selectPic();
                break;
            case R.id.btn_selectbank://选择银行卡照片
                Intent intentBank = mOcrManager.selectBankPic();
                startActivityForResult(intentBank, PHOTO);
//                flag = BANK_CARD;
//                selectPic();
                break;
            case R.id.btn_camera_id://拍身份证照
//                flag = ID_CARD;
                Intent intentShootId = null;
                try {
                    intentShootId = mOcrManager.shootIdPhoto(MainActivity.this);
                    if(intentShootId != null){
                        goShoot(intentShootId);
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "请插入存储卡", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btn_camera_bank://拍银行卡照
//                flag = BANK_CARD;
                Intent intentShootBank = null;
                try {
                    intentShootBank = mOcrManager.shootBankPhoto(MainActivity.this);
                    if(intentShootBank != null){
                        goShoot(intentShootBank);
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "请插入存储卡", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btn_discriminate://识别
                testView.setText("");
                pd = ProgressDialog
                        .show(MainActivity.this, "", "正在识别请稍后......");
                DiscernThread discernThread = new DiscernThread();
                new Thread(discernThread).start();
                break;
        }
    }
    /**
     * 去拍照
     * */
    private void goShoot(Intent intent){
        // 启动相机
        startActivityForResult(intent, CAMERA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        testView.setText("");
        switch (requestCode){
            case PHOTO:
                if (data != null) {
//                    iv_image.setImageBitmap(imageProcess(data));
                    iv_image.setImageBitmap(mOcrManager.photoImageProcess(this,data));

                }
                break;
            case CAMERA:
                if(resultCode == RESULT_OK){
                   /* picPath = cameraUri.getPath();
                    System.out.println(picPath);
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(picPath, options);
                    options.inSampleSize = BitmapUtil.calculateInSampleSize(options,1280, 720);
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
                    iv_image.setImageBitmap(bitmap);*/
                    iv_image.setImageBitmap(mOcrManager.shootImageProcess());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class DiscernThread implements Runnable {

        @Override
        public void run() {
            try {
            result = null;

                result = mOcrManager.discriminate(mOcrManager.getPicPath());


//            if(flag == ID_CARD){
//                result = hwCloudManagerIdcard.idCardLanguage(picPath);//身份证普通版
//            }else if(flag == BANK_CARD){
//                result = hwCloudManagerIdcard.bankCardCropLanguage(picPath);//银行卡切图版
//            }else{
//                Toast.makeText(getApplicationContext(),"请选择对应的卡",Toast.LENGTH_SHORT).show();
//            }
//                String resultBank = hwCloudManagerIdcard.bankCardLanguage(picPath);
//				result = hwCloudManagerIdcard.idCardLanguage(picBitmap);
                // result = hwCloudManagerIdcard.idCardLanguage4Https(picPath);
//				result = hwCloudManagerIdcard.idCardCropLanguage(picPath);//切图版
//				result = hwCloudManagerIdcard.idCardCropLanguage(picBitmap);
            } catch (Exception e) {
                // TODO: handle exception
            }
            Bundle mBundle = new Bundle();
            mBundle.putString("responce", result);
            Message msg = new Message();
            msg.setData(mBundle);
            msg.what = 0;
            discernHandler.sendMessage(msg);
        }
    }

    public class DiscernHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0 :
                    pd.dismiss();
                    Bundle bundle = msg.getData();
                    String responce = bundle.getString("responce");
                    testView.setText(responce);
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(),"数据传输错误1",Toast.LENGTH_LONG);
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(),"数据传输错误2",Toast.LENGTH_LONG);
                    break;
            }

        }
    }

    private Bitmap imageProcess(Intent data){
        Uri uri = data.getData();
        // 通过uri获取图片路径
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null,
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

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        options.inSampleSize = BitmapUtil.calculateInSampleSize(options,1280, 720);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
        return  bitmap;
    }
}
