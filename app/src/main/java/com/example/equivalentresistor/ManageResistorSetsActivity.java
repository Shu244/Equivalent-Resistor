package com.example.equivalentresistor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManageResistorSetsActivity extends AppCompatActivity {

    private static final String TAG = "ManageResistorSets";

    private FloatingActionButton mDownloadFAB;
    private FloatingActionButton mAddFAB;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<ResistorSetsAdapter.ResistorSetsViewHolder> adapter;
    private DownloadData mBackground;
    private DownloadSetDialog mDownloadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Resistor Sets");
        setContentView(R.layout.activity_manage_resistor_sets);

        mDownloadFAB = findViewById(R.id.downloadFAB);
        mAddFAB = findViewById(R.id.addFAB);
        mRecyclerView = findViewById(R.id.recyclerView);

        adapter = new ResistorSetsAdapter(getDataFileNames(getDataFiles(this)));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDownloadFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mBackground = new DownloadData();
                mDownloadDialog = new DownloadSetDialog();
                GetURLDialog getURL = new GetURLDialog(mBackground, mDownloadDialog);
                getURL.show(getSupportFragmentManager(), "");
            }
        });
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                new AddResistorSetDialog().show(getSupportFragmentManager(), "");
            }
        });
    }

    public static Intent getIntent(Context context) {
        Intent i = new Intent(context, ManageResistorSetsActivity.class);
        // Add extras to intent if needed here.
        return i;
    }

    public static File[] getDataFiles(Context context) {
        File dir = new File(context.getFilesDir(),MainActivity.mDataDirName);
        File[] dataFiles = dir.listFiles();
        if(dataFiles == null)
            return new File[0];
        else
            return dataFiles;
    }

    public List<String> getDataFileNames(File[] files) {
        List<String> names = new ArrayList<>();
        for(File file : files) {
            String name = file.getName();
            Log.d(TAG, "Name of a file: " + name);
            // No need to remove any markings on file name.
//            int end = name.indexOf('.');
//            name = end == -1 ? name : name.substring(0, end); // Removes extensions
//            if(name.charAt(0) == '~') // Removes tildes
//                name = name.substring(1);
            names.add(name);
        }
        return names;
    }

    public static class AddResistorSetDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_resistor_set_dialog, null);
            final EditText setNameTextView = v.findViewById(R.id.setNameTextView);
            setNameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus)
                        setNameTextView.setTextColor(Color.BLACK);
                }
            });
            // Creating custom centered title
            final AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                    .setView(v) // Set date selector view between title and button(s)
                    .setTitle(R.string.add_resistor_set_dialog_title)
                    // null can be DialogInterface.OnClickListener
                    .setNegativeButton(android.R.string.cancel, null) // Will close automatically when clicked.
                    .setPositiveButton(android.R.string.ok, null) // Overriding next.
                    .create();
            // Overriding action of positive button so that it only closes when input is acceptable.
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    // Defining
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setNameTextView.clearFocus(); // For smooth UI.
                            String newFileName = setNameTextView.getText().toString();
                            Log.d(TAG, "positive button clicked");
                            if(legalFileName(newFileName)) {
                                // Launch next intent
                                Log.d(TAG, "Positive button legal name.");
                                startActivity(EditSetActivity.getIntent(getActivity(), setNameTextView.getText().toString()));
                            } else {
                                // TO DO: Does not work as follows
                                Log.d(TAG, "Positive button not legal name.");
                                setNameTextView.setTextColor(Color.RED);
                            }
                        }
                    });
                }
            });
            return dialog;
        }
    }

    public static boolean legalFileName(String newFileName) {
        Log.d(TAG, "Matches: " + newFileName.matches("[a-zA-Z0-9_]+"));
        if(!newFileName.matches("[a-zA-Z0-9_]+"))
            return false;
        return true;
    }

    public static class GetURLDialog extends DialogFragment {
        private DownloadData mBackground;
        private DownloadSetDialog mDownloadDialog;

        public GetURLDialog(DownloadData background, DownloadSetDialog downloadDialog) {
            mBackground = background;
            mDownloadDialog = downloadDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.get_url_dialog, null);
            final EditText urlEditText = v.findViewById(R.id.setUrlEditText);
            // Creating custom centered title
            final AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                    .setView(v) // Set date selector view between title and button(s)
                    .setTitle(R.string.set_url_dialog_title)
                    // null can be DialogInterface.OnClickListener
                    .setNegativeButton(android.R.string.cancel, null) // Will close automatically when clicked.
                    .setPositiveButton(android.R.string.ok, null) // Overriding next.
                    .create();
            // Overriding action of positive button so that it only closes when input is acceptable.
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    // Defining positive button listener
                    final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url = urlEditText.getText().toString();
                            Log.d(TAG, "URL: " + url);
                            dialog.dismiss();
                            mDownloadDialog.setValues(mBackground, url);
                            mDownloadDialog.show(getFragmentManager(), "");
                        }
                    });
                }
            });
            return dialog;
        }
    }

    public static class DownloadSetDialog extends DialogFragment {
        private DownloadData mBackground;
        private String mUrl;

        public void setValues(DownloadData background, String url) {
            mBackground = background;
            mUrl = url;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.downloading_dialog, null);
            // Creating custom centered title
            final AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                    .setView(v) // Set date selector view between title and button(s)
                    .setTitle(R.string.wait)
                    // null can be DialogInterface.OnClickListener
                    .setNegativeButton(android.R.string.cancel, null) // Will close automatically when clicked.
                    .create();
            // Overriding action of positive button so that it only closes when input is acceptable.
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    // Completing task in the background.
                    mBackground.execute(mUrl);

                    // Defining negative button listener
                    final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mBackground.cancel(false);
                            dialog.dismiss();
                        }
                    });
                }
            });
            return dialog;
        }
    }

    private class DownloadData extends AsyncTask<String,Void,String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String url = params[0];
            String content = "";
            try {
                URLConnection connec =  new URL(url).openConnection();
                Scanner scanner = new Scanner(connec.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
                scanner.close();
            }catch ( IOException ex ) {
                return null;
            }

            Log.d(TAG, "Url context: " + content);

            // Data read successfully, now trying to parse data.
            String fileName;
            try {
                Scanner in = new Scanner(content);
                fileName = in.nextLine().trim();
                if(!legalFileName(fileName))
                    throw new Exception("Illegal filename");
                StringBuilder entries = new StringBuilder();
                while(in.hasNextLine()) {
                    String entry = in.nextLine().trim();
                    String[] components = entry.split(" ");
                    if(components.length != 2)
                        return null;
                    EditSetActivity.getPositiveDouble(components[0]);
                    EditSetActivity.getPositiveInt(components[1]);
                    entries.append(entry + "\n");
                }
                content = entries.toString();
            } catch(Exception e) {
                return null;
            }
            return new String[]{fileName, content};
        }


        @Override
        protected void onPostExecute(String[] results) {
            String fileName;
            try {
                fileName = results[0];
                String content = results[1];
                Context context = getApplicationContext();
                EditSetActivity.writeFileOnInternalStorage(context, fileName, content);
                fileName = "~" + fileName;
            } catch (Exception e) {
                downloadFail();
                return;
            }
            downloadSuccessful(fileName);
        }
    }

    private void downloadFail() {
        mDownloadDialog.dismiss();
        Toast.makeText(this, getResources().getText(R.string.cant_download), Toast.LENGTH_LONG).show();

    }

    private void downloadSuccessful(String fileName) {
        startActivity(EditSetActivity.getIntent(this, fileName));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBackground != null)
            mBackground.cancel(false);
    }
}
