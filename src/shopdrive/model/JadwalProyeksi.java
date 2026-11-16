package shopdrive.model;

import java.time.LocalDate;
import java.util.List;

/** Hasil proyeksi satu jadwal servis di masa depan. */
public class JadwalProyeksi {
    private final int urutan;
    private final int perkiraanKm;
    private final LocalDate perkiraanTanggal;
    private final List<String> pekerjaan;

    public JadwalProyeksi(int urutan, int perkiraanKm, LocalDate perkiraanTanggal, List<String> pekerjaan) {
        this.urutan = urutan;
        this.perkiraanKm = perkiraanKm;
        this.perkiraanTanggal = perkiraanTanggal;
        this.pekerjaan = pekerjaan;
    }

    public int getUrutan() { return urutan; }
    public int getPerkiraanKm() { return perkiraanKm; }
    public LocalDate getPerkiraanTanggal() { return perkiraanTanggal; }
    public List<String> getPekerjaan() { return pekerjaan; }
}
