package com.example.sagivproject.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.utils.ImageUtil;

import java.util.List;

public class MedicationImagesTableAdapter extends RecyclerView.Adapter<MedicationImagesTableAdapter.ViewHolder> {
    private final List<ImageData> imageList;
    private final OnImageActionListener listener;

    public interface OnImageActionListener {
        void onDeleteImage(ImageData image);
        void onImageClicked(ImageData image);
    }

    public MedicationImagesTableAdapter(List<ImageData> imageList, OnImageActionListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageData data = imageList.get(position);

        if (data.getId() != null) {
            holder.txtId.setVisibility(View.VISIBLE);
            holder.txtId.setText("ID: " + data.getId());
        } else {
            holder.txtId.setVisibility(View.GONE);
        }

        holder.btnDelete.setVisibility(View.VISIBLE);

        if (data.getBase64() != null) {
            Bitmap bitmap = ImageUtil.convertFrom64base(data.getBase64());
            holder.imgView.setImageBitmap(bitmap);
        }

        holder.imgView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClicked(data);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteImage(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView txtId;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_ItemCard_card_image);
            txtId = itemView.findViewById(R.id.txt_ItemCard_image_id);
            btnDelete = itemView.findViewById(R.id.btn_ItemCard_delete);
        }
    }
}