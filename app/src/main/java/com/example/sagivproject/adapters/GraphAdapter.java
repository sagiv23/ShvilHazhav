package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.GraphData;
import com.example.sagivproject.ui.SimpleXYGraphView;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a list of graphs, typically used within a {@link androidx.viewpager2.widget.ViewPager2}.
 * <p>
 * This adapter manages the mapping between {@link GraphData} objects and the custom
 * {@link SimpleXYGraphView}. It is designed to display time-series data such as
 * game wins, math accuracy, and medication compliance.
 * </p>
 */
public class GraphAdapter extends BaseAdapter<GraphData, GraphAdapter.GraphViewHolder> {

    /**
     * Constructs a new GraphAdapter.
     */
    @Inject
    public GraphAdapter() {
    }

    @NonNull
    @Override
    public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_graph, parent, false);
        return new GraphViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GraphViewHolder holder, int position) {
        GraphData data = getItem(position);
        holder.graphView.setData(data.getPoints(), data.getXLabels(), data.getTitle(), data.getLabelX(), data.getLabelY());
    }

    /**
     * ViewHolder class for individual graph items.
     */
    public static class GraphViewHolder extends RecyclerView.ViewHolder {
        /**
         * The custom view responsible for rendering the XY graph.
         */
        final SimpleXYGraphView graphView;

        /**
         * Constructs a new GraphViewHolder.
         *
         * @param itemView The view representing a single graph container.
         */
        public GraphViewHolder(@NonNull View itemView) {
            super(itemView);
            graphView = itemView.findViewById(R.id.graph_view);
        }
    }
}