package de.tadris.fitness.util.charts.formatter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataTypes;

public class FractionedDateFormatter extends ValueFormatter {
    Context ctx;
    SimpleDateFormat format;
    AggregationSpan span;

    public FractionedDateFormatter(Context ctx, AggregationSpan span)
    {
        setAggregationSpan(span);
        this.span = span;
        this.ctx = ctx;
    }

    public void setAggregationSpan(AggregationSpan span)
    {
        format = span.dateFormat;
    }
    public AggregationSpan getSpan() {return span;}

    @Override
    public String getFormattedValue(float value) {
        return format.format(new Date((long) (value + span.spanInterval/2)));
    }
}
