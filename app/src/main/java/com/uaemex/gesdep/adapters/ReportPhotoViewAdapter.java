package com.uaemex.gesdep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.uaemex.gesdep.R;

import java.util.List;

public class ReportPhotoViewAdapter extends RecyclerView.Adapter<ReportPhotoViewAdapter.PhotoViewHolder> {

    private List<String> photoUrls;

    public ReportPhotoViewAdapter(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_photo_view, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);

        Glide.with(holder.itemView.getContext())
                .load(photoUrl)
                .centerCrop()
                .into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }
}
