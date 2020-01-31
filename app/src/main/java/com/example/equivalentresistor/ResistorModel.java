package com.example.equivalentresistor;

import java.util.ArrayList;
import java.util.List;

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

    public void setResistances(double[] resistances) {
        mResistances.clear();
        for(double resistance : resistances)
            mResistances.add(resistance);
    }

    public List<Double> getResistancesList()  {
        return mResistances;
    }

    public double[] getResistancesArr() {
        int size = mResistances.size();
        double[] arr = new double[size];
        for(int i = 0; i < size; i++)
            arr[i] = mResistances.get(i);
        return arr;
    }

    // Private since you cannot construct this class.
    private ResistorModel() {
    }
}
