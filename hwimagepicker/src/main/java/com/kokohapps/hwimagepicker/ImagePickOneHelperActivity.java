package com.kokohapps.hwimagepicker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by Woo on 2016-10-22.
 */

public abstract class ImagePickOneHelperActivity extends AppCompatActivity {

    private static final String TAG = "ImagePickerOneHelper";
    private static final int REQ_GALLERY = 701;
    private static final int REQ_CAMERA = 702;
    private static final int REQ_CROP = 703;

    private static final String WORKING_FILENAME = "work.jpg";

    private int mImageMaxWidth = 1024;
    private boolean mIsCrop = false;
    private String mRequestString;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("maxWith", mImageMaxWidth);
        outState.putBoolean("isCrop", mIsCrop);
        outState.putString("requestString", mRequestString);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageMaxWidth = savedInstanceState.getInt("maxWidth", 1024);
        mIsCrop = savedInstanceState.getBoolean("isCrop", false);
        mRequestString = savedInstanceState.getString("requestString");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_GALLERY && resultCode == RESULT_OK) {
            // 갤러리의 경우 곧바로 data에 uri가 넘어옴.
            Uri uri = data.getData();
            if(copyUriToFile(uri, getWorkingFile())){   //일단 파일 복사를 시도해서 성공한다면.
                if(mIsCrop){
                    //크롭 모드라면, 크롭을 시작하고,
                    cropImage();
                }else{
                    //크롭 모드가 아니라면, 이미지 후처리를 해서 내보내준다.
                   handleResultImage();
                }
            }

        } else if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            //크롭 모드라면,
            if(mIsCrop){
                cropImage();
            }else{
                handleResultImage();
            }
        }else if (requestCode == REQ_CROP && resultCode == RESULT_OK){
            handleResultImage();
        }

    }

    private void handleResultImage(){
        try {
            //크롭 모드가 아니라면, 이미지 후처리를 해서 내보내준다.
            Bitmap bitmap = HWImageTool.getBitmapInSampleSize(mImageMaxWidth, getWorkingFile(), true);
            this.onImageSelected(bitmap, getWorkingFile(), mRequestString);
        }
        catch (IOException e){
            Log.e(TAG, "결과 이미지 핸들링 익셉션");
            e.printStackTrace();
        }
    }

    protected void imagePickerStartFromGallery(int maxWidth, boolean isCropImage, String requestString){
        mImageMaxWidth = maxWidth;
        mIsCrop = isCropImage;
        mRequestString = requestString;

        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(MediaStore.Images.Media.CONTENT_TYPE);
        i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(Intent.createChooser(i, "Select Picture"), REQ_GALLERY);
        startActivityForResult(i, REQ_GALLERY);
    }

    protected void imagePickerStartFromCamera(int maxWidth, boolean isCropImage, String requestString){
        mImageMaxWidth = maxWidth;
        mIsCrop = isCropImage;
        mRequestString = requestString;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getWorkingFile()));
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQ_CAMERA);
    }



    private void cropImage() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> cropToolLists = getPackageManager().queryIntentActivities(intent, 0);
        int size = cropToolLists.size();
        if (size == 0) {
            // crop 을 처리 할 앱이 없음. 곧바로 처리.
            Toast.makeText(this, "NotFound Crop Apps", Toast.LENGTH_LONG).show();
            handleResultImage();
        } else {
            intent.setData(Uri.fromFile(getWorkingFile()));
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("output", Uri.fromFile(getWorkingFile()));
            Intent i = new Intent(intent);
            ResolveInfo res = cropToolLists.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, REQ_CROP);
        }
    }



    //원본 이미지 Uri 를 임시 파일로 복사해두기.
    private boolean copyUriToFile(Uri srcUri, File target)  {
        try{
            FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(srcUri);
            FileOutputStream outputStream = new FileOutputStream(target);

            // 채널 생성.
            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            // 채널을 통한 스트림 전송.
            fcin.transferTo(0, fcin.size(), fcout);

            fcout.close();
            fcin.close();
            outputStream.close();
            inputStream.close();
            return true;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    private File getWorkingFile(){
        File cacheDir = getExternalCacheDir();
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        return new File(cacheDir, WORKING_FILENAME);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = getWorkingFile();
        if(file.exists()){
            file.delete();
        }
    }


    //액티비티에 결과물 보내줄때.
    protected abstract void onImageSelected(Bitmap bitmap, File imageFile, String requestString);

}
