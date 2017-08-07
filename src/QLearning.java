import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;

public class QLearning extends JFrame {
    // Program boyunca kullanılacak sabitler burada tanımlanıyor
    private static final double ogrenmeKatsayisi = 0.8;
    private static final Random random           = new Random();
    private static final int    kenarKalinligi   = 10;

    // Programın, çalışma esnasında kullanacağı değerleri buraya tanımlıyoruz
    private int[][]           rMatrisi;
    private double[][]        qMatrisi;
    private ArrayList<Durum>  durumlar;
    private ArrayList<JPanel> paneller;
    private boolean           calisti;

    // Tasarımda kullanılan elemanlar
    private JPanel statusBar;
    private JLabel statusLabel;
    private JPanel labirent;
    private JLabel baslangicLabel, bitisLabel, infoLabel, iterasyonLabel;
    private JButton baslatBtn;
    private ArrayList<Durum> cevap;

    // Adım takipleri
    private int simdikiDurum = -1, satirSayisi, hedef = -1, iterasyon = 3000;

    // Başlatma fonksiyonu
    public static void main(String[] args) {
        QLearning qLearning = new QLearning();
        qLearning.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        qLearning.setSize(500, 500);

        qLearning.arayuzuTasarla();

        qLearning.setVisible(true);
    }

    private void degerleriSifirla() {
        simdikiDurum = -1;
        satirSayisi = 0;
        hedef = -1;
        calisti = false;
        // durumlar = null;

        for (JPanel panel : paneller)
            panel.setBackground(Color.WHITE);

        setStatus("Yenilendi!");

        for (int i = 0; i < qMatrisi.length; i++) {
            for (int j = 0; j < qMatrisi[i].length; j++) {
                qMatrisi[i][j] = 0;
            }
        }

        baslatBtn.setText("Başlat");
        baslatBtn.setEnabled(false);

        getContentPane().removeAll();
        arayuzuTasarla();
    }

