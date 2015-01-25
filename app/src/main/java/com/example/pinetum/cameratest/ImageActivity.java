package com.example.pinetum.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Pinetum on 2015/1/22.
 */
public class ImageActivity extends Activity{


    final String SERVER_URL ="http://google.com.tw";
    final int n_MAX_RESPONSE_FILE_SIZE = 1024; //byte
    private ImageView m_imageView_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        m_imageView_result      = (ImageView)   findViewById(R.id.id_imageView_result);
        Intent m_intent = getIntent();
        String jpgfilePath = m_intent.getStringExtra("jpgPath");
        if(jpgfilePath.length() < 10 ){

            finish();
        }
        else{
            Bitmap bImg = BitmapFactory.decodeFile(jpgfilePath);

            m_imageView_result.setImageBitmap(bImg);

        }
    }


    private boolean postFile2Server(URL serverURL, String postKeyName, File file ){
        //--TODO HTTP POST FILE

        //此處為用於存放用於上傳的檔案
        RequestParams params = new RequestParams();//建立key-value用於ＨＴＴＰ-ＰＯＳＴ
        AsyncHttpClient myPostFile = new AsyncHttpClient();//使用open的第三方同步httpLibrary(import com.loopj.android.http.*;)
        //把傳入的file放入value中
        //key需對應於server端取得requesr.form.file時的名稱，此處為cvt2gray
        try{
            params.put(postKeyName, file);
        } catch(FileNotFoundException e) {
            Log.e("params", "add file to params fail:" + e.toString());
        }

        myPostFile.post(SERVER_URL,params,new FileAsyncHttpResponseHandler(this) {
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.e("HTTP","POST Faileure:");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                byte[] mjpgBytes = null;
                Intent showImageIntent = new Intent(getApplicationContext(), ImageActivity.class);
                try{
                    FileInputStream fis = new FileInputStream(file);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] buf = new byte[n_MAX_RESPONSE_FILE_SIZE];
                    try {
                        for (int readNum; (readNum = fis.read(buf)) != -1;) {
                            bos.write(buf, 0, readNum); //no doubt here is 0
                            //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                            System.out.println("read " + readNum + " bytes,");
                        }
                    } catch (IOException ex) {
                        Log.e("File","cvtFile2Byte error");
                    }
                    mjpgBytes = bos.toByteArray();
                }catch (Exception e){
                    Log.e("File","cvtFile2Byte error" + e.toString());
                }


                showImageIntent.putExtra("jpgBytes", mjpgBytes);
                startActivity(showImageIntent);
            }
        });

        return true;
    }

}
