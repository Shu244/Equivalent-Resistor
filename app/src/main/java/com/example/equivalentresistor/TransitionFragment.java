package com.example.equivalentresistor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class TransitionFragment extends Fragment {

    private TextView mMessageTextView;
    private static final String mMessage = "com.example.equivalentresistor.transistion_message";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.transition_fragment, container, false);
        mMessageTextView = v.findViewById(R.id.messageTextView);

        Bundle args = getArguments();
        mMessageTextView.setText(args.getString(mMessage));

        return v;
    }

    public static TransitionFragment getFragment(String message) {
        TransitionFragment transition = new TransitionFragment();

        Bundle args = new Bundle();
        args.putString(mMessage, message);
        transition.setArguments(args);

        return transition;
    }

}
