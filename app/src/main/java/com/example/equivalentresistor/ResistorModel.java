package com.example.equivalentresistor;

class ResistorModel {

    private static final ResistorModel mModel = new ResistorModel();
    private double[] mResistances = new double[0];

    static ResistorModel getInstance() {
        return mModel;
    }

    public int getSize() {
        return mResistances.length;
    }

    public void setResistances(double[] resistances) {
        mResistances = resistances;
    }

    public double[] getResistances()  {
        return mResistances;
    }

    // Private since you cannot construct this class.
    private ResistorModel() {
    }
}
