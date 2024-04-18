import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Population {
    private Individual[] population;
    private int[] fitness;

    public Population(int size, Lkw[] lkws, Auftrag[] auftraege) {
        population = initPopulation(size, lkws, auftraege);
        fitness = new int[size];
        calcFitness(lkws, auftraege);
    }
 
    public void calcFitness(Lkw[] lkws, Auftrag[] auftraege) {
        for(int i = 0; i<fitness.length; i++){
            fitness[i] = population[i].calculateFitness(auftraege, lkws);
        };
    }

    private Individual[] initPopulation(int populationSize, Lkw[] lkws, Auftrag[] auftraege) {
        // Create a List to store valid individuals
        List<Individual> population = new ArrayList<>();

        while (population.size() < populationSize) {
            Individual individual = new Individual(lkws, auftraege);
            // Only if solution is Valid, add to population
            if (individual.isValid(lkws, auftraege)) {
                population.add(individual);
            }
        }
        // Collect to 3D array
        Individual[] populationArray = population.stream().toArray(Individual[]::new);
        return populationArray;
    }

    public int getBestSolutionIndex() {
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
    //SELECTION STRATEGIES
    public Individual[] selectParentsRoulette(int numParents) {
        List<Integer> parentList = new ArrayList<>();
        while (parentList.size() < numParents) {
            int currentParent = selectParentRoulette();
            if (!parentList.contains(currentParent)) {
                parentList.add(currentParent);
            }
        }

        int[] parentIndices = parentList.stream().mapToInt(i -> i).toArray();
        // Get parents from parentIndices
        Individual[] parents = getParentsFromIndices(numParents, parentIndices);
        return parents;
    }

    private int selectParentRoulette() {
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

    public Individual[] selectParentsBestN(int numParents) {
        //Fill parent indices with best numParents indices
        int[] parentIndices = getNHighestIndices(fitness, numParents);

        // Get parents from parentIndices
        Individual[] parents = getParentsFromIndices(numParents, parentIndices);
        return parents;
    }

    private Individual[] getParentsFromIndices(int numParents, int[] parentIndices) {
        Individual[] parents = new Individual[numParents];
        int index = 0;
        for (int i : parentIndices) {
            parents[index++] = population[i];
        }
        return parents;
    }

    //RECOMBINATION
    public Individual[] generateOffspring(Individual[] parents, Lkw[] lkws, Auftrag[] auftraege) {
        int num_parents = parents.length;
        // Create a List to store valid offspring individuals
        List<Individual> childrenList = new ArrayList<>();

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
            Individual child;
            //child = verticalBandCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            child = horizontalBandCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            //child = blockCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            //child = uniformCrossover(parents[combinations[i][0]], parents[combinations[i][1]]);
            if (child.isValid(lkws, auftraege)) {
                childrenList.add(child);
            }
        }
        Individual[] childrenArray = childrenList.stream().toArray(Individual[]::new);
        return childrenArray;
    }

    //CROSSOVER OPTIONS
    private Individual uniformCrossover(Individual parentA, Individual parentB) {
        int[][] child = Arrays.stream(parentB.getIndividual()).map(int[]::clone).toArray(int[][]::new);
        Random random = new Random();
        // Iterate through array, with 50% chance, replace parentB's gene with parentA's
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child[0].length; j++) {
                if(random.nextDouble()<0.5){
                    child[i][j] = parentA.getIndividual()[i][j];
                }
            }
        }
        return new Individual(child);
    }

    private Individual verticalBandCrossover(Individual parentA, Individual parentB) {
        int[][] child = Arrays.stream(parentB.getIndividual()).map(int[]::clone).toArray(int[][]::new);

        // Select two random points for crossover (in the range of Aufträge)
        Random random = new Random();
        int startPoint = random.nextInt(child[0].length);
        int endPoint = random.nextInt(child[0].length - startPoint) + startPoint;

        // Copy the segment between the two points from parentA to child
        for (int j = startPoint; j <= endPoint; j++) {
            for (int i = 0; i < parentA.getIndividual().length; i++) {
                child[i][j] = parentA.getIndividual()[i][j];
            }
        }
        return new Individual(child);
    }

    private Individual horizontalBandCrossover(Individual parentA, Individual parentB) {
        int[][] child = Arrays.stream(parentB.getIndividual()).map(int[]::clone).toArray(int[][]::new);

        // Select two random points for crossover (in the range of LKW)
        Random random = new Random();
        int startPoint = random.nextInt(child.length);
        int endPoint = random.nextInt(child.length - startPoint) + startPoint;

        // Copy the segment between the two points from parentA to child
        for (int i = startPoint; i <= endPoint; i++) {
            for (int j = 0; j < parentA.getIndividual()[0].length; j++) {
                child[i][j] = parentA.getIndividual()[i][j];
            }
        }
        return new Individual(child);
    }

    private Individual blockCrossover(Individual parentA, Individual parentB) {
        int[][] child = Arrays.stream(parentB.getIndividual()).map(int[]::clone).toArray(int[][]::new);

        Random random = new Random();
        // Select two random points for crossover (in the range of LKW)
        int startPointLkw = random.nextInt(child.length);
        int endPointLkw = random.nextInt(child.length - startPointLkw) + startPointLkw;

        // Select two random points for crossover (in the range of Aufträge)
        int startPointAuftraege = random.nextInt(child[0].length);
        int endPointAuftraege = random.nextInt(child[0].length - startPointAuftraege) + startPointAuftraege;

        // Copy the segment between the 4 pointss from parentA to child
        for (int i = startPointLkw; i <= endPointLkw; i++) {
            for (int j = startPointAuftraege; j < endPointAuftraege; j++) {
                child[i][j] = parentA.getIndividual()[i][j];
            }
        }
        return new Individual(child);
    }

    //MUTATION

    public Individual[] mutation(Individual[] populationSubset, double mutationRate, Auftrag[] auftraege, Lkw[] lkws) {
        int mutationAmount = (int) Math.ceil(mutationRate * population.length);
        // Choose random contracts to mutate
        Random random = new Random();
        for (int i = 0; i < mutationAmount; i++) {
            int indexMutation = random.nextInt(populationSubset.length);
            Individual mutationCandidate = new Individual(populationSubset[indexMutation].getIndividual());
            mutationCandidate.mutateSwapRow();
            mutationCandidate.mutateAdd();
            if (mutationCandidate.isValid(lkws, auftraege)) {
                populationSubset[indexMutation] = mutationCandidate;
            }
        }
        return populationSubset;
    }

    //REPLACEMENT
    public void replacePopulation(Individual[] offspring) {
        int[] worstIndices = getNLowestIndices(fitness, offspring.length);
        int index = 0;
        for (int i = 0; i < worstIndices.length; i++) {
            population[worstIndices[i]] = offspring[index++];
        }
    }

    //GETTERS
    public int[] getFitness() {
        return fitness;
    }

    public Individual[] getPopulation() {
        return population;
    }

    //PRINT METHODS
    public void printFitnessStats(){
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
    }

    //HELPER METHODS
    private int[] getNHighestIndices(int[] array, int n) {
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

    private int[] getNLowestIndices(int[] array, int n) {
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
}
