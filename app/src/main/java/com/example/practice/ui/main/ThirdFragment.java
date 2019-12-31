package com.example.practice.ui.main;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.net.Uri;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.practice.BuildConfig;
import com.example.practice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.soundcloud.android.crop.Crop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class ThirdFragment extends Fragment implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private static Uri mImageCaptureUri;
    String saveFileName = "myphoto.jpg";
    private String imagePath;
    String saveFolderName = "cameraTemp";
    File mediaFile = null;

    private ThirdViewModel mViewModel;
    protected FloatingActionButton fromcamera;
    protected FloatingActionButton fromgallery;
    private ImageView resultView;

    public static ThirdFragment newInstance() {
        return new ThirdFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.third_fragment, container, false);
        View resultview = inflater.inflate(R.layout.third_resultview, container, false);

        resultView = (ImageView) view.findViewById(R.id.result_image);
        fromcamera = view.findViewById(R.id.fromcamera);
        fromgallery = view.findViewById(R.id.fromgallery);
        fromcamera.setOnClickListener(this);
        fromgallery.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fromcamera:
                doTakeCameraAction();
                break;
            case R.id.fromgallery:
                doTakeAlbumAction();
                break;
        }
        resultView.setImageDrawable(null);
        //Crop.pickImage(getActivity(), this);
        //Intent intent = new Intent(getActivity(), CameraCropActivity.class);
        //startActivity(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ThirdViewModel.class);
        // TODO: Use the ViewModel
    }

    private void doTakeAlbumAction() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private void doTakeCameraAction() {
        /*
         * 참고 해볼곳
         * http://2009.hfoss.org/Tutorial:Camera_and_Gallery_Demo
         * http://stackoverflow.com/questions/1050297/how-to-get-the-url-of-the-captured-image
         * http://www.damonkohler.com/2009/02/android-recipes.html
         * http://www.firstclown.us/tag/android/
         */
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(Environment.getExternalStorageDirectory(), url);
        mImageCaptureUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            // RESULT_OK 는 동작 성공을 의미하며 수치는 -1 인데, Fragment에는 없다.
            // 따라서, Activity에서 사용되는 RESULT_OK값을 가져와서 사용한다.
            Log.d("onActivityResult", "pick from album");
            beginCrop(result.getData());
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            // RESULT_OK 는 동작 성공을 의미하며 수치는 -1 인데, Fragment에는 없다.
            Uri mPicImageURI = null;
            if(mImageCaptureUri != null)
                mPicImageURI = mImageCaptureUri;
            else
                mPicImageURI = result.getData();
            Log.d("onActivityResult", "pick from camera");
            beginCrop(mPicImageURI);
        } else if (requestCode == Crop.REQUEST_CROP) { //CROP된 이미지
            Log.d("onActivityResult", "request crop");
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Log.d("beginCrop", "start");
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getContext(), this);
        //start(Activity activity) 부분을 start(Context context, Fragment fragment)로 변경
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            // Activity 의 RESULT_OK값을 사용
            Log.d("handleCrop", "RESULT_OK");
            resultView.setImageURI(Crop.getOutput(result));
/*           // 따라서, Activity에서 사용되는 RESULT_OK값을 가져와서 사용한다.
            Bundle extras = result.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            resultView.setImageBitmap(imageBitmap);*/
        } else if (resultCode == Crop.RESULT_ERROR) {
            Log.d("handleCrop", "RESULT_ERROR");
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
            //Activity에서 사용되던 this는 Fragment에서 보통 getActivity() 또는 getContext() 로 변경해서 사용한다.
        }
    }
}

