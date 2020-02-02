package optimizer;

import java.util.*;

public class EvolveOptimalResistors {
    private final Resistor[] mAvailableResistors;
    private int mSizePriority;
    private int mAccuracyPriority;
    private DNA[] mPop;
    private double mMutationRate;
    private double mDesiredResistance;
    private double mTotalFitness;
    private int mNumGenerations;

    /*
    Higher sizePriority means more priority towards size so equivalent resistors will tend to be smaller.
    Recommended sizePriority is 75-80.
     */
    public EvolveOptimalResistors(Resistor[] availableResistors, double desiredResistance, int sizePriority,
                                  int popSize, int mutationRate, int numGen, DNA[] initial) {
        check0_100(sizePriority);
        check0_100(mutationRate);
        if(popSize < 1)
            throw new IllegalArgumentException("Population must be a positive number");
        if(desiredResistance <= 0)
            throw new IllegalArgumentException("Desired resistance must be a positive number.");
        if(numGen < 0)
            throw new IllegalArgumentException("Number of generations must be >= 0.");


        mAvailableResistors = availableResistors;
        int numAvailable = availableResistors.length;

        if(numAvailable < 1)
            throw new IllegalArgumentException("Must provide at least one available resistor.");

        mSizePriority = sizePriority;
        mAccuracyPriority = 100 - sizePriority;

        mPop = new DNA[popSize];
        System.arraycopy( initial, 0, mPop, 0, initial.length );
        for(int i = initial.length; i < popSize; i ++)
            mPop[i] = new DNA(numAvailable, 100 - sizePriority);


        mMutationRate = mutationRate;
        mDesiredResistance = desiredResistance;
        mNumGenerations = numGen;
    }

    /*
    Constructor to automatically set genomic algorithm parameters.
     */
    public EvolveOptimalResistors(Resistor[] availableResistors, double desiredResistance,  DNA[] initial) {
        mAvailableResistors = availableResistors;
        int numAvailable = mAvailableResistors.length;

        if(numAvailable < 1)
            throw new IllegalArgumentException("Must provide at least one available resistor.");
        if(desiredResistance <= 0)
            throw new IllegalArgumentException("Desired resistance must be a positive number.");

        mSizePriority = 50;
        mAccuracyPriority = 100 - mSizePriority;

        int popSize = 3000;
        mPop = new DNA[popSize];
        System.arraycopy( initial, 0, mPop, 0, initial.length );
        for(int i = initial.length; i < popSize; i ++)
            mPop[i] = new DNA(numAvailable, 100 - mSizePriority);

        mMutationRate = 4.5;
        mDesiredResistance = desiredResistance;
        mNumGenerations = 50;
    }

    private void check0_100(int val) {
        if(val < 0 || val > 100)
            throw new IllegalArgumentException("Percentages must be between [0, 100]");
    }

    public void evolve() {
        // Aggressive early stopping to since speed is very important.
        double prevTotalFitness = 0;
        boolean earlyStoppingTiggered = false;
        for(int i = 0; i < mNumGenerations; i ++) {
            mTotalFitness = 0;
            double[] allFitness = computeAllFitness();
            if(mTotalFitness < prevTotalFitness) {
                System.out.println("Early stopping triggered.");
                earlyStoppingTiggered = true;
                break;
            }
            prevTotalFitness = mTotalFitness;
            mPop = children(allFitness);
            String log = String.format("Ending Generation %d, AVG FITNESS: %.5f", i+1, mTotalFitness);
            System.out.println(log);
        }
        if(!earlyStoppingTiggered)
            // Update the fitness for last generation of DNA.
            computeAllFitness();
    }

    private double[] computeAllFitness() {
        int numPop = mPop.length;
        double[] allFitness = new double[numPop];
        for(int i = 0; i < numPop; i ++) {
            allFitness[i] = fitness(i);
            mTotalFitness += allFitness[i];
        }
        return allFitness;
    }

    /*
    Account for size, accuracy, and error propagation?
     */
    private double fitness(int index) {
        DNA one = mPop[index];
        boolean[] survivors = one.getSurvivers();
        int[][] order = one.getOrder();
        int[] receivers = order[0];
        int[] givers = order[1];
        int[] connections = order[2];

        List<Resistor> sampleResistors = getSelectedResistors(survivors);

        Resistor r = collapseResistors(sampleResistors, receivers, givers, connections);

        double weightedSum = inverseWeightedSum(r);
        //double weightedSum = linearWeightedSum(r);

        one.setFitness(weightedSum);
        one.setTotalResistance(r.getTempTotalResistance());
        one.setSize(r.getTempSize());
        return weightedSum;
    }

