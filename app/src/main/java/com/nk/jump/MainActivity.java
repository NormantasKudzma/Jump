package com.nk.jump;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.nk.jump.fragments.StatsTab;
import com.nk.jump.fragments.WorkoutTab;

public class MainActivity extends AppCompatActivity {
    private static final Class<?>[] TABS = new Class[]{
        WorkoutTab.class,
        StatsTab.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    changeTab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            }
        );

        changeTab(0);
    }

    private void changeTab(int index){
        try {
            Fragment fragment = (Fragment)TABS[index].newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, TABS[index].getName())
                    .commit();
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
}
