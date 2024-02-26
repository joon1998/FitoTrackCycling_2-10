package de.tadris.fitness.util.charts.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

import de.tadris.fitness.util.unit.DistanceUnitUtils;

public class DistanceFormatter extends ValueFormatter {
    DistanceUnitUtils distanceUnitUtils;
    boolean useLongUnit = false;

    public DistanceFormatter(DistanceUnitUtils distanceUnitUtils, int longestDistanceInMeters) {
        this.distanceUnitUtils = distanceUnitUtils;
        useLongUnit = longestDistanceInMeters/distanceUnitUtils.getDistanceUnitSystem().getMetersFromLongDistance(1) > 1 ? true : false;
    }

    @Override
    public String getFormattedValue(float value) {
        return distanceUnitUtils.getDistanceWithoutUnit((int)value, useLongUnit, 1);
    }
}