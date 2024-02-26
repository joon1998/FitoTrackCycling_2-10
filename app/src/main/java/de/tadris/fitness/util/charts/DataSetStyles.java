package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.LineDataSet;

import de.tadris.fitness.R;

public class DataSetStyles {
    public static CandleDataSet applyDefaultCandleStyle(Context ctx, CandleDataSet candleDataSet) {
        candleDataSet.setShadowColor(ContextCompat.getColor(ctx, R.color.colorAccent));
        candleDataSet.setShadowWidth(6f);
        candleDataSet.setNeutralColor(Color.TRANSPARENT);
        candleDataSet.setDrawValues(false);
        return candleDataSet;
    }

    public static LineDataSet applyBackgroundLineStyle(Context ctx, LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.GRAY);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setCircleColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setCircleHoleColor(Color.TRANSPARENT);
        lineDataSet.setValueTextColor(Color.TRANSPARENT);
        return lineDataSet;
    }

    public static BarDataSet applyDefaultBarStyle(Context ctx, BarDataSet barDataSet) {
        barDataSet.setColor(ContextCompat.getColor(ctx, R.color.colorAccent));
        return barDataSet;
    }
}
