package com.example.equivalentresistor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class NoResistorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v = inflater.inflate(R.layout.no_resistor_fragment, container, false);
        return v;
    }

    public static Fragment getFragment() {
        return new NoResistorFragment();
    }
}
