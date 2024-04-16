import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Individual {
    private int[][] individual;

    public Individual(int[][] individual) {
        this.individual = individual;
    }

    public Individual(Lkw[] lkws, Auftrag[] auftraege) {
        this.individual = createRandomIndividual(lkws, auftraege);
    }

    private int[][] createRandomIndividual(Lkw[] lkws, Auftrag[] auftraege) {
        Random rand = new Random();
        // Create individual 2D-array
        int[][] individualArray = new int[lkws.length][auftraege.length];

        // Permutation List to randomly fill the individual
        ArrayList<Integer> auftragPermutationList = new ArrayList<>();
        for (int i = 0; i < auftraege.length; i++) {
            auftragPermutationList.add(i);
        }
        Collections.shuffle(auftragPermutationList, rand);

        // Randomly fill, but consider limitation
        while (auftragPermutationList.size() > 0) {
            int currentAuftrag = auftragPermutationList.remove(0);
            int remainingKisten = auftraege[currentAuftrag].getAnzahlKisten();
            int kistenGewicht = auftraege[currentAuftrag].getGewichtKisten();

            // Create lkw permutation List to randomly fill LKWs
            ArrayList<Integer> lkwPermutationList = new ArrayList<>();
            for (int i = 0; i < lkws.length; i++) {
                lkwPermutationList.add(i);
            }
            Collections.shuffle(lkwPermutationList, rand);

            // Check LKW Capacity limitations
            while (remainingKisten > 0 && lkwPermutationList.size() > 0) {
                int currentLkw = lkwPermutationList.remove(0);
                int remainingGewicht = remainingKisten * kistenGewicht;
                // check if current lkw already drives to another Ziel
                if (!lkwHasAnotherZiel(individualArray, lkws, currentLkw, auftraege[currentAuftrag].getZiel(),
                        auftraege)) {
                    // Check if current lkw has capacity
                    int lkwCapacityKisten = calcLkwCapacityKisten(individualArray, lkws, currentLkw);
                    int lkwCapacityGewicht = calcLkwCapacityGewicht(individualArray, lkws, currentLkw, auftraege);
                    if (lkwCapacityKisten > 0 && lkwCapacityGewicht > 0) {
                        if (lkwCapacityKisten > remainingKisten && lkwCapacityGewicht > remainingGewicht) {
                            individualArray[currentLkw][currentAuftrag] = remainingKisten;
                            remainingKisten = 0;
                        } else {
                            // find out how many kisten the lkw can take
                            int gewichtKistenLimit = (int) lkwCapacityGewicht / kistenGewicht;
                            gewichtKistenLimit = (lkwCapacityKisten > gewichtKistenLimit) ? gewichtKistenLimit
                                    : lkwCapacityKisten;
                            // Fill Lkw with calculated kistenLimit
                            individualArray[currentLkw][currentAuftrag] = gewichtKistenLimit;
                            remainingKisten -= gewichtKistenLimit;
                        }
                    }
                }
            }
        }

        return individualArray;
    }

    public int calculateFitness(Auftrag[] auftraege, Lkw[] lkws) {
        int gewinn = 0;
        // Iteriere über Aufträge j, berechne anzahl ausgefahrener Kisten und Dauer
        for (int j = 0; j < individual[0].length; j++) {
            Auftrag auftrag = auftraege[j];
            int kistenVorgabe = auftrag.getAnzahlKisten();
            int kistenIndividual = 0;
            int entfernung = auftrag.getEntfernung();
            int gewinnAuftrag = 0;
            double dauer = 0.0;
            for (int i = 0; i < individual.length; i++) {
                kistenIndividual += individual[i][j];
                if (individual[i][j] != 0) {
                    double dauerNeu = (entfernung / lkws[i].getKmh()) * 2;
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

    public boolean isValid(Lkw[] lkws, Auftrag[] auftraege) {
        // Ein LKW darf nicht mehr Kisten fahren als erlaubt
        // Ein LKW darf nicht mehr Gewicht fahren als erlaubt
        for (int i = 0; i < individual.length; i++) {
            int kapaKisten = lkws[i].getKapaKisten();
            int kapaGewicht = lkws[i].getKapaGewicht();
            int sumKisten = 0;
            int sumGewicht = 0;
            for (int j = 0; j < individual[i].length; j++) {
                sumKisten += individual[i][j];
                sumGewicht += individual[i][j] * auftraege[j].getGewichtKisten();
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
                    char nextZiel = auftraege[j].getZiel();
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

//MUTATION METHODS
public void mutateSwapRow() {
    int[][] mutatedCandidate = Arrays.stream(individual).map(int[]::clone).toArray(int[][]::new);
    Random random = new Random();
    int swapCol = random.nextInt(mutatedCandidate[0].length);
    int swapRow1 = random.nextInt(mutatedCandidate.length);
    int swapRow2 = random.nextInt(mutatedCandidate.length);

    // Perform the swap within the same auftrag
    int temp = mutatedCandidate[swapRow1][swapCol];
    mutatedCandidate[swapRow1][swapCol] = mutatedCandidate[swapRow2][swapCol];
    mutatedCandidate[swapRow2][swapCol] = temp;
}

public void mutateAdd() {
    int[][] mutatedCandidate = Arrays.stream(individual).map(int[]::clone).toArray(int[][]::new);
    Random random = new Random();
    int col = random.nextInt(mutatedCandidate[0].length);
    int row = random.nextInt(mutatedCandidate.length);

    // Add 1 to random gene
    mutatedCandidate[row][col] += 1;
}

//PRINTER METHODS
public void printStrategy(int bestSolutionScore) {
    System.out.println("Profit: " + bestSolutionScore);
    for (int i = 0; i < individual.length; i++) {
        System.out.print("Lkw " + (i + 1) + ": ");
        for (int auftrag : individual[i]) {
            System.out.printf("%3d", auftrag); // Adjust the width as needed
        }
        System.out.println(); // Move to the next line after printing each row
    }
}

// HELPER FUNCTIONS
private boolean lkwHasAnotherZiel(int[][] individual, Lkw[] lkws, int currentLkw, char currentTarget,
        Auftrag[] auftraege) {
    for (int j = 0; j < individual[0].length; j++) {
        if (individual[currentLkw][j] != 0) {
            if (auftraege[j].getZiel() != currentTarget) {
                return true;
            }
        }
    }
    return false;
}

private int calcLkwCapacityGewicht(int[][] individual, Lkw[] lkws, int lkw, Auftrag[] auftraege) {
    // Ein LKW darf nicht mehr Kisten fahren als erlaubt
    int kapaGewicht = lkws[lkw].getKapaGewicht();
    int sumGewicht = 0;
    for (int j = 0; j < individual[lkw].length; j++) {
        sumGewicht += individual[lkw][j] * auftraege[j].getGewichtKisten();
    }
    return kapaGewicht - sumGewicht;
}

private int calcLkwCapacityKisten(int[][] individual, Lkw[] lkws, int lkw) {
    // Ein LKW darf nicht mehr Kisten fahren als erlaubt
    int kapaKisten = lkws[lkw].getKapaKisten();
    int sumKisten = 0;
    for (int j = 0; j < individual[lkw].length; j++) {
        sumKisten += individual[lkw][j];
    }
    return kapaKisten - sumKisten;
}

//GETTER
public int[][] getIndividual() {
    return individual;
}

}