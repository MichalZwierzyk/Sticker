package com.mz.sticker.screen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.mz.sticker.R;
import com.mz.sticker.adapter.GalleryAdapter;
import com.mz.sticker.camera.CameraUtil;
import com.mz.sticker.crop.Crop;
import com.mz.sticker.stick.Stick;
import com.mz.sticker.task.ListFilesInGalleryTask;
import com.mz.sticker.toast.ToastManager;
import com.mz.sticker.util.ScreenUtil;

import butterknife.InjectView;

public class GalleryActivity extends BaseNoActionBarActivity {

    @InjectView(R.id.gallery_recycler_view)
    RecyclerView galleryRecyclerView;

    private RecyclerView.Adapter galleryAdapter;

    private RecyclerView.LayoutManager galleryLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_gallery);
        super.onCreate(savedInstanceState);

        // use a grid layout manager for gallery
        Point displayArea = ScreenUtil.getAppDisplayArea();
        int spanCount = (int) Math.max(1, displayArea.x / getResources().getDimension(R.dimen.gallery_photo_width));
        int gridItemWidth = displayArea.x / spanCount;
        galleryLayoutManager = new GridLayoutManager(this, spanCount);
        galleryRecyclerView.setLayoutManager(galleryLayoutManager);

        // specify an adapter for gallery
        galleryAdapter = new GalleryAdapter(this,gridItemWidth, (int) getResources().getDimension(R.dimen.gallery_photo_margin), null);
        galleryRecyclerView.setAdapter(galleryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ListFilesInGalleryTask(this, (GalleryAdapter) galleryAdapter).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Crop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    Uri outputUri = (Uri) data.getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
                    new Stick(outputUri).output(outputUri).asSquare().start(this, true);
                }
                break;
            default:
                break;
        }
    }

}
