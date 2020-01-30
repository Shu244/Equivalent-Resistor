package com.example.equivalentresistor;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResistorSetsViewHolder extends RecyclerView.ViewHolder {

    private TextView mSetNameTextView;
    private Button mDeleteButton;
    private List<String> mNames;

    public ResistorSetsViewHolder(View itemView, List<String> names) {
        super(itemView);
        mSetNameTextView = itemView.findViewById(R.id.setNameTextView);
        mDeleteButton = itemView.findViewById(R.id.deleteButton);
        mNames = names;

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                mNames.remove(getAdapterPosition());
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                // Launch Fragment.
            }
        });
    }

    public void bind(String name) {
        mSetNameTextView.setText(name);
    }
}
