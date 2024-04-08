import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Beladungsstrategie {
    public static void main(String[] args) {
        List<Auftrag> auftraege = readAuftraegeFromCSV("geneticAlgorithm/data/auftraege_2.csv");
        List<Lkw> lkws = readLkwsFromCSV("geneticAlgorithm/data/lkw_2.csv");

        for (Auftrag a : auftraege) {
            System.out.println(a);
        }
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
