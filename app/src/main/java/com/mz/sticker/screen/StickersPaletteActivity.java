package com.mz.sticker.screen;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mz.sticker.R;
import com.mz.sticker.adapter.StickersAdapter;
import com.mz.sticker.util.ScreenUtil;

import java.util.ArrayList;

import butterknife.InjectView;

public class StickersPaletteActivity extends BaseNoActionBarActivity {

    public static final int REQUEST_STICKERS_PALETTE = 101;
    public static final String REQUEST_STICKERS_PALETTE_NUM = "request_stickers_palette_num";
    public static final String REQUEST_STICKER_ID = "request_sticker_id";

    @InjectView(R.id.stickers_recycler_view)
    RecyclerView stickersRecyclerView;

    private RecyclerView.Adapter stickersAdapter;

    private RecyclerView.LayoutManager stickersLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_stickers_palette);
        super.onCreate(savedInstanceState);

        // use a grid layout manager for stickers palette
        Point displayArea = ScreenUtil.getAppDisplayArea();
        int spanCount = (int) Math.max(1, displayArea.x / getResources().getDimension(R.dimen.stickers_palette_item_width));
        int gridItemWidth = displayArea.x / spanCount;
        stickersLayoutManager = new GridLayoutManager(this, spanCount);
        stickersRecyclerView.setLayoutManager(stickersLayoutManager);

        // specify an adapter for stickers palette
        stickersAdapter = new StickersAdapter(this, null);
        stickersRecyclerView.setAdapter(stickersAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int paletteNum = getIntent().getIntExtra(REQUEST_STICKERS_PALETTE_NUM, -1);
        setStickersPalette(paletteNum);
    }

    private void setStickersPalette(int paletteNum) {
        // In the future release distinguish here what kind of stickers to show
        ArrayList<Integer> stickers = new ArrayList<>();
        stickers.add(R.drawable.emo_im_angel);
        stickers.add(R.drawable.emo_im_cool);
        stickers.add(R.drawable.emo_im_crying);
        stickers.add(R.drawable.emo_im_embarrassed);
        stickers.add(R.drawable.emo_im_foot_in_mouth);
        stickers.add(R.drawable.emo_im_happy);
        stickers.add(R.drawable.emo_im_kissing);
        stickers.add(R.drawable.emo_im_laughing);
        stickers.add(R.drawable.emo_im_lips_are_sealed);
        stickers.add(R.drawable.emo_im_money_mouth);
        stickers.add(R.drawable.emo_im_sad);
        stickers.add(R.drawable.emo_im_surprised);
        ((StickersAdapter) stickersAdapter).setStickers(stickers);
        stickersAdapter.notifyDataSetChanged();
    }

}
