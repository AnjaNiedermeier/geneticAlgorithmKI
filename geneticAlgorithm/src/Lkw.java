public class Lkw {
    private int nr;
    private int kapa_kisten;
    private int kapa_gewicht;
    private int kmh;

    public Lkw(String[] attributes){
        this.nr = Integer.parseInt(attributes[0]);
        this.kapa_kisten = Integer.parseInt(attributes[1]);
        this.kapa_gewicht = Integer.parseInt(attributes[2]);
        this.kmh = Integer.parseInt(attributes[3]);
    }

    @Override
    public String toString() {
        return String.format("Lkw #%d: Kapazität %d Kisten mit insg. %d kg, Geschwindigkeit: %d km/h", this.nr, this.kapa_kisten, this.kapa_gewicht, this.kmh);
    }
}
