package com.example.equivalentresistor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResistorSetsAdapter extends RecyclerView.Adapter<ResistorSetsAdapter.ResistorSetsViewHolder> {

    private List<String> mSetNames;

    public ResistorSetsAdapter(List<String> setNames) {
        mSetNames = setNames;
    }

    @Override
    public ResistorSetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resistor_sets_viewholder, parent, false);
        return new ResistorSetsViewHolder(v, mSetNames, this);
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

    public static class ResistorSetsViewHolder extends RecyclerView.ViewHolder {

        private TextView mSetNameTextView;
        private ImageButton mDeleteButton;
        private List<String> mNames;
        private ResistorSetsAdapter mAdapter;

        public ResistorSetsViewHolder(View itemView, List<String> names, ResistorSetsAdapter adapter) {
            super(itemView);
            mSetNameTextView = itemView.findViewById(R.id.setNameTextView);
            mDeleteButton = itemView.findViewById(R.id.deleteButton);
            mNames = names;
            mAdapter = adapter;

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if(pos < 0 || pos > mNames.size())
                        return;
                    String name = mNames.remove(pos);
                    if(name.charAt(0) == '~')
                        ResistorModel.getInstance().setResistances(new double[0]);
                    MainActivity.removeFile(v.getContext(), name);
                    mAdapter.notifyItemRemoved(pos);
                    mAdapter.notifyItemRangeChanged(pos, mNames.size());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    Context context = v.getContext();
                    context.startActivity(EditSetActivity.getIntent(context, mSetNameTextView.getText().toString()));
                }
            });
        }

        public void bind(String name) {
            mSetNameTextView.setText(name);
        }
    }
}
