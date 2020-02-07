package com.example.equivalentresistor;

import optimizer.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    public static final String mDataDirName = "data";
    public static final String TAG = "MainActivity";
    private static final String VISUALS = "com.example.equivalentresistor.visuals";
    private static final String TOTAL_RESISTANCES = "com.example.equivalentresistor.total_resistances";
    private static final String RANKS = "com.example.equivalentresistor.ranks";
    private static final String SIZES = "com.example.equivalentresistor.sizes";
    private static final String GOAL = "com.example.equivalentresistor.goal";
    public static final String SEARCH = "com.example.equivalentresistor.searchs";


    private ViewPager mViewPager;
    private SeekBar mSizePrioritySeekBar;
    private TextView mSearchEditText;
    private Button mSearchButton;

    private ResistorModel mModel;
    private String[] mVisuals = new String[0];
    private double[] mTotalResistances = new double[0];
    private int[] mRanks = new int[0];
    private int[] mSizes = new int[0];
    private double mGoalResistance;
    private RunOptimizer mOptimizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate() called.");
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPager);
        mSizePrioritySeekBar = findViewById(R.id.sizePrioritySeekBar);
        mSearchEditText = findViewById(R.id.searchEditText);
        mSearchButton = findViewById(R.id.searchButton);

        mModel = ResistorModel.getInstance();
        String[] tempVisuals = null;
        double[] tempResistances = null;
        int[] tempRanks = null;
        int[] tempSizes = null;
        if(savedInstanceState != null) {
            // savedInstanceState is not null but desired items might not be saved.
            tempVisuals = savedInstanceState.getStringArray(VISUALS);
            tempResistances = savedInstanceState.getDoubleArray(TOTAL_RESISTANCES);
            tempRanks = savedInstanceState.getIntArray(RANKS);
            tempSizes = savedInstanceState.getIntArray(SIZES);
            mGoalResistance = savedInstanceState.getDouble(GOAL, 0);

            String search = savedInstanceState.getString(SEARCH, "");
            mSearchEditText.setText(search);
        }
        if(tempVisuals != null && tempResistances != null && tempRanks != null && tempSizes != null) {
            // There are old results to display
            mVisuals = tempVisuals;
            mTotalResistances = tempResistances;
            mRanks = tempRanks;
            mSizes = tempSizes;
            setPagerWithResults();
        } else if (mModel.getSize() == 0) {
            // Data needs to be filled in.
            File dir = new File(getFilesDir(),mDataDirName);
            File[] dataFiles;
            if(dir.exists() && (dataFiles = dir.listFiles()).length != 0) {
                // There's data to fill in.
                fillModel(dataFiles);
                setPagerWithMessage(getResources().getString(R.string.ready));
            } else {
                // There's no data to fill in. Disable search.
                disableSearch();
                setPagerWithMessage(getResources().getString(R.string.no_resistors_message));
            }
        } else {
            // Theres data in the model
            setPagerWithMessage(getResources().getString(R.string.ready));
        }

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mSearchEditText.clearFocus();
                int compactPriority = mSizePrioritySeekBar.getProgress();
                if(compactPriority == 100)
                    // If compactPriority = 0, one resistor is returned pretty much regardless of accuracy.
                    compactPriority = 99;
                String search = mSearchEditText.getText().toString();
                try {
                    mGoalResistance = EditSetActivity.getPositiveDouble(search);
                    setPagerWithMessage(getResources().getString(R.string.wait));
                    mSearchButton.setEnabled(false);
                    mOptimizer = new RunOptimizer();
                    mOptimizer.execute(mGoalResistance, (double)compactPriority);
                } catch (NumberFormatException e) {
                    mSearchEditText.setTextColor(Color.RED);
                }
            }
        });

        mSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // XML also selects all text when it is in focus.
                    mSearchEditText.setTextColor(Color.BLACK);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity onStart() called.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume() called.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editMenuItem:
                startActivity(ManageResistorSetsActivity.getIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void deleteAllData(Context context) {
        File dir = new File(context.getFilesDir(), mDataDirName);
        File[] dataFiles = dir.listFiles();
        for(File file : dataFiles)
            file.delete();
        dir.delete();
    }

    public static void removeFile(Context context, String fileName) {
        File file = new File(context.getFilesDir(), mDataDirName + "/" + fileName);
        file.delete();
    }

    private File getMarkedFile(File[] dataFiles) throws Exception {
        for(int i = 0; i < dataFiles.length; i ++) {
            String fileName = dataFiles[i].getName();
            if(fileName.charAt(0) == '~')
                return dataFiles[i];
        }

        Log.d(TAG, "getMarkedFile: No marked files even though one should be marked.");
        File first = dataFiles[0];
        File newFile = new File(this.getFilesDir(), mDataDirName + "/~" + first.getName());
        boolean result = first.renameTo(newFile);
        if(!result)
            throw new Exception();
        else
            return newFile;
    }

    public static void fillModel(File dataFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dataFile));
        List<Double> resistances = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] elements = line.trim().split(" ");
            double resistance = Double.parseDouble(elements[0]);
            int qty = Integer.parseInt(elements[1]);
            for(int qty_i = 0; qty_i < qty; qty_i ++)
                resistances.add(resistance);
        }
        ResistorModel model = ResistorModel.getInstance();
        model.setResistances(resistances);
    }

    /*
    Fills model with data.
     */
    private void fillModel(File[] dataFiles) {
        try {
            File selected = getMarkedFile(dataFiles);
            fillModel(selected);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Selected file not found.", e);
            disableSearch();
        } catch (IOException e) {
            Log.d(TAG, "Selected file not found.", e);
            disableSearch();
        } catch (NumberFormatException e) {
            Log.d(TAG, "Unexpected error when filling model.", e);
            disableSearch();
        }catch (Exception e) {
            Log.d(TAG, "Unexpected error when filling model.", e);
            disableSearch();
        }
    }

    public static Intent getIntent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        return i;
    }

    /*
    Disables options to search since no data is available.
     */
    private void disableSearch() {
        mSizePrioritySeekBar.setEnabled(false);
        mSearchEditText.setEnabled(false);
        mSearchButton.setEnabled(false);
    }

    private void setPagerWithResults() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                String visual = mVisuals[i];
                double totalResistance = mTotalResistances[i];
                int rank = mRanks[i];
                int size = mSizes[i];
                return ResistorFragment.getFragment(visual, mGoalResistance,totalResistance, rank, size);
            }

            @Override
            public int getCount() {
                return mVisuals.length;
            }
        });
        mSearchButton.setEnabled(true);
    }

    private void setPagerWithMessage(final String message) {
        // Adding a waiting screen to ViewPager.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return TransitionFragment.getFragment(message);
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
    }

    public void setResultsPagerToFront() {
        mViewPager.setCurrentItem(0);
    }

    private class RunOptimizer extends AsyncTask<Double,Void,ResultsWrapper> {
        // Code to run in the background.
        // Void… params means to put the remaining arguments into an array of type Void named params.
        // Works only as the last argument.
        @Override
        protected ResultsWrapper doInBackground(Double... params) {
            double searchDouble = params[0];
            double compactPriority = params[1];
            Log.d(TAG, "Desired resistance: " + searchDouble);
            Log.d(TAG, "Size priority: " + compactPriority);

            double[] resistances = mModel.getResistancesArr();
            int TOP = 10;
            List<Queue<DNADecipherUnit>> queues = MainOptimizer.runAndGetQueues(resistances, searchDouble, (int)compactPriority, 3000, 3, 50, TOP);

            int size = queues.size();
            String[] visuals = new String[size];
            double[] totalResistances = new double[size];
            int[] ranks = new int[size];
            int[] sizes = new int[size];

            for(int i = 0; i < size; i ++) {
                Queue<DNADecipherUnit> queue = queues.get(i);
                DNADecipherUnit first = queue.peek();
                DNA instructions = first.getDNA();

                String visual = EvolveOptimalResistors.visualizeDNA(queue, false);
                double totalResistance = instructions.getTotalResistance();
                int rank = instructions.getRank();
                int resistorSize = instructions.getSize();

                visuals[i] = visual;
                totalResistances[i] = totalResistance;
                ranks[i] = rank;
                sizes[i] = resistorSize;
            }
            return new ResultsWrapper(visuals, totalResistances, ranks, sizes);
        }


        @Override
        protected void onPostExecute(ResultsWrapper results) {
            // Best practice to change UI components in main thread.
            mVisuals = results.mVisuals;
            mTotalResistances = results.mTotalResistances;
            mRanks = results.mRanks;
            mSizes = results.mSizes;
            setPagerWithResults();
        }
    }

    static class ResultsWrapper {
        String[] mVisuals;
        double[] mTotalResistances;
        int[] mRanks;
        int[] mSizes;

        public ResultsWrapper(String[] visuals, double[] totalResistances, int[] ranks, int[] sizes) {
            mVisuals = visuals;
            mTotalResistances = totalResistances;
            mRanks = ranks;
            mSizes = sizes;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "MainActivity onSaveInstanceState() called.");
        if(mVisuals.length != 0) {
            bundle.putStringArray(VISUALS, mVisuals);
            bundle.putDoubleArray(TOTAL_RESISTANCES, mTotalResistances);
            bundle.putIntArray(RANKS, mRanks);
            bundle.putIntArray(SIZES, mSizes);
            bundle.putDouble(GOAL, mGoalResistance);
        }
        String search = mSearchEditText.getText().toString();
        bundle.putString(SEARCH, search);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity onPause() called.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity onStop() called.");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy() called.");
        if(mOptimizer != null)
            mOptimizer.cancel(false);
    }

}
