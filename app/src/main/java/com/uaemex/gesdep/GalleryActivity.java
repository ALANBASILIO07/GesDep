package com.uaemex.gesdep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        List<String> images = getIntent().getStringArrayListExtra("images");
        int position = getIntent().getIntExtra("position", 0);

        ViewPager2 viewPager = findViewById(R.id.viewPagerGallery);
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        if (images != null) {
            viewPager.setAdapter(new FullScreenAdapter(images));
            viewPager.setCurrentItem(position, false);
        }
    }

    class FullScreenAdapter extends RecyclerView.Adapter<FullScreenAdapter.ViewHolder> {
        private List<String> urls;
        public FullScreenAdapter(List<String> urls) { this.urls = urls; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_fullscreen, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView).load(urls.get(position)).into((ImageView) holder.itemView);
        }
        @Override public int getItemCount() { return urls.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) { super(v); }
        }
    }
}