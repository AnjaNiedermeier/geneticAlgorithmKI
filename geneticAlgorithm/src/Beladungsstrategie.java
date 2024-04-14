import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Beladungsstrategie {
    public static void main(String[] args) {
        // Algorithm Hyperparameters
        int populationSize = 1000;
        int maxRounds = 300;
        double mutationRate = 0.3;

        List<Auftrag> auftraege;
        List<Lkw> lkws;
        int[][][] population;

        // Einlesen von LKWs und Aufträgen
        System.out.println("Einlesen der Aufträge und LKWs...");
        auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        // Initialisieren der Anfangspopulation
        System.out.println("Initialisiere Anfangspopulation...");
        population = initPopulation(populationSize, lkws.size(), auftraege.size(), lkws, auftraege);
        System.out.println("Finished Initialization");

        // Wiederhole für maxRounds Runden
        for (int round = 1; round <= maxRounds; round++) {

            System.out.println("Runde " + round + ": ");
            // Berechne fitness der Lösungen
            int[] fitness = calculateFitnessPopulation(populationSize, auftraege, lkws, population);

            // Select Parents for Reproduction
            //int numParents = (int) Math.round(crossoverRate * populationSize);
            int numParents = getNumNegativeFitnessValues(fitness);
            int[][][] parents = selectParents(populationSize, numParents, auftraege, lkws, population, fitness);

            // Generate n offspring individuals from the n selected parents
            int[][][] offspring = generateOffspring(parents, lkws, auftraege);

            // Replace population with offspring individuals
            replacePopulation(population, offspring, fitness);

            // Mutate random individuals of the population
            population = mutation(population, mutationRate, auftraege, lkws);
        }

        //Print final Population Fitnesses
        System.out.println("-------------------------------");
        System.out.println("Final Population Fitness Scores:");
        int[] fitness = calculateFitnessPopulation(populationSize, auftraege, lkws, population);
    }

    private static int getNumNegativeFitnessValues(int[] fitness) {
        int num = 0;
        for(int i = 0; i<fitness.length; i++){
            if (fitness[i] < 0){
                num ++;
            }
        }
        return num;
    }

    private static int[][][] mutation(int[][][] population, double mutationRate, List<Auftrag> auftraege, List<Lkw> lkws) {
        int mutationAmount = (int) Math.ceil(mutationRate * population.length);
        // Choose random contracts to mutate
        Random random = new Random();
        for (int i = 0; i < mutationAmount; i++) {
            int indexMutation = random.nextInt(population.length);
            int[][] mutationCandidate = population[indexMutation];
            mutationCandidate = mutateCandidate(mutationCandidate, 0.01);
            if(isValidIndividual(mutationCandidate, lkws, auftraege)){
                population[indexMutation] = mutationCandidate;
            }
        }
        return population;
    }

    private static int[][] mutateCandidate(int[][] mutationCandidate, double mutationRate) {
        int[][] mutatedCandidate = Arrays.stream(mutationCandidate).map(int[]::clone).toArray(int[][]::new);
        Random random = new Random();

        // Perform swap mutation with a certain probability
        for (int i = 0; i < mutatedCandidate.length; i++) {
            for (int j = 0; j < mutatedCandidate[i].length; j++) {
                if (random.nextDouble() < mutationRate) {
                    // Randomly select another element to swap with
                    int swapRow = random.nextInt(mutatedCandidate.length);
                    int swapCol = random.nextInt(mutatedCandidate[0].length);

                    // Perform the swap
                    int temp = mutatedCandidate[i][j];
                    mutatedCandidate[i][j] = mutatedCandidate[swapRow][swapCol];
                    mutatedCandidate[swapRow][swapCol] = temp;
                }
            }
        }

        return mutatedCandidate;
    }

    // Get the worst individuals of the population and replace them
    private static void replacePopulation(int[][][] population, int[][][] offspring, int[] fitness) {
        int[] worstIndices = getWorstIndividuals(fitness, offspring.length);
        int index = 0;
        for (int i = 0; i < worstIndices.length; i++) {
            population[i] = offspring[index++];
        }
    }

    private static int[] getWorstIndividuals(int[] fitness, int n) {
        // Create a copy of the original array
        int[] sortedFitness = Arrays.copyOf(fitness, fitness.length);
        Arrays.sort(sortedFitness);
        int[] worstIndices = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < fitness.length; j++) {
                if (fitness[j] == sortedFitness[i]) {
                    worstIndices[i] = j;
                }
            }
        }
        return worstIndices;
    }

    private static int[][][] selectParents(int populationSize, int numParents, List<Auftrag> auftraege,
            List<Lkw> lkws, int[][][] population, int[] fitness) {

        List<Integer> parentList = new ArrayList<>();
        while (parentList.size() < numParents) {
            int currentParent = selectParent(fitness);
            //if (!parentList.contains(currentParent)) {
                parentList.add(currentParent);
            //}
        }

        int[] parentIndices = parentList.stream().mapToInt(i -> i).toArray();
        // Get parents from parentIndices
        int[][][] parents = new int[numParents][lkws.size()][auftraege.size()];
        int index = 0;
        for (int i : parentIndices) {
            parents[index++] = population[i];
        }
        return parents;
    }

    private static int[] calculateFitnessPopulation(int populationSize, List<Auftrag> auftraege, List<Lkw> lkws,
            int[][][] population) {
        int[] fitness = new int[populationSize];
        for (int i = 0; i < fitness.length; i++) {
            fitness[i] = calculateFitnessIndividual(population[i], auftraege, lkws);
        }
        // Print average, highest an lowest fitness
        int fitnessSum = 0;
        int minFitness = Integer.MAX_VALUE;
        int maxFitness = Integer.MIN_VALUE;
        for (int fit : fitness) {
            fitnessSum += fit;
            if(fit < minFitness){
                minFitness = fit;
            }
            if(fit > maxFitness){
                maxFitness = fit;
            }
        }
        System.out.println("Average Fitness: " + fitnessSum / fitness.length);
        System.out.println("Min Fitness: " + minFitness);
        System.out.println("Max Fitness: " + maxFitness);
        return fitness;
    }

    private static int[][][] generateOffspring(int[][][] parents, List<Lkw> lkws, List<Auftrag> auftraege) {
        int num_parents = parents.length;
        // Create a List to store valid offspring individuals
        List<int[][]> childrenList = new ArrayList<>();

        // Generate Combinations of two parents to crossover
        int[][] combinations = new int[num_parents][2];
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < num_parents; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        // Fill the array with combinations of tuples
        for (int i = 0; i < num_parents; i++) {
            combinations[i][0] = numbers.get(i);
            combinations[i][1] = numbers.get((i + 1) % num_parents); // Ensuring no tuple contains the same number
        }
        // Fill Children with crossovers from combinations of two parents
        for (int i = 0; i < num_parents; i++) {
            int[][] child = verticalBandCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            if (isValidIndividual(child, lkws, auftraege)) {
                childrenList.add(child);
            }
        }
        int[][][] childrenArray = childrenList.stream().toArray(int[][][]::new);
        return childrenArray;
    }

    // Two random numbers are generated , and information inside the vertical region
    // of the grid determined by the numbers is exchanged. In this case, we take
    // parentB and copy some random auftraege from parentA into it
    private static int[][] verticalBandCrossover(int[][] parentA, int[][] parentB) {
        // Select two random points for crossover (in the range of Aufträge)
        Random random = new Random();
        int startPoint = random.nextInt(parentA[0].length);
        int endPoint = random.nextInt(parentA[0].length - startPoint) + startPoint;

        // Copy the segment between the two points from parentA to child
        for (int j = startPoint; j <= endPoint; j++) {
            for (int i = 0; i < parentA.length; i++) {
                parentB[i][j] = parentA[i][j];
            }
        }
        return parentB;
    }

    private static int selectParent(int[] fitness) {
        // Calculate total Fitness of population (Roulette Wheel)
        int totalFitness = 0;
        for (int i = 0; i < fitness.length; i++) {
            if(fitness[i]>0){
                totalFitness += fitness[i];
            }
        }

        // Generate a random number between 0 and totalFitness
        Random random = new Random();
        int randomNumber = random.nextInt(totalFitness);

        // Find the individual that corresponds with the random number
        int cumulativeFitness = 0;
        for (int i = 0; i < fitness.length; i++) {
            if(fitness[i]>0){
                cumulativeFitness += fitness[i];
            }
            if (cumulativeFitness >= randomNumber) {
                return i;
            }
        }
        // Default: Give back the last element
        return fitness.length - 1;
    }

    private static int calculateFitnessIndividual(int[][] individual, List<Auftrag> auftraege, List<Lkw> lkws) {
        int gewinn = 0;
        // Iteriere über Aufträge j, berechne anzahl ausgefahrener Kisten und Dauer
        for (int j = 0; j < individual[0].length; j++) {
            Auftrag auftrag = auftraege.get(j);
            int kistenVorgabe = auftrag.getAnzahlKisten();
            int kistenIndividual = 0;
            int entfernung = auftrag.getEntfernung();
            int gewinnAuftrag = 0;
            double dauer = 0.0;
            for (int i = 0; i < individual.length; i++) {
                kistenIndividual += individual[i][j];
                if (individual[i][j] != 0) {
                    double dauerNeu = (entfernung / lkws.get(i).getKmh()) * 2;
                    dauerNeu += 1;
                    dauer = (dauer > dauerNeu) ? dauer : dauerNeu;
                }
            }
            // Falls Auftrag Erfüllt
            if (kistenIndividual == kistenVorgabe) {
                gewinnAuftrag = auftrag.getEntlohnung();
                // Zeitstrafe oder Bonus?
                if (dauer > auftrag.getZeitlimitStrafe()) {
                    gewinnAuftrag -= auftrag.getBetragStrafe();
                } else if (dauer < auftrag.getZeitlimitBonus()) {
                    gewinnAuftrag += auftrag.getBetragBonus();
                }
            } else {
                gewinnAuftrag = -auftrag.getBetragStrafe();
            }
            // System.out.println("Gewinn Auftrag "+ j + ": "+ gewinnAuftrag);
            gewinn += gewinnAuftrag;
        }
        // System.out.println("Gesamtgewinn des Individuums: "+ gewinn);
        return gewinn;
    }

    private static int[][][] initPopulation(int populationSize, int numLkws, int numAuftraege, List<Lkw> lkws,
            List<Auftrag> auftraege) {
        Random rand = new Random();
        // Create a List to store valid individuals
        List<int[][]> population = new ArrayList<>();

        while (population.size() < populationSize) {
            // Create individual 2D-array
            int[][] individual = new int[numLkws][numAuftraege];

            // Permutation List to randomly fill the individual
            ArrayList<Integer> auftragPermutationList = new ArrayList<>();
            for (int i = 0; i < numAuftraege; i++) {
                auftragPermutationList.add(i);
            }
            Collections.shuffle(auftragPermutationList, rand);

            // Randomly fill, but consider limitation
            while (auftragPermutationList.size() > 0) {
                int currentAuftrag = auftragPermutationList.remove(0);
                int remainingKisten = auftraege.get(currentAuftrag).getAnzahlKisten();
                int kistenGewicht = auftraege.get(currentAuftrag).getGewichtKisten();

                // Create lkw permutation List to randomly fill LKWs
                ArrayList<Integer> lkwPermutationList = new ArrayList<>();
                for (int i = 0; i < numLkws; i++) {
                    lkwPermutationList.add(i);
                }
                Collections.shuffle(lkwPermutationList, rand);

                // Check LKW Capacity limitations
                while (remainingKisten > 0 && lkwPermutationList.size() > 0) {
                    int currentLkw = lkwPermutationList.remove(0);
                    int remainingGewicht = remainingKisten * kistenGewicht;
                    // check if current lkw already drives to another Ziel
                    if (!lkwHasAnotherZiel(individual, lkws, currentLkw, auftraege.get(currentAuftrag).getZiel(),
                            auftraege)) {
                        // Check if current lkw has capacity
                        int lkwCapacityKisten = calcLkwCapacityKisten(individual, lkws, currentLkw);
                        int lkwCapacityGewicht = calcLkwCapacityGewicht(individual, lkws, currentLkw, auftraege);
                        if (lkwCapacityKisten > 0 && lkwCapacityGewicht > 0) {
                            if (lkwCapacityKisten > remainingKisten && lkwCapacityGewicht > remainingGewicht) {
                                individual[currentLkw][currentAuftrag] = remainingKisten;
                                remainingKisten = 0;
                            } else {
                                // find out how many kisten the lkw can take
                                int gewichtKistenLimit = (int) lkwCapacityGewicht / kistenGewicht;
                                gewichtKistenLimit = (lkwCapacityKisten > gewichtKistenLimit) ? gewichtKistenLimit
                                        : lkwCapacityKisten;
                                // Fill Lkw with calculated kistenLimit
                                individual[currentLkw][currentAuftrag] = gewichtKistenLimit;
                                remainingKisten -= gewichtKistenLimit;
                            }
                        }
                    }
                }
            }

            // Only if solution is Valid, add to population
            if (isValidIndividual(individual, lkws, auftraege)) {
                population.add(individual);
                // print
                // for (int i = 0; i < individual.length; i++) {
                // for (int j = 0; j < individual[i].length; j++) {
                // System.out.print(individual[i][j] + " ");
                // }
                // System.out.println(); // Move to the next line after printing each row
                // }
            }
        }
        // Collect to 3D array
        int[][][] populationArray = population.stream().toArray(int[][][]::new);

        return populationArray;
    }

    private static boolean lkwHasAnotherZiel(int[][] individual, List<Lkw> lkws, int currentLkw, char currentTarget,
            List<Auftrag> auftraege) {
        for (int j = 0; j < individual[0].length; j++) {
            if (individual[currentLkw][j] != 0) {
                if (auftraege.get(j).getZiel() != currentTarget) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int calcLkwCapacityGewicht(int[][] individual, List<Lkw> lkws, int lkw, List<Auftrag> auftraege) {
        // Ein LKW darf nicht mehr Kisten fahren als erlaubt
        int kapaGewicht = lkws.get(lkw).getKapaGewicht();
        int sumGewicht = 0;
        for (int j = 0; j < individual[lkw].length; j++) {
            sumGewicht += individual[lkw][j] * auftraege.get(j).getGewichtKisten();
        }
        return kapaGewicht - sumGewicht;
    }

    private static int calcLkwCapacityKisten(int[][] individual, List<Lkw> lkws, int lkw) {
        // Ein LKW darf nicht mehr Kisten fahren als erlaubt
        int kapaKisten = lkws.get(lkw).getKapaKisten();
        int sumKisten = 0;
        for (int j = 0; j < individual[lkw].length; j++) {
            sumKisten += individual[lkw][j];
        }
        return kapaKisten - sumKisten;
    }

    private static boolean isValidIndividual(int[][] individual, List<Lkw> lkws, List<Auftrag> auftraege) {
        // Ein LKW darf nicht mehr Kisten fahren als erlaubt
        // Ein LKW darf nicht mehr Gewicht fahren als erlaubt
        for (int i = 0; i < individual.length; i++) {
            int kapaKisten = lkws.get(i).getKapaKisten();
            int kapaGewicht = lkws.get(i).getKapaGewicht();
            int sumKisten = 0;
            int sumGewicht = 0;
            for (int j = 0; j < individual[i].length; j++) {
                sumKisten += individual[i][j];
                sumGewicht += individual[i][j] * auftraege.get(j).getGewichtKisten();
            }
            if (sumKisten > kapaKisten || sumGewicht > kapaGewicht) {
                // System.out.println("Individual does not fulfill kisten or gewichts limit of
                // LKWS");
                return false;
            }
        }
        /*
         * //Es sollen genauso viele Kisten transportiert werden wie für jeden Auftrag
         * nötig
         * for(int j = 0; j<individual[0].length;j++){
         * int auftragGroeße = auftraege.get(j).getAnzahlKisten();
         * int sumKisten = 0;
         * for(int i = 0; i<individual.length; i++){
         * sumKisten+=individual[i][j];
         * }
         * if(sumKisten != auftragGroeße){
         * System.out.println("Individual does not fulfill auftrags größe");
         * return false;
         * }
         * }
         */
        // Mehrere Aufträge auf einem LKW müssen das gleiche Ziel haben
        for (int i = 0; i < individual.length; i++) {
            char ziel = ' ';
            for (int j = 0; j < individual[i].length; j++) {
                if (individual[i][j] != 0) {
                    char nextZiel = auftraege.get(j).getZiel();
                    if (nextZiel != ziel && ziel != ' ') {
                        // System.out.println("Individual does not fulfill Ziel restrictions");
                        return false;
                    }
                    ziel = nextZiel;
                }
            }
        }
        return true;
    }

    private static List<Lkw> readLkwsFromCSV(String pathToFile) {
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

        return lkws;
    }

    private static List<Auftrag> readAuftraegeFromCSV(String pathToFile) {
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

        return auftraege;
    }

}
