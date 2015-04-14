package com.mz.sticker.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mz.sticker.R;
import com.mz.sticker.application.StickerApplication;
import com.mz.sticker.image.ImageLoader;
import com.mz.sticker.crop.Crop;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Activity activity;
    private int gridItemWidth;
    private int gridItemHeight;
    private int gridItemMargin;
    private ArrayList<String> photosFiles;
    private LayoutInflater layoutInflater;

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoImageView;
        public ViewHolder(ImageView photoImageView) {
            super(photoImageView);
            this.photoImageView = photoImageView;
        }
    }

    public GalleryAdapter(Activity activity, int gridItemWidth, int gridItemMargin, ArrayList<String> photosFiles) {
        this.activity = activity;
        this.gridItemWidth = gridItemWidth;
        gridItemHeight = (int) (gridItemWidth * 0.66);
        this.gridItemMargin = gridItemMargin;
        this.photosFiles = photosFiles;
        layoutInflater = (LayoutInflater) StickerApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        photosFiles.clear();
    }

    public void addPhotoFile(String photoFile) {
        photosFiles.add(photoFile);
    }

    public void setPhotosFiles(ArrayList<String> photosFiles) {
        this.photosFiles = photosFiles;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView photoImageView =  (ImageView) layoutInflater.inflate(R.layout.gallery_item_view, parent, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(gridItemWidth, gridItemHeight);
        layoutParams.setMargins(gridItemMargin, gridItemMargin, gridItemMargin, gridItemMargin);
        photoImageView.setLayoutParams(layoutParams);
        ViewHolder vh = new ViewHolder(photoImageView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String photoFileName = photosFiles.get(position);
        ImageLoader.loadBitmap(photoFileName, gridItemWidth, gridItemHeight, false, holder.photoImageView);
        holder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri photoUri = Uri.fromFile(new File(photoFileName));
                new Crop(photoUri).output(photoUri).asSquare().start(activity, true);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(photosFiles == null) {
            return 0;
        }
        return photosFiles.size();
    }
}