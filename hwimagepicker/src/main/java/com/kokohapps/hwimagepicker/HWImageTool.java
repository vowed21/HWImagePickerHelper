package com.kokohapps.hwimagepicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Woo on 2016-10-23.
 */

public class HWImageTool {

    private static String TAG = "HWImageTool";
    private static final Bitmap.CompressFormat formatDefault = Bitmap.CompressFormat.JPEG;
    private static final int qualityDefault = 90;




    public static void saveBitmap(Bitmap bitmap, File targetFile, Bitmap.CompressFormat format, int quality) throws  IOException {
        FileOutputStream fos = new FileOutputStream(targetFile, false);
        bitmap.compress(format, quality, fos);
        fos.close();
    }


    public static Bitmap getBitmapInSampleSize(int maxWidth, File imageFile, boolean isFileOverWrite) throws IOException, OutOfMemoryError{

        if(imageFile == null || !imageFile.exists()){
            throw new IOException("File Not Found");
        }

        //일단 회전값 가져온다.
        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifRotateDegree = exifOrientationToDegrees(exifOrientation);

        Log.e(TAG, "회전각도 : " + exifRotateDegree);


        //원본 사진의 가로 세로 사이즈를 가져온다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        Log.e(TAG, "원본사진의 가로 : " + options.outWidth + " 세 로 : " + options.outHeight);

        int stand = options.outWidth;
        if(exifRotateDegree == 90 || exifRotateDegree == 270){
            stand = options.outHeight;
        }

        Log.e(TAG, "기준이 되는 길이 : " + stand + " 현재 바운더리 기준 : " + maxWidth);
        int sampleSize = 1;
        if (stand > maxWidth) {
            sampleSize = stand / maxWidth;
            if(sampleSize >8){
                sampleSize = 8;
            }else if(sampleSize > 4){
                sampleSize = 4;
            }else if(sampleSize > 2){
                sampleSize = 2;
            }
        }

        Log.e(TAG, "최종 샘플사이즈 : " + sampleSize);

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        //실제 샘플사이즈로 이미지를 가져왔다.
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //이제 회전시킨다.
        bitmap = rotateImage(bitmap, exifRotateDegree);

        //이제 실제 maxWidth 로 리사이즈해준다.
        bitmap = resizeImage(bitmap, maxWidth);

        //파일을 대치할거라면 저장해준다.
        if(isFileOverWrite){
            saveBitmap(bitmap, imageFile, formatDefault, qualityDefault);
        }

        return bitmap;
    }




    private static int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }




    //실제로 이미지 회전시키기.
    private static Bitmap rotateImage(Bitmap bitmap, int degrees) throws OutOfMemoryError {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (bitmap != converted) {
                bitmap.recycle();
                bitmap = converted;
            }
        }
        return bitmap;
    }

    //실제로 maxWidth 에 맞게 리사이즈 해주기.

    private static Bitmap resizeImage(Bitmap bitmap, int maxWidth){
        if(bitmap.getWidth() > maxWidth){
            float resizeFactor = maxWidth * 1f / bitmap.getWidth();
            int targetWidth = (int) (bitmap.getWidth() * resizeFactor);
            int targetHeight = (int) (bitmap.getHeight() * resizeFactor);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
            bitmap.recycle();
            bitmap = resizedBitmap;
        }
        return bitmap;
    }

}
