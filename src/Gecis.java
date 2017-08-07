public class Gecis {
    private Durum   konum;
    private Durum   hedef;

    public Gecis() {
    }

    public Gecis(Durum konum) {
        this.konum = konum;
    }

    public Gecis(Durum konum, Durum hedef) {
        this.konum = konum;
        this.hedef = hedef;
    }

    @Override
    public String toString() {
        return "(" + konum.getYatayKonum() + ", " + konum.getDikeyKonum() + ") -> (" + hedef.getYatayKonum() + ", " + hedef.getDikeyKonum() + ")";
    }

    public Durum getKonum() {
        return konum;
    }

    public void setKonum(Durum konum) {
        this.konum = konum;
    }

    public Durum getHedef() {
        return hedef;
    }

    public void setHedef(Durum hedef) {
        this.hedef = hedef;
    }
}