    // Menü hazırlanıyor
    private void menubarTasarla() {
        if (getJMenuBar() != null) return;
        JMenuBar  menuBar   = new JMenuBar();
        JMenu     menu      = new JMenu("Dosya");
        JMenuItem miDosyaAc = new JMenuItem("Dosya Seç...");
        menu.add(miDosyaAc);

        miDosyaAc.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(miDosyaAc) == JFileChooser.APPROVE_OPTION) {
                    durumlariAl(fileChooser.getSelectedFile().getAbsolutePath());
                    matrisleriOlustur();
                    labirentOlustur();
                    setStatus("Durumlar Alındı!", Color.WHITE, Color.GRAY);
                    labirentiTasarla();
                }
            }
        });

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void labirentiTasarla() {
        int sutunSayisi = (int) Math.sqrt(rMatrisi.length);
        labirent.setLayout(new GridLayout(sutunSayisi, sutunSayisi));

        // Her bir panel için, etrafını çevreleyen bir çerçeve ekleniyor
        for (int i = 0; i < paneller.size(); i++) {
            JPanel panel = paneller.get(i);
            Durum  durum = durumlar.get(i);

            // Çerçeve ve boyutları tanımlanıyor
            MatteBorder border = (MatteBorder) panel.getBorder();
            Insets      insets = border.getBorderInsets();

            // Her bir geçiş için
            for (Gecis gecis : durum.getGecisler()) {
                // Yatay ve dikey yerleri alınıyor
                int durumYatay = durum.getYatayKonum(), durumDikey = durum.getDikeyKonum();
                int hedefYatay = gecis.getHedef().getYatayKonum(), hedefDikey = gecis.getHedef().getDikeyKonum();

                // Var olan bağlantı yerlerindeki kenarlıklar kaldırılıyor
                if (durumYatay == hedefYatay) {
                    if (durumDikey > hedefDikey) {
                        insets.left = 0;
                        panel.setBorder(new MatteBorder(insets, Color.BLACK));
                    }
                    else {
                        insets.right = 0;
                        panel.setBorder(new MatteBorder(insets, Color.BLACK));
                    }
                }
                else {
                    if (durumYatay > hedefYatay) {
                        insets.top = 0;
                        panel.setBorder(new MatteBorder(insets, Color.BLACK));
                    }
                    else {
                        insets.bottom = 0;
                        panel.setBorder(new MatteBorder(insets, Color.BLACK));
                    }
                }
            }

            // Labirente bu oda ekleniyor
            labirent.add(panel);
        }
    }

    private void arayuzuTasarla() {
        menubarTasarla();
        setLayout(new MigLayout());

        calisti = false;

        // Bilgi paneli tanımlanıyor
        JPanel bilgiPaneli = new JPanel(new MigLayout());

        baslatBtn = new JButton("Başlat");
        baslatBtn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (calisti) {
                    degerleriSifirla();
                    return;
                }

                baslatBtn.setText("Yeniden Başlat");

                // Hedef durumu al ve
                Durum hedefDurum = durumlar.get(hedef);

                // Hedefe giden her bir yol için
                for (Gecis gecis : hedefDurum.getGecisler()) {
                    // Eğer yolun sonunda yine hedefin kendisi yoksa
                    if (gecis.getHedef() == hedefDurum) continue;

                    // O yolun başına, hedefe gidiş yönüne doğru rMatrisinde 100 puan ver
                    Durum gelis = gecis.getHedef();
                    rMatrisi[gelis.getIndex()][hedef] = 100;
                }

                // Hedefin, kendisine dönüşü 100 puandır
                rMatrisi[hedef][hedef] = 100;

                // En son durumda, yol haritası çıkarmak için ilkDurum'u not et
                int ilkDurum = simdikiDurum;

                // Her bir iterasyonda, başlangıç noktasından hedefe tekrar gitmeye çalış
                for (int i = 0; i < iterasyon; i++)
                    hareketeBasla(simdikiDurum);

                // Şimdiki yerin kaydını tut
                int simdikiYer = ilkDurum;

                cevap = new ArrayList<>();

                do {
                    // Q-Matrisinden, şimdiki yerden sonraki yeri tekrarlı bir şekilde
                    // bularak, en kısa yolu tercih ettiriyoruz
                    int      sonrakiYer    = 0;
                    double   enYuksekDeger = -1;
                    double[] gecisPuanları = qMatrisi[simdikiYer];
                    for (int hedefYer = 0; hedefYer < gecisPuanları.length; hedefYer++) {
                        double hedefeGecisPuani = gecisPuanları[hedefYer];

                        if (enYuksekDeger < hedefeGecisPuani) {
                            enYuksekDeger = hedefeGecisPuani;
                            sonrakiYer = hedefYer;
                        }
                    }

                    // Yollara şimdiki yeri ekle
                    cevap.add(durumlar.get(simdikiYer));

                    // Sonraki yere geç
                    simdikiYer = sonrakiYer;
                } while (simdikiYer != hedef);

                // Geçilen yolları temsil eden panelleri kırmızıya boya
                for (int yol = 1; yol < cevap.size(); yol++) {
                    int panel = cevap.get(yol).getIndex();
                    paneller.get(panel).setBackground(Color.RED);
                }

                // Matrisleri yazdır ve dosyaya ver
                matrisleriYazdir();

                calisti = true;
            }
        });

        // İlk durumda başlat butonu çalışmaz
        baslatBtn.setEnabled(false);

        // Bilgi paneli değişkenleri, alt bilgi çubuğu ve diğer metinleri oluştur
        infoLabel = new JLabel("Labirent Boyutu: 0x0");
        statusBar = new JPanel(new GridLayout(1, 1));
        statusLabel = new JLabel();
        labirent = new JPanel();

        baslangicLabel = new JLabel("Başlangıç Düğümü: Seçilmedi");
        bitisLabel = new JLabel("Bitiş Düğümü: Seçilmedi");
        infoLabel = new JLabel("<html><body>Başlangıç ve bitiş düğümünü seçmek için soldaki labirentteki odalardan birine tıklayın<body></html>");
        iterasyonLabel = new JLabel("İterasyon: 3000 (Değiştirmek için tıklayın)");

        iterasyonLabel.addMouseListener(new MouseClickEvent(new Function<MouseEvent>() {
            @Override
            public void run(MouseEvent e) {
                String  cevap;
                boolean tekrarAl;
                do {
                    cevap = JOptionPane.showInputDialog(e.getComponent(), "Lütfen iterasyon miktarını giriniz", "İterasyonu Değiştir", JOptionPane.QUESTION_MESSAGE);

                    try {
                        iterasyon = Integer.parseInt(cevap);

                        if (iterasyon < 1) throw new NumberFormatException();

                        iterasyonLabel.setText(String.format("İterasyon: %d (Değiştirmek için tıklayın)", iterasyon));

                        tekrarAl = false;
                    }
                    catch (NumberFormatException nfe) {
                        // Hatalı giriş
                        tekrarAl = cevap != null;
                    }
                } while (tekrarAl);
            }
        }));

        // Durum çubuğunu ekle
        statusBar.add(statusLabel);

        // Bilgi paneline, elemanları ekle
        bilgiPaneli.add(infoLabel, "wrap");
        bilgiPaneli.add(baslangicLabel, "wrap");
        bilgiPaneli.add(bitisLabel, "wrap");
        bilgiPaneli.add(iterasyonLabel, "wrap");
        bilgiPaneli.add(new JSeparator(), "pushx, growx, span");
        bilgiPaneli.add(infoLabel, "wrap");

        // Bu forma labirent, bilgi paneli, başlatma butonu ve durum çubuğunu ekle
        add(labirent, "grow, push, spany 2, w 70%");
        add(new JSeparator(JSeparator.VERTICAL), "growy, pushy, spany 2");
        add(bilgiPaneli, "grow, push, w 30%, wrap");
        add(baslatBtn, "pushx, gapleft push, span");
        add(statusBar, "pushx, growx, span");
    }

    private void hareketeBasla(int baslangic) {
        // Hedefte olduğu sürece, matrisi hareketlere yeniden başlayıp güncelleme fonksiyonu

        simdikiDurum = baslangic;
        do {
            qMatrisiniGuncelle();
        } while (simdikiDurum == hedef);

        for (int satir = 0; satir < satirSayisi; satir++) qMatrisiniGuncelle();
    }

    private void qMatrisiniGuncelle() {
        // Rastgele bir ihtimal seçip onu değerlendiren fonksiyon
        int sonrakiIhtimal = hareketSec(satirSayisi);

        if (rMatrisi[simdikiDurum][sonrakiIhtimal] >= 0) {
            qMatrisi[simdikiDurum][sonrakiIhtimal] = q(sonrakiIhtimal);
            simdikiDurum = sonrakiIhtimal;
        }
    }

    private double q(int aksiyon) {
        return (int) (rMatrisi[simdikiDurum][aksiyon] + ogrenmeKatsayisi * maximumBul(aksiyon));
    }

    private double maximumBul(int durum) {
        // Durumdan hedefe giden yollardan maximum puana sahip olanı bulan fonksiyon
        int     hedef      = 0;
        boolean hedefBulundu;
        boolean donguyuKir = false;

        while (!donguyuKir) {
            hedefBulundu = false;

            for (int hedefDurum = 0; hedefDurum < satirSayisi; hedefDurum++) {
                if (hedefDurum == hedef) continue;

                if (qMatrisi[durum][hedefDurum] > qMatrisi[durum][hedef]) {
                    hedef = hedefDurum;
                    hedefBulundu = true;
                }
            }

            if (!hedefBulundu) donguyuKir = true;
        }

        return qMatrisi[durum][hedef];
    }

    private int hareketSec(int max) {
        int hareket;
        do {
            hareket = random.nextInt(max);
            if (rMatrisi[simdikiDurum][hareket] > -1) return hareket;
        } while (true);
    }

    private void setStatus(String status) {
        statusLabel.setText(status);
    }

    private void setStatus(String status, Color background) {
        setStatus(status);
        statusBar.setBackground(background);
    }

    private void setStatus(String status, Color color, Color background) {
        setStatus(status, background);
        statusLabel.setForeground(color);
    }

    private void labirentOlustur() {
        // Labirent içerisinde odaları belirten panelleri tut
        paneller = new ArrayList<>(rMatrisi.length * rMatrisi.length);

        // Her bir durum için
        for (Durum durum : durumlar) {
            // Bir oda/panel oluştur
            JPanel panel = new JPanel(new BorderLayout());

            // Arkaplanı beyaz olan
            panel.setBackground(Color.WHITE);

            // Ve kenarKalinligi'na sahip siyah kenarlığı olan
            panel.setBorder(new MatteBorder(kenarKalinligi, kenarKalinligi, kenarKalinligi, kenarKalinligi, Color.BLACK));

            // Ve bir yazı oluştur, bu başlangıç ve bitişi ifade edecek
            final JLabel durumLabel = new JLabel("");

            // Metni ortala
            durumLabel.setHorizontalTextPosition(SwingConstants.CENTER);

            panel.addMouseListener(new MouseClickEvent(new Function<MouseEvent>() {
                @Override
                public void run(MouseEvent e) {
                    // Başlangıç veya hedef seçili değilse seç, bunlardan birinin
                    // üzerine tıklandı ise iptal et

                    if (calisti) return;

                    if (durum.getIndex() == simdikiDurum) {
                        panel.setBackground(Color.WHITE);
                        simdikiDurum = -1;
                        durumLabel.setText("");
                        setStatus("Başlangıç noktası iptal edildi!", Color.PINK);
                        baslatBtn.setEnabled(false);
                        return;
                    }
                    else if (durum.getIndex() == hedef) {
                        panel.setBackground(Color.WHITE);
                        hedef = -1;
                        durumLabel.setText("");
                        setStatus("Hedef noktası iptal edildi!", Color.PINK);
                        baslatBtn.setEnabled(false);
                        return;
                    }

                    if (simdikiDurum < 0) {
                        simdikiDurum = durum.getIndex();
                        durumLabel.setText("Başlangıç");
                        panel.setBackground(Color.GREEN);
                        setStatus("Başlangıç noktası seçildi!", Color.GREEN);
                    }
                    else if (hedef < 0) {
                        hedef = durum.getIndex();
                        durumLabel.setText("Hedef");
                        panel.setBackground(Color.PINK);
                        setStatus("Hedef noktası seçildi!", Color.GREEN);
                    }

                    if (durumLabel.getText().isEmpty()) panel.add(durumLabel, BorderLayout.CENTER);

                    baslatBtn.setEnabled(true);
                }
            }));

            // Bu yeni odayı/paneli paneller listesine ekle
            paneller.add(durum.getIndex(), panel);
        }
    }

    private void matrisleriYazdir() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nQ Matrisi:\n");
        for (double[] sonucSirasi : qMatrisi) {
            for (double sonuc : sonucSirasi) {
                sb.append(String.format(" %2.2f ", sonuc));
            }
            sb.append('\n');
        }

        File outFile = new File("./outQ.txt");
        dosyayaYaz(outFile, sb.toString());

        sb = new StringBuilder();

        sb.append("\nR Matrisi:\n");
        for (int[] sonucSirasi : rMatrisi) {
            for (int sonuc : sonucSirasi) {
                sb.append(String.format("%4d", sonuc));
            }
            sb.append('\n');
        }

        outFile = new File("./outR.txt");
        dosyayaYaz(outFile, sb.toString());

        sb = new StringBuilder();
        for (Durum durum : cevap) {
            sb.append(String.format("(%d, %d) - %d\n", durum.getYatayKonum(), durum.getDikeyKonum(), durum.getIndex()));
        }

        outFile = new File("./outPath.txt");
        dosyayaYaz(outFile, sb.toString());
    }

    private void dosyayaYaz(File outFile, String content) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(outFile);
            bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
            fw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fw != null) fw.close();
                if (bw != null) bw.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void matrisleriOlustur() {
        int boy = durumlar.size();

        rMatrisi = new int[boy][boy];
        qMatrisi = new double[boy][boy];

        for (Durum durum : durumlar) {
            ArrayList<Durum> hedefler = new ArrayList<>();
            for (Gecis g : durum.getGecisler()) hedefler.add(g.getHedef());

            for (Durum hedef : durumlar) {
                int hedefYeri = hedef.getIndex();
                int durumYeri = durum.getIndex();

                if (hedefYeri == durumYeri && durum.isSonDurum())
                    rMatrisi[durumYeri][hedefYeri] = 100;
                else if (hedefler.contains(hedef))
                    rMatrisi[durumYeri][hedefYeri] = hedef.isSonDurum() ? 100 : 0;
                else rMatrisi[durumYeri][hedefYeri] = -1;
            }
        }
    }

    private void durumlariAl(String path) {
        FileReader     fr;
        BufferedReader br;
        File           file = new File(path);
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            HashMap<Integer, String> bilgiler = new HashMap<>();

            int    sutunSayisi;
            String satir;
            while ((satir = br.readLine()) != null) {
                bilgiler.put(satirSayisi++, satir);
            }

            satirSayisi = bilgiler.size();
            double karekok = Math.sqrt(satirSayisi);
            if ((karekok - ((int) karekok)) != 0) {
                setStatus("Dosyayı yorumlamada hata! Lütfen dosya içeriğini kontrol ediniz!", Color.PINK);
                throw new IOException();
                // Tam karekök değil, hata ver
            }

            durumlar = new ArrayList<>((int) karekok);
            int index = 0;
            for (int satirSayisi = 0; satirSayisi < karekok; satirSayisi++) {
                for (sutunSayisi = 0; sutunSayisi < karekok; sutunSayisi++) {
                    Durum durum = new Durum(satirSayisi, sutunSayisi);
                    durum.setIndex(index++);
                    durumlar.add(durum);
                }
            }

            int konumImleci = 0;
            for (int satirSayisi = 0; satirSayisi < karekok; satirSayisi++) {
                for (sutunSayisi = 0; sutunSayisi < karekok; sutunSayisi++) {
                    String   bilgi    = bilgiler.get(konumImleci);
                    String[] hedefler = bilgi.split(",");

                    Durum simdikiDurum = durumlar.get(konumImleci);
                    for (String hedef : hedefler) {
                        int   hedefKonum = Integer.parseInt(hedef);
                        Durum hedefDurum = durumlar.get(hedefKonum);
                        Gecis gecis      = new Gecis(simdikiDurum, hedefDurum);

                        simdikiDurum.gecisEkle(gecis);
                    }

                    konumImleci++;
                }
            }
        }
        catch (IOException e) {
            setStatus("Durumları Almada Hata! Dosya içeriğini kontrol ediniz", Color.WHITE, Color.PINK);
            e.printStackTrace();
        }
    }
}
