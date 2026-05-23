package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.utils.CalendarUtil;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a list of "Tip of the Day" entries.
 * <p>
 * This adapter is primarily used by administrators to view and filter historical tips.
 * </p>
 */
public class TipAdapter extends BaseAdapter<TipOfTheDay, TipAdapter.ViewHolder> {
    private final CalendarUtil calendarUtil;
    private boolean isAdmin = false;
    private OnTipActionListener listener;

    @Inject
    public TipAdapter(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }

    /**
     * Configures the adapter with admin privileges and a listener for tip actions.
     *
     * @param admin    True if the user has admin rights.
     * @param listener Callback for edit and delete actions.
     */
    public void setAdmin(boolean admin, OnTipActionListener listener) {
        this.isAdmin = admin;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TipOfTheDay tip = getItem(position);

        String displayDate;
        long millis = calendarUtil.parseDateFromDatabase(tip.getId());
        if (millis != -1) {
            displayDate = calendarUtil.formatDate(millis);
        } else {
            displayDate = "";
        }

        if (displayDate.isEmpty()) {
            holder.tvDate.setVisibility(View.GONE);
        } else {
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvDate.setText(displayDate);
        }
        holder.tvContent.setText(tip.getTip());

        if (isAdmin) {
            holder.adminActions.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(tip);
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(tip);
            });
        } else {
            holder.adminActions.setVisibility(View.GONE);
        }
    }

    /**
     * Interface definition for a callback to be invoked when an action is performed on a tip.
     */
    public interface OnTipActionListener {
        /**
         * Called when a tip's edit button is clicked.
         *
         * @param tip The tip to be edited.
         */
        void onEdit(TipOfTheDay tip);

        /**
         * Called when a tip's delete button is clicked.
         *
         * @param tip The tip to be deleted.
         */
        void onDelete(TipOfTheDay tip);
    }

    /**
     * ViewHolder class for tip items.
     */
    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView tvDate, tvContent;
        final View adminActions, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_itemTip_date);
            tvContent = itemView.findViewById(R.id.tv_itemTip_content);
            adminActions = itemView.findViewById(R.id.layout_itemTip_admin_actions);
            btnEdit = itemView.findViewById(R.id.btn_itemTip_edit);
            btnDelete = itemView.findViewById(R.id.btn_itemTip_delete);
        }
    }
}
