package optimizer;

public class Resistor implements Comparable<Resistor> {
    /**
     * Default values to describe a parallel connection
     */
    public static final int PARALLEL = 1;
    /**
     * Default values to describe a series connection
     */
    public static final int SERIES = 0;

    /**
     * Original resistance of this Resistor.
     */
    private final double mResistance;
    /**
     * Temp resistance value that can change when evolving.
     */
    private double mTempTotalResistance;
    /**
     * Temp Resistor size that can change when evolving.
     */
    private int mTempSize;

    /**
     * Constructor.
     * @param r Original resistance.
     */
    public Resistor(double r) {
        mResistance = r;
    }

    /**
     * Resets temp values for a new evolution cycle.
     */
    public void updateTemps() {
        mTempSize = 1;
        mTempTotalResistance = mResistance;
    }

    public double getTempTotalResistance() {
        return mTempTotalResistance;
    }

    public void setTempTotalResistance(double temp) {
        mTempTotalResistance = temp;
    }

    public int getTempSize() {
        return mTempSize;
    }

    public void addTempSize(int size) {
        mTempSize += size;
    }

    public double getResistance() {
        return mResistance;
    }

    public static double series(double r1, double r2) {
        return r1 + r2;
    }

    public static double parallel(double r1, double r2) {
        return (r1*r2)/(r1+r2);
    }

    @Override
    public int compareTo(Resistor rn) {
        double dif = mResistance - rn.mResistance;
        if(dif > 0) {
            return 1;
        } else if (dif < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return mResistance + "";
    }
}
