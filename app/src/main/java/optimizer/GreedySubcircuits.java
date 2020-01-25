package optimizer;

import java.util.*;

public class GreedySubcircuits {

    private final Resistor[] mResistors;
    private final double mDesiredResistance;
    private DNA[] mDNA;
    private HashMap<Resistor, Integer> indices;

    /**
     * Constructor to keep arguments and initialize instance variables. Goal is to provide
     * genetic algorithm with close to optimal initial DNA to reduce number of generations.
     * @param resistances Array of resistances.
     * @param desiredResistance Resistance to achieve.
     * @param percentToProvide Percent of initial DNA to provided. Value should be [0, 100].
     */
    public GreedySubcircuits(double[] resistances, double desiredResistance, double percentToProvide) {
        if (desiredResistance <= 0)
            throw new IllegalArgumentException("Desired resistance must be a positive number.");
        if (resistances.length == 0)
            throw new IllegalArgumentException("Must supply at least one resistor.");
        if(percentToProvide > 100  || percentToProvide < 0)
            throw new IllegalArgumentException("Percent of DNA to provide must be between [0, 100]");
        mResistors = formatResistors(resistances);
        mDesiredResistance = desiredResistance;
        int DNASize = (int)Math.round(percentToProvide/100.0*resistances.length);
        mDNA = new DNA[DNASize];

        // For building the subcircuit DNA. (worth the expense?)
        indices = new HashMap();
        for(int i = 0; i < resistances.length; i++)
            indices.put(mResistors[i], i);
    }

    /**
     * Convert the array of resistances to an ordered array of Resistor Objects.
     * @param resistances Array of double resistances
     * @return Array of Resistor Objects.
     */
    public static Resistor[] formatResistors(double[] resistances) {
        int size = resistances.length;
        Arrays.sort(resistances);
        Resistor[] resistors = new Resistor[size];
        for(int i = 0; i < size; i ++)
            resistors[i] = new Resistor(resistances[i]);
        return resistors;
    }


    /**
     * Finds the index closest to the optimal resistance in the array resistors.
     *
     * The desired resistor either has an exact match or has
     * two existing mResistors that are the closest possible values: one is smaller
     * and one is larger. While performing a Binary Search and there is no exact match,
     * we find one of the two closest mResistors.
     *
     * @param resistors Space to search.
     * @param resistance Value to find.
     * @return Index of closest element in the array.
     */
    public static int indexOfClosestResistance(List<Resistor> resistors, double resistance) {
        int size = resistors.size();
        if (size == 1)
            return 0;

        int l = 0, r = size - 1, m = -1;
        double mResistance = -1;
        while (l <= r) {
            m = l + (r - l) / 2;
            mResistance = resistors.get(m).getResistance();

            if (mResistance == resistance) {
                return m;
            } else if (mResistance < resistance) {
                // Search right side.
                l = m + 1;
            } else {
                // Search left side.
                r = m - 1;
            }
        }

        /*Exact max is not found.*/
        int otherIndex;
        if (mResistance > resistance) {
            // The largest of the 2 closest mResistors are found.
            if (m != 0)
                // Desired resistor can be lower than all possible resistors,
                otherIndex = m - 1;
            else
                return m;
        } else {
            // The smaller of the 2 closest mResistors are found.
            if (m != size - 1)
                // Desired resistor can be larger than all possible resistors,
                otherIndex = m + 1;
            else
                return m;
        }

        double otherMDiff = Math.abs(resistors.get(otherIndex).getResistance() - resistance);
        double curMDiff = Math.abs(mResistance - resistance);
        if (curMDiff > otherMDiff) {
            return otherIndex;
        } else {
            return m;
        }
    }

    /**
     * Finds the best resistance to add in series with argument to minimize difference
     * between desired resistance and argument.
     *
     * @param subcircuitResistance Subcircuit resistance.
     * @return Optimal resistance to add in series.
     */
    private double bestSeriesResistance(double subcircuitResistance) {
        double best = mDesiredResistance - subcircuitResistance;
        if(best <= 0)
            throw new ArithmeticException("Best resistance cannot be negative.");
        return best;
    }

    /**
     * Finds the best resistance to add in parallel with argument to minimize difference
     * between desired resistance and argument.
     *
     * @param subcircuitResistance Subcircuit resistance.
     * @return Optimal resistance to add in series.
     */
    private double bestParallelResistance(double subcircuitResistance) {
        double best = (mDesiredResistance*subcircuitResistance)/(subcircuitResistance - mDesiredResistance);
        if(best <= 0)
            throw new ArithmeticException("Best resistance cannot be negative.");
        return best;
    }

