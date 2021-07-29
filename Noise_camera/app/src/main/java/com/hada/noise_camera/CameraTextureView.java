package com.hada.noise_camera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

public class  CameraTextureView extends Thread {
    private final static String TAG = "CameraTextureView : ";

    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;
    private String mCameraId = "0";
    private Button mNormalAngleButton;
    private Button mWideAngleButton;
    private Button mCameraCaptureButton;
    private Button mCameraDirectionButton;
    private Mat matInput;
    private ImageView noise_img,bt_gallery;
    private Activity mainActivity;
    private int width;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);
    private ParcelFileDescriptor pdf;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public CameraTextureView(Context context, TextureView textureView, Button button1, Button button2, Button button3, Button button4,ImageView noiseimg,Activity getmainActivity,int getwidth,ImageView btgallery) {
        mContext = context;
        mTextureView = textureView;
        noise_img = noiseimg;
        mainActivity = getmainActivity;
        mNormalAngleButton = button1;
        mWideAngleButton = button2;
        mCameraCaptureButton = button3;
        mCameraDirectionButton = button4;
        width = getwidth;
        bt_gallery = btgallery;
//        mNormalAngleButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                onPause();
//                mCameraId = "0";
//                openCamera();
//            }
//        });

//        mWideAngleButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                onPause();
//                mCameraId = "2";
//                openCamera();
//            }
//        });

        mCameraCaptureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture(1);
            }
        });

        mCameraDirectionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPause();
                if (mCameraId.equals("1")) {
                    mCameraId = "0";
                } else {
                    mCameraId = "1";
                }
                openCamera();
            }
        });
    }

    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = getBackFacingCameraId(manager);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
            int permissionWEXStor = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionREXStor = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_DENIED|| permissionWEXStor == PackageManager.PERMISSION_DENIED|| permissionREXStor == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQUEST_CAMERA);
            } else {
                manager.openCamera(mCameraId, mStateCallback, null);


            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // TODO Auto-generated method stub
            DisplayMetrics outMetrics = new DisplayMetrics();
            mainActivity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            int px = outMetrics.densityDpi;
            Log.e(TAG, "onSurfaceTextureAvailable, width=" + width + ", height=" + height + ", density=" + px);
//            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    //딜레이 후 시작할 코드 작성
                    startPreview();
                }
            }, 600);


        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");
        }

    };

    protected void startPreview() {
        // TODO Auto-generated method stub
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (null == texture) {
            Log.e(TAG, "texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected void updatePreview() {
        // TODO Auto-generated method stub
        if (null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Runnable mDelayPreviewRunnable = new Runnable() {
        @Override
        public void run() {
            startPreview();

        }
    };

    protected void takePicture(int type) {
        if (null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }

        try {
            Size[] jpegSizes = null;
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
//                Log.d("TEST", "map != null " + jpegSizes.length);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
//                for (int i = 0 ; i < jpegSizes.length; i++) {
//                    Log.d("TEST", "getHeight = " + jpegSizes[i].getHeight() + ", getWidth = " + jpegSizes[i].getWidth());
//                }
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(2560, 1920, ImageFormat.JPEG, 1);
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.RAW_SENSOR, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            // Orientation
            int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");

            final File file = new File(Environment.getExternalStorageDirectory() +"/DCIM/Camera/"+ dateFormat.format(date) + ".jpg");
//            System.out.println("2222222222222"+file.getPath().toString());
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.Images.Media.DISPLAY_NAME, "/pic_" + dateFormat.format(date) +".JPG");
//            values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
////            values.put(MediaStore.Images.Media.MIME_TYPE, getMIMEType(fileRoot));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                values.put(MediaStore.Images.Media.IS_PENDING, 1);
//            }
//            ContentResolver contentResolver = mContext.getContentResolver();
//            Uri item = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            System.out.println("------"+item);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
//                        pdf = contentResolver.openFileDescriptor(item, "w", null);

                        image = reader.acquireLatestImage();

//                        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_1847);
//                        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
//                        bmp.compress(Bitmap.CompressFormat.JPEG,40, stream1);
//                        byte[] byteArray = stream1.toByteArray();
//                        bmp = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        Log.d("bytelength", bytes.length+"");
                        buffer.get(bytes);
                        OpenCVLoader.initDebug();
//                        int bmpwidth = 1280*2;  //이미지의 너비
//
//                        int bmpheight = 960*2; //이미지의 높이
//                        Bitmap bmp = Bitmap.createBitmap(bmpwidth, bmpheight, Bitmap.Config.ARGB_8888);
//                        ByteBuffer bmpbuffer = ByteBuffer.wrap(bytes);
//                        bmpbuffer.rewind();
//                        bmp.copyPixelsFromBuffer(bmpbuffer);

                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

//                        imageView.setImageBitmap(bitmapps);
                        Log.d("bytelength", bmp.getWidth()+""+bmp.getHeight());


//                        Bitmap resized_bmp =  resizeBitmap(bmp,mTextureView.getHeight(),mTextureView.getWidth());
//                        imageView.setImageBitmap(bmp);
                        matInput = new Mat();
//

                        Utils.bitmapToMat(bmp, matInput);

//

                        Mat noise = new Mat(matInput.size(), matInput.type());
//                        Log.d("data.lengthSize", bytes.length+""+matInput.type());
//                        Log.d("Bitmap.lengthSize", bmp.getWidth()+""+bmp.getHeight());
//                        Log.d("matInputSize", matInput.size()+"");

                        MatOfDouble mean = new MatOfDouble ();
                        MatOfDouble dev = new MatOfDouble ();
                        Core.meanStdDev(matInput,mean,dev);

//                        Core.randn(noise,mean.get(0,0)[0], dev.get(0,0)[0]);
                        Core.randn(noise,0.0, 50.0);

                        Core.add(matInput, noise, matInput);

                        Utils.matToBitmap(matInput,bmp);
                        Core.rotate(matInput,matInput,Core.ROTATE_90_CLOCKWISE);


                        // 이미지 중심으로 90도 회전 Matrix
                        Matrix matrix = new Matrix();
                        matrix.preRotate(90, 0, 0);
                        // 이미지 회전
                        Bitmap mbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
                        Utils.matToBitmap(matInput,mbmp);


                        Core.rotate(noise,noise,Core.ROTATE_90_CLOCKWISE);



                        Bitmap noisebmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
                        Utils.matToBitmap(noise,noisebmp);

//                        mainActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d("please", "run");
////                                imageView.setImageBitmap(bmp);
//                                noise_img.setImageBitmap(noisebmp);
//                            }
//                        });
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mbmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                        noisebmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] bytes1 = stream.toByteArray();
                        save(bytes1);
                        //                        save(bytes);
                        Log.d(TAG, "save()");
                        mContext.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));


                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (image != null) {
                            image.close();
                            reader.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        setRecentImageView();
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);
            final Handler delayPreview = new Handler();

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(mContext, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    delayPreview.postDelayed(mDelayPreviewRunnable, 1000);
//                    startPreview();
                }

            };



            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
        final Cursor cursor = mainActivity.getApplicationContext().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");

        if (cursor.moveToFirst()) {
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {   // TODO: is there a better way to do this?

                Bitmap image = BitmapFactory.decodeFile(imageLocation);//loading the large bitmap is fine.
                int w = image.getWidth();//get width
                int h = image.getHeight();//get height
                int aspRat = w / h;//get aspect ratio
                int W = width*15/100;//do whatever you want with width. Fixed, screen size, anything
                int H;
                if (aspRat>0) {
                    H = W * aspRat;//set the height based on width and aspect ratio
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
    public void setSurfaceTextureListener() {
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        setSurfaceTextureListener();
//        openCamera();

    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d(TAG, "CameraDevice Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

//    static public Bitmap resizeBitmap(Bitmap original, int width, int height) {
//
//        Bitmap result = Bitmap.createScaledBitmap(original, width, height, false);
//        if (result != original) {
//            original.recycle();
//        }
//        return result;
//    }

}


