package com.example.sagivproject.adapters;

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

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for displaying a table of {@link ImageData} objects, intended for admin use.
 * <p>
 * This adapter is used to manage the images for the memory game. It displays each image
 * along with its ID and provides a delete button for each. It also handles click events
 * for viewing a full-size image.
 * </p>
 */
public class MedicationImagesTableAdapter extends RecyclerView.Adapter<MedicationImagesTableAdapter.ViewHolder> {
    private final OnImageActionListener listener;
    private final List<ImageData> imageList;

    /**
     * Constructs a new MedicationImagesTableAdapter.
     *
     * @param listener The listener for image actions (delete, click).
     */
    public MedicationImagesTableAdapter(OnImageActionListener listener) {
        this.listener = listener;
        this.imageList = new ArrayList<>();
    }

    public void setImages(List<ImageData> images) {
        imageList.clear();
        imageList.addAll(images);
        notifyDataSetChanged();
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
            holder.txtId.setText(String.format("ID: %s", data.getId()));
        } else {
            holder.txtId.setVisibility(View.GONE);
        }

        // The delete button is always visible in this admin-focused adapter.
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

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    /**
     * An interface for handling actions performed on an image item in the table.
     */
    public interface OnImageActionListener {
        /**
         * Called when the delete button for an image is clicked.
         *
         * @param image The image data to be deleted.
         */
        void onDeleteImage(ImageData image);

        /**
         * Called when an image is clicked, intended for showing a larger view.
         *
         * @param image     The image data that was clicked.
         * @param imageView The ImageView that was clicked, to be used for transitions or dialogs.
         */
        void onImageClicked(ImageData image, ImageView imageView);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
