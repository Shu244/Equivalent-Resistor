package com.example.equivalentresistor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ResistorFragment extends Fragment {
    private TextView mMessageTextView;
    private TextView mRankerTextView;
    private FloatingActionButton mInformationFAB;
    private FloatingActionButton mReturnToFrontFAB;

    private String mVisual;
    private double mGoalR;
    private double mTotalResistance;
    private int mRank;
    private int mSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        super.onCreateView(inflater, container, bundle);
        // When screen rotates, the arrays are saved in MainActivity but the values in here are destroyed.
        // This is a temporary gimmick to use.
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.resistor_fragment, container, false);
        mMessageTextView = v.findViewById(R.id.messageTextView);
        mRankerTextView = v.findViewById(R.id.rankerTextView);
        mInformationFAB = v.findViewById(R.id.informationFAB);
        mReturnToFrontFAB = v.findViewById(R.id.returnToFrontFAB);

        mMessageTextView.setText(injectPreference(mVisual, mTotalResistance));
        if(mRank == 1)
            mReturnToFrontFAB.hide();
        mRankerTextView.setText(mRank + "");
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInformationFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                double diff = Math.abs(mGoalR - mTotalResistance);
                InformationDialog dialog = InformationDialog.getDialog(mTotalResistance, diff, mRank, mSize);
               dialog.show(getFragmentManager(), "");
            }
        });
        mReturnToFrontFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                MainActivity main = (MainActivity)getActivity();
                main.setResultsPagerToFront();
            }
        });
    }

    private String injectPreference(String visual, double totalResistance) {
        return String.format("%s \n\n Total: %.1f ohms", visual, totalResistance);
    }

    public static ResistorFragment getFragment(String visual, double goal, double totalResistance, int rank, int size) {
        ResistorFragment frag = new ResistorFragment();

        frag.mVisual = visual;
        frag.mGoalR = goal;
        frag.mTotalResistance = totalResistance;
        frag.mRank = rank;
        frag.mSize = size;

        return frag;
    }

    private static TextView customCenteredTitle(Context context) {
        TextView title = new TextView(context);
        title.setText(R.string.dialog_information_title);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        return title;
    }

    public static class InformationDialog extends DialogFragment {

        private static final String DIFF = "com.example.equivalentresistor.diff";
        private static final String TOTAL_RESISTANCE = "com.example.equivalentresistor.total_resistance";
        private static final String RANK = "com.example.equivalentresistor.rank";
        private static final String SIZE = "com.example.equivalentresistor.size";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.information_dialog, null);

            Bundle args = getArguments();
            double ohms = args.getDouble(TOTAL_RESISTANCE);
            double diff = args.getDouble(DIFF);
            int rank = args.getInt(RANK);
            int size = args.getInt(SIZE);

            TextView ohmsView = v.findViewById(R.id.resistanceTextView);
            ohmsView.setText(String.format("%.1f", ohms));
            TextView diffView = v.findViewById(R.id.diffTextView);
            diffView.setText(String.format("%.1f", diff));
            TextView rankView = v.findViewById(R.id.rankTextView);
            rankView.setText(rank + "");
            TextView sizeView = v.findViewById(R.id.sizeTextView);
            sizeView.setText(size + "");

            return new AlertDialog.Builder(getActivity())
                    .setView(v) // Set date selector view between title and button(s)
                    .setCustomTitle(customCenteredTitle(getActivity()))
                    // null can be DialogInterface.OnClickListener
                    .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }

        public static InformationDialog getDialog(double ohms, double diff, int rank, int size) {
            InformationDialog dialog = new InformationDialog();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putDouble(TOTAL_RESISTANCE, ohms);
            args.putDouble(DIFF, diff);
            args.putInt(RANK, rank);
            args.putInt(SIZE, size);
            dialog.setArguments(args);

            return dialog;
        }
    }

}
