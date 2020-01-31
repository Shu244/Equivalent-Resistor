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

public class MainActivity extends AppCompatActivity {

    public static final String mDataDirName = "data";
    public static final String TAG = "MainActivity";
    public static final String RESULTS = "com.example.equivalentresistor.results";

    private ResistorModel mModel;

    private ViewPager mViewPager;
    private SeekBar mSizePrioritySeekBar;
    private TextView mSearchEditText;
    private Button mSearchButton;

    private List<String> mResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewPager);
        mSizePrioritySeekBar = findViewById(R.id.sizePrioritySeekBar);
        mSearchEditText = findViewById(R.id.searchEditText);
        mSearchButton = findViewById(R.id.searchButton);

        mModel = ResistorModel.getInstance();
        if(savedInstanceState != null) {
            mModel.setResistances(savedInstanceState.getDoubleArray(RESULTS));
        } else if (mModel.getSize() == 0) {
            // Data needs to be filled in.
            File dir = new File(getFilesDir(),mDataDirName);
            File[] dataFiles;
            if(dir.exists() && (dataFiles = dir.listFiles()).length != 0) {
                // There's data to fill in.
                // Adding a waiting screen to ViewPager.
                FragmentManager fragmentManager = getSupportFragmentManager();
                mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
                    @Override
                    public Fragment getItem(int position) {
                        return TransitionFragment.getFragment(getResources().getString(R.string.wait));
                    }

                    @Override
                    public int getCount() {
                        return 1;
                    }
                });
                fillModel(dataFiles);
            } else {
                // There's no data to fill in. Disable search.
                disableSearch();
            }
        }

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                int compactPriority = mSizePrioritySeekBar.getProgress();
                String search = mSearchEditText.getText().toString();
                try {
                    double searchDouble = Double.parseDouble(search);
                    mSearchEditText.setTextColor(Color.GREEN);
                    new RunOptimizer().execute(searchDouble, (double)compactPriority);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editMenuItem:
                startActivity(ManageResistorSetsActivity.getIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private File getMarkedFile(File[] dataFiles) throws Exception {
        for(int i = 0; i < dataFiles.length; i ++) {
            String fileName = dataFiles[i].getName();
            if(fileName.charAt(0) == '~')
                return dataFiles[i];
        }

        Log.d(TAG, "getMarkedFile: No marked files even though one should be marked.");
        File first = dataFiles[0];
        File newFile = new File(mDataDirName + "/~" + first.getName());
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
        // Fill ViewPager with fragment to display no resistance.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return TransitionFragment.getFragment(getResources().getString(R.string.no_resistors_message));
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
        mSizePrioritySeekBar.setEnabled(false);
        mSearchEditText.setEnabled(false);
        mSearchButton.setEnabled(false);
    }

    private class RunOptimizer extends AsyncTask<Double,Void,List<String>> {
        // Code to run in the background.
        // Void… params means to put the remaining arguments into an array of type Void named params.
        // Works only as the last argument.
        @Override
        protected List<String> doInBackground(Double... params) {
            double searchDouble = params[0];
            double compactPriority = params[1];
            double[] resistances = mModel.getResistancesArr();
            List<String> outputs = MainOptimizer.run(resistances, searchDouble, (int)compactPriority, 3000, 3, 50, -1);
            int numOutputs = outputs.size();
            int TOP = 10;
            int keepNum = numOutputs < TOP ? numOutputs : TOP;
            return outputs.subList(0, keepNum);
        }


        @Override
        protected void onPostExecute(List<String> strings) {
            mResults = strings;
            FragmentManager fragmentManager = getSupportFragmentManager();
            mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
                @Override
                public Fragment getItem(int position) {
                    String RPNMessage = mResults.get(position);
                    return ResistorFragment.getFragment(RPNMessage, position);
                }

                @Override
                public int getCount() {
                    return mResults.size();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if(mModel.getSize() != 0) {
            bundle.putDoubleArray(RESULTS, mModel.getResistancesArr());
        }
    }
}
