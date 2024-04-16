import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Beladungsstrategie {
    public static void main(String[] args) {
        // Algorithm Hyperparameters
        int populationSize = 2000;
        int maxRounds = 1000;
        final double initialMutationRate = 0.2;
        final double finalMutationRate = 0.6;
        double mutationRate = initialMutationRate;
        double crossoverRate = 0.2;

        List<Auftrag> auftraege;
        List<Lkw> lkws;
        int[][][] population;
        int bestSolutionScore = 0;

        // Einlesen von LKWs und Aufträgen
        System.out.println("Einlesen der Aufträge und LKWs...");
        auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        // Initialisieren der Anfangspopulation
        System.out.println("Initialisiere Anfangspopulation...");
        population = initPopulation(populationSize, lkws.size(), auftraege.size(), lkws, auftraege);
        System.out.println("Finished Initialization");

        int[][] bestSolution = new int[lkws.size()][auftraege.size()];
        // Wiederhole für maxRounds Runden
        for (int round = 1; round <= maxRounds; round++) {
            //gradually decrease mutation rate
            if(mutationRate<finalMutationRate){
                mutationRate+= (finalMutationRate-initialMutationRate)/maxRounds;
                System.out.println("Mutation Rate: "+ mutationRate);
            }
            System.out.println("Runde " + round + ": ");
            // Berechne fitness der Lösungen
            int[] fitness = calculateFitnessPopulation(populationSize, auftraege, lkws, population);
            // System.out.println("Fitness Scores:");
            // for(int fit : fitness){
            //     System.out.print(fit + ", ");
            // }

            // Merke beste valide Lösung
            int[][] bestSolutionRound = population[getBestSolutionIndex(fitness)];
            int bestSolutionRoundScore = calculateFitnessIndividual(bestSolutionRound, auftraege, lkws);
            if (bestSolutionRoundScore > bestSolutionScore && isValidIndividual(bestSolutionRound, lkws, auftraege)) {
                bestSolution = bestSolutionRound;
                bestSolutionScore = bestSolutionRoundScore;
            }

            // Select Parents for Reproduction
            //int numParents = getNumNegativeFitnessValues(fitness);
            int numParents = (int) Math.ceil(populationSize * crossoverRate);
            System.out.println("Select "+ numParents + " parents for reproduction");
            int[][][] parents = selectParentsRoulette(populationSize, numParents, auftraege, lkws, population, fitness);
            //int[][][] parents = selectParentsBestN(populationSize, numParents, auftraege, lkws, population, fitness);

            // Generate n offspring individuals from the n selected parents
            int[][][] offspring = generateOffspring(parents, lkws, auftraege);

            //Mutate random individuals of the offspring
            offspring = mutation(offspring, mutationRate, auftraege, lkws);

            // Replace population with offspring individuals
            replacePopulation(population, offspring, fitness);

            // Mutate random individuals of the population
            //population = mutation(population, mutationRate, auftraege, lkws);

        }

        // Print Best Solution
        System.out.println("-------------------------------");
        System.out.println("Best Strategy that was found:");
        printStrategy(bestSolution, bestSolutionScore);

        //investigate population
        // for (int[][] pop : population){
        //     printStrategy(pop, bestSolutionScore);
        // }
    }

    private static void printStrategy(int[][] bestSolution, int bestSolutionScore) {
        System.out.println("Profit: " + bestSolutionScore);
        for (int i = 0; i < bestSolution.length; i++) {
            System.out.print("Lkw " + (i + 1) + ": ");
            for (int item : bestSolution[i]) {
                System.out.printf("%3d", item); // Adjust the width as needed
            }
            System.out.println(); // Move to the next line after printing each row
        }
    }

    private static int getBestSolutionIndex(int[] fitness) {
        int bestScore = 0;
        int bestScoreIndex = 0;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > bestScore) {
                bestScore = fitness[i];
                bestScoreIndex = i;
            }
        }
        return bestScoreIndex;
    }

    private static int getNumNegativeFitnessValues(int[] fitness) {
        int num = 0;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] < 0) {
                num++;
            }
        }
        return num;
    }

    private static int[][][] mutation(int[][][] population, double mutationRate, List<Auftrag> auftraege,
            List<Lkw> lkws) {
        int mutationAmount = (int) Math.ceil(mutationRate * population.length);
        // Choose random contracts to mutate
        Random random = new Random();
        for (int i = 0; i < mutationAmount; i++) {
            int indexMutation = random.nextInt(population.length);
            int[][] mutationCandidate = population[indexMutation];
            mutationCandidate = mutateCandidateSwapRow(mutationCandidate);
            mutationCandidate = mutateCandidateAdd(mutationCandidate);
            if (isValidIndividual(mutationCandidate, lkws, auftraege)) {
                population[indexMutation] = mutationCandidate;
            }
            
        }
        return population;
    }

    private static int[][] mutateCandidateSwapRow(int[][] mutationCandidate) {
        int[][] mutatedCandidate = Arrays.stream(mutationCandidate).map(int[]::clone).toArray(int[][]::new);
        Random random = new Random();
        int swapCol = random.nextInt(mutatedCandidate[0].length);
        int swapRow1 = random.nextInt(mutatedCandidate.length);
        int swapRow2 = random.nextInt(mutatedCandidate.length);

        // Perform the swap within the same auftrag
        int temp = mutatedCandidate[swapRow1][swapCol];
        mutatedCandidate[swapRow1][swapCol] = mutatedCandidate[swapRow2][swapCol];
        mutatedCandidate[swapRow2][swapCol] = temp;
        return mutatedCandidate;
    }

    private static int[][] mutateCandidateAdd(int[][] mutationCandidate) {
        int[][] mutatedCandidate = Arrays.stream(mutationCandidate).map(int[]::clone).toArray(int[][]::new);
        Random random = new Random();
        int col = random.nextInt(mutatedCandidate[0].length);
        int row = random.nextInt(mutatedCandidate.length);

        // Add 1 to random gene
        mutatedCandidate[row][col] += 1;
        return mutatedCandidate;
    }

    // Get the worst individuals of the population and replace them
    private static void replacePopulation(int[][][] population, int[][][] offspring, int[] fitness) {
        int[] worstIndices = getNLowestIndices(fitness, offspring.length);
        int index = 0;
        for (int i = 0; i < worstIndices.length; i++) {
            population[worstIndices[i]] = offspring[index++];
        }
    }


    private static int[][][] selectParentsBestN(int populationSize, int numParents, List<Auftrag> auftraege,
            List<Lkw> lkws, int[][][] population, int[] fitness) {

        //Fill parent indices with best numParents indices
        int[] parentIndices = getNHighestIndices(fitness, numParents);

        // Get parents from parentIndices
        int[][][] parents = new int[numParents][lkws.size()][auftraege.size()];
        int index = 0;
        for (int i : parentIndices) {
            parents[index++] = population[i];
        }
        return parents;
    }

    private static int[] getNHighestIndices(int[] array, int n) {
        // Create a Map to store indices and values
        Map<Integer, Integer> indices = new HashMap<>();
        
        // Fill Map with indices and values from array
        for (int i = 0; i < array.length; i++) {
            indices.put(i, array[i]);
        }
        
        // Sort the Map by values in descending order
        List<Map.Entry<Integer, Integer>> sortedFitness = new ArrayList<>(indices.entrySet());
        sortedFitness.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Extract the first N indices
        int[] highestIndices = new int[n];
        for (int i = 0; i < n && i < sortedFitness.size(); i++) {
            highestIndices[i] = (int) sortedFitness.get(i).getKey();
        }
        // System.out.println(n + " highest Indices");
        // for(int i :  highestIndices){
        //     System.out.print(i + ", ");
        // }
        // System.out.println();
        return highestIndices;
    }

    private static int[] getNLowestIndices(int[] array, int n) {
        // Create a Map to store indices and values
        Map<Integer, Integer> indices = new HashMap<>();
        
        // Fill Map with indices and values from array
        for (int i = 0; i < array.length; i++) {
            indices.put(i, array[i]);
        }
        
        // Sort the Map by values in descending order
        List<Map.Entry<Integer, Integer>> sortedFitness = new ArrayList<>(indices.entrySet());
        sortedFitness.sort((a, b) -> a.getValue().compareTo(b.getValue()));
        
        // Extract the first N indices
        int[] lowestIndices = new int[n];
        for (int i = 0; i < n && i < sortedFitness.size(); i++) {
            lowestIndices[i] = (int) sortedFitness.get(i).getKey();
        }
        
        // System.out.println(n + " lowest Indices");
        // for(int i :  lowestIndices){
        //     System.out.print(i + ", ");
        // }
        //System.out.println();
        return lowestIndices;
    }

    private static int[][][] selectParentsRoulette(int populationSize, int numParents, List<Auftrag> auftraege,
            List<Lkw> lkws, int[][][] population, int[] fitness) {

        List<Integer> parentList = new ArrayList<>();
        while (parentList.size() < numParents) {
            int currentParent = selectParentRoulette(fitness);
            if (!parentList.contains(currentParent)) {
                parentList.add(currentParent);
            }
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

    private static int selectParentRoulette(int[] fitness) {
        // Calculate total Fitness of population (Roulette Wheel)
        int totalFitness = 0;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > 0) {
                totalFitness += fitness[i];
            }
        }

        // Generate a random number between 0 and totalFitness
        Random random = new Random();
        int randomNumber = random.nextInt(totalFitness);

        // Find the individual that corresponds with the random number
        int cumulativeFitness = 0;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > 0) {
                cumulativeFitness += fitness[i];
            }
            if (cumulativeFitness >= randomNumber) {
                return i;
            }
        }
        // Default: Give back the last element
        return fitness.length - 1;
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
            if (fit < minFitness) {
                minFitness = fit;
            }
            if (fit > maxFitness) {
                maxFitness = fit;
            }
        }
        System.out.println("Avg. Fitness: " + fitnessSum / fitness.length);
        System.out.println("Min. Fitness: " + minFitness);
        System.out.println("Max. Fitness: " + maxFitness);
        System.out.println();
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
            //Crossover Options: 
            int[][] child;
            //child = verticalBandCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            child = horizontalBandCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            //child = blockCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            //child = uniformCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            if (isValidIndividual(child, lkws, auftraege)) {
                childrenList.add(child);
            }
        }
        int[][][] childrenArray = childrenList.stream().toArray(int[][][]::new);
        System.out.println(childrenArray.length + " valid children generated");
        return childrenArray;
    }

    private static int[][] uniformCrossover(int[][] parentA, int[][] parentB) {
        int[][] child = Arrays.stream(parentB).map(int[]::clone).toArray(int[][]::new);
        Random random = new Random();
        // Iterate through array, with 50% chance, replace parentB's gene with parentA's
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child[0].length; j++) {
                if(random.nextDouble()<0.5){
                    child[i][j] = parentA[i][j];
                }
            }
        }
        return child;
    }

    // Two random numbers are generated , and information inside the vertical region
    // of the grid determined by the numbers is exchanged. In this case, we take
    // parentB and copy some random auftraege from parentA into it
    private static int[][] verticalBandCrossover(int[][] parentA, int[][] parentB) {
        int[][] child = Arrays.stream(parentB).map(int[]::clone).toArray(int[][]::new);

        // Select two random points for crossover (in the range of Aufträge)
        Random random = new Random();
        int startPoint = random.nextInt(parentA[0].length);
        int endPoint = random.nextInt(parentA[0].length - startPoint) + startPoint;

        // Copy the segment between the two points from parentA to child
        for (int j = startPoint; j <= endPoint; j++) {
            for (int i = 0; i < parentA.length; i++) {
                child[i][j] = parentA[i][j];
            }
        }
        return child;
    }

    private static int[][] horizontalBandCrossover(int[][] parentA, int[][] parentB) {
        int[][] child = Arrays.stream(parentB).map(int[]::clone).toArray(int[][]::new);

        // Select two random points for crossover (in the range of LKW)
        Random random = new Random();
        int startPoint = random.nextInt(parentA.length);
        int endPoint = random.nextInt(parentA.length - startPoint) + startPoint;

        // Copy the segment between the two points from parentA to child
        for (int i = startPoint; i <= endPoint; i++) {
            for (int j = 0; j < parentA[0].length; j++) {
                child[i][j] = parentA[i][j];
            }
        }
        return child;
    }

    private static int[][] blockCrossover(int[][] parentA, int[][] parentB) {
        int[][] child = Arrays.stream(parentB).map(int[]::clone).toArray(int[][]::new);

        Random random = new Random();
        // Select two random points for crossover (in the range of LKW)
        int startPointLkw = random.nextInt(parentA.length);
        int endPointLkw = random.nextInt(parentA.length - startPointLkw) + startPointLkw;

        // Select two random points for crossover (in the range of Aufträge)
        int startPointAuftraege = random.nextInt(parentA[0].length);
        int endPointAuftraege = random.nextInt(parentA[0].length - startPointAuftraege) + startPointAuftraege;

        // Copy the segment between the 4 pointss from parentA to child
        for (int i = startPointLkw; i <= endPointLkw; i++) {
            for (int j = startPointAuftraege; j < endPointAuftraege; j++) {
                child[i][j] = parentA[i][j];
            }
        }
        return child;
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
