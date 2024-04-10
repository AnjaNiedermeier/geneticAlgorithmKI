import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Beladungsstrategie {
    public static void main(String[] args) {
        //Algorithm Hyperparameters
        int population_size = 2;
        int current_round = 0;
        int max_rounds = 50;
        double crossover_rate = 0.5;
        double mutation_rate = 0.1;

        List<Auftrag> auftraege;
        List<Lkw> lkws;
        int[][][] population;

        //Einlesen von LKWs und Aufträgen
        System.out.println("Einlesen der Aufträge und LKWs...");
        auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        //Initialisieren der Anfangspopulation
        System.out.println("Initialisiere Anfangspopulation...");
        population = initPopulation(population_size, lkws.size(), auftraege.size(), lkws, auftraege);
    }

    private static int[][][] initPopulation(int population_size, int num_lkws, int num_auftraege, List<Lkw> lkws, List<Auftrag> auftraege) {
        //int[][][] population = new int[population_size][num_lkws][num_auftraege];
        
        // Create a List to store valid individuals
        List<int[][]> population = new ArrayList<>();
        while(population.size()<population_size){
            //Create individual 2D-array
            int[][] individual = new int[num_lkws][num_auftraege];
            //Fill with random values between 0 and 50
            Random random = new Random();
            for (int i = 0; i < num_lkws; i++) {
                for (int j = 0; j < num_auftraege; j++) {
                    individual[i][j] = random.nextInt(10);
                }
            }
            //Only if solution is Valid, add to population
            if(isValidIndividual(individual, lkws, auftraege)){
                population.add(individual);
            }
        }
        //Collect to 3D array
        int[][][] population_array = population.stream().toArray(int[][][]::new);
        
        return population_array;
    }

    private static boolean isValidIndividual(int[][] individual, List<Lkw> lkws, List<Auftrag> auftraege) {
        //Ein LKW darf nicht mehr Kisten fahren als erlaubt
        //Ein LKW darf nicht mehr Gewicht fahren als erlaubt
        for(int i = 0; i<individual.length; i++){
            int kapa_kisten = lkws.get(i).getKapa_kisten();
            int kapa_gewicht = lkws.get(i).getKapa_gewicht();
            int sum_kisten = 0;
            int sum_gewicht = 0;
            for(int j = 0; j<individual[i].length; j++){
                sum_kisten+=individual[i][j];
                sum_gewicht+=individual[i][j]*auftraege.get(j).getGewicht_kisten();
            }
            if(sum_kisten > kapa_kisten || sum_gewicht > kapa_gewicht){
                return false;
            }
        }
        //Es sollen genauso viele Kisten transportiert werden wie für jeden Auftrag nötig
        for(int j = 0; j<individual[0].length;j++){
            int auftrag_groeße = auftraege.get(j).getAnzahl_kisten();
            int sum_kisten = 0;
            for(int i = 0; i<individual.length; i++){
                sum_kisten+=individual[i][j];
            }
            if(sum_kisten != auftrag_groeße){
                return false;
            }
        }
        //Mehrere Aufträge auf einem LKW müssen das gleiche Ziel haben
        for(int i = 0; i<individual.length; i++){
            char ziel = ' ';
            for(int j = 0; j<individual[i].length; j++){
                if(individual[i][j]!=0){
                    char next_ziel = auftraege.get(j).getZiel();
                    if(next_ziel != ziel && ziel !=' '){
                        return false;
                    }
                    ziel = next_ziel;
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
