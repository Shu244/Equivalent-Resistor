package com.example.equivalentresistor;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResistorsViewHolder extends RecyclerView.ViewHolder {
    private TextView mOhmsEditText;
    private TextView mQtyEditText;
    private Button mDeleteButton;
    private List<String[]> mResistorEntries;
    private List<Boolean[]> mLegalValues;

    public ResistorsViewHolder(View itemView, List<String[]> resistorEntries, List<Boolean[]> legalValues) {
        super(itemView);
        mOhmsEditText = itemView.findViewById(R.id.ohmsEditText);
        mQtyEditText = itemView.findViewById(R.id.qtyEditText);
        mDeleteButton = itemView.findViewById(R.id.deleteButton);
        mResistorEntries = resistorEntries;
        mLegalValues = legalValues;

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mResistorEntries.remove(getAdapterPosition());
            }
        });
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mOhmsEditText.setTextColor(Color.BLACK);
                    mQtyEditText.setTextColor(Color.BLACK);
                } else {
                    String ohms = mOhmsEditText.getText().toString();
                    String qty = mQtyEditText.getText().toString();
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
