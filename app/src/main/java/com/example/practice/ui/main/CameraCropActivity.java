package com.example.practice.ui.main;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.example.practice.R;

public class CameraCropActivity extends Activity
{

    private Button mButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameracrop);

        mButton = (Button) findViewById(R.id.button);
       // mPhotoImageView = (ImageView) findViewById(R.id.image);

        //mButton.setOnClickListener(this);
    }}

    /**
     * 카메라에서 이미지 가져오기
     */

    /**
     * 앨범에서 이미지 가져오기
     */

/*    @Override
    public void onClick(View v)
    {
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakePhotoAction();
            }
        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakeAlbumAction();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();
    }}*/

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PROFILE_IMAGE_CAPTURE:
                doCrop();
                break;
            case REQUEST_CODE_PROFILE_IMAGE_CROP:
                File tempFile2 = getTempFile();
                if ( tempFile2.exists() )
                    ((ImageView)findViewById( R.id.result_image)).setImageBitmap( BitmapFactory.decodeFile( tempFile2.toString() ) );
                break;
        }
    }*/
