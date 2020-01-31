package com.example.equivalentresistor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResistorsAdapter extends RecyclerView.Adapter<ResistorsViewHolder> {
    private List<String[]> mResistorEntries;
    private List<Boolean[]> mLegalValues;

    public ResistorsAdapter(List<String[]> resistorEntries, List<Boolean[]> legalValues) {
        mResistorEntries = resistorEntries;
        mLegalValues = legalValues;
    }

    @Override
    public ResistorsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resistance_entry_viewholder, parent, false);
        return new ResistorsViewHolder(v, mResistorEntries, mLegalValues);
        // Create ViewHolder here and set listener here.
    }

    @Override
    public void onBindViewHolder(ResistorsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mResistorEntries.size();
    }
}