    /**
     * Generate the DNA for greedy approach to build equivalence resistance.
     */
    public void generateDNA() {
        for(int i = 0; i < mDNA.length; i++) {
            int index = (int)(Math.random()*mResistors.length);
            mDNA[i] = growOn(index);
        }
    }

    public DNA growOnTest(int i) {
        return growOn(i);
    }

    /**
     * Finds and returns DNA for greedy approach.
     * May be too inefficient. Find a better heuristic to use.
     *
     * @param startI Index to start greedy search.
     * @return DNA of greedy solution.
     */
    private DNA growOn(int startI) {
        // Create mutable list of Resistor Objects to grow from. Unused resistors are stored here.
        List<Resistor> resistors = new ArrayList<>(); Collections.addAll(resistors, mResistors);
        Resistor startResistor = resistors.remove(startI);
        double totalResistance = startResistor.getResistance();

        // Setting up algorithm
        int max = 15; // Used to ensure near constant time.
        List<Resistor> involvedR = new ArrayList<>();
        involvedR.add(startResistor);

        // Setting up information for DNA.
        boolean[] survivors = new boolean[mResistors.length];
        survivors[startI] = true;
        int[] connections = new int[max];

        // Building subcircuit DNA.
        for(int i = 0; i < max && resistors.size() > 0; i++) {
            if (totalResistance < mDesiredResistance) {
                // Need to increase resistance value in this resistor via adding in series.
                int bestI = indexOfClosestResistance(resistors, bestSeriesResistance(totalResistance));
                Resistor bestResistor = resistors.get(bestI);
                double bestR = bestResistor.getResistance();
                double newR = Resistor.series(totalResistance, bestR);

                double newDiff = Math.abs(mDesiredResistance - newR);
                double oldDiff = Math.abs(mDesiredResistance - totalResistance);
                if (newDiff < oldDiff) {
                    // Adding resistor is beneficial.
                    survivors[indices.get(bestResistor)] = true;
                    connections[i] = Resistor.SERIES;
                    totalResistance = newR;
                    involvedR.add(bestResistor);
                    resistors.remove(bestI);
                } else {
                    break;
                }
            } else {
                // Need to decrease the resistance value in this resistor.
                double bestParallel = bestParallelResistance(totalResistance);
                int bestI = indexOfClosestResistance(resistors, bestParallel);
                Resistor bestResistor = resistors.get(bestI);
                double bestR = bestResistor.getResistance();
                double newR = Resistor.parallel(totalResistance, bestR);


                double newDiff = Math.abs(mDesiredResistance - newR);
                double oldDiff = Math.abs(mDesiredResistance - totalResistance);
                if (newDiff < oldDiff) {
                    // Adding resistor is beneficial.
                    survivors[indices.get(bestResistor)] = true;
                    connections[i] = Resistor.PARALLEL;
                    totalResistance = newR;
                    involvedR.add(bestResistor);
                    resistors.remove(bestI);
                } else {
                    break;
                }
            }
        }

        // Setting up information for DNA Object.
        int numSurvivors = involvedR.size();
        int[] receivers = new int[numSurvivors-1]; // Should always contain index for startResistor.
        int[] givers = new int[numSurvivors-1];
        int[] connectionsFit = Arrays.copyOfRange(connections, 0, numSurvivors-1);

        // Sorting involved Resistors to build DNA.
        List<Resistor> connectors = new ArrayList<>(involvedR.subList(1, numSurvivors));
        Collections.sort(involvedR);
        int receiverI = involvedR.indexOf(startResistor);
        for(int i = 0; i < numSurvivors-1; i++) {
            // Always adding to starting resistor: Resistor receiver = startResistor;
            Resistor giver = connectors.get(i);

            int giverIndex = involvedR.indexOf(giver);
            receivers[i] = receiverI;
            givers[i] = giverIndex;

            involvedR.remove(giverIndex);
            if(giverIndex < receiverI)
                --receiverI;
        }

        return new DNA(survivors, numSurvivors, receivers, givers, connectionsFit);
    }

    /**
     * Returns formatted resistances.
     * @return Resistor array.
     */
    public Resistor[] getFormattedResistances() {
        return mResistors;
    }

    /**
     * Returns DNA Objects.
     * @return DNA array.
     */
    public DNA[] getDNA() {
        return mDNA;
    }

}
