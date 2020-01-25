package optimizer;

public class DNA implements Comparable<DNA>{
    /**
     * Chromosome describing which Resistor from the available Resistor Objects to select.
     */
    private PickerChromosome mPicker;
    /**
     * Chromosome describing how to structure the selected Resistor Objects.
     */
    private OrderChromosome mOrder;
    /**
     * Fitness of the DNA.
     */
    private double mFitness;
    /**
     * Total resistance of the resistor that this DNA builds.
     */
    private double mTotalResistance;
    /**
     * Size of resistor that this DNA builds.
     */
    private int mSize;
    /**
     * Rank of DNA.
     */
    private int mRank;

    /**
     * Constructor
     * @param size Number of available resistors.
     * @param upperPercentageOfSurvivors Upper percent of survivors to accept. This is important
     *                                   because the user is unlikely to want to use all the available resistors. This
     *                                   increases the speed of the algorithm by restricting the sample space.
     */
    public DNA(int size, int upperPercentageOfSurvivors) {
        if(upperPercentageOfSurvivors <= 0 || upperPercentageOfSurvivors > 100)
            upperPercentageOfSurvivors = 20;
        mPicker = new PickerChromosome(size, upperPercentageOfSurvivors);
        mOrder = new OrderChromosome(mPicker.mSize);
    }

    /**
     * Constructor.
     * @param pc PickerChromosome.
     * @param oc OrderChromosome.
     */
    public DNA(PickerChromosome pc, OrderChromosome oc) {
        mPicker = pc;
        mOrder = oc;
    }

    /**
     * Constructor
     * @param survivors Survivors.
     * @param numSurvivors Number of survivors.
     * @param receivers Order receivers.
     * @param givers Order givers.
     * @param connections Order connections.
     */
    public DNA(boolean[] survivors, int numSurvivors, int[] receivers, int[] givers, int[] connections) {
        mPicker = new PickerChromosome(survivors, numSurvivors);
        mOrder = new OrderChromosome(receivers, givers, connections);
    }

    @Override
    public int compareTo(DNA dna) {
        // Descending order.
        return ((Double)dna.mFitness).compareTo(mFitness);
    }

    public int getRank() {
        return mRank;
    }

    public void setRank(int rank) {
        mRank = rank;
    }

    public double getTotalResistance() {
        return mTotalResistance;
    }

    public void setTotalResistance(double totalResistance) {
        mTotalResistance = totalResistance;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;
    }


    public void setFitness(double fitness) {
        mFitness = fitness;
    }

    public double getFitness() {
        return mFitness;
    }

    public boolean[] getSurvivers() {
        return mPicker.mSurvivers;
    }

    public int[][] getOrder() {
        int[][] orders = {mOrder.mReceivers, mOrder.mGivers, mOrder.mConnections};
        return orders;
    }

    /**
     * Crosses this DNA with partner.
     * @param partner Partner to crossover with.
     * @param mutation Rate of mutations.
     * @return Child DNA.
     */
    public DNA crossover(DNA partner, double mutation) {
        mutation = mutation/100;
        PickerChromosome pc = crossoverPicker(partner.mPicker, mutation);
        OrderChromosome oc = crossoverOrder(partner.mOrder, pc.mSize, mutation);
        return new DNA(pc, oc);
    }

    /**
     * Mutate the survivors.
     * @param newSurvivors Survivors to mutate.
     * @param index Index to mutate.
     */
    private void mutateSurvivors(boolean[] newSurvivors, int index) {
        if(Math.random() < .5)
            newSurvivors[index] = true;
        else
            newSurvivors[index] = false;
    }

    /**
     * Mutate order.
     * @param receiver Receiver to mutate.
     * @param giver Giver to mutate.
     * @param connection Connection to mutate.
     * @param index index to mutate.
     * @param max Max index to select from,
     */
    private void mutateOrder(int[] receiver, int[] giver, int[] connection, int index, int max) {
        int[] orders = OrderChromosome.operands(max);
        receiver[index] = orders[0];
        giver[index] = orders[1];
        connection[index] = OrderChromosome.randConnection();
    }