    /*
    Requires extreme priorities to have visible effect.
     */
    private double inverseWeightedSum(Resistor r) {
        double diff = Math.abs(mDesiredResistance - r.getTempTotalResistance());
        double squareSizePenalty = Math.pow(r.getTempSize(), 1.5);
        double result =  1 / (diff * mAccuracyPriority + squareSizePenalty * mSizePriority);
        return result;
    }

    private double linearWeightedSum(Resistor r) {
        double diff = Math.abs(mDesiredResistance - r.getTempTotalResistance());
        double result =  Integer.MAX_VALUE - (diff * mAccuracyPriority + r.getTempSize() * mSizePriority);
        result = Math.max(result, 0);
        return result;
    }


    private List<Resistor> getSelectedResistors(boolean[] survivors) {
        List<Resistor> resistors = new ArrayList<>();
        for(int i = 0; i < survivors.length; i ++) {
            if(survivors[i]) {
                Resistor r = mAvailableResistors[i];
                // Allows us to track changes via temp variables during evolution
                r.updateTemps();
                resistors.add(r);
            }
        }
        return resistors;
    }

    /*
    One method for improvement: When an exact match is found, replace the DNA to contain
    only information to recreate the exact match. This prevents unnecessary data from
    propagating into later generations.
     */
    private Resistor collapseResistors(List<Resistor> sampleResistors, int[] receivers, int[] givers, int[] connections) {
        int size = receivers.length;
        for(int i = 0; i < size; i ++) {
            Resistor r = sampleResistors.get(receivers[i]);
            Resistor g = sampleResistors.remove(givers[i]);
            int connection = connections[i];
            collapseResistorHelper(r, g, connection);
        }

        if(sampleResistors.size() != 1)
            throw new ArithmeticException("Collapsed resistor didn't collapse to one resistor.");
        return sampleResistors.get(0);
    }

    private void collapseResistorHelper(Resistor r, Resistor g, int connection) {
        double resistance1 = r.getTempTotalResistance();
        double resistance2 = g.getTempTotalResistance();

        if(connection == Resistor.SERIES) {
            r.setTempTotalResistance(Resistor.series(resistance1, resistance2));
        } else if (connection == Resistor.PARALLEL) {
            r.setTempTotalResistance(Resistor.parallel(resistance1, resistance2));
        } else {
            throw new IllegalArgumentException("A connection can only be series or parallel.");
        }
        r.addTempSize(g.getTempSize());
    }

    /*
    What is best when rTotal == mDesiredResistance?
     */
    private int parallelOrSeries(Resistor r) {
        double rTotal = r.getTempTotalResistance();
        /* An exact resistor match isn't the perfect solution b/c
           the size also matters */
        if(rTotal < mDesiredResistance) {
            // Need to increase resistance via series.
            return Resistor.SERIES;
        } else {
            // Need to increase resistance via parallel.
            return Resistor.PARALLEL;
        }
    }

    /*
    Need a method based on fitness to determine who moves on to the next generation. This method
    should be O(n) where n is the size of the population. My method is to select a number between
    [0, 100]. Each DNA takes up an area between [0, 100]. The range that covers the selected
    number will be a parent to the next generation.
     */
    private DNA[] children(double[] allFitness) {
        int size = mPop.length;
        DNA[] children = new DNA[size];
        double[] cumFitness = cumFitnessArr(allFitness);
        for(int i = 0; i < size; i++) {
            // Random numbers between [0, 1)
            double ran1 = Math.random();
            double ran2 = Math.random();
            DNA mom = getParent(ran1, cumFitness);
            DNA dad = getParent(ran2, cumFitness);
            DNA child = crossover(mom, dad);
            children[i] = child;
        }
        return children;
    }

    private double[] cumFitnessArr(double[] allFitness) {
        int size = allFitness.length;
        double sum = 0;
        double[] cumFitness = new double[size];
        for(int i = 0; i < size; i ++) {
            sum += (allFitness[i]/mTotalFitness);
            cumFitness[i] = sum;
        }
        return cumFitness;
    }

    private DNA getParent(double random, double[] cumFitness) {
        int indexOfParent = getFirstGreaterOrEqual(cumFitness, random);
        return mPop[indexOfParent];
    }

    public static int getFirstGreaterOrEqual(double[] arr, double target) {
        int start = 0, end = arr.length - 1;
        int index = -1;
        while (start <= end) {
            int mid = (start + end) / 2;

            // Move to right side if target is greater.
            if (arr[mid] < target) {
                start = mid + 1;
            }

            // Move left side.
            else {
                index = mid;
                end = mid - 1;
            }
        }
        if (index == -1)
            // target is the largest number. Exceptionally unlikely to occur.
            return arr.length - 1;
        else
            return index;
    }

