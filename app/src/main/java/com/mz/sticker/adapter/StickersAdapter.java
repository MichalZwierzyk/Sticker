package com.mz.sticker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mz.sticker.R;
import com.mz.sticker.application.StickerApplication;
import com.mz.sticker.screen.StickersPaletteActivity;

import java.util.ArrayList;

public class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Integer> stickers;
    private LayoutInflater layoutInflater;

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView stickerImageView;
        public ViewHolder(ImageView stickerImageView) {
            super(stickerImageView);
            this.stickerImageView = stickerImageView;
        }
    }

    public StickersAdapter(Activity activity, ArrayList<Integer> stickers) {
        this.activity = activity;
        this.stickers = stickers;
        layoutInflater = (LayoutInflater) StickerApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        stickers.clear();
    }

    public void addSticker(int sticker) {
        stickers.add(sticker);
    }

    public void setStickers(ArrayList<Integer> stickers) {
        this.stickers = stickers;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public StickersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView stickerImageView =  (ImageView) layoutInflater.inflate(R.layout.sticker_item_view, parent, false);
        ViewHolder vh = new ViewHolder(stickerImageView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int sticker = stickers.get(position);
        holder.stickerImageView.setImageResource(sticker);
        holder.stickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setResult(Activity.RESULT_OK, new Intent().putExtra(StickersPaletteActivity.REQUEST_STICKER_ID, sticker));
                activity.finish();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(stickers == null) {
            return 0;
        }
        return stickers.size();
    }
}
