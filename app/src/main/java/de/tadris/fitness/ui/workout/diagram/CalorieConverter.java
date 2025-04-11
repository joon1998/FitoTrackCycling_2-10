package de.tadris.fitness.ui.workout.diagram;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.util.charts.marker.DisplayValueMarker;

/**
 * Converts workout data into calorie consumption charts with configurable time intervals
 */
public class CalorieConverter {
    private final Context context;
    private final GpsWorkout workout;
    private final List<GpsSample> samples;
    private final int themeTextColor;
    private final int chartHeight;

    public CalorieConverter(Context context, GpsWorkout workout, List<GpsSample> samples,
                            int themeTextColor, int chartHeight) {
        this.context = context;
        this.workout = workout;
        this.samples = samples;
        this.themeTextColor = themeTextColor;
        this.chartHeight = chartHeight;
    }

    public View createView() {
        // Create main container
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create input row
        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        inputRow.setPadding(0, 0, 0, 20);

        // Create input field for interval
        EditText intervalInput = new EditText(context);
        intervalInput.setId(View.generateViewId());
        intervalInput.setText("5");
        intervalInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        intervalInput.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        intervalInput.setMinEms(3);
        intervalInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Create unit text
        TextView unitText = new TextView(context);
        unitText.setText(context.getString(R.string.time_minute_short));
        unitText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        unitText.setPadding(10, 0, 0, 0);
        unitText.setTextSize(18);
        unitText.setTextColor(themeTextColor);

        // Add views to input row
        inputRow.addView(intervalInput);
        inputRow.addView(unitText);
        container.addView(inputRow);

        // Create the chart
        LineChart chart = createCalorieChart(5);
        container.addView(chart);

        // Set up event listeners
        final LineChart[] chartRef = new LineChart[]{chart};
        intervalInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateChart(container, chartRef, intervalInput);
                return true;
            }
            return false;
        });

        intervalInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateChart(container, chartRef, intervalInput);
            }
        });

        return container;
    }

    private LineChart createCalorieChart(float intervalMinutes) {
        LineChart chart = new LineChart(context);

        // Prepare sample data (replace with real data)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 150));
        entries.add(new Entry(1, 220));
        entries.add(new Entry(2, 180));
        entries.add(new Entry(3, 300));
        entries.add(new Entry(4, 250));

        // Create main dataset
        LineDataSet dataSet = new LineDataSet(entries, context.getString(R.string.calories));
        dataSet.setColor(Color.parseColor("#FF5722"));
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#E64A19"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFCCBC"));
        dataSet.setFillAlpha(100);

        // Create chart data
        LineData lineData = new LineData(dataSet);

        // Add moving average if enough data points
        if (entries.size() >= 3) {
            LineDataSet avgDataSet = new LineDataSet(
                    calculateMovingAverage(entries, 3),
                    context.getString(R.string.moving_average)
            );
            avgDataSet.setColor(Color.parseColor("#4285F4"));
            avgDataSet.setLineWidth(2f);
            avgDataSet.setDrawCircles(false);
            avgDataSet.setDrawValues(false);
            avgDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            avgDataSet.enableDashedLine(15f, 10f, 0f);
            lineData.addDataSet(avgDataSet);
        }

        // Configure chart
        chart.setData(lineData);

        // X-axis setup
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(themeTextColor);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f %s", (value + 1) * intervalMinutes,
                        context.getString(R.string.time_minute_short));
            }
        });

        // Y-axis setup
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(themeTextColor);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f kcal", value);
            }
        });
        chart.getAxisRight().setEnabled(false);

        // General chart settings
        chart.getLegend().setTextColor(themeTextColor);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setMinimumHeight(chartHeight);
        chart.setExtraBottomOffset(20);
        chart.setMarker(new DisplayValueMarker(context, leftAxis.getValueFormatter(), "", lineData));
        chart.animateY(800);

        return chart;
    }

    private List<Entry> calculateMovingAverage(List<Entry> entries, int windowSize) {
        List<Entry> avgEntries = new ArrayList<>();
        int halfWindow = windowSize / 2;

        for (int i = 0; i < entries.size(); i++) {
            float sum = 0;
            int count = 0;

            for (int j = Math.max(0, i - halfWindow); j <= Math.min(entries.size() - 1, i + halfWindow); j++) {
                sum += entries.get(j).getY();
                count++;
            }
            avgEntries.add(new Entry(entries.get(i).getX(), sum / count));
        }
        return avgEntries;
    }

    private void updateChart(LinearLayout container, LineChart[] chartRef, EditText input) {
        try {
            float interval = Float.parseFloat(input.getText().toString());
            if (interval > 0) {
                container.removeView(chartRef[0]);
                LineChart newChart = createCalorieChart(interval);
                container.addView(newChart);
                chartRef[0] = newChart;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, R.string.invalid_interval, Toast.LENGTH_SHORT).show();
        }
    }
}