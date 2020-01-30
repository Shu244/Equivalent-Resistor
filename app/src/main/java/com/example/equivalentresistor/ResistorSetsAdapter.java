package com.example.equivalentresistor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResistorSetsAdapter extends RecyclerView.Adapter<ResistorSetsViewHolder> {

    private List<String> mSetNames;

    public ResistorSetsAdapter(List<String> setNames) {
        mSetNames = setNames;
    }

    @Override
    public ResistorSetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resistor_sets_viewholder, parent, false);
        return new ResistorSetsViewHolder(v, mSetNames);
        // Create ViewHolder here and set listener here.
    }

    @Override
    public void onBindViewHolder(ResistorSetsViewHolder holder, int position) {
        holder.bind(mSetNames.get(position));
    }

    @Override
    public int getItemCount() {
        return mSetNames.size();
    }
}
