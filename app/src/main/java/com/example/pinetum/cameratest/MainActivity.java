package com.example.pinetum.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.CameraInfo;
import android.widget.Toast;


public class MainActivity extends Activity {
    public static Bitmap           m_bitmap_shutter;



    private Button          m_btn_shut;
    private Camera          m_Camera_myCam;
    private FrameLayout     m_frameLayout_main;
    private CameraPreview   m_CameraPreview_perview;
    private PictureCallback mPicture;
    private DisplayMetrics  m_metrics ;
    private int             m_n_orientation = 0;//angle
    private float           m_f_x_touch = 0, m_f_y_touch = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        全螢幕狀態
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        強制直螢幕顯示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_camera_perview);
//        螢幕不關
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        算出螢幕大小
        m_metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(m_metrics);
//        初始化變數
        m_btn_shut                  = new Button(this);
        m_CameraPreview_perview     = new CameraPreview(this, m_Camera_myCam);
        m_frameLayout_main          = (FrameLayout) findViewById(R.id.id_frameLayout_main);
//        設定按鈕背景
        m_btn_shut.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_btn_shutter));
        m_btn_shut.setAlpha((float)0.95);
        FrameLayout.LayoutParams position_shutterBtn = new FrameLayout.LayoutParams(m_btn_shut.getWidth(), m_btn_shut.getHeight());
        position_shutterBtn.gravity = Gravity.CENTER_HORIZONTAL;
//        加入元件
        FrameLayout.LayoutParams myBtnLayout =
                new FrameLayout.LayoutParams(m_metrics.widthPixels/5,//width
                                                m_metrics.widthPixels/5,//height
                                                    Gravity.CENTER|Gravity.BOTTOM);//position

        myBtnLayout.setMargins(0, 0, 0 ,m_metrics.widthPixels/6);
        m_frameLayout_main.addView(m_CameraPreview_perview);
        m_frameLayout_main.addView(m_btn_shut, myBtnLayout);

//        觸碰自動對焦
        m_CameraPreview_perview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //m_Camera_myCam.autoFocus(getFocusCallBack(false));

            }
        });
//        取得觸控的位置
        m_CameraPreview_perview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    float x = event.getX();
                    float y = event.getY();
                    float touchMajor = event.getTouchMajor();
                    float touchMinor = event.getTouchMinor();
                    Log.i("Touch","up");
                    Rect touchRect = new Rect((int)(x - touchMajor / 2), (int)(y - touchMinor / 2), (int)(x + touchMajor / 2), (int)(y + touchMinor / 2));

                    FocusAreaRect(touchRect);
                }

                return false;
            }
        });
//        按鈕對焦並拍照
        m_btn_shut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                m_btn_shut.setSelected(true);

                m_Camera_myCam.autoFocus(getFocusCallBack(true));


            }
        });
        OrientationEventListener myListen = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                m_n_orientation = orientation;
                //Log.i("orientation","::"+orientation);
            }
        };
        if(myListen.canDetectOrientation())
            myListen.enable();

    }
    //呼叫相機啟動快門，完成後會呼叫callBack
    private void getCameraPic(){
        int phone_orientation = m_n_orientation;//裝置角度
        int pic_rotation = 0;//算出圖片rota
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(findBackFacingCamera(), info);
        phone_orientation = (phone_orientation + 45) / 90 * 90;
        //google提供的角度算法，因為有分前後鏡頭，角度算法不同，上面呼叫的findBackFace為直接回傳後面相機id
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT)
            pic_rotation = (info.orientation - phone_orientation + 360) % 360;
        else  // back-facing camera
            pic_rotation = (info.orientation + phone_orientation) % 360;

        Camera.Parameters myParameters = m_Camera_myCam.getParameters();
        myParameters.setRotation(pic_rotation);
        m_Camera_myCam.setParameters(myParameters);

        m_Camera_myCam.takePicture(null, null, mPicture);//raw postView jpg callback
    }
    //檢查機器是否支援相機功能
    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void releaseCamera() {
        // stop and release camera
        if (m_Camera_myCam != null) {
            m_Camera_myCam.release();
            m_Camera_myCam = null;
        }
    }
    private Camera.AutoFocusCallback getFocusCallBack(final boolean shutter){
        Camera.AutoFocusCallback focus = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.i("Camera","autoFocus:"+String.valueOf(success));


                if(success && shutter)
                    getCameraPic();
                else if(!success)
                    Toast.makeText(getApplicationContext(), "對焦失敗",Toast.LENGTH_SHORT).show();

            }
        };
        return  focus;
    }
    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                String filePath = getOutputMediaFilePath();
                File pictureFile = new File(filePath);

                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Toast toast = Toast.makeText(MainActivity.this, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
                    toast.show();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
                Log.i("shot","bytes:"+String.valueOf(data.length));
                //refresh camera to continue preview
                //m_CameraPreview_perview.refreshCamera(m_Camera_myCam);
                //不能在intent中放入超過40kb資料
                Intent go2ImgActvty = new Intent(getBaseContext(), ImageActivity.class);
                go2ImgActvty.putExtra("jpgPath",filePath);
                startActivity(go2ImgActvty);
                m_btn_shut.setSelected(false);
            }
        };
        return picture;
    }
    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    private static String getOutputMediaFilePath() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "MyCamera");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //File mediaFile;
        //and make a media file:
        //mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
    }
    public void onResume() {
        super.onResume();
        if (!hasCamera(this)) {
            Toast toast = Toast.makeText(this, "不支援相機!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (m_Camera_myCam == null) {
            openCamera(findBackFacingCamera());

        }
    };
    private void openCamera(int cameraId){

        if (cameraId >= 0) {

            m_Camera_myCam = Camera.open(cameraId);
//            複寫相機自動對焦設定，圖片寬高設定
            Camera.Parameters myParameters = m_Camera_myCam.getParameters();

            myParameters.setFocusMode(myParameters.FOCUS_MODE_AUTO);
            myParameters.setPreviewSize(m_metrics.heightPixels ,m_metrics.widthPixels);
            myParameters.setPictureSize(m_metrics.heightPixels ,m_metrics.widthPixels);
            Log.i("setPreviewSize", m_metrics.widthPixels + "x" + m_metrics.heightPixels);
            m_Camera_myCam.setParameters(myParameters);
            mPicture = getPictureCallback();
            m_CameraPreview_perview.refreshCamera(m_Camera_myCam);
            m_Camera_myCam.setDisplayOrientation(90);
            m_Camera_myCam.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    Log.i("findFace!!", String.valueOf(faces.length));
                }
            });
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }
    private void FocusAreaRect(Rect myArea){

        Camera.Parameters setting = m_Camera_myCam.getParameters();
        if(setting.getMaxNumFocusAreas() == 0)
            return;
        Rect focusArea = new Rect();
        Log.i("Rect", "rect:"+myArea.toShortString());
        focusArea.set(myArea.left * 2000 / m_CameraPreview_perview.getWidth() - 1000,
                myArea.top * 2000 / m_CameraPreview_perview.getHeight() - 1000,
                myArea.right * 2000 / m_CameraPreview_perview.getWidth() - 1000,
                myArea.bottom * 2000 / m_CameraPreview_perview.getHeight() - 1000);

        // Submit focus area to camera

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(focusArea, 1000));

        setting.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        setting.setFocusAreas(focusAreas);
        try{
            m_Camera_myCam.setParameters(setting);
        }catch(Exception e){e.printStackTrace();}

    }

}

