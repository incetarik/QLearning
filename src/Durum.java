import java.util.ArrayList;

public class Durum {
    private int              yatayKonum;
    private int              dikeyKonum;
    private int              index;
    private ArrayList<Gecis> gecisler;
    private boolean          sonDurum;
    private boolean          gecisYolu;

    public Durum(int yatayKonum, int dikeyKonum, int index, ArrayList<Gecis> gecisler, boolean sonDurum, boolean gecisYolu) {

        this.yatayKonum = yatayKonum;
        this.dikeyKonum = dikeyKonum;
        this.index = index;
        this.gecisler = gecisler;
        this.sonDurum = sonDurum;
        this.gecisYolu = gecisYolu;
    }

    public Durum(int yatayKonum, int dikeyKonum, ArrayList<Gecis> gecisler, boolean sonDurum, boolean gecisYolu) {

        this.yatayKonum = yatayKonum;
        this.dikeyKonum = dikeyKonum;
        this.gecisler = gecisler;
        this.sonDurum = sonDurum;
        this.gecisYolu = gecisYolu;
    }

    public Durum(int yatayKonum, int dikeyKonum) {
        this.yatayKonum = yatayKonum;
        this.dikeyKonum = dikeyKonum;
        this.gecisler = new ArrayList<>();
    }

    public Durum(int yatayKonum, int dikeyKonum, ArrayList<Gecis> gecisler) {
        this.yatayKonum = yatayKonum;
        this.dikeyKonum = dikeyKonum;
        this.gecisler = gecisler;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isGecisYolu() {
        return gecisYolu;
    }

    public void setGecisYolu(boolean gecisYolu) {
        this.gecisYolu = gecisYolu;
    }

    @Override
    public String toString() {
        return "Durum(" + yatayKonum + ", " + dikeyKonum + ")";
    }

    public boolean isSonDurum() {
        return sonDurum;
    }

    public void setSonDurum(boolean sonDurum) {
        this.sonDurum = sonDurum;
    }

    public Durum gecisEkle(Gecis gecis) {
        gecisler.add(gecis);
        return this;
    }

    public int getYatayKonum() {
        return yatayKonum;
    }

    public void setYatayKonum(int yatayKonum) {
        this.yatayKonum = yatayKonum;
    }

    public int getDikeyKonum() {
        return dikeyKonum;
    }

    public void setDikeyKonum(int dikeyKonum) {
        this.dikeyKonum = dikeyKonum;
    }

    public ArrayList<Gecis> getGecisler() {
        return gecisler;
    }

    public void setGecisler(ArrayList<Gecis> gecisler) {
        this.gecisler = gecisler;
    }
}
