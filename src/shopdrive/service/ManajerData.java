package shopdrive.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shopdrive.data.Penyimpanan;
import shopdrive.model.DataTidakValidException;
import shopdrive.model.Kendaraan;
import shopdrive.model.RiwayatServis;

/**
 * Pengelola data di memori + sinkronisasi ke Penyimpanan (CRUD kendaraan dan pencatatan servis).
 * Panel GUI mendaftarkan "listener" supaya otomatis menyegarkan tampilan saat data berubah.
 */
public class ManajerData {
    private final Penyimpanan penyimpanan;
    private final List<Kendaraan> daftarKendaraan = new ArrayList<>();
    private final List<RiwayatServis> daftarRiwayat = new ArrayList<>();
    private final List<Runnable> pendengar = new ArrayList<>();

    public ManajerData(Penyimpanan penyimpanan) throws IOException {
        this.penyimpanan = penyimpanan;
        daftarKendaraan.addAll(penyimpanan.muatKendaraan());
        daftarRiwayat.addAll(penyimpanan.muatRiwayat());
    }

    public void tambahPendengar(Runnable r) { pendengar.add(r); }

    public void beritahu() {
        for (Runnable r : pendengar) {
            r.run();
        }
    }

    public List<Kendaraan> getKendaraan() { return new ArrayList<>(daftarKendaraan); }

    public List<RiwayatServis> getRiwayat() { return new ArrayList<>(daftarRiwayat); }

    public Kendaraan cari(String nopol) {
        for (Kendaraan k : daftarKendaraan) {
            if (k.getNopol().equalsIgnoreCase(nopol)) {
                return k;
            }
        }
        return null;
    }

    public List<RiwayatServis> riwayatDari(String nopol) {
        List<RiwayatServis> hasil = new ArrayList<>();
        for (RiwayatServis r : daftarRiwayat) {
            if (r.getNopol().equalsIgnoreCase(nopol)) {
                hasil.add(r);
            }
        }
        return hasil;
    }

    public void tambahKendaraan(Kendaraan k) throws DataTidakValidException, IOException {
        if (cari(k.getNopol()) != null) {
            throw new DataTidakValidException("Nomor polisi " + k.getNopol() + " sudah terdaftar.");
        }
        daftarKendaraan.add(k);
        simpanSemua();
    }

    /** Mengubah data kendaraan yang sudah ada (nopol tidak diubah). */
    public void ubahKendaraan(Kendaraan baru) throws DataTidakValidException, IOException {
        Kendaraan lama = cari(baru.getNopol());
        if (lama == null) {
            throw new DataTidakValidException("Kendaraan " + baru.getNopol() + " tidak ditemukan.");
        }
        lama.setPemilik(baru.getPemilik());
        lama.setNoHp(baru.getNoHp());
        lama.setMerkModel(baru.getMerkModel());
        lama.setTahun(baru.getTahun());
        lama.setKmSaatIni(baru.getKmSaatIni());
        lama.setKmServisTerakhir(baru.getKmServisTerakhir());
        lama.setTglServisTerakhir(baru.getTglServisTerakhir());
        lama.setIntervalKm(baru.getIntervalKm());
        lama.setIntervalBulan(baru.getIntervalBulan());
        simpanSemua();
    }

    /** Fitur utama: memperbarui jarak tempuh (odometer) terbaru sebuah kendaraan. */
    public void perbaruiJarakTempuh(String nopol, int kmBaru) throws DataTidakValidException, IOException {
        Kendaraan k = cari(nopol);
        if (k == null) {
            throw new DataTidakValidException("Kendaraan " + nopol + " tidak ditemukan.");
        }
        if (kmBaru < k.getKmSaatIni()) {
            throw new DataTidakValidException("Jarak tempuh baru (" + PerhitunganServis.formatKm(kmBaru)
                    + " km) tidak boleh lebih kecil dari yang tercatat (" + PerhitunganServis.formatKm(k.getKmSaatIni()) + " km).");
        }
        k.setKmSaatIni(kmBaru);
        simpanSemua();
    }

    public void hapusKendaraan(String nopol) throws IOException {
        daftarKendaraan.removeIf(k -> k.getNopol().equalsIgnoreCase(nopol));
        daftarRiwayat.removeIf(r -> r.getNopol().equalsIgnoreCase(nopol));
        simpanSemua();
    }

    /** Mencatat servis baru; jika servis ini yang terbaru, data servis terakhir kendaraan ikut diperbarui. */
    public void catatServis(RiwayatServis r) throws DataTidakValidException, IOException {
        Kendaraan k = cari(r.getNopol());
        if (k == null) {
            throw new DataTidakValidException("Kendaraan " + r.getNopol() + " tidak ditemukan.");
        }
        daftarRiwayat.add(r);
        if (!r.getTanggal().isBefore(k.getTglServisTerakhir())) {
            k.setTglServisTerakhir(r.getTanggal());
            k.setKmServisTerakhir(r.getKm());
            if (r.getKm() > k.getKmSaatIni()) {
                k.setKmSaatIni(r.getKm());
            }
        }
        simpanSemua();
    }

    private void simpanSemua() throws IOException {
        penyimpanan.simpanKendaraan(daftarKendaraan);
        penyimpanan.simpanRiwayat(daftarRiwayat);
        beritahu();
    }
}
