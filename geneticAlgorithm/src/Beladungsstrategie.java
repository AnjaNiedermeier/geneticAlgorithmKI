import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Beladungsstrategie {
    public static void main(String[] args) {
        //Algorithm Hyperparameters
        int populationSize = 2;
        int currentRound = 0;
        int maxRounds = 50;
        double crossoverRate = 0.5;
        double mutationRate = 0.1;

        List<Auftrag> auftraege;
        List<Lkw> lkws;
        int[][][] population;

        //Einlesen von LKWs und Aufträgen
        System.out.println("Einlesen der Aufträge und LKWs...");
        auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        //Initialisieren der Anfangspopulation
        System.out.println("Initialisiere Anfangspopulation...");
        population = initPopulation(populationSize, lkws.size(), auftraege.size(), lkws, auftraege);
        System.out.println("Finished");
    }

    private static int[][][] initPopulation(int populationSize, int numLkws, int numAuftraege, List<Lkw> lkws, List<Auftrag> auftraege) {
        //int[][][] population = new int[population_size][num_lkws][num_auftraege];
        Random rand = new Random();

        // Create a List to store valid individuals
        List<int[][]> population = new ArrayList<>();
        while(population.size()<populationSize){
            ArrayList<Integer> auftragPermutationList = new ArrayList<>();
            for(int i=0;i<numAuftraege;i++){
                auftragPermutationList.add(i);
            }
        Collections.shuffle(auftragPermutationList, rand);
            //Create individual 2D-array
            int[][] individual = new int[numLkws][numAuftraege];
            //Randomly fill, but consider limitation
            while(auftragPermutationList.size()>0){
                int currentAuftrag = auftragPermutationList.remove(0);
                System.out.println("Current AUftrag: "+ currentAuftrag);
                int remainingKisten = auftraege.get(currentAuftrag).getAnzahlKisten();
                int kistenGewicht = auftraege.get(currentAuftrag).getGewichtKisten();
                //Create lkw permutation List
                ArrayList<Integer> lkwPermutationList = new ArrayList<>();
                for(int i=0;i<numLkws;i++){
                    lkwPermutationList.add(i);
                }
                Collections.shuffle(lkwPermutationList, rand);

                while(remainingKisten>0 && lkwPermutationList.size()>0){
                    int currentLkw = lkwPermutationList.remove(0);
                    int remainingGewicht = remainingKisten*kistenGewicht;
                    //Check if current lkw has capacity
                    int lkwCapacityKisten = calcLkwCapacityKisten(individual, lkws, currentLkw);
                    int lkwCapacityGewicht = calcLkwCapacityGewicht(individual, lkws, currentLkw, auftraege);
                    if(lkwCapacityKisten > 0 && lkwCapacityGewicht > 0){
                        if(lkwCapacityKisten>remainingKisten && lkwCapacityGewicht>remainingGewicht){
                            individual[currentLkw][currentAuftrag]=remainingKisten;
                            remainingKisten = 0;
                        }
                        else{
                            //find out how many kisten the lkw can take
                            int gewichtKistenLimit = (int) lkwCapacityGewicht/kistenGewicht;
                            gewichtKistenLimit = (lkwCapacityKisten>gewichtKistenLimit) ? gewichtKistenLimit : lkwCapacityKisten;
                            //Fill Lkw with calculated kistenLimit
                            individual[currentLkw][currentAuftrag]=gewichtKistenLimit;
                            remainingKisten -= gewichtKistenLimit;
                        }
                    }
                }
            }
            
            //Only if solution is Valid, add to population
            if(isValidIndividual(individual, lkws, auftraege)){
                population.add(individual);
                //print
                for (int i = 0; i < individual.length; i++) {
                    for (int j = 0; j < individual[i].length; j++) {
                        System.out.print(individual[i][j] + " ");
                    }
                    System.out.println(); // Move to the next line after printing each row
                }
            }
        }
        //Collect to 3D array
        int[][][] populationArray = population.stream().toArray(int[][][]::new);
        
        return populationArray;
    }

    private static int calcLkwCapacityGewicht(int[][] individual, List<Lkw> lkws, int lkw, List<Auftrag> auftraege) {
        //Ein LKW darf nicht mehr Kisten fahren als erlaubt
        int kapaGewicht = lkws.get(lkw).getKapaGewicht();
        int sumGewicht = 0;
        for(int j = 0; j<individual[lkw].length; j++){
            sumGewicht+=individual[lkw][j]*auftraege.get(j).getGewichtKisten();
        }
        return kapaGewicht-sumGewicht;
    }

    private static int calcLkwCapacityKisten(int[][] individual, List<Lkw> lkws, int lkw) {
        //Ein LKW darf nicht mehr Kisten fahren als erlaubt
        int kapaKisten = lkws.get(lkw).getKapaKisten();
        int sumKisten = 0;
        for(int j = 0; j<individual[lkw].length; j++){
            sumKisten+=individual[lkw][j];
        }
        return kapaKisten-sumKisten;
    }

    private static boolean isValidIndividual(int[][] individual, List<Lkw> lkws, List<Auftrag> auftraege) {
        System.out.println("Check if individual is valid...");
        //Ein LKW darf nicht mehr Kisten fahren als erlaubt
        //Ein LKW darf nicht mehr Gewicht fahren als erlaubt
        for(int i = 0; i<individual.length; i++){
            int kapaKisten = lkws.get(i).getKapaKisten();
            int kapaGewicht = lkws.get(i).getKapaGewicht();
            int sumKisten = 0;
            int sumGewicht = 0;
            for(int j = 0; j<individual[i].length; j++){
                sumKisten+=individual[i][j];
                sumGewicht+=individual[i][j]*auftraege.get(j).getGewichtKisten();
            }
            if(sumKisten > kapaKisten || sumGewicht > kapaGewicht){
                System.out.println("Individual does not fulfill kisten or gewichts limit of LKWS");
                return false;
            }
        }
        //Es sollen genauso viele Kisten transportiert werden wie für jeden Auftrag nötig
        for(int j = 0; j<individual[0].length;j++){
            int auftragGroeße = auftraege.get(j).getAnzahlKisten();
            int sumKisten = 0;
            for(int i = 0; i<individual.length; i++){
                sumKisten+=individual[i][j];
            }
            if(sumKisten != auftragGroeße){
                System.out.println("Individual does not fulfill auftrags größe");
                return false;
            }
        }
        //Mehrere Aufträge auf einem LKW müssen das gleiche Ziel haben
        for(int i = 0; i<individual.length; i++){
            char ziel = ' ';
            for(int j = 0; j<individual[i].length; j++){
                if(individual[i][j]!=0){
                    char nextZiel = auftraege.get(j).getZiel();
                    if(nextZiel != ziel && ziel !=' '){
                        System.out.println("Individual does not fulfill Ziel restrictions");
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
