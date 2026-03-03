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
 * <p>
 * This adapter displays images in a grid or table format, allowing an administrator to view
 * all uploaded medication-related images and perform actions like deletion or full-screen viewing.
 * </p>
 */
public class MedicationImagesTableAdapter extends BaseAdapter<ImageData, MedicationImagesTableAdapter.ViewHolder> {
    private final ImageUtil imageUtil;
    private OnImageActionListener listener;

    /**
     * Constructs a new MedicationImagesTableAdapter.
     *
     * @param imageUtil A utility for loading and processing images.
     */
    @Inject
    public MedicationImagesTableAdapter(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    /**
     * Sets the listener for image-related actions.
     *
     * @param listener The listener to be notified of user actions.
     */
    public void setListener(OnImageActionListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of images to be displayed.
     *
     * @param images The new list of {@link ImageData} objects.
     */
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

        // Display the image ID if available
        if (data.getId() != null) {
            holder.txtId.setVisibility(View.VISIBLE);
            holder.txtId.setText(String.format("ID: %s", data.getId()));
        } else {
            holder.txtId.setVisibility(View.GONE);
        }

        holder.btnDelete.setVisibility(View.VISIBLE);
        imageUtil.loadImage(data.getBase64(), holder.imgView);

        // Handle image click for full-screen view
        holder.imgView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClicked(data, holder.imgView);
            }
        });

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteImage(data);
            }
        });
    }

    /**
     * An interface for handling user interactions with image items in the table.
     */
    public interface OnImageActionListener {
        /**
         * Called when the delete button for an image is clicked.
         *
         * @param image The image data to be deleted.
         */
        void onDeleteImage(ImageData image);

        /**
         * Called when an image is clicked, typically to show it in full screen.
         *
         * @param image     The image data that was clicked.
         * @param imageView The ImageView containing the clicked image (for transitions).
         */
        void onImageClicked(ImageData image, ImageView imageView);
    }

    /**
     * A ViewHolder that describes an image item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final ImageView imgView;
        final TextView txtId;
        final ImageButton btnDelete;

        /**
         * Initializes the ViewHolder with the item view.
         *
         * @param itemView The view representing a single image card.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_ItemCard_card_image);
            txtId = itemView.findViewById(R.id.txt_ItemCard_image_id);
            btnDelete = itemView.findViewById(R.id.btn_ItemCard_delete);
        }
    }
}
