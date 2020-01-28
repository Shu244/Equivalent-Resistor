package com.example.equivalentresistor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        // Do I need super? NO
        View v = inflater.inflate(R.layout.transition_fragment, container, false);
        mMessageTextView = v.findViewById(R.id.messageTextView);
        mRankerTextView = v.findViewById(R.id.rankerTextView);
        mInformationFAB = v.findViewById(R.id.informationFAB);
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
        frag.mMessageTextView.setText(message);
        frag.mRankerTextView.setText(rank + "");
        return frag;
    }

    class InformationDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.information_dialog, null);
            return new AlertDialog.Builder(getActivity())
                    .setView(v) // Set date selector view between title and button(s)
                    .setTitle(R.string.dialog_information_title)
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
