import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Beladungsstrategie {
    public static void main(String[] args) {
        // Algorithm Hyperparameters
        int populationSize = 10;
        int currentRound = 0;
        int maxRounds = 1;
        double crossoverRate = 0.5;
        double mutationRate = 0.1;

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

        //Wiederhole für maxRounds Runden
        for(int round = 1; round<=maxRounds; round++){
            System.out.println("Runde "+ round + ": ");
            //Berechne fitness der Lösungen
            int[] fitness = new int[populationSize];
            for(int i = 0; i<fitness.length;i++){
                fitness[i] = calculateFitness(population[i], auftraege, lkws);
                System.out.println(fitness[i]);
            }
            
            //Select Parents for Reproduction
            int numParents = (int) Math.round(crossoverRate * populationSize);
            List<Integer> parentList = new ArrayList<>();
            while(parentList.size()<numParents){
                int currentParent = selectParent(fitness);
                if(!parentList.contains(currentParent)){
                    parentList.add(currentParent);
                    System.out.println(currentParent);
                }
            }
            int[] parents = parentList.stream().mapToInt(i -> i).toArray();

            
        }
    }

    private static int selectParent(int[] fitness) {
        //Calculate total Fitness of population (Roulette Wheel)
        int totalFitness = 0;
        for (int i = 0; i<fitness.length; i++) {
            totalFitness += fitness[i];
        }

        // Generate a random number between 0 and totalFitness
        Random random = new Random();
        int randomNumber = random.nextInt(totalFitness);

        // Find the individual that corresponds with the random number
        int cumulativeFitness = 0;
        for (int i = 0; i < fitness.length; i++) {
            cumulativeFitness += fitness[i];
            if (cumulativeFitness >= randomNumber) {
                return i;
            }
        }
        //Default: Give back the last element
        return fitness.length - 1;
    }

    private static int calculateFitness(int[][] individual, List<Auftrag> auftraege, List<Lkw> lkws) {
        int gewinn = 0;
        //Iteriere über Aufträge j, berechne anzahl ausgefahrener Kisten und Dauer
        for(int j=0; j<individual[0].length; j++){
            Auftrag auftrag = auftraege.get(j);
            int kistenVorgabe = auftrag.getAnzahlKisten();
            int kistenIndividual = 0;
            int entfernung = auftrag.getEntfernung();
            int gewinnAuftrag = 0;
            double dauer = 0.0;
            for(int i=0; i<individual.length; i++){
                kistenIndividual+=individual[i][j];
                if(individual[i][j]!=0){
                    double dauerNeu = (entfernung/lkws.get(i).getKmh())*2;
                    dauerNeu += 1;
                    dauer = (dauer>dauerNeu)? dauer : dauerNeu;
                }
            }
            //Falls Auftrag Erfüllt
            if(kistenIndividual == kistenVorgabe){
                gewinnAuftrag = auftrag.getEntlohnung();
                //Zeitstrafe oder Bonus?
                if(dauer>auftrag.getZeitlimitStrafe()){
                    gewinnAuftrag -= auftrag.getBetragStrafe();
                }
                else if(dauer<auftrag.getZeitlimitBonus()){
                    gewinnAuftrag += auftrag.getBetragBonus();
                }
            }
            else{
                gewinnAuftrag = - auftrag.getBetragStrafe();
            }
            //System.out.println("Gewinn Auftrag "+ j + ": "+ gewinnAuftrag);
            gewinn += gewinnAuftrag;
        }
        //System.out.println("Gesamtgewinn des Individuums: "+ gewinn);
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

            //Permutation List to randomly fill the individual
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

                //Check LKW Capacity limitations
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
                //print
                // for (int i = 0; i < individual.length; i++) {
                //     for (int j = 0; j < individual[i].length; j++) {
                //         System.out.print(individual[i][j] + " ");
                //     }
                //     System.out.println(); // Move to the next line after printing each row
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
