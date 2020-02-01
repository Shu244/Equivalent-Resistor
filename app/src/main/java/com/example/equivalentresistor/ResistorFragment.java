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

    private static final String mMessage = "com.example.equivalentresistor.resistor_message";
    private static final String mRank = "com.example.equivalentresistor.rank";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        // Do I need super? NO
        View v = inflater.inflate(R.layout.resistor_fragment, container, false);
        mMessageTextView = v.findViewById(R.id.messageTextView);
        mRankerTextView = v.findViewById(R.id.rankerTextView);
        mInformationFAB = v.findViewById(R.id.informationFAB);

        Bundle args = getArguments();
        mMessageTextView.setText(args.getString(mMessage));
        mRankerTextView.setText(args.getInt(mRank) + "");

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Do I need supper?
        super.onActivityCreated(savedInstanceState);
        mInformationFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                new InformationDialog().show(getFragmentManager(), "");
            }
        });
    }

    public static ResistorFragment getFragment(String message, int rank) {
        ResistorFragment frag = new ResistorFragment();

        Bundle args = new Bundle();
        args.putString(mMessage, message);
        args.putInt(mRank, rank);
        frag.setArguments(args);

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
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.information_dialog, null);
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
    }

}
