package com.hada.noise_camera;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class SelectGalleryView extends AppCompatActivity {

    private int currentPosition = 0;
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();
    private ImageView currentImage;
    private TextView textView;
    private ImageButton cancleButton;
    private ImageButton returnButton;
    private ImageView bigWord;
    private LinearLayout linearLayout;
    private final int DELETE_PERMISSION_REQUEST = 0x1033;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gallery_view);
        Bundle bundle = getIntent().getExtras();
        currentPosition = bundle.getInt("position");
        getAllPhotos();

        Display display = getWindowManager().getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();


        textView = (TextView)findViewById(R.id.count);
        currentImage = (ImageView)findViewById(R.id.currentImage);
        currentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        returnButton = (ImageButton) findViewById(R.id.return_button);
        cancleButton = (ImageButton) findViewById(R.id.cancelButton);
        bigWord = (ImageView)findViewById(R.id.big_word);

        ConstraintLayout.LayoutParams mLayoutParams = (ConstraintLayout.LayoutParams) linearLayout.getLayoutParams();
        mLayoutParams.topMargin = height * 6 /100;
        mLayoutParams.bottomMargin = height * 14/100;
        linearLayout.setLayoutParams(mLayoutParams);

        ConstraintLayout.LayoutParams cl = (ConstraintLayout.LayoutParams) returnButton.getLayoutParams();
        cl.leftMargin = width * 4 / 100;
        returnButton.setLayoutParams(cl);

        ConstraintLayout.LayoutParams cl2 = (ConstraintLayout.LayoutParams) cancleButton.getLayoutParams();
        cl2.rightMargin = width * 4 / 100;
        cancleButton.setLayoutParams(cl2);

        returnButton.getLayoutParams().height = height * 3/100;
        returnButton.getLayoutParams().width = width * 3/100;
        cancleButton.getLayoutParams().height = (int)(height * 3/100);
        cancleButton.getLayoutParams().width = (int) (width * 5/100);
        bigWord.getLayoutParams().height = height * 3/100;
        bigWord.getLayoutParams().width = width * 30/100;
        textView.getLayoutParams().height = height * 4/100;
        textView.getLayoutParams().width = width * 25/100;
        textView.setText((currentPosition+1)+"/"+urls.size());

        Glide.with(this).load(urls.get(currentPosition)).fitCenter().into(currentImage);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),GalleryView.class);
                startActivity(intent);
                finish();
            }
        });

        cancleButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            ids.get(currentPosition).toString()
                    );
                    ContentResolver contentResolver = getContentResolver();
                    contentResolver.delete(contentUri,null,null);
                } catch (RecoverableSecurityException e) {
                    IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                    try {
                        startIntentSenderForResult(intentSender, DELETE_PERMISSION_REQUEST,null,0,0,0,null);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
                getAllPhotos();
                currentImage = (ImageView)findViewById(R.id.currentImage);
                currentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(getApplicationContext()).load(urls.get(currentPosition)).into(currentImage);
            }
        });
    }

    private void getAllPhotos() {
        urls.clear();
        ids.clear();
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,null,null,MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");

        if(cursor != null) {
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
                urls.add(url);
            }
            cursor.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ids.get(currentPosition).toString()
                );
                ContentResolver contentResolver = getContentResolver();
                contentResolver.delete(contentUri,null,null);

                getAllPhotos();
                currentImage = (ImageView)findViewById(R.id.currentImage);
                currentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this).load(urls.get(currentPosition)).into(currentImage);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),GalleryView.class);
        startActivity(intent);
        finish();

    }
}