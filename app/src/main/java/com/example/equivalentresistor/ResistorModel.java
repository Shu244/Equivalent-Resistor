package com.example.equivalentresistor;

import java.util.ArrayList;
import java.util.List;
import exceptions.NoResistancesException;

class ResistorModel {

    private static final ResistorModel mModel = new ResistorModel();
    private List<Double> mResistances = new ArrayList<>();

    static ResistorModel getInstance() {
        return mModel;
    }

    public int getSize() {
        return mResistances.size();
    }

    public void setResistances(List<Double> resistances) {
        mResistances = resistances;
    }

    public List<Double> getResistances() throws NoResistancesException {
        if(mResistances.size() == 0)
            throw new NoResistancesException();
        return mResistances;
    }

    // Private since you cannot construct this class.
    private ResistorModel() {
    }
}
