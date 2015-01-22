package com.example.pinetum.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by Pinetum on 2015/1/22.
 */
public class ImageActivity extends Activity{

    private ImageView m_imgV_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        m_imgV_main = (ImageView) findViewById(R.id.id_imageView_rst);
        Intent m_intent = getIntent();
        byte[] jpgfile = m_intent.getByteArrayExtra("jpgBytes");
        if(jpgfile.length < 1 ){

            finish();
        }
        else{

            //m_imgV_main.setImageBitmap();
        }
    }
}
