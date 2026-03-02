package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.utils.ImageUtil;

import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a table of {@link ImageData} objects, intended for admin use.
 */
public class MedicationImagesTableAdapter extends BaseAdapter<ImageData, MedicationImagesTableAdapter.ViewHolder> {
    private OnImageActionListener listener;

    @Inject
    public MedicationImagesTableAdapter() {
    }

    public void setListener(OnImageActionListener listener) {
        this.listener = listener;
    }

    public void setImages(List<ImageData> images) {
        setData(images);
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
        ImageData data = getItem(position);

        if (data.getId() != null) {
            holder.txtId.setVisibility(View.VISIBLE);
            holder.txtId.setText(String.format("ID: %s", data.getId()));
        } else {
            holder.txtId.setVisibility(View.GONE);
        }

        holder.btnDelete.setVisibility(View.VISIBLE);
        ImageUtil.loadImage(data.getBase64(), holder.imgView);

        holder.imgView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClicked(data, holder.imgView);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteImage(data);
            }
        });
    }

    public interface OnImageActionListener {
        void onDeleteImage(ImageData image);

        void onImageClicked(ImageData image, ImageView imageView);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final ImageView imgView;
        final TextView txtId;
        final ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_ItemCard_card_image);
            txtId = itemView.findViewById(R.id.txt_ItemCard_image_id);
            btnDelete = itemView.findViewById(R.id.btn_ItemCard_delete);
        }
    }
}
