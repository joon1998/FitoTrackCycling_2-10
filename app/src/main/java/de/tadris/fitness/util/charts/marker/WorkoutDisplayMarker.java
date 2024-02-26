package de.tadris.fitness.util.charts.marker;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutType;

public class WorkoutDisplayMarker extends MarkerView {
    private TextView tvContent;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     */
    public WorkoutDisplayMarker(Context context) {
        super(context, R.layout.marker_view);
        tvContent = (TextView) findViewById(R.id.marker_text);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        WorkoutType w = (WorkoutType) e.getData();
        tvContent.setText(w.title); // set the entry-value as the display text
        super.refreshContent(e, highlight);
    }
}
