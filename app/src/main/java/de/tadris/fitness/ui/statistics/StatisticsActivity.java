package de.tadris.fitness.ui.statistics;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.tadris.fitness.R;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.adapter.StatisticsAdapter;

public class StatisticsActivity extends FitoTrackActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setTitle(getString(R.string.statistics));
        setupActionBar();

        StatisticsAdapter statisticsAdapter = new StatisticsAdapter(getSupportFragmentManager(), getLifecycle(), this);
        ViewPager2 pager = this.findViewById(R.id.statistics_pager);
        pager.setAdapter(statisticsAdapter);
        pager.setUserInputEnabled(false);

        TabLayout tabLayout = this.findViewById(R.id.statistics_tabs);

        new TabLayoutMediator(tabLayout, pager,
                (tab, position) -> tab.setText(statisticsAdapter.getTitles().get(position))
        ).attach();
    }
}
