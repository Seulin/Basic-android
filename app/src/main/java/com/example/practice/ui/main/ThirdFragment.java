package com.example.practice.ui.main;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

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

import com.example.practice.BuildConfig;
import com.example.practice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.soundcloud.android.crop.Crop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
       // String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        //File file = new File(Environment.getExternalStorageDirectory(), url);
        //mImageCaptureUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID+".fileprovider", file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri(saveFileName));
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    private Uri getImageUri(String saveFile) {
        // 임시로 사용할 파일의 경로를 생성
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/DCIM", saveFolderName);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if(saveFile != null){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + saveFile);
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "pic_"+ timeStamp + ".jpg");
        }
        mImageCaptureUri = FileProvider.getUriForFile(getContext(),BuildConfig.APPLICATION_ID+".fileprovider", mediaFile);
        imagePath = mImageCaptureUri.getPath();
        Log.e("mImageCaptureUri : ", mImageCaptureUri.toString());
        Log.e("imagePath : ", imagePath);

        return mImageCaptureUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case Crop.REQUEST_CROP: {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                final Bundle extras = data.getExtras();
                String filePath = getImageUri(saveFileName).getPath();

                Log.e("mImageCaptureUri : ", "Croped " + mImageCaptureUri.toString());

                imagePath = filePath;

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    Log.e("bitmapfac", imagePath);
                    saveCropImage(photo,imagePath);
                    Log.d("extras", photo+"");
                    photo = BitmapFactory.decodeFile(imagePath);
                    resultView.setImageBitmap(photo);
                }
                break;

                // 임시 파일 삭제
/*                File f = new File(mImageCaptureUri.getPath());
                if(f.exists()) {
                    f.delete();
                }
                break;*/
            }
            case PICK_FROM_ALBUM: {
                mImageCaptureUri = data.getData();
                //Log.e("앨범이미지 CROP",mImageCaptureUri.getPath().toString());
                imagePath = getRealPathFromURI(mImageCaptureUri); // 실제 파일이 존재하는 경로
                Log.e("앨범이미지 경로",imagePath);
            }
            case PICK_FROM_CAMERA: {
                Log.d("onActivityResult", "request pick");
                beginCrop(mImageCaptureUri);
                break;
            }
        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void saveCropImage(Bitmap bitmap, String filePath) {
        //read image file
        File copyFile = new File(filePath);
        BufferedOutputStream bos = null;
        try {
            copyFile.createNewFile();
            int quality = 100;
            bos = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            // 이미지가 클 경우 OutOfMemoryException 발생이 예상되어 압축
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void beginCrop(Uri source) {
        Log.d("beginCrop", "start");
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getContext(),this);
        //start(Activity activity) 부분을 start(Context context, Fragment fragment)로 변경
    }

}
