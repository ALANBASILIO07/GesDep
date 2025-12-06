package com.uaemex.gesdep.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.uaemex.gesdep.R;

import java.util.List;

public class ReportPhotoAdapter extends RecyclerView.Adapter<ReportPhotoAdapter.PhotoViewHolder> {

    private List<Uri> photos;
    private OnPhotoRemoveListener removeListener;

    public interface OnPhotoRemoveListener {
        void onRemove(int position);
    }

    public ReportPhotoAdapter(List<Uri> photos, OnPhotoRemoveListener removeListener) {
        this.photos = photos;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photos.get(position);

        Glide.with(holder.itemView.getContext())
                .load(photoUri)
                .centerCrop()
                .into(holder.ivPhoto);

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemovePhoto);
        }
    }
}
