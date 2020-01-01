package com.example.practice.ui.main.Third;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.net.Uri;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
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
import com.example.practice.ui.main.First.Dictionary;
import com.example.practice.ui.main.First.FirstViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.soundcloud.android.crop.Crop;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubFilter;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
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

    static String Number;


    //private LogAdapter logAdapter;
    FirstViewModel mViewModel;

    protected Button fromcamera, fromgallery, crop, Filter1, Filter2, Filter3, Filter4, sendmessage;
    private ImageView resultView;

    public static ThirdFragment newInstance() {
        return new ThirdFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(FirstViewModel.class); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.third_fragment, container, false);
        View resultview = inflater.inflate(R.layout.third_resultview, container, false);

        System.loadLibrary("NativeImageProcessor");

        resultView = (ImageView) view.findViewById(R.id.result_image);
        fromcamera = view.findViewById(R.id.fromcamera);
        fromgallery = view.findViewById(R.id.fromgallery);
        crop = view.findViewById(R.id.crop);
        Filter1 = view.findViewById(R.id.filter1);
        Filter2 = view.findViewById(R.id.filter2);
        Filter3 = view.findViewById(R.id.filter3);
        Filter4 = view.findViewById(R.id.filter4);

        sendmessage = view.findViewById(R.id.sendmessage);
        fromcamera.setOnClickListener(this);
        fromgallery.setOnClickListener(this);
        crop.setOnClickListener(this);
        Filter1.setOnClickListener(this);
        Filter2.setOnClickListener(this);
        Filter3.setOnClickListener(this);
        Filter4.setOnClickListener(this);

        sendmessage.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        BitmapDrawable drawable;
        Bitmap inputImage;
        Bitmap outputImage;
        Filter myFilter;

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
            case R.id.filter1:
                myFilter = new Filter();
                myFilter.addSubFilter(new VignetteSubFilter(v.getContext(), 100));
                resultView.invalidate();
                drawable = (BitmapDrawable) resultView.getDrawable();
                inputImage = drawable.getBitmap();
                outputImage = myFilter.processFilter(inputImage);
                resultView.setImageBitmap(outputImage);
                break;

            case R.id.filter2:
                myFilter = new Filter();
                myFilter.addSubFilter(new ColorOverlaySubFilter(100, .2f, .2f, .0f));
                resultView.invalidate();
                drawable = (BitmapDrawable) resultView.getDrawable();
                inputImage = drawable.getBitmap();
                outputImage = myFilter.processFilter(inputImage);
                resultView.setImageBitmap(outputImage);
                break;

            case R.id.filter3:
                myFilter = new Filter();
                myFilter.addSubFilter(new ContrastSubFilter(1.2f));
                resultView.invalidate();
                drawable = (BitmapDrawable) resultView.getDrawable();
                inputImage = drawable.getBitmap();
                outputImage = myFilter.processFilter(inputImage);
                resultView.setImageBitmap(outputImage);
                break;

            case R.id.filter4:
                myFilter = new Filter();
                myFilter.addSubFilter(new BrightnessSubFilter(30));
                resultView.invalidate();
                drawable = (BitmapDrawable) resultView.getDrawable();
                inputImage = drawable.getBitmap();
                outputImage = myFilter.processFilter(inputImage);
                resultView.setImageBitmap(outputImage);
                break;

            case R.id.sendmessage:
                pickContact(); //Number is assigned
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                imagePath = getRealPathFromURI(getContext(), resultUri);
                InputStream in = getContext().getContentResolver().openInputStream(result.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                if (bitmap != null) {
                    Log.d("bitmap!, imagePath", imagePath+"");
                    Bitmap resultbitmap = checkRotate(imagePath, bitmap);
                    resultView.setImageBitmap(resultbitmap);
                }
            } catch (Exception error) { error.printStackTrace(); }
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                Log.d("onActivityResult", "pick from camera");
                resultUri = Uri.fromFile(new File(imagePath));
                Log.d("cameraResult", resultUri+"");
                //resultUri = result.getData(); It caused error. result.getData() == null
                File file = new File(imagePath);
                Log.d("cameraResultsecond", Uri.fromFile(file)+"");
                //////////// same code below
                Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                if (bitmap2 != null) {
                    Bitmap resultbitmap2 = checkRotate(imagePath, bitmap2);
                    Log.d("bitmap2!, imagePath", imagePath+"");
                    resultView.setImageBitmap(resultbitmap2);
                }
            } catch (Exception error) { error.printStackTrace(); Log.d("errorcamera", resultUri+"");}
            ///////// please check before revise this!!

        } else if (requestCode == Crop.REQUEST_CROP) { //CROP된 이미지
            Log.d("beforehandlecrop" , "requestcrop");
            handleCrop(resultCode, result);
        }
    }

    private void pickContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an address");
// add a radio button list
        //String[] contacts;
        ArrayList<String> contactsArr = new ArrayList<>();
        final String[] contacts = new String[mViewModel.getSize()];
        for (Dictionary dict : mViewModel.getList()) {
            contactsArr.add(dict.getName()+" ("+dict.getNumber()+") ");
        }
        contactsArr.toArray(contacts);

        int checkedItem = 0; // cow
        builder.setSingleChoiceItems(contacts, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Number = contacts[which];
                // user checked an item
            }
        });
// add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BUTTON_NEGATIVE:
                        Number = null;
                        // int which = -2
                        dialog.dismiss();
                        break;
                    case BUTTON_POSITIVE:
                        // int which = -1
                        Toast.makeText(getActivity(), Number, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        break;
            }}
        });
        builder.setNegativeButton("Cancel", null);
// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void beginCrop(Uri source) { //if from camera source=file:///storage/emulated/0/tmp_1577811465781.jpg
                                        //if from album source=content://media/external/imgaes/media/43
        Log.d("beginCropscource:", source+"");
        File file = new File(getActivity().getCacheDir(), "cropped");
        Uri destination = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
        Crop.of(source, destination).asSquare().start(getContext(), this);
        //start(Activity activity) 부분을 start(Context context, Fragment fragment)로 변경
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            // Activity 의 RESULT_OK값을 사용
            resultUri = (Crop.getOutput(result));//imagepath써
            try {//////////// same code above
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Crop.getOutput(result));
                if (bitmap != null) {
                    Bitmap resultbitmap = checkRotate(imagePath, bitmap);
                    Log.d("bitmap!!handle,Path", imagePath + "");
                    resultView.setImageBitmap(resultbitmap);
                }
            } catch (Exception error) { error.printStackTrace(); Log.d("errorcamera", resultUri+"");}
            ///////// please check before revise this!!

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

