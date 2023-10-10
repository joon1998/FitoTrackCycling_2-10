package de.tadris.fitness.util.charts.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

import de.tadris.fitness.util.unit.DistanceUnitUtils;

public class SpeedFormatter extends ValueFormatter {
    DistanceUnitUtils distanceUnitUtils;

    public SpeedFormatter(DistanceUnitUtils distanceUnitUtils) {
        this.distanceUnitUtils = distanceUnitUtils;
    }

    @Override
    public String getFormattedValue(float value) {
        return distanceUnitUtils.getSpeedString(value);
    }
}