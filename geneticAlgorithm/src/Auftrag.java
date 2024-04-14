public class Auftrag {
    private int nr;
    private char ziel;
    private int entfernung;
    private int anzahlKisten;
    private int gewichtKisten;
    private int zeitlimitBonus;
    private int betragBonus;
    private int entlohnung;
    private int zeitlimitStrafe;
    private int betragStrafe;

    public Auftrag(String[] attributes) {
        this.nr = Integer.parseInt(attributes[0]);
        this.ziel = attributes[1].charAt(0);
        this.entfernung = Integer.parseInt(attributes[2]);
        this.anzahlKisten = Integer.parseInt(attributes[3]);
        this.gewichtKisten = Integer.parseInt(attributes[4]);
        this.zeitlimitBonus = attributes[5].isEmpty() ? 0 : Integer.parseInt(attributes[5]);
        this.betragBonus = attributes[6].isEmpty() ? 0 : Integer.parseInt(attributes[6]);
        this.entlohnung = Integer.parseInt(attributes[7]);
        this.zeitlimitStrafe = attributes[8].isEmpty() ? 0 : Integer.parseInt(attributes[8]);
        this.betragStrafe = attributes[9].isEmpty() ? 0 : Integer.parseInt(attributes[9]);
    }

    @Override
    public String toString() {
        return String.format("Auftrag #%d: Ziel %c, %d Kisten mit jeweils. %d kg", this.nr, this.ziel, this.anzahlKisten, this.gewichtKisten);
    }

    public int getGewichtKisten() {
        return gewichtKisten;
    }

    public int getAnzahlKisten() {
        return anzahlKisten;
    }

    public char getZiel() {
        return ziel;
    }

    public int getEntlohnung() {
        return entlohnung;
    }

    public int getBetragBonus() {
        return betragBonus;
    }

    public int getBetragStrafe() {
        return betragStrafe;
    }

    public int getEntfernung() {
        return entfernung;
    }

    public int getZeitlimitBonus() {
        return zeitlimitBonus;
    }

    public int getZeitlimitStrafe() {
        return zeitlimitStrafe;
    }
}
