package com.example.equivalentresistor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ResistorModel mModel;

    private ViewPager mViewPager;
    private SeekBar mSizePrioritySeekBar;
    private TextView mSearchEditText;
    private Button mSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPager);
        mSizePrioritySeekBar = findViewById(R.id.sizePrioritySeekBar);
        mSearchEditText = findViewById(R.id.searchEditText);
        mSearchButton = findViewById(R.id.searchButton);

        mModel = ResistorModel.getInstance();
        if (mModel.getSize() == 0) {
            // Data needs to be filled in.
            if(hasData()) {
                // There's data to fill in.
                fillModel();
            } else {
                // There's no data to fill in. Disable search.
                disableSearch();
            }
        }

//        mSearchButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v)
//            {
//                //DO SOMETHING! {RUN SOME FUNCTION ... DO CHECKS... ETC}
//            }
//        });
    }

    /*
    Checks if there are any data files.
     */
    private boolean hasData() {

        return false;
    }

    /*
    Fills model with data.
     */
    private void fillModel() {

    }

    /*
    Disables options to search since no data is available.
     */
    private void disableSearch() {
        // Fill ViewPager with fragment to display no resistance.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return NoResistorFragment.getFragment();
            }

            @Override
            public int getCount() {
                return 1;
            }
        });

        // Still need to update UI
        mSizePrioritySeekBar.setEnabled(false);
        //mSizePrioritySeekBar.getThumb().setColorFilter(getResources().getColor(R.color.inactive), PorterDuff.Mode.MULTIPLY);
        mSearchEditText.setEnabled(false);
        mSearchButton.setEnabled(false);
    }
}
