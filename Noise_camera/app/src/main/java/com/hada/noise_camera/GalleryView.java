package com.hada.noise_camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class GalleryView extends AppCompatActivity {

    private ArrayList<String> urls = new ArrayList<>();
    private CustomAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_view);

        Display display = getWindowManager().getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();

        getAllPhotos();

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        ImageView mRencently = (ImageView) findViewById(R.id.recently_gallery);
        ImageButton mXButton = (ImageButton) findViewById(R.id.gallery_x_button);

        ConstraintLayout.LayoutParams mLayoutParams = (ConstraintLayout.LayoutParams) mRecyclerView.getLayoutParams();
        mLayoutParams.topMargin = height * 7 /100;
        mRecyclerView.setLayoutParams(mLayoutParams);

        ConstraintLayout.LayoutParams cl = (ConstraintLayout.LayoutParams) mXButton.getLayoutParams();
        cl.leftMargin = width * 8 / 100;
        mXButton.setLayoutParams(cl);

        mRecyclerView.getLayoutParams().height = height * 90/100;
        mRencently.getLayoutParams().width = width * 20/100;
        mRencently.getLayoutParams().height = width * 6/100;
        mXButton.getLayoutParams().height = width * 6/100;
        mXButton.getLayoutParams().width = width * 6/100;

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,3);
        RecyclerView.ItemDecoration dividerItemDecoration =
                new DividerItemDecorator(ContextCompat.getDrawable(this,R.drawable.divider));
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.addItemDecoration(dividerItemDecoration);


        mAdapter = new CustomAdapter(this,urls, Glide.with(getApplicationContext()),width);
        mAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), SelectGalleryView.class);
                intent.putExtra("position",position);
                startActivity(intent);
                finish();
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void getAllPhotos() {
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

                urls.add(url);
            }
            cursor.close();
        }
    }

    public void onClickBackButton(View view) {
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
        finish();

    }
}