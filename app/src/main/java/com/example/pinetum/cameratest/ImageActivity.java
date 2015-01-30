package com.example.pinetum.cameratest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Created by Pinetum on 2015/1/22.
 */
public class ImageActivity extends Activity{


    private final String    SERVER_URL                  ="http://140.138.178.72/";
    //private final int       n_MAX_RESPONSE_FILE_SIZE    = 1024; //byte
    private ImageView       m_imageView_result;
    private Button          m_btn_sent2Server;
    private String          m_str_FilePath;
    private ProgressDialog  m_PGDialog;
    private EditText        m_editText_funName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        m_imageView_result      = (ImageView)   findViewById(R.id.id_imageView_result);
        m_btn_sent2Server       = (Button)      findViewById(R.id.id_btn_2server);
        m_editText_funName      = (EditText)    findViewById(R.id.id_editTexidt_function);
        Intent m_intent = getIntent();
        m_str_FilePath  = m_intent.getStringExtra("jpgPath");
        if(m_str_FilePath.length() < 1 ){

            finish();
        }
        else{
            Bitmap bImg = BitmapFactory.decodeFile(m_str_FilePath);

            m_imageView_result.setImageBitmap(bImg);

        }
        m_btn_sent2Server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File jpgFile = new File(m_str_FilePath);
                if (!jpgFile.canRead()){
                    new AlertDialog.Builder(ImageActivity.this)
                            .setMessage("檔案錯誤")
                            .setTitle("檔案錯誤")
                            .setPositiveButton("OK",null)
                            .show();

                }
                else{

                    postFile2Server(SERVER_URL+m_editText_funName.getText().toString(),
                                    "FileUpload",
                                    jpgFile);
                }
            }
        });

    }


    private boolean postFile2Server(String serverURL, String postKeyName, File file ){
        //提示等待窗
        m_PGDialog = ProgressDialog.show(ImageActivity.this,"請稍候","上傳中");
        m_PGDialog.setCancelable(false);
        m_PGDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //此處為用於存放用於上傳的檔案
        RequestParams params = new RequestParams();//建立key-value用於ＨＴＴＰ-ＰＯＳＴ
        AsyncHttpClient myPostFile = new AsyncHttpClient();//使用open的第三方同步httpLibrary(import com.loopj.android.http.*;)
        //把傳入的file放入value中
        //key需對應於server端取得requesr.form.file時的名稱
        try{
            params.put(postKeyName, file);
        } catch(FileNotFoundException e) {
            Log.e("params", "add file to params fail:" + e.toString());
        }

        myPostFile.post(serverURL,params,new FileAsyncHttpResponseHandler(this) {
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.e("HTTP","POST Faileure:");
                m_PGDialog.dismiss();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                m_PGDialog.setProgress(bytesWritten);
                m_PGDialog.setMax(totalSize);
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                //檔案轉Bitmap塞入ImagView中
                m_imageView_result.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
                Log.i("File",file.getPath());
                m_PGDialog.dismiss();

            }
        });

        return true;
    }

    @Override
    public void finish() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_from_left);
        super.finish();
    }
}
