package com.mz.sticker.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mz.sticker.R;
import com.mz.sticker.application.StickerApplication;
import com.mz.sticker.screen.StickersPaletteActivity;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Integer> storeItems;
    private LayoutInflater layoutInflater;

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView storeItemImageView;
        public TextView storeItemTitleTextView;
        public TextView storeItemDescrTextView;
        public Button storeItemButton;
        public ViewHolder(LinearLayout storeItemLayout) {
            super(storeItemLayout);
            storeItemImageView = (ImageView) storeItemLayout.findViewById(R.id.store_item_image);
            storeItemTitleTextView = (TextView) storeItemLayout.findViewById(R.id.store_item_title_text);
            storeItemDescrTextView = (TextView) storeItemLayout.findViewById(R.id.store_item_descr_text);
            storeItemButton = (Button) storeItemLayout.findViewById(R.id.store_item_button);
        }
    }

    public StoreAdapter(Activity activity, ArrayList<Integer> storeItems) {
        this.activity = activity;
        this.storeItems = storeItems;
        layoutInflater = (LayoutInflater) StickerApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        storeItems.clear();
    }

    public void addStoreItem(int storeItem) {
        storeItems.add(storeItem);
    }

    public void setStoreItems(ArrayList<Integer> storeItems) {
        this.storeItems = storeItems;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public StoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout storeItemLayout =  (LinearLayout) layoutInflater.inflate(R.layout.store_item_view, parent, false);
        ViewHolder vh = new ViewHolder(storeItemLayout);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int storeItem = storeItems.get(position);
        holder.storeItemImageView.setImageResource(R.mipmap.ic_launcher);
        holder.storeItemTitleTextView.setText(activity.getString(R.string.store_item_title, storeItem));
        holder.storeItemDescrTextView.setText(activity.getString(R.string.store_item_descr, storeItem));
        holder.storeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Purchase stickers pallete from store and add them to users stickers
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.purchase_dialog_message).setTitle(R.string.purchase_dialog_title);
                builder.setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(storeItems == null) {
            return 0;
        }
        return storeItems.size();
    }
}
