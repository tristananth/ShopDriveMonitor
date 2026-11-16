package shopdrive.model;

import java.time.LocalDate;

/**
 * Class Kendaraan: menyimpan atribut mobil pelanggan beserta data servis terakhir.
 * Materi: Class, Object, Atribut, Method (getter/setter).
 */
public class Kendaraan {
    private String nopol;
    private String pemilik;
    private String noHp;
    private String merkModel;
    private int tahun;
    private int kmSaatIni;            // jarak tempuh (odometer) terbaru
    private int kmServisTerakhir;     // odometer saat servis terakhir
    private LocalDate tglServisTerakhir;
    private int intervalKm;           // interval servis (km)
    private int intervalBulan;        // interval servis (bulan)

    public Kendaraan(String nopol, String pemilik, String noHp, String merkModel, int tahun,
                     int kmSaatIni, int kmServisTerakhir, LocalDate tglServisTerakhir,
                     int intervalKm, int intervalBulan) {
        this.nopol = nopol;
        this.pemilik = pemilik;
        this.noHp = noHp;
        this.merkModel = merkModel;
        this.tahun = tahun;
        this.kmSaatIni = kmSaatIni;
        this.kmServisTerakhir = kmServisTerakhir;
        this.tglServisTerakhir = tglServisTerakhir;
        this.intervalKm = intervalKm;
        this.intervalBulan = intervalBulan;
    }

    public String getNopol() { return nopol; }
    public String getPemilik() { return pemilik; }
    public String getNoHp() { return noHp; }
    public String getMerkModel() { return merkModel; }
    public int getTahun() { return tahun; }
    public int getKmSaatIni() { return kmSaatIni; }
    public int getKmServisTerakhir() { return kmServisTerakhir; }
    public LocalDate getTglServisTerakhir() { return tglServisTerakhir; }
    public int getIntervalKm() { return intervalKm; }
    public int getIntervalBulan() { return intervalBulan; }

    public void setPemilik(String pemilik) { this.pemilik = pemilik; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
    public void setMerkModel(String merkModel) { this.merkModel = merkModel; }
    public void setTahun(int tahun) { this.tahun = tahun; }
    public void setKmSaatIni(int kmSaatIni) { this.kmSaatIni = kmSaatIni; }
    public void setKmServisTerakhir(int kmServisTerakhir) { this.kmServisTerakhir = kmServisTerakhir; }
    public void setTglServisTerakhir(LocalDate tgl) { this.tglServisTerakhir = tgl; }
    public void setIntervalKm(int intervalKm) { this.intervalKm = intervalKm; }
    public void setIntervalBulan(int intervalBulan) { this.intervalBulan = intervalBulan; }

    @Override
    public String toString() {
        return nopol + " - " + merkModel;
    }
}
