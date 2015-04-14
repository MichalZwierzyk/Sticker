package com.mz.sticker.test;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.LinearLayout;

import com.mz.sticker.R;
import com.mz.sticker.screen.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mainTestActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mainTestActivity = getActivity();
    }

    public void testPreconditions() {
        assertNotNull("mainTestActivity is null", mainTestActivity);
    }

    @SmallTest
    public void testIsAppConfigurationAccurate() {
        String expected = "Sticker";
        String actual = mainTestActivity.getString(R.string.app_name);
        assertEquals(expected, actual);
        expected = "com.mz.sticker";
        actual = mainTestActivity.getPackageName();
        assertEquals(expected, actual);
        PackageInfo pinfo = null;
        try {
            pinfo = mainTestActivity.getPackageManager().getPackageInfo(mainTestActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int appVersionCode = pinfo.versionCode;
        String appVersionName = pinfo.versionName;
        assertEquals(appVersionCode, 1);
        assertEquals(appVersionName, "1.0");
    }

    @SmallTest
    public void testIsUICorrect() {
        assertNotNull(mainTestActivity.findViewById(R.id.main_activity_layout));
        assertNotNull(mainTestActivity.findViewById(R.id.cameraButton));
        assertNotNull(mainTestActivity.findViewById(R.id.galleryButton));

        LinearLayout linearLayout = (LinearLayout) mainTestActivity.findViewById(R.id.main_activity_layout);
        ViewAsserts.assertGroupContains(linearLayout, mainTestActivity.findViewById(R.id.cameraButton));
        ViewAsserts.assertGroupContains(linearLayout, mainTestActivity.findViewById(R.id.galleryButton));
    }

}
