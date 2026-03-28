package com.example.sagivproject.models;

import com.example.sagivproject.ui.SimpleXYGraphView;

import java.util.List;
import java.util.Objects;

/**
 * Data model representing the information required to render a graph using {@link SimpleXYGraphView}.
 * <p>
 * This class encapsulates the title, data points, axis labels, and a unique identifier for a graph.
 * It is used by the {@link com.example.sagivproject.adapters.GraphAdapter} to display
 * statistical summaries in a ViewPager2.
 * </p>
 */
public class GraphData implements Idable {
    private final String title;
    private final List<SimpleXYGraphView.Point> points;
    private final List<String> xLabels;
    private final String labelX;
    private final String labelY;
    private String id;

    /**
     * Constructs a new GraphData object.
     *
     * @param id      The unique identifier for the graph.
     * @param title   The title displayed at the top of the graph.
     * @param points  The list of data points (X, Y) to be plotted.
     * @param xLabels Labels for the X-axis (e.g., dates).
     * @param labelX  The name or unit of the X-axis.
     * @param labelY  The name or unit of the Y-axis.
     */
    public GraphData(String id, String title, List<SimpleXYGraphView.Point> points, List<String> xLabels, String labelX, String labelY) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.xLabels = xLabels;
        this.labelX = labelX;
        this.labelY = labelY;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The title of the graph.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The list of XY points to plot.
     */
    public List<SimpleXYGraphView.Point> getPoints() {
        return points;
    }

    /**
     * @return The list of labels for the X-axis markers.
     */
    public List<String> getXLabels() {
        return xLabels;
    }

    /**
     * @return The X-axis descriptive label.
     */
    public String getLabelX() {
        return labelX;
    }

    /**
     * @return The Y-axis descriptive label.
     */
    public String getLabelY() {
        return labelY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphData graphData = (GraphData) o;
        return Objects.equals(id, graphData.id) && Objects.equals(title, graphData.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
