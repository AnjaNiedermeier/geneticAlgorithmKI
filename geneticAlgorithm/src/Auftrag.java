public class Auftrag {
    private int nr;
    private char ziel;
    private int entfernung;
    private int anzahl_kisten;
    private int gewicht_kisten;
    private int zeitlimit_bonus;
    private int betrag_bonus;
    private int entlohnung;
    private int zeitlimit_strafe;
    private int betrag_strafe;

    public Auftrag(String[] attributes) {
        this.nr = Integer.parseInt(attributes[0]);
        this.ziel = attributes[1].charAt(0);
        this.entfernung = Integer.parseInt(attributes[2]);
        this.anzahl_kisten = Integer.parseInt(attributes[3]);
        this.gewicht_kisten = Integer.parseInt(attributes[4]);
        this.zeitlimit_bonus = attributes[5].isEmpty() ? 0 : Integer.parseInt(attributes[5]);
        this.betrag_bonus = attributes[6].isEmpty() ? 0 : Integer.parseInt(attributes[6]);
        this.entlohnung = Integer.parseInt(attributes[7]);
        this.zeitlimit_strafe = attributes[8].isEmpty() ? 0 : Integer.parseInt(attributes[8]);
        this.betrag_strafe = attributes[9].isEmpty() ? 0 : Integer.parseInt(attributes[9]);
    }

    @Override
    public String toString() {
        return String.format("Auftrag #%d: Ziel %c, %d Kisten mit jeweils. %d kg", this.nr, this.ziel, this.anzahl_kisten, this.gewicht_kisten);
    }

    public int getGewicht_kisten() {
        return gewicht_kisten;
    }

    public int getAnzahl_kisten() {
        return anzahl_kisten;
    }

    public char getZiel() {
        return ziel;
    }
}
