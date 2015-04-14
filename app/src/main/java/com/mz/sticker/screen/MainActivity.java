package com.mz.sticker.screen;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import com.mz.sticker.R;
import com.mz.sticker.camera.CameraUtil;
import com.mz.sticker.crop.Crop;
import com.mz.sticker.stick.Stick;
import com.mz.sticker.toast.ToastManager;

import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseNoActionBarActivity {

    @InjectView(R.id.cameraButton)
    Button cameraButton;

    @InjectView(R.id.galleryButton)
    Button galleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CameraUtil.CAPTURE_MEDIA_ACTIVITY_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    Uri lastCapturedMediaUri = CameraUtil.getLastCapturedMediaUri();
                    new Crop(lastCapturedMediaUri).output(lastCapturedMediaUri).asSquare().start(this, false);
                }
                else if(resultCode != Activity.RESULT_CANCELED) {
                    ToastManager.showToast(R.string.cannot_capture_picture, Toast.LENGTH_LONG);
                }
                break;
            case Crop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    Uri outputUri = (Uri) data.getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
                    new Stick(outputUri).output(outputUri).asSquare().start(this, false);
                }
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.cameraButton)
    public void openCamera() {
        CameraUtil.captureImage(this);
    }

    @OnClick(R.id.galleryButton)
    public void openGallery() {
        startActivity(new Intent(this, GalleryActivity.class));
    }

}
