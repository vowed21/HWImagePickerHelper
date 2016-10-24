package com.kokohapps.hwimagepickersample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import com.kokohapps.hwimagepicker.ImagePickOneHelperActivity;

import java.io.File;

public class SampleActivity extends ImagePickOneHelperActivity {

    private ImageView imageView;
    private String TAG = "SampleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        imageView = (ImageView) findViewById(R.id.imageView);
    }



    public void clickCamera(View view){
        imagePickerStartFromCamera(1024, false, null);
    }

    public void clickAlbum(View view){
        imagePickerStartFromGallery(1024, false, null);
    }


    @Override
    protected void onImageSelected(Bitmap bitmap, File imageFile, String requestString) {
        this.imageView.setImageBitmap(bitmap);
    }
}



