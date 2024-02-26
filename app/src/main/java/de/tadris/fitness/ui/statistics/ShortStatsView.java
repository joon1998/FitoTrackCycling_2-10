package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import de.tadris.fitness.R;
import de.tadris.fitness.ui.adapter.ShortStatsAdapter;

public class ShortStatsView extends ConstraintLayout {
    ViewPager2 myViewPager2;
    ShortStatsAdapter adapter;

    public ShortStatsView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.view_short_stats, this);

        myViewPager2 = findViewById(R.id.viewpager_short_stats);
        adapter = new ShortStatsAdapter(this.getContext());
        myViewPager2.setAdapter(adapter);
    }

    public void refresh()
    {
        myViewPager2.getAdapter().notifyDataSetChanged();
    }
}
