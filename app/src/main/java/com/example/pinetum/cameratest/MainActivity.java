package com.example.pinetum.cameratest;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.apache.http.HttpResponse;


public class MainActivity extends ActionBarActivity {

    final String SERVER_URL ="http://google.com.tw";

    private Button      btn_perView;
    private Button      btn_shot;
    private Button      btn_sent2server;
    private SurfaceView sfv_perview;
    private TextView    textV_output;
    private File        f_tempImage;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;
    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;
    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        btn_perView        = (Button)       findViewById(R.id.id_btn_preview);
        btn_shot           = (Button)       findViewById(R.id.id_btn_shot);
        btn_sent2server    = (Button)       findViewById(R.id.id_btn_sent2server);
        sfv_perview        = (SurfaceView)  findViewById(R.id.id_surfaceView_perView);
        textV_output       = (TextView)     findViewById(R.id.id_textView_output);

        btn_shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("I","btn_shot click");
                //--TODO stop perview

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            Log.e("params","add file to params fail:" + e.toString());
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

                    byte[] buf = new byte[1024];
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
