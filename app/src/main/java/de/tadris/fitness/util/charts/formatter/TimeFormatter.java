package de.tadris.fitness.util.charts.formatter;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;

public class TimeFormatter extends ValueFormatter {
    TimeUnit input;
    boolean dispSecs, dispMins, dispHours;

    public TimeFormatter(TimeUnit input) {
        this(input, true, true, true);
    }

    public TimeFormatter(TimeUnit input, boolean dispSecs, boolean dispMins, boolean dispHours) {
        this.input = input;
        this.dispSecs = dispSecs;
        this.dispMins = dispMins;
        this.dispHours = dispHours;
    }

    /**
     * @param value time in the unit defined by TimeInput of the constructor
     * @return
     */
    @Override
    public String getFormattedValue(float value) {
        long multip = 1000000;
        long s = input.toSeconds((long) (value*multip))/multip; // "Trick" to avoid precision loss
        if(dispSecs)
            return de.tadris.fitness.util.unit.TimeFormatter.formatDuration(s);
        else if (dispHours)
            return de.tadris.fitness.util.unit.TimeFormatter.formatHoursMinutes(s);
        else
            return de.tadris.fitness.util.unit.TimeFormatter.formatMinutesOnly(s);
        // TODO: Implement rest
    }

    public String getUnit(Context ctx)
    {
        if(dispHours)
            return ctx.getString(R.string.timeHourShort);
        else
            return ctx.getString(R.string.timeMinuteShort);
    }
}
