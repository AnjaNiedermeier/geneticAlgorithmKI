import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Beladungsstrategie {
    // Algorithm Hyperparameters
    private static int populationSize = 2000;
    private static int maxRounds = 1000;
    private static double crossoverRate = 0.2;

    private static final double initialMutationRate = 0.2;
    private static final double finalMutationRate = 0.6;
    private static double mutationRate = initialMutationRate;

    private static Auftrag[] auftraege;
    private static Lkw[] lkws;
    private static Population population;

    public static void main(String[] args) {

        // Einlesen von LKWs und Aufträgen
        System.out.println("Einlesen der Aufträge und LKWs...");
        auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        // Initialisieren der Anfangspopulation
        System.out.println("Initialisiere Anfangspopulation...");
        population = new Population(populationSize, lkws, auftraege);
        System.out.println("Finished Initialization");

        //Variables for storing best Solution
        Individual bestSolution = new Individual(new int[lkws.length][auftraege.length]);
        int bestSolutionScore = 0;

        // Wiederhole für maxRounds Runden
        for (int round = 1; round <= maxRounds; round++) {
            System.out.println("Runde " + round + ": ");
            
            // Merke beste valide Lösung
            Individual bestSolutionRound = population.getPopulation()[population.getBestSolutionIndex()];
            int bestSolutionRoundScore = bestSolutionRound.calculateFitness(auftraege, lkws);
            if (bestSolutionRoundScore > bestSolutionScore && bestSolutionRound.isValid(lkws, auftraege)) {
                System.out.println("Neue Beste Lösung gefunden");
                bestSolution = bestSolutionRound;
                bestSolutionScore = bestSolutionRoundScore;
            }

            // Select Parents for Reproduction
            int numParents = (int) Math.ceil(populationSize * crossoverRate);
            Individual[] parents = population.selectParentsRoulette(numParents);
            //Individual[] parents = population.selectParentsBestN(numParents);

            // Generate n offspring individuals from the n selected parents
            Individual[] offspring = population.generateOffspring(parents, lkws, auftraege);

            //gradually decrease mutation rate
            if(mutationRate<finalMutationRate){
                mutationRate+= (finalMutationRate-initialMutationRate)/maxRounds;
                System.out.println("Mutation Rate: "+ mutationRate);
            }
            //Mutate random individuals of the offspring
            offspring = population.mutation(offspring, mutationRate, auftraege, lkws);

            // Replace population with offspring individuals
            population.replacePopulation(offspring);

            // Mutate random individuals of the population
            //population = population.mutation(population, mutationRate, auftraege, lkws);

            // Berechne fitness der Lösungen
            population.calcFitness(lkws, auftraege);
            population.printFitnessStats();
        }

        // Print Best Solution
        System.out.println("-------------------------------");
        System.out.println("Best Solution:");
        bestSolution.printStrategy(bestSolutionScore);
    }

    private static Lkw[] readLkwsFromCSV(String pathToFile) {
        List<Lkw> lkws = new ArrayList<>();

        // create BufferedReader to read csv File line by line
        try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
            // read the first line from the text file (attribute names)
            String line = br.readLine();
            // loop until all lines are read
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split(";");
                lkws.add(new Lkw(attributes));
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Lkw[] lkwArray = lkws.stream().toArray(Lkw[]::new);
        return lkwArray;
    }

    private static Auftrag[] readAuftraegeFromCSV(String pathToFile) {
        List<Auftrag> auftraege = new ArrayList<>();

        // create BufferedReader to read csv File line by line
        try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
            // read the first line from the text file (attribute names)
            String line = br.readLine();
            // loop until all lines are read
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split(";");
                auftraege.add(new Auftrag(attributes));
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Auftrag[] auftraegeArray = auftraege.stream().toArray(Auftrag[]::new);
        return auftraegeArray;
    }

}
