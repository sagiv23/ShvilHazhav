package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.utils.ImageUtil;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a table of {@link ImageData} objects, intended for administrator use.
 * <p>
 * This adapter displays images in a grid or table format, allowing an administrator to view
 * all uploaded medication-related images used in the memory game. It provides actions for
 * viewing the image in full screen and deleting specific images from the database.
 * </p>
 */
public class MedicationImagesTableAdapter extends BaseAdapter<ImageData, MedicationImagesTableAdapter.ViewHolder> {
    private final ImageUtil imageUtil;
    private OnImageActionListener listener;

    /**
     * Constructs a new MedicationImagesTableAdapter.
     * @param imageUtil A utility class for loading and processing images from Base64 strings.
     */
    @Inject
    public MedicationImagesTableAdapter(ImageUtil imageUtil) { this.imageUtil = imageUtil; }

    /**
     * Sets the listener for image-related interactions (clicks and deletes).
     * @param listener The {@link OnImageActionListener} to handle events.
     */
    public void setListener(OnImageActionListener listener) { this.listener = listener; }

    /**
     * Updates the data set with a new list of images.
     * @param images The new list of {@link ImageData} objects to display.
     */
    public void setImages(List<ImageData> images) { setData(images); }

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

        String displayId = data.getId() != null ? data.getId() : "אנונימי";
        holder.txtId.setVisibility(View.VISIBLE);
        holder.txtId.setText(String.format("ID: %s", displayId));

        holder.btnDelete.setVisibility(View.VISIBLE);
        imageUtil.loadImage(data.getBase64(), holder.imgView);

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

    /** Interface for handling user interactions with image items in the table. */
    public interface OnImageActionListener {
        /**
         * Called when the delete button for an image is clicked.
         * @param image The {@link ImageData} object to be deleted.
         */
        void onDeleteImage(ImageData image);

        /**
         * Called when an image is clicked, typically to trigger a full-screen view.
         * @param image The {@link ImageData} object that was clicked.
         * @param imageView The {@link ImageView} containing the clicked image.
         */
        void onImageClicked(ImageData image, ImageView imageView);
    }

    /** ViewHolder class for image items in the table. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** ImageView displaying the card's content. */
        final ImageView imgView;

        /** TextView displaying the unique ID of the image. */
        final TextView txtId;

        /** Button for deleting the image from the database. */
        final MaterialButton btnDelete;

        /**
         * Constructs a new ViewHolder.
         * @param itemView The view representing a single image card item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.img_ItemCard_card_image);
            txtId = itemView.findViewById(R.id.txt_ItemCard_image_id);
            btnDelete = itemView.findViewById(R.id.btn_ItemCard_delete);
        }
    }
}