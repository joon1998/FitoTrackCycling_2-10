/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.ui.workout.diagram;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.ui.workout.ShowGpsWorkoutActivity;
import de.tadris.fitness.util.charts.marker.DisplayValueMarker;
import de.tadris.fitness.util.sections.SectionListModel;

public class DistanceConverter {

    private final Context context;
    private final GpsWorkout workout;
    private final List<GpsSample> samples;
    private final int themeTextColor;
    private final int mapHeight;

    public DistanceConverter(Context context, GpsWorkout workout, List<GpsSample> samples, int themeTextColor, int mapHeight) {
        this.context = context;
        this.workout = workout;
        this.samples = samples;
        this.themeTextColor = themeTextColor;
        this.mapHeight = mapHeight;
    }

    /**
     * Creates and returns a view containing the distance interval chart and its controls
     */
    public View createView() {
        // Create container layout
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create input row
        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        inputRow.setPadding(0, 0, 0, 20);

        // Create input field for interval
        EditText intervalInput = new EditText(context);
        intervalInput.setText("5");
        intervalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        intervalInput.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        intervalInput.setMinEms(3);
        intervalInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        inputRow.addView(intervalInput);

        // Create unit text
        TextView unitText = new TextView(context);
        unitText.setText(context.getString(R.string.timeMinuteShort));
        unitText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        unitText.setPadding(10, 0, 0, 0);
        unitText.setTextSize(18);
        inputRow.addView(unitText);

        // Add input row to container
        container.addView(inputRow);

        // Create the chart - initially with 5 minute intervals
        LineChart chart = createDistanceChart(5);
        container.addView(chart);

        // Array to hold chart reference that can be modified from lambda expressions
        final LineChart[] chartRef = new LineChart[]{chart};

        // Add listener to update chart when interval changes
        intervalInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intervalInput.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(intervalInput.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        // Add blur listener to update chart when focus is lost
        intervalInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    float interval = Float.parseFloat(intervalInput.getText().toString());
                    if (interval > 0) {
                        // Remove old chart
                        container.removeView(chartRef[0]);

                        // Create new chart with updated interval
                        LineChart newChart = createDistanceChart(interval);
                        container.addView(newChart);
                        chartRef[0] = newChart;
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, do nothing
                }
            }
        });

        return container;
    }

    private LineChart createDistanceChart(float intervalMinutes) {
        LineChart chart = new LineChart(context);

        // Convert interval from minutes to milliseconds
        long intervalMs = (long) (intervalMinutes * 60 * 1000);

        // Create a section model with time-based sections using the specified interval
        SectionListModel sectionModel = new SectionListModel(workout, samples);
        sectionModel.setCriterion(SectionListModel.SectionCriterion.TIME);
        sectionModel.setSectionLength(intervalMs);
        List<SectionListModel.Section> sections = sectionModel.getSectionList();

        // Create the data entries for distance per interval
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            SectionListModel.Section section = sections.get(i);
            float intervalNumber = i;
            entries.add(new Entry(intervalNumber, (float) section.getDist()));
        }

        // Create dataset and customize appearance
        LineDataSet dataSet = new LineDataSet(entries, context.getString(R.string.workoutDistance));
        dataSet.setColor(Color.parseColor("#FF9800"));  // Orange color like in speed chart
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        // Add moving average line if there are enough points
        List<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        if (entries.size() >= 3) {
            List<Entry> movingAvgEntries = calculateMovingAverage(entries, 3); // Window size of 3
            LineDataSet movingAvgDataSet = new LineDataSet(movingAvgEntries, context.getString(R.string.movingAverage));
            movingAvgDataSet.setColor(Color.parseColor("#2196F3")); // Blue color
            movingAvgDataSet.setLineWidth(2f);
            movingAvgDataSet.setDrawCircles(false);
            movingAvgDataSet.setDrawValues(false);
            movingAvgDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve
            movingAvgDataSet.enableDashedLine(10f, 5f, 0f); // Dashed line style
            dataSets.add(movingAvgDataSet);
        }

        // Create and configure the chart
        LineData lineData = new LineData();
        for (LineDataSet dataset : dataSets) {
            lineData.addDataSet(dataset);
        }
        chart.setData(lineData);

        // Set axis formatting
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setTextColor(themeTextColor);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int interval = (int)value + 1; // +1 because we start at 0
                float minutes = interval * intervalMinutes;
                if (intervalMinutes == 1) {
                    return String.format("%d:00", (int)minutes);
                } else {
                    return String.format("%.1f %s", minutes, context.getString(R.string.timeMinuteShort));
                }
            }
        });

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setTextColor(themeTextColor);
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Instance.getInstance(context).distanceUnitUtils.getDistance((int)value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(themeTextColor);
        chart.getDescription().setEnabled(false);

        // Set chart ling
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.setScaleEnabled(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.setPinchZoom(true);

        // Set marker for displaying values when touched
        chart.setMarker(new DisplayValueMarker(context, chart.getAxisLeft().getValueFormatter(), "", lineData));

        // Fit the chart to the screen
        int height = mapHeight / 2;
        chart.setMinimumHeight(height);
        chart.setExtraBottomOffset(20);

        // Animate the chart
        chart.animateY(500);

        return chart;
    }

    private List<Entry> calculateMovingAverage(List<Entry> entries, int windowSize) {
        if (windowSize <= 1 || entries.size() < windowSize) {
            return new ArrayList<>(entries);
        }

        List<Entry> avgEntries = new ArrayList<>();

        // We need at least windowSize/2 points on each side for a centered average
        int halfWindow = windowSize / 2;

        for (int i = 0; i < entries.size(); i++) {
            float sum = 0;
            int count = 0;

            // Calculate average for the window centered at position i
            for (int j = Math.max(0, i - halfWindow); j <= Math.min(entries.size() - 1, i + halfWindow); j++) {
                sum += entries.get(j).getY();
                count++;
            }

            float average = sum / count;
            avgEntries.add(new Entry(entries.get(i).getX(), average));
        }

        return avgEntries;
    }
}