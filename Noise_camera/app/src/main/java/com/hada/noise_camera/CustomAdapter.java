package com.hada.noise_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>{
    ArrayList<String> items = new ArrayList<String>();
    Context mContext;
    private OnItemClickListener mListener = null;
    private final RequestManager glide;
    private int mWidth,recyclerViewWidth;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iv;

        public CustomViewHolder(View view) {
            super(view);
            this.iv = (ImageView) view.findViewById(R.id.id_listitem);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION) {
                        if(mListener != null){
                            mListener.onItemClick(v,position);
                        }
                    }
                }
            });
        }
    }

    public CustomAdapter(Context context,ArrayList<String> list, RequestManager glide, int width) {
        this.items = list;
        this.mContext = context;
        this.glide = glide;
        this.mWidth = width;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams)viewHolder.itemView.getLayoutParams();
        layoutParams.height = mWidth/3;
        viewHolder.itemView.requestLayout();

        Log.d("CustomViewHolder", "CustomViewHolder: "+layoutParams.height+" "+layoutParams.width+" "+mWidth);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewHolder, int position) {
        viewHolder.iv.setPadding(2,2,2,2);

        viewHolder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        RequestOptions myOptions = new RequestOptions()
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE);
//        Glide.with(mContext).asBitmap().apply(myOptions).load(items.get(position)).into(viewHolder.iv);
        glide.load(items.get(position)).override(300, 300).into(viewHolder.iv);
    }


    @Override
    public int getItemCount(){
        return items.size();
    }
}
