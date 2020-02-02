package com.example.equivalentresistor;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResistorsAdapter extends RecyclerView.Adapter<ResistorsAdapter.ResistorsViewHolder> {
    private final static String TAG = "ResistorsAdapter";

    private List<String[]> mResistorEntries;
    private List<Boolean[]> mLegalValues;
    private List<ResistorsViewHolder> mHolders;

    public ResistorsAdapter(List<String[]> resistorEntries, List<Boolean[]> legalValues) {
        mResistorEntries = resistorEntries;
        mLegalValues = legalValues;
        mHolders = new ArrayList<>();
    }

    public List<ResistorsViewHolder> getHolders() {
        return mHolders;
    }

    @Override
    public ResistorsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resistance_entry_viewholder, parent, false);
        ResistorsViewHolder holder = new ResistorsViewHolder(v, mResistorEntries, mLegalValues, this);
        mHolders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(ResistorsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mResistorEntries.size();
    }

    public static class ResistorsViewHolder extends RecyclerView.ViewHolder {
        private TextView mOhmsEditText;
        private TextView mQtyEditText;
        private ImageButton mDeleteButton;
        private List<String[]> mResistorEntries;
        private List<Boolean[]> mLegalValues;
        private ResistorsAdapter mAdapter;

        public ResistorsViewHolder(View itemView, List<String[]> resistorEntries, List<Boolean[]> legalValues, ResistorsAdapter adapter) {
            super(itemView);
            mOhmsEditText = itemView.findViewById(R.id.ohmsEditText);
            mQtyEditText = itemView.findViewById(R.id.qtyEditText);
            mDeleteButton = itemView.findViewById(R.id.deleteButton);
            mResistorEntries = resistorEntries;
            mLegalValues = legalValues;
            mAdapter = adapter;

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if(pos < 0 || pos > mResistorEntries.size())
                        return;
                    mResistorEntries.remove(pos);
                    mLegalValues.remove(pos);
                    mAdapter.notifyItemRemoved(pos);
                    mAdapter.notifyItemRangeChanged(pos, mResistorEntries.size());
                }
            });
            mOhmsEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!(getAdapterPosition() >= 0))
                        return;
                    if(hasFocus) {
                        mOhmsEditText.setTextColor(Color.BLACK);
                        Log.d(TAG, "Focus on ohms editor");
                    } else {
                        Log.d(TAG, "Focus off ohms editor");
                        String ohms = mOhmsEditText.getText().toString();
                        String qty = mResistorEntries.get(getAdapterPosition())[1];
                        mResistorEntries.set(getAdapterPosition(), new String[]{ohms, qty});
                    }
                }
            });
            mQtyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!(getAdapterPosition() >= 0))
                        return;
                    if(hasFocus) {
                        mQtyEditText.setTextColor(Color.BLACK);
                        Log.d(TAG, "Focus on qty editor");
                    } else {
                        Log.d(TAG, "Focus off qty editor");
                        String qty = mQtyEditText.getText().toString();
                        String ohms = mResistorEntries.get(getAdapterPosition())[0];
                        mResistorEntries.set(getAdapterPosition(), new String[]{ohms, qty});
                    }
                }
            });
        }

        public void bind(int position) {
            String[] resistorEntry = mResistorEntries.get(position);
            Boolean[] legalVals = mLegalValues.get(position);

            mOhmsEditText.setText(resistorEntry[0]);
            mQtyEditText.setText(resistorEntry[1]);

            if(!legalVals[0]) {
                mOhmsEditText.setTextColor(Color.RED);
            }
            if(!legalVals[1]) {
                mQtyEditText.setTextColor(Color.RED);
            }
        }
    }
}
