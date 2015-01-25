package com.example.pinetum.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.hardware.Camera;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Date;


import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.CameraInfo;
import android.widget.Toast;


public class MainActivity extends Activity {
    public static Bitmap           m_bitmap_shutter;



    private Button          m_btn_shot;
    private Camera          m_Camera_myCam;
    private FrameLayout     m_frameLayout_main;
    private CameraPreview   m_CameraPreview_perview;
    private PictureCallback mPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_perview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_btn_shot          = new Button(this);
        m_frameLayout_main        = (FrameLayout) findViewById(R.id.id_frameLayout_main);
        m_CameraPreview_perview            = new CameraPreview(this, m_Camera_myCam);
        m_btn_shot.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        m_btn_shot.setText("asdafdgsfhgjhg");
        FrameLayout.LayoutParams LP_shutterBtn = new FrameLayout.LayoutParams(m_btn_shot.getWidth(), m_btn_shot.getHeight());
        LP_shutterBtn.gravity = Gravity.CENTER|Gravity.BOTTOM;

        m_frameLayout_main.addView(m_CameraPreview_perview, FrameLayout.LayoutParams.WRAP_CONTENT );
        m_frameLayout_main.addView(m_btn_shot, LP_shutterBtn);
        m_CameraPreview_perview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Camera_myCam.autoFocus(getFocusCallBack(false));
            }
        });
        m_btn_shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Camera_myCam.autoFocus(getFocusCallBack(true));

            }
        });
    }
    private void shutCamera(){
        m_Camera_myCam.takePicture(null, null, mPicture);//raw postView jpg callback
    }
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
                    shutCamera();
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
            Toast toast = Toast.makeText(this, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (m_Camera_myCam == null) {
            openCamera(findBackFacingCamera());
            m_Camera_myCam.setDisplayOrientation(90);
            m_Camera_myCam.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    Log.i("findFace!!", String.valueOf(faces.length));
                }
            });
        }
    };
    private void openCamera(int cameraId){

        if (cameraId >= 0) {

            m_Camera_myCam = Camera.open(cameraId);
            Camera.Parameters myParameters = m_Camera_myCam.getParameters();

            myParameters.setFocusMode("auto");
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            myParameters.setPreviewSize(metrics.heightPixels ,metrics.widthPixels);
            myParameters.setPictureSize(metrics.heightPixels ,metrics.widthPixels);
            Log.i("setPreviewSize", metrics.widthPixels + "x" + metrics.heightPixels);
            m_Camera_myCam.setParameters(myParameters);
            mPicture = getPictureCallback();
            m_CameraPreview_perview.refreshCamera(m_Camera_myCam);
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }


}

