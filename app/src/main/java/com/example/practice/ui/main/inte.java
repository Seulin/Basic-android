/*
package com.example.practice.ui.cameracrop;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practice.R;

import java.io.File;

public class inte extends AppCompatActivity {

    private static final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 545;
    private static final int REQUEST_CODE_PROFILE_IMAGE_CROP = 2103;
    private static final String TYPE_IMAGE = "image/*";
    private static final int PROFILE_IMAGE_ASPECT_X = 3;
    private static final int PROFILE_IMAGE_ASPECT_Y = 1;
    private static final int PROFILE_IMAGE_OUTPUT_X = 600;
    private static final int PROFILE_IMAGE_OUTPUT_Y = 200;
    private static final String TEMP_FILE_NAME = "tempFile.jpg";

    private Uri mTempImageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_fragment);
        findViewById( R.id.add ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE, null);
                startActivityForResult( intent, REQUEST_CODE_PROFILE_IMAGE_CAPTURE );
            }
        });
    }

    private File getTempFile(){
        File file = new File( Environment.getExternalStorageDirectory(), TEMP_FILE_NAME );
        try{
            file.createNewFile();
        }
        catch( Exception e ){
            Log.e("cklee", "fileCreation fail" );
        }
        return file;
    }

    private Uri getJustTakenPictureUri() {
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (cursor == null) return null;
        String fileName = null;
        if (cursor.moveToLast())
            fileName = cursor.getString(0);
        cursor.close();

        if (TextUtils.isEmpty(fileName)) return null;
        return Uri.fromFile(new File(fileName));
    }

    private void doCrop(){
        Uri justTakenPictureUri = getJustTakenPictureUri();
        mTempImageUri = Uri.fromFile( getTempFile() );
        Intent intent = new Intent( "com.android.camera.action.CROP" );
        intent.setDataAndType( justTakenPictureUri, TYPE_IMAGE );
        intent.putExtra( "scale", true );
        intent.putExtra( "aspectX", PROFILE_IMAGE_ASPECT_X );
        intent.putExtra( "aspectY", PROFILE_IMAGE_ASPECT_Y );
        intent.putExtra( "outputX", PROFILE_IMAGE_OUTPUT_X);
        intent.putExtra( "outputY", PROFILE_IMAGE_OUTPUT_Y);
        intent.putExtra( MediaStore.EXTRA_OUTPUT, mTempImageUri );
        startActivityForResult( intent,  REQUEST_CODE_PROFILE_IMAGE_CROP );
    }


}
*/