    private DNA crossover(DNA parent1, DNA parent2) {
        return parent1.crossover(parent2, mMutationRate);
    }

    /**
     * Holds the resistor that contains the temp information for the completed circuit.
     */
    public Queue<DNADecipherUnit> RPNQueue(DNA dna) {
        boolean[] survivors = dna.getSurvivers();
        int[][] orders = dna.getOrder();
        int[] receivers = orders[0];
        int[] givers = orders[1];
        int[] connections = orders[2];
        int size = givers.length;

        List<Resistor> sampleResistors = getSelectedResistors(survivors);
        Set<Resistor> inQueue = new HashSet<>();
        Queue<DNADecipherUnit> RPN = new LinkedList<>();
        RPN.add(new DNADecipherUnit(dna));

        for(int i = 0; i < size; i ++) {
            Resistor r = sampleResistors.get(receivers[i]);
            Resistor g = sampleResistors.remove(givers[i]);
            int connection = connections[i];
            collapseResistorHelper(r, g, connection);

            DNADecipherUnit operator = new DNADecipherUnit(connection);
            if(!inQueue.contains(r)) {
                DNADecipherUnit operand = new DNADecipherUnit(r);
                inQueue.add(r);
                RPN.add(operand);
            }
            if(!inQueue.contains(g)) {
                DNADecipherUnit operand = new DNADecipherUnit(g);
                inQueue.add(g);
                RPN.add(operand);
            }
            RPN.add(operator);

        }
        if(RPN.size() == 1) {
            if(sampleResistors.size() > 1)
                throw new ArithmeticException("RPNQueue contains no Resistor Objects but there are more than 1 Resistor in the sample.");
            // There is only one resistor in the sample.
            DNADecipherUnit unit = new DNADecipherUnit(sampleResistors.get(0));
            RPN.add(unit);
        }
        return RPN;
    }

    public void rankDNA() {
        Arrays.sort(mPop);
        for(int i = 0; i < mPop.length; i ++)
            mPop[i].setRank(i+1);
    }

    public DNA[] mostFitDNA(int num) {
        rankDNA();
        if (num == -1) {
            return mPop;
        } else {
            int maxResults = num > mPop.length ? mPop.length : num;
            DNA[] topResults = Arrays.copyOfRange(mPop, 0, maxResults);
            return topResults;
        }
    }

    public List<String> uniqueMostFitStr(int num) {
        DNA[] topResults = mostFitDNA(-1);
        List<String> visuals = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for(int i = 0; i < topResults.length && visuals.size() < num; i ++) {
            DNA one = topResults[i];
            String code = one.getTotalResistance() + " " + one.getSize();
            if (!used.contains(code)) {
                used.add(code);
                Queue<DNADecipherUnit> queue = RPNQueue(one);
                visuals.add(visualizeDNA(queue, true));
            }
        }
        return visuals;
    }

    public List<Queue<DNADecipherUnit>> uniqueMostFitQueues(int numTop) {
        DNA[] dna = mostFitDNA(-1);
        List<Queue<DNADecipherUnit>> queues = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for(int i = 0; i < dna.length && queues.size() < numTop; i++) {
            DNA one = dna[i];
            String code = one.getTotalResistance() + " " + one.getSize();
            if (!used.contains(code)) {
                used.add(code);
                Queue<DNADecipherUnit> queue = RPNQueue(one);
                queues.add(queue);
            }
        }
        return queues;
    }

    public static String visualizeDNA(Queue<DNADecipherUnit> RPNQueue, boolean appendExtras) {
        DNADecipherUnit first = RPNQueue.poll();
        if(first.type() != DNADecipherUnit.DNA)
            throw new ArithmeticException("First element in RPN must be DNA that created this RPN.");
        DNA instructions = first.getDNA();

        Stack<DNADecipherUnit> stack = new Stack<>();
        while(!RPNQueue.isEmpty()) {
            DNADecipherUnit unit = RPNQueue.poll();
            if(unit.type() == DNADecipherUnit.OPERATOR) {
                DNADecipherUnit giver = stack.pop();
                DNADecipherUnit receiver = stack.pop();
                receiver.combine(giver, unit.getOperator());
                stack.add(receiver);
            } else {
                stack.add(unit);
            }
        }
        DNADecipherUnit result = stack.pop();
        if(appendExtras) {
            String visual = String.format("%s, TOTAL RESISTANCE: %.3f ohms, TOTAL SIZE: %d, RANK: %d",
                    result.getVisual(), instructions.getTotalResistance(), instructions.getSize(), instructions.getRank());
            return visual;
        } else {
            return result.getVisual();
        }
    }
}
