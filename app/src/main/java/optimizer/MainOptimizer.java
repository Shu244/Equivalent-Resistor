package optimizer;

import java.util.*;

public class MainOptimizer {

    private static GreedySubcircuits genSubsDNA(double[] resistances, double desiredResistance) {
        System.out.println("------------Start--------------");
        GreedySubcircuits greedyAlgo = new GreedySubcircuits(resistances, desiredResistance, 40);
        System.out.println("Generating subcircuits' DNA.");
        greedyAlgo.generateDNA();
        System.out.println("Subcircuits' DNA generated.");
        return greedyAlgo;
    }

    private static List<String> optimize(EvolveOptimalResistors geneticAlgo, int numTop) {
        System.out.println("Evolving resistors.");
        geneticAlgo.evolve();
        System.out.println("Resistors evolved.");
        List<String> uniqueMostFitStrs = geneticAlgo.uniqueMostFitStr(numTop);
        System.out.println("------------End--------------");
        return uniqueMostFitStrs;
    }

    public static List<String> run(double[] resistances, double desiredResistance, int numTop) {
        GreedySubcircuits greedyAlgo = genSubsDNA(resistances, desiredResistance);
        Resistor[] availableResistors = greedyAlgo.getFormattedResistances();
        DNA[] initialDNA = greedyAlgo.getDNA();
//        Resistor[] noInitial = new Resistor[resistances.length];
//        for(int i = 0; i < resistances.length; i ++)
//            noInitial[i] = new Resistor(resistances[i]);
        EvolveOptimalResistors geneticAlgo = new EvolveOptimalResistors(availableResistors, desiredResistance, initialDNA);
        return optimize(geneticAlgo, numTop);
    }

    public static List<String> run(double[] resistances, double desiredResistance, int sizePriority, int popSize, int mutationRate, int numGen, int numTop) {
        GreedySubcircuits greedyAlgo = genSubsDNA(resistances, desiredResistance);
        Resistor[] availableResistors = greedyAlgo.getFormattedResistances();
        DNA[] initialDNA = greedyAlgo.getDNA();
        EvolveOptimalResistors geneticAlgo = new EvolveOptimalResistors(availableResistors, desiredResistance, sizePriority,
                popSize, mutationRate, numGen, initialDNA);
        return optimize(geneticAlgo, numTop);
    }


    private static List<Queue<DNADecipherUnit>> optimizeAndGetQueues(EvolveOptimalResistors geneticAlgo, int numTop) {
        System.out.println("Evolving resistors.");
        geneticAlgo.evolve();
        System.out.println("Resistors evolved.");
        List<Queue<DNADecipherUnit>> queues = geneticAlgo.uniqueMostFitQueues(numTop);
        System.out.println("------------End--------------");
        return queues;
    }


    public static List<Queue<DNADecipherUnit>> runAndGetQueues(double[] resistances, double desiredResistance, int sizePriority, int popSize, int mutationRate, int numGen, int numTop) {
        GreedySubcircuits greedyAlgo = genSubsDNA(resistances, desiredResistance);
        Resistor[] availableResistors = greedyAlgo.getFormattedResistances();
        DNA[] initialDNA = greedyAlgo.getDNA();
        EvolveOptimalResistors geneticAlgo = new EvolveOptimalResistors(availableResistors, desiredResistance, sizePriority,
                popSize, mutationRate, numGen, initialDNA);
        return optimizeAndGetQueues(geneticAlgo, numTop);
    }
}
