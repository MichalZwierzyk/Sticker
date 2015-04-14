package com.mz.sticker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.mz.sticker.adapter.GalleryAdapter;
import com.mz.sticker.camera.CameraUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Task to list all files names in particular directory
 */
public class ListFilesInGalleryTask extends AsyncTask<Void, Void, ArrayList<String>> {

    private Context context;
    private GalleryAdapter galleryAdapter;

    public ListFilesInGalleryTask(Context context, GalleryAdapter galleryAdapter) {
        this.context = context;
        this.galleryAdapter = galleryAdapter;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... v) {
        File mediaStorageDir = CameraUtil.getOutputMediaDir(context);
        File[] filesInGallery = mediaStorageDir.listFiles();
        ArrayList<String> photosFiles = new ArrayList<>();
        for(File file : filesInGallery) {
            photosFiles.add(file.getAbsolutePath());
        }
        return photosFiles;
    }

    @Override
    protected void onPostExecute(ArrayList<String> photosFiles) {
        galleryAdapter.setPhotosFiles(photosFiles);
        galleryAdapter.notifyDataSetChanged();
    }

}
