package shopdrive.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Daftar pekerjaan perawatan berkala berdasarkan kelipatan jarak tempuh.
 * Materi: Array 2 dimensi, looping (for), operator aritmatika (/), percabangan (if).
 *
 * Catatan: interval di bawah adalah acuan umum perawatan berkala mobil (kelipatan
 * 5.000 / 10.000 / 20.000 / 40.000 km). Untuk akurasi, ikuti buku servis masing-masing mobil.
 */
public final class ChecklistServis {

    /** Array 2D: {nama pekerjaan, interval km}. */
    private static final String[][] BUTIR = {
        {"Ganti oli mesin", "5000"},
        {"Ganti filter oli", "5000"},
        {"Periksa rem, ban & tekanan angin", "5000"},
        {"Rotasi ban & cek spooring/balancing", "10000"},
        {"Servis berkala 10.000 km (tune-up ringan)", "10000"},
        {"Ganti filter udara & filter kabin", "20000"},
        {"Ganti filter bensin & busi", "20000"},
        {"Ganti minyak rem", "40000"},
        {"Ganti oli transmisi & oli gardan", "40000"},
        {"Ganti air radiator (coolant)", "40000"}
    };

    private ChecklistServis() { }

    public static int jumlahButir() {
        return BUTIR.length;
    }

    public static String namaButir(int i) {
        return BUTIR[i][0];
    }

    public static int intervalButir(int i) {
        return Integer.parseInt(BUTIR[i][1]);
    }

    /**
     * Sebuah pekerjaan jatuh tempo bila ada kelipatan intervalnya yang "terlewati"
     * di antara kmAwal (servis terakhir) dan kmTujuan (servis berikutnya).
     * Contoh: 47.350 -> 52.350 melewati 50.000, sehingga pekerjaan 5.000 dan 10.000 km jatuh tempo.
     */
    public static List<String> butirJatuhTempo(int kmAwal, int kmTujuan) {
        List<String> hasil = new ArrayList<>();
        for (int i = 0; i < BUTIR.length; i++) {
            int interval = intervalButir(i);
            if (kmTujuan / interval > kmAwal / interval) {
                hasil.add(namaButir(i));
            }
        }
        return hasil;
    }
}
