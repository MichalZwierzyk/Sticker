package com.mz.sticker.screen;

import android.app.Activity;
import android.os.Bundle;

import butterknife.ButterKnife;

public abstract class BaseNoActionBarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
    }

}
