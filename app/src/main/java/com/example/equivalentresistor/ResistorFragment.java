package com.example.equivalentresistor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ResistorFragment extends Fragment {
    private TextView mMessageTextView;
    private FloatingActionButton mDownloadFAB;
    private FloatingActionButton mInformationFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.transition_fragment, container, false);
        mMessageTextView = v.findViewById(R.id.messageTextView);
        mDownloadFAB = v.findViewById(R.id.downloadFAB);
        mInformationFAB = v.findViewById(R.id.informationFAB);
        return v;
    }

    public static ResistorFragment getFragment(String message) {
        ResistorFragment frag = new ResistorFragment();
        frag.mMessageTextView.setText(message);
        return frag;
    }
}