    /**
     * Crossover PickerChromosome.
     * @param partner Partner to cross with.
     * @param mutation Mutation rate.
     * @return New PickerChromosome.
     */
    private PickerChromosome crossoverPicker(PickerChromosome partner, double mutation) {
        boolean[] survivors = partner.mSurvivers;
        int size = survivors.length, numSurvivors = 0;
        boolean[] newSurvivors = new boolean[size]; // Size is the number of available resistors, which is the same for all DNA.
        boolean[] thisGene = mPicker.mSurvivers;

        double middle = (1+mutation)/2;

        for (int i = 0; i < size; i++) {
            double ran = Math.random();
            if(ran <= mutation) {
                // Mutate this index.
                mutateSurvivors(newSurvivors, i);
            } else if(ran < middle) {
                // Choose this gene.
                newSurvivors[i] = thisGene[i];
            } else {
                // Choose partner's gene.
                newSurvivors[i] = survivors[i];
            }
            if(newSurvivors[i])
                ++numSurvivors;
        }
        if(numSurvivors == 0) {
            // Cannot return DNA with no survivors.
            if(Math.random() < 0.5)
                return mPicker;
            else
                return partner;
        } else {
            return new PickerChromosome(newSurvivors, numSurvivors);
        }
    }

    /**
     * Crossover OrderChromosome.
     * @param partner Partner to cross with.
     * @param numSurvivors Number of survivors from PickerChromosome.
     * @param mutation Mutate rate.
     * @return Return new OrderChromosome.
     */
    private OrderChromosome crossoverOrder(OrderChromosome partner, int numSurvivors, double mutation) {
        int[] partnerReceiver = partner.mReceivers, partnerGiver = partner.mGivers, partnerConnection = partner.mConnections;
        int[] thisReceiver = mOrder.mReceivers, thisGiver = mOrder.mGivers, thisConnection = mOrder.mConnections;
        int[] newReceiver = new int[numSurvivors - 1], newGiver = new int[numSurvivors - 1], newConnection = new int[numSurvivors - 1];

        int thisEndIndex = thisReceiver.length - 1;
        int partnerEndIndex = partnerReceiver.length - 1;
        int newEndIndex = newReceiver.length - 1;

        double middle = (1+mutation)/2;

        while(thisEndIndex >= 0 && partnerEndIndex >= 0 && newEndIndex >= 0) {
            double ran = Math.random();
            if(ran <= mutation) {
                // Mutate this index.
                /*
                numSurvivors - 1 - newEndIndex: The right-most index is deciding how to order or connect 2 resistors,
                so the max index is between [0, 1]; the second right-most index is deciding between 3 resistors, so
                the max index is between [0, 2]; etc.. This equation calculates the max index a certain position can have.
                The right-most position would then have a max index of 1 for example.
                 */
                mutateOrder(newReceiver, newGiver, newConnection, newEndIndex, numSurvivors - 1 - newEndIndex);
            } else if(ran < middle) {
                // Choose this gene.
                newReceiver[newEndIndex] = thisReceiver[thisEndIndex];
                newGiver[newEndIndex] = thisGiver[thisEndIndex];
                newConnection[newEndIndex] = thisConnection[thisEndIndex];
            } else {
                // Choose partner's gene.
                newReceiver[newEndIndex] = partnerReceiver[partnerEndIndex];
                newGiver[newEndIndex] = partnerGiver[partnerEndIndex];
                newConnection[newEndIndex] = partnerConnection[partnerEndIndex];
            }
            --thisEndIndex;
            --partnerEndIndex;
            --newEndIndex;
        }

        // Some survivors in the new OrderChromosome need instructions.
        if(newEndIndex >= 0) {
            while(newEndIndex >= 0) {
                if (thisEndIndex >= 0) {
                    newReceiver[newEndIndex] = thisReceiver[thisEndIndex];
                    newGiver[newEndIndex] = thisGiver[thisEndIndex];
                    newConnection[newEndIndex] = thisConnection[thisEndIndex];
                    --thisEndIndex;
                } else if(partnerEndIndex >= 0) {
                    newReceiver[newEndIndex] = partnerReceiver[partnerEndIndex];
                    newGiver[newEndIndex] = partnerGiver[partnerEndIndex];
                    newConnection[newEndIndex] = partnerConnection[partnerEndIndex];
                    --partnerEndIndex;
                } else {
                    // Must randomly generate them.
                    int[] operands = OrderChromosome.operands(numSurvivors - 1 - newEndIndex);
                    newReceiver[newEndIndex] = operands[0];
                    newGiver[newEndIndex] = operands[1];
                    newConnection[newEndIndex] = OrderChromosome.randConnection();
                }
                --newEndIndex;
            }
        }
        OrderChromosome oc = new OrderChromosome(newReceiver, newGiver, newConnection);
        return oc;
    }

