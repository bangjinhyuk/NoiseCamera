package com.hada.noise_camera;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;


import java.io.ByteArrayOutputStream;
import java.io.File;


public class CameraActivity extends AppCompatActivity {

    private TextureView mCameraTextureView;
    private CameraTextureView mPreview;
    private Button mNormalAngleButton;
    private Button mWideAngleButton;
    private Button mCameraCaptureButton;
    private Button mCameraDirectionButton;
    private ImageView grid, noiseimg, title, bt_gallery;
    private Mat matInput;
    Activity mainActivity = this;
    private int width,height;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Mat noise_img;
    private static final String TAG = "CAMERAACTIVITY";
    public static Context mContext;

    static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Display display = getWindowManager().getDefaultDisplay();

        width = display.getWidth();
        height = display.getHeight();

        Log.d("width", "onCreate: "+width);


        mCameraCaptureButton = (Button) findViewById(R.id.bt_shutter);
        mCameraDirectionButton = (Button) findViewById(R.id.camera_front_back);
        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        noiseimg = (ImageView) findViewById(R.id.noiseimg);
        grid = (ImageView) findViewById(R.id.grid);
        title = (ImageView) findViewById(R.id.title);
        bt_gallery = (ImageView) findViewById(R.id.bt_gallery);

        //화면의 가로 길이를구하여 세로길이를 4대3 비율로 정해줌
        mCameraTextureView.getLayoutParams().height= width*4/3;
        noiseimg.getLayoutParams().height= width*4/3;
        grid.getLayoutParams().height= width*4/3;


        //버튼 크기 정해주기
        Log.d("amolang2",width+"->"+width*15/100);
        mCameraCaptureButton.getLayoutParams().width = width*20/100;
        mCameraCaptureButton.getLayoutParams().height = width*20/100;
        mCameraDirectionButton.getLayoutParams().width = width*10/100;
        mCameraDirectionButton.getLayoutParams().height = width*10/100;
        title.getLayoutParams().width = width*30/100;
        bt_gallery.getLayoutParams().width = width*12/100;
        bt_gallery.getLayoutParams().height = width*12/100;

        Log.d("width", "onCreate: "+ width+","+ width*4/3);

        //초반 앱을 켤때에 노이즈 필터 적용
        OpenCVLoader.initDebug();
        Bitmap bmp5120 = BitmapFactory.decodeResource(this.getResources(), R.drawable.img_2560);
        Log.d("width", "onCreate-bmp5120: "+ bmp5120.getWidth()+","+ bmp5120.getHeight());

        Bitmap resized = Bitmap.createScaledBitmap(bmp5120,1440, 1920, true);
        matInput = new Mat();
        Utils.bitmapToMat(resized, matInput);
        Mat noise = new Mat(matInput.size(), matInput.type());
        Mat noise_1 = new Mat(matInput.size(), matInput.type());


        MatOfDouble mean = new MatOfDouble ();
        MatOfDouble dev = new MatOfDouble ();
        Core.meanStdDev(matInput,mean,dev);
        Core.randn(noise,0.0, 45.0);
        Core.randn(noise_1,0.0, 65.0);
        Matrix matrix = new Matrix();
        Bitmap noisebmp = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, false);
        Utils.matToBitmap(noise,noisebmp);
        noiseimg.setImageBitmap(noisebmp);
        Log.d("width", "onCreate-noisebmp: "+ noisebmp.getWidth()+","+ noisebmp.getHeight());
        bmp5120.recycle();
        this.noise_img = noise_1;
        setRecentImageView();
        mContext = this;

    }

    public Mat getNoise_img(){
        return this.noise_img;
    }

    public void setRecentImageView(){
        //최신 사진 가져오는 부분
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, //the album it in
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getApplicationContext().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");

        if (cursor.moveToFirst()) {
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {   // TODO: is there a better way to do this?

                Bitmap image = BitmapFactory.decodeFile(imageLocation);//loading the large bitmap is fine.
                int w = image.getWidth(); //get width
                int h = image.getHeight(); //get height
                int aspRat = w / h; //get aspect ratio
                int W = width*15/100; //do whatever you want with width. Fixed, screen size, anything
                int H;
                if (aspRat>0) {
                    H = W * aspRat; //set the height based on width and aspect ratio
                }else {
                    H = W;
                }
                Log.d(TAG, "onCreate: width"+width+""+aspRat);
                Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);//scale the bitmap
                bt_gallery.setImageBitmap(b);//set the image view
                image.recycle();//save memory on the bitmap called 'image'
            }
        }
    }
    private Bitmap compressBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,40, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        return compressedBitmap;
    }


    @Override
    protected void onResume() {
        super.onResume();
//        mPreview.onResume();
        Log.d(TAG, "CameraTextureView called");
        mPreview = new CameraTextureView(this, mCameraTextureView, mNormalAngleButton, mWideAngleButton, mCameraCaptureButton, mCameraDirectionButton,noiseimg,mainActivity,width,bt_gallery);
        Log.d(TAG, "openCamera called");
        mPreview.openCamera();
        Log.d(TAG, "onResume called");
        mPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }

    public void onClickGallery(View view) {
        Intent gallery = new Intent(getApplicationContext(), GalleryView.class);
        startActivity(gallery);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
        finishAndRemoveTask();
    }
}