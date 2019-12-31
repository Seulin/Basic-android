package com.example.practice.ui.main.Third;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.net.Uri;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.practice.BuildConfig;
import com.example.practice.MainActivity;
import com.example.practice.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.soundcloud.android.crop.Crop;


import java.io.File;
import java.io.InputStream;

import static com.android.mms.logs.LogTag.TAG;

public class ThirdFragment extends Fragment implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int REQUEST_IMG_SEND = 30;
    boolean cameraImage; //camera로부터 받아온 이미지인지, 임시 생성한 file을 삭제하는 기준

    private static Uri mImageCaptureUri; // 카메라 - 임시 uri
    private static Uri resultUri; //pick from camera or gallery //위와 동일?
    private static Bitmap resultbitmap;
    private String imagePath; // 카메라:file.getAbsolutePath()로, in doTakeCameraAction
    //갤러리:getRealPathFromURI(Uri)로, in onActivityResult(),
    String saveFileName = "myphoto.jpg";
    String saveFolderName = "cameraTemp";
    File mediaFile = null;


    //private LogAdapter logAdapter;


    private ThirdViewModel mViewModel;
    protected Button fromcamera, fromgallery, crop, draw, sendmessage;
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
        crop = view.findViewById(R.id.crop);
        draw = view.findViewById(R.id.draw);
        sendmessage = view.findViewById(R.id.sendmessage);
        fromcamera.setOnClickListener(this);
        fromgallery.setOnClickListener(this);
        crop.setOnClickListener(this);
        draw.setOnClickListener(this);
        sendmessage.setOnClickListener(this);
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
            case R.id.crop:
                beginCrop(resultUri); //contain handlecrop
                break;
            case R.id.draw:
                //doTakeAlbumAction();
                break;
            case R.id.sendmessage:
                sendMMS("01066549455", resultUri);
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ThirdViewModel.class);
        // TODO: Use the ViewModel
    }

    public static Bitmap rotateImage(Bitmap source, float angle) { //회전하는 이미지를 원래대로 돌리는 함수
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void doTakeAlbumAction() {
        // 앨범 호출
        cameraImage = false;
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
        cameraImage = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(Environment.getExternalStorageDirectory(), url);
        mImageCaptureUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
        imagePath = file.getAbsolutePath();
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
            try {
                Log.d("onActivityResult", "pick from album");
                resultUri = result.getData(); //work at album, but not at camera!
                Log.d("albumResult", resultUri+"");
                imagePath = getRealPathFromURI(getContext(), resultUri);//////////// same code below
                InputStream in = getContext().getContentResolver().openInputStream(result.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                if (bitmap != null) {
                    Log.d("bitmap!, imagePath", imagePath+"");
                    resultbitmap = checkRotate(imagePath, bitmap);
                    resultView.setImageBitmap(resultbitmap);
                }
            } catch (Exception error) { error.printStackTrace(); }///////// please check before revise this!!
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                Log.d("onActivityResult", "pick from camera");
                //resultUri = (Crop.getOutput(result));
                Log.d("cameraResult", resultUri+"");
                //resultUri = result.getData(); It caused error. result.getData() == null
                File file = new File(imagePath);
                Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                if (bitmap2 != null) {
                    resultbitmap = checkRotate(imagePath, bitmap2);
                    Log.d("bitmap2!, imagePath", imagePath+"");
                    resultView.setImageBitmap(resultbitmap);
                }
            } catch (Exception error) { error.printStackTrace(); Log.d("errorcamera", resultUri+"");}
/*            Uri mPicImageURI = null;
            if (mImageCaptureUri != null)
                mPicImageURI = mImageCaptureUri;
            else
                mPicImageURI = result.getData();
            Log.d("onActivityResult", "pick from camera");
            resultUri = mPicImageURI;*/
        } else if (requestCode == Crop.REQUEST_CROP) { //CROP된 이미지
            Log.d("beforehandlecrop" , "requestcrop");
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Log.d("beginCrop", "start");
        File file = new File(getActivity().getCacheDir(), "cropped");
        Uri destination = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
        Crop.of(source, destination).asSquare().start(getContext(), this);
        //start(Activity activity) 부분을 start(Context context, Fragment fragment)로 변경
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {

            // Activity 의 RESULT_OK값을 사용
            resultUri = (Crop.getOutput(result));

            resultView.setImageURI(resultUri);
            //sendMMS(Crop.getOutput(result));
            if (cameraImage) {
                File f = new File(mImageCaptureUri.getPath());
                Log.d("if cameraImage??", "OK");
                if (f.exists()) {
                    f.delete();
                }
            } else if (resultCode == Crop.RESULT_ERROR) {
                Log.d("handleCrop", "RESULT_ERROR");
                Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
                //Activity에서 사용되던 this는 Fragment에서 보통 getActivity() 또는 getContext() 로 변경해서 사용한다.
            }
        }
    }

    private Bitmap checkRotate(String path, Bitmap bitmap) {
        try {
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
        } catch (Exception error) { error.printStackTrace(); }
        Bitmap nothing = null;
        return nothing;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void sendMMS(String phone, Uri imageUri) {

        Settings sendSettings = new Settings();

        sendSettings.setMmsc(" http://omms.nate.com:9082/oma_mms");
        sendSettings.setProxy("smart.nate.com");
        sendSettings.setPort("9093");
        sendSettings.setGroup(true);
        sendSettings.setDeliveryReports(false);
        sendSettings.setSplit(false);
        sendSettings.setSplitCounter(false);
        sendSettings.setStripUnicode(false);
        sendSettings.setSignature("");
        sendSettings.setSendLongAsMms(true);
        sendSettings.setSendLongAsMmsAfter(3);

        Log.d(TAG, "sendMMS(Method) : " + "start");

        String subject = "제목";
        String text = "내용";

        // 예시 (절대경로) : String imagePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20190312-181007.png";
        String imagePath = "이미지 경로";
        imagePath = imageUri.getPath();

        Log.d(TAG, "subject : " + subject);
        Log.d(TAG, "text : " + text);
        Log.d(TAG, "imagePath : " + imagePath);

/*        Settings settings = new Settings();
        settings.setUseSystemSending(true);*/


        // TODO : 이 Transaction 클래스를 위에 링크에서 다운받아서 써야함
        Transaction transaction = new Transaction(getContext(), sendSettings);

        // 제목이 있을경우
        Message message = new Message(text, phone, subject);

        // 제목이 없을경우
        // Message message = new Message(text, number);

        if (!(imagePath.equals("") || imagePath == null)) {
            // 예시2 (앱 내부 리소스) :
            // Bitmap mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mms_test_1);
            Bitmap mBitmap = BitmapFactory.decodeFile(imagePath);
            // TODO : image를 여러장 넣고 싶은경우, addImage를 계속호출해서 넣으면됨
            message.addImage(mBitmap);
        }

        long id = android.os.Process.getThreadPriority(android.os.Process.myTid());

        transaction.sendNewMessage(message, id);
        Toast.makeText(getActivity(), "successfully send", Toast.LENGTH_SHORT).show();
    }

}

