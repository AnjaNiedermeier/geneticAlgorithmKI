public class Lkw {
    private int nr;
    private int kapaKisten;
    private int kapaGewicht;
    private int kmh;

    public Lkw(String[] attributes){
        this.nr = Integer.parseInt(attributes[0]);
        this.kapaKisten = Integer.parseInt(attributes[1]);
        this.kapaGewicht = Integer.parseInt(attributes[2]);
        this.kmh = Integer.parseInt(attributes[3]);
    }

    @Override
    public String toString() {
        return String.format("Lkw #%d: Kapazität %d Kisten mit insg. %d kg, Geschwindigkeit: %d km/h", this.nr, this.kapaKisten, this.kapaGewicht, this.kmh);
    }

    public int getKapaKisten() {
        return kapaKisten;
    }

    public int getKapaGewicht() {
        return kapaGewicht;
    }
}
