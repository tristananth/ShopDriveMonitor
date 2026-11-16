package shopdrive.model;

import java.time.LocalDate;

/** Satu catatan servis yang pernah dilakukan pada sebuah kendaraan. */
public class RiwayatServis {
    private final String nopol;
    private final LocalDate tanggal;
    private final int km;
    private final String jenisServis;
    private final long biaya;
    private final String catatan;

    public RiwayatServis(String nopol, LocalDate tanggal, int km, String jenisServis,
                         long biaya, String catatan) {
        this.nopol = nopol;
        this.tanggal = tanggal;
        this.km = km;
        this.jenisServis = jenisServis;
        this.biaya = biaya;
        this.catatan = catatan;
    }

    public String getNopol() { return nopol; }
    public LocalDate getTanggal() { return tanggal; }
    public int getKm() { return km; }
    public String getJenisServis() { return jenisServis; }
    public long getBiaya() { return biaya; }
    public String getCatatan() { return catatan; }
}