    /**
     * Chromosome that specifies which available resistors to use.
     */
    private static class PickerChromosome {
        /**
         * Number of available resistors selected.
         */
        private int mSize = 0;
        /**
         * Describes which resistors to select.
         */
        private boolean[] mSurvivers;
        public PickerChromosome(int totalSize, int upperPercentage) {
            // Chance of surviving.
            double mKeep = (int)(Math.random()*(upperPercentage + 1)); // Uniformly random [0, 1). Can change if desired
            mSurvivers = new boolean[totalSize];
            if(totalSize == 1) {
                // Must have at least one survivor.
                mSurvivers[0] = true;
                ++mSize;
                return;
            }
            while(mSize == 0) {
                for (int i = 0; i < totalSize; i++) {
                    int ran = (int) (Math.random() * 101);
                    if (ran <= mKeep) {
                        // Resistor survives.
                        mSurvivers[i] = true;
                        ++mSize;
                    } else {
                        // Resistor dies.
                        mSurvivers[i] = false;
                    }
                }
                // Ensures we do not get stuck in a long loop.
                mKeep = Math.min(mKeep*1.2, upperPercentage);
            }
        }

        /**
         * Constructor
         * @param survivors Describes survivors.
         * @param size Number of survivors.
         */
        public PickerChromosome(boolean[] survivors, int size) {
            mSurvivers = survivors;
            mSize = size;
        }
    }

    /**
     * Chromosome to describe how to structure selected resistors.
     */
    private static class OrderChromosome {
        /**
         * Receivers the connection type from giver.
         */
        private int[] mReceivers;
        /**
         * Gives the connection type to receiver.
         */
        private int[] mGivers;
        /**
         * Connection type.
         */
        private int[] mConnections;

        /**
         * Randomly create structure.
         * @param numSurvivors Number of survivors.
         */
        public OrderChromosome(int numSurvivors) {
            /*
            What happens when size = 1? Evolution code (specifically the collapseResistor(...) method)
            should work fine.
             */

            int validSize = numSurvivors - 1;
            mReceivers = new int[validSize];
            mGivers = new int[validSize];
            mConnections = new int[validSize];

            int max = validSize;
            // mGiver[0] will combine with mReceiver[0] etc.
            for(int i = 0; i < validSize; i ++) {
                int[] operands = operands(max);
                mReceivers[i] = operands[0];
                mGivers[i] = operands[1];
                mConnections[i] = randConnection();
                --max;
            }
        }

        /**
         * Initialize structure with predetermined valued.
         * @param receivers Receivers.
         * @param givers Givers.
         * @param connections Connections
         */
        public OrderChromosome(int[] receivers, int[] givers, int[] connections) {
            mReceivers = receivers;
            mGivers = givers;
            mConnections = connections;
        }

        /**
         * Generate random receiver and giver value.
         * @param max
         * @return
         */
        private static int[] operands(int max) {
            if (max <= 0)
                throw new IllegalArgumentException("Max cannot be less than or equal to 0.");
            int operand1 = (int)(Math.random()*(max + 1));
            // Cannot choose the same index twice.
            int operand2 = (int)(Math.random()*max);
            // To account for the removal of operand1 and left shift.
            if (operand2 >= operand1)
                ++operand2;
            return new int[] {operand1, operand2};
        }

        /**
         * Generate random connection.
         * @return
         */
        private static int randConnection() {
            return Math.random() < 0.5 ? Resistor.SERIES : Resistor.PARALLEL;
        }
    }
}
