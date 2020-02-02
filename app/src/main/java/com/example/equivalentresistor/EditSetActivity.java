package com.example.equivalentresistor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditSetActivity extends AppCompatActivity {

    private final static String TAG = "EditSetActivity";
    private final static String SET_NAME = "com.example.equvialentresistor.set_name";
    private final static String OHM_ENTRIES = "com.example.equvialentresistor.ohm_entries";
    private final static String QTY_ENTRIES = "com.example.equvialentresistor.qty_entries";
    private final static String OHM_ERRORS = "com.example.equvialentresistor.ohm_errors";
    private final static String QTY_ERRORS = "com.example.equvialentresistor.qty_errors";

    private final static String DEFAULT_R = "1000"; // Ohms
    private final static String DEFAULT_QTY = "1"; // Ohms

    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddFAB;
    private Button mSubmitButton;
    private ResistorsAdapter mAdapter;

    private List<String[]> mResistorEntries;
    private List<Boolean[]> mLegalValues;
    private String mSetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_set);

        Intent i = getIntent();
        mSetName = i.getStringExtra(SET_NAME);

        setTitle("Edit set: " + mSetName);

        mRecyclerView = findViewById(R.id.resistorsRecyclerView);
        mAddFAB = findViewById(R.id.addResistorFAB);
        mSubmitButton = findViewById(R.id.submitSetButton);
        mLegalValues = new ArrayList<>();
        mResistorEntries = new ArrayList<>();

        if(savedInstanceState != null) {
            String[] ohmEntries = savedInstanceState.getStringArray(OHM_ENTRIES);
            String[] qtyEntries = savedInstanceState.getStringArray(QTY_ENTRIES);
            boolean[] ohmErrors = savedInstanceState.getBooleanArray(OHM_ERRORS);
            boolean[] qtyErrors = savedInstanceState.getBooleanArray(QTY_ERRORS);
            fillEntryArrays(new String[][]{ohmEntries, qtyEntries});
            fillErrorArrays(new boolean[][]{ohmErrors, qtyErrors});
        } else {
            try {
                mResistorEntries = getFileData(mSetName);
                for(String[] _ : mResistorEntries)
                    mLegalValues.add(new Boolean[]{true, true});
            } catch (IOException e) {
                Log.d(TAG, "Mal-formatted file was saved somehow");
            }
        }

        mAdapter = new ResistorsAdapter(mResistorEntries, mLegalValues);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAddFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mResistorEntries.add(new String[]{DEFAULT_R, DEFAULT_QTY});
                mLegalValues.add(new Boolean[]{true, true});
                mAdapter.notifyItemInserted(mResistorEntries.size() - 1);
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                clearHoldersFocus();
                List<Double> resistances = allDataLegal();
                if(resistances != null && resistances.size() != 0) {
                    // All inputted data are valid.
                    String fileBody = genFileBody();
                    try {
                        writeFileOnInternalStorage(v.getContext(), mSetName, fileBody);
                        ResistorModel.getInstance().setResistances(resistances);
                        startActivity(MainActivity.getIntent(v.getContext()));
                    } catch (IOException e) {
                        Toast.makeText(v.getContext(), getResources().getText(R.string.cant_write), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Some data are invalid
                    Toast.makeText(v.getContext(), getResources().getText(R.string.cannot_save), Toast.LENGTH_LONG).show();
                    // Show the red marks.
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void clearHoldersFocus() {
        List<ResistorsAdapter.ResistorsViewHolder> holders = mAdapter.getHolders();
        for (ResistorsAdapter.ResistorsViewHolder holder : holders)
            holder.itemView.clearFocus();
    }

    public static double getPositiveDouble(String num) throws NumberFormatException {
        double result = Double.parseDouble(num);
        if(result <= 0)
            throw new NumberFormatException();
        return result;
    }

    public static int getPositiveInt(String num) throws NumberFormatException {
        int result = Integer.parseInt(num);
        if(result <= 0)
            throw new NumberFormatException();
        return result;
    }

    private List<Double> allDataLegal() {
        boolean returnNull = false;
        int size = mResistorEntries.size();
        List<Double> resistances = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            String[] entry = mResistorEntries.get(i);
            Boolean[] legals = mLegalValues.get(i);
            double resistance = 0;
            try {
                resistance = getPositiveDouble(entry[0]);
                legals[0] = true;
            } catch(NumberFormatException e) {
                legals[0] = false;
                returnNull = true;
            }
            try {
                int qty = getPositiveInt(entry[1]);
                legals[1] = true;
                for(int qty_i = 0;legals[0] &&  qty_i  <qty; qty_i++)
                    resistances.add(resistance);
            } catch(NumberFormatException e) {
                legals[1] = false;
                returnNull = true;
            }
        }
        if(returnNull)
            return null;
        return resistances;
    }

    private List<String[]> getFileData(String fileName) throws IOException {
        File dataFile = new File(this.getFilesDir() , MainActivity.mDataDirName + "/" + fileName);
        if(!dataFile.exists()) {
            return new ArrayList<>();
        } else {
            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            List<String[]> resistances = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] elements = line.trim().split(" ");

                if(elements.length != 2)
                    return new ArrayList<>();

                resistances.add(elements);
            }
            return resistances;
        }
    }

    public static void unMarkFile(Context context, File[] existingFiles, String fileName) throws IllegalArgumentException {
        for(File existingFile : existingFiles) {
            String existingFileName = existingFile.getName();
            // Keeping track of previous marked file.
            if (existingFileName.charAt(0) == '~') {
                String markedNameNoMark = existingFileName.substring(1);

                // Now unmarking
                File newFile = new File(context.getFilesDir(), MainActivity.mDataDirName + "/" + markedNameNoMark);
                boolean result = existingFile.renameTo(newFile);
            }
        }
    }

    public static void deletePotentialDuplicates(Context context, String unMarkFileName) {
        File dir = new File(context.getFilesDir(), MainActivity.mDataDirName);
        File unMarked = new File(dir, unMarkFileName);
        File marked = new File(dir, "~" + unMarkFileName);
        if(unMarked.exists())
            unMarked.delete();
        if(marked.exists())
            marked.delete();
    }

    /*
    Automatically marks newly added file and unmarks old file. User must handle exceptions.
     */
    public static void writeFileOnInternalStorage(Context context, String fileName, String body) throws IOException {
        File dir = new File(context.getFilesDir(), MainActivity.mDataDirName);
        if(!dir.exists())
            dir.mkdir();

        if(fileName.charAt(0) != '~') {
            deletePotentialDuplicates(context, fileName);
            unMarkFile(context, dir.listFiles(), fileName);
            fileName = "~" + fileName;
        }
        File dataFile = new File(dir, fileName);
        FileWriter writer = new FileWriter(dataFile);
        writer.append(body);
        writer.flush();
        writer.close();
    }

    private String genFileBody() {
        StringBuilder strB = new StringBuilder();
        for(String[] entry : mResistorEntries)
            strB.append(entry[0] + " " + entry[1] + "\n");
        return strB.toString();
    }

    public static Intent getIntent(Context context, String setName) {
        Intent i = new Intent(context, EditSetActivity.class);
        i.putExtra(SET_NAME, setName);
        return i;
    }

    private String[][] getEntryArrays() { ;
        int size = mResistorEntries.size();
        String[] ohms = new String[size];
        String[] qty = new String[size];
        for(int i = 0; i < size; i ++) {
            String[] entry = mResistorEntries.get(i);
            ohms[i] = entry[0];
            qty[i] = entry[1];
        }
        return new String[][]{ohms, qty};
    }

    private boolean[][] getErrorArrays() {
        int size = mLegalValues.size();
        boolean[] ohmsLegal = new boolean[size];
        boolean[] qtyLegal = new boolean[size];
        for(int i = 0; i < size; i ++) {
            Boolean[] entry = mLegalValues.get(i);
            ohmsLegal[i] = entry[0];
            qtyLegal[i] = entry[1];
        }
        return new boolean[][]{ohmsLegal, qtyLegal};
    }

    private void fillEntryArrays(String[][] entryArrs) {
        String[] ohmEntries = entryArrs[0];
        String[] qtyEntries = entryArrs[1];
        int size = ohmEntries.length;
        for(int i = 0; i < size; i ++)
            mResistorEntries.add(new String[]{ohmEntries[i], qtyEntries[i]});
    }

    private void fillErrorArrays(boolean[][] errorArrs) {
        boolean[] ohmErrors = errorArrs[0];
        boolean[] qtyErrors = errorArrs[1];
        int size = ohmErrors.length;
        for(int i = 0; i < size; i ++)
            mLegalValues.add(new Boolean[]{ohmErrors[i], qtyErrors[i]});
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // To save information that is currently being updated during rotation.
        clearHoldersFocus();
        int size = mResistorEntries.size();
        if(size != 0) {
            String[][] entryArrs = getEntryArrays();
            boolean[][] errorArrs = getErrorArrays();
            bundle.putStringArray(OHM_ENTRIES, entryArrs[0]);
            bundle.putStringArray(QTY_ENTRIES, entryArrs[1]);
            bundle.putBooleanArray(OHM_ERRORS, errorArrs[0]);
            bundle.putBooleanArray(QTY_ERRORS, errorArrs[1]);
        }
    }
}
