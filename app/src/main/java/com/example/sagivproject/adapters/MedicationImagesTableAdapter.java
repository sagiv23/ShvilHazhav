package com.example.sagivproject.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.utils.ImageUtil;

import java.util.List;

public class MedicationImagesTableAdapter extends RecyclerView.Adapter<MedicationImagesTableAdapter.ViewHolder> {
    private List<ImageData> imageList;

    public MedicationImagesTableAdapter(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageData data = imageList.get(position);

        if (data.getId() != null) {
            holder.txtId.setVisibility(View.VISIBLE);
            holder.txtId.setText("ID: " + data.getId());
        }

        if (data.getBase64() != null) {
            Bitmap bitmap = ImageUtil.convertFrom64base(data.getBase64());
            holder.imgView.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() { return imageList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView txtId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_ItemCard_card_image);
            txtId = itemView.findViewById(R.id.txt_ItemCard_image_id);
        }
    }
}