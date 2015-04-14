package com.mz.sticker.screen;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mz.sticker.R;
import com.mz.sticker.adapter.StoreAdapter;

import java.util.ArrayList;

import butterknife.InjectView;

public class StickersStoreActivity extends BaseNoActionBarActivity {

    @InjectView(R.id.store_recycler_view)
    RecyclerView storeRecyclerView;

    private RecyclerView.Adapter storeAdapter;

    private RecyclerView.LayoutManager storeLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_stickers_store);
        super.onCreate(savedInstanceState);

        // use a list layout manager for stickers store
        storeLayoutManager = new LinearLayoutManager(this);
        storeRecyclerView.setLayoutManager(storeLayoutManager);

        // specify an adapter for stickers store
        storeAdapter = new StoreAdapter(this, null);
        storeRecyclerView.setAdapter(storeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStoreItems();
    }

    private void setStoreItems() {
        // In the future release distinguish here what kind of store items to show
        ArrayList<Integer> storeItems = new ArrayList<>();
        storeItems.add(0);
        storeItems.add(1);
        storeItems.add(2);
        storeItems.add(3);
        storeItems.add(4);
        storeItems.add(5);
        storeItems.add(6);
        storeItems.add(7);
        storeItems.add(8);
        storeItems.add(9);
        storeItems.add(10);
        storeItems.add(11);
        storeItems.add(12);
        ((StoreAdapter) storeAdapter).setStoreItems(storeItems);
        storeAdapter.notifyDataSetChanged();
    }

}
