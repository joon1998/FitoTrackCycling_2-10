package de.tadris.fitness.util.charts.formatter;

import android.content.Context;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;

public class DayTimeFormatter extends ValueFormatter {
    Context ctx;

    public DayTimeFormatter(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * @param value in seconds
     * @return Time of the day
     */
    @Override
    public String getFormattedValue(float value) {
        return Instance.getInstance(ctx).userDateTimeUtils.formatTime(new Date((long)value));
    }
}
