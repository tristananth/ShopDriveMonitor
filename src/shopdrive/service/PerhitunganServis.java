package shopdrive.service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import shopdrive.model.JadwalProyeksi;
import shopdrive.model.Kendaraan;
import shopdrive.model.RiwayatServis;
import shopdrive.model.StatusServis;

/**
 * Inti logika monitoring servis berkala.
 *
 * Aturan status (dua pemicu, mana yang lebih dulu tercapai):
 *   - sisa jarak  = (km servis terakhir + interval km) - km saat ini
 *   - sisa waktu  = (tanggal servis terakhir + interval bulan) - hari ini
 *   TERLAMBAT : sisa jarak < 0 ATAU sisa waktu < 0
 *   SEGERA    : sisa jarak <= 500 km ATAU sisa waktu <= 14 hari
 *   AMAN      : selain itu
 *
 * Materi: method static, overloading, percabangan, array + perulangan, rekursi, operator.
 */
public final class PerhitunganServis {
    public static final int AMBANG_KM_SEGERA = 500;
    public static final int AMBANG_HARI_SEGERA = 14;
    public static final double KM_PER_HARI_DEFAULT = 40.0;   // perkiraan jika data belum cukup

    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private PerhitunganServis() { }

    // ---------- Format tampilan ----------
    public static String formatKm(int km) {
        return NumberFormat.getIntegerInstance(ID).format(km);
    }

    public static String formatRupiah(long nilai) {
        return "Rp " + NumberFormat.getIntegerInstance(ID).format(nilai);
    }

    // ---------- Perhitungan dasar (dengan overloading) ----------
    public static int sisaKm(Kendaraan k) {
        return (k.getKmServisTerakhir() + k.getIntervalKm()) - k.getKmSaatIni();
    }

    public static LocalDate jatuhTempoTanggal(Kendaraan k) {
        return k.getTglServisTerakhir().plusMonths(k.getIntervalBulan());
    }

    /** Overload 1: memakai tanggal hari ini dari Waktu. */
    public static long sisaHari(Kendaraan k) {
        return sisaHari(k, Waktu.hariIni());
    }

    /** Overload 2: memakai tanggal acuan yang diberikan. */
    public static long sisaHari(Kendaraan k, LocalDate hariIni) {
        return ChronoUnit.DAYS.between(hariIni, jatuhTempoTanggal(k));
    }

    /** Overload 1: status dari dua angka sisa. */
    public static StatusServis tentukanStatus(int sisaKm, long sisaHari) {
        if (sisaKm < 0 || sisaHari < 0) {
            return StatusServis.TERLAMBAT;
        } else if (sisaKm <= AMBANG_KM_SEGERA || sisaHari <= AMBANG_HARI_SEGERA) {
            return StatusServis.SEGERA;
        }
        return StatusServis.AMAN;
    }

    /** Overload 2: status langsung dari objek kendaraan. */
    public static StatusServis tentukanStatus(Kendaraan k, LocalDate hariIni) {
        return tentukanStatus(sisaKm(k), sisaHari(k, hariIni));
    }

    // ---------- Estimasi ----------
    /** Rata-rata jarak tempuh per hari sejak servis terakhir. */
    public static double rataKmPerHari(Kendaraan k, LocalDate hariIni) {
        long hari = ChronoUnit.DAYS.between(k.getTglServisTerakhir(), hariIni);
        int selisihKm = k.getKmSaatIni() - k.getKmServisTerakhir();
        if (hari <= 0 || selisihKm <= 0) {
            return KM_PER_HARI_DEFAULT;
        }
        return (double) selisihKm / hari;
    }

    /** Perkiraan tanggal servis: mana yang lebih dulu antara batas waktu dan batas jarak. */
    public static LocalDate estimasiServis(Kendaraan k, LocalDate hariIni) {
        LocalDate byWaktu = jatuhTempoTanggal(k);
        int sk = sisaKm(k);
        if (sk <= 0) {
            return hariIni;   // batas jarak sudah terlewati
        }
        long hariKeKm = Math.round(sk / rataKmPerHari(k, hariIni));
        LocalDate byKm = hariIni.plusDays(hariKeKm);
        LocalDate paling = byKm.isBefore(byWaktu) ? byKm : byWaktu;
        return paling.isBefore(hariIni) ? hariIni : paling;   // batas waktu terlewati: servis sekarang
    }

    // ---------- Pengurutan manual (selection sort pada array) ----------
    /** Urutkan: status paling mendesak dulu, lalu sisa hari paling sedikit. */
    public static Kendaraan[] urutkanUrgensi(List<Kendaraan> daftar, LocalDate hariIni) {
        Kendaraan[] arr = daftar.toArray(new Kendaraan[0]);
        for (int i = 0; i < arr.length - 1; i++) {
            int terpilih = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (lebihMendesak(arr[j], arr[terpilih], hariIni)) {
                    terpilih = j;
                }
            }
            Kendaraan tmp = arr[i];
            arr[i] = arr[terpilih];
            arr[terpilih] = tmp;
        }
        return arr;
    }

    private static boolean lebihMendesak(Kendaraan a, Kendaraan b, LocalDate hariIni) {
        int pa = tentukanStatus(a, hariIni).getPrioritas();
        int pb = tentukanStatus(b, hariIni).getPrioritas();
        if (pa != pb) {
            return pa > pb;
        }
        return sisaHari(a, hariIni) < sisaHari(b, hariIni);
    }

    /** Hitung jumlah kendaraan per status: indeks array = prioritas status. */
    public static int[] ringkasan(List<Kendaraan> daftar, LocalDate hariIni) {
        int[] jumlah = new int[StatusServis.values().length];
        for (Kendaraan k : daftar) {
            jumlah[tentukanStatus(k, hariIni).getPrioritas()]++;
        }
        return jumlah;
    }

    // ---------- Pesan notifikasi ----------
    public static String alasan(Kendaraan k, LocalDate hariIni) {
        int sk = sisaKm(k);
        long sh = sisaHari(k, hariIni);
        String bagianKm = (sk < 0) ? "melewati batas " + formatKm(-sk) + " km"
                                   : "sisa " + formatKm(sk) + " km";
        String bagianHari = (sh < 0) ? "terlambat " + (-sh) + " hari"
                                     : "sisa " + sh + " hari";
        return bagianKm + ", " + bagianHari;
    }

    /** Pesan singkat untuk daftar notifikasi di dashboard. */
    public static String pesanNotifikasi(Kendaraan k, LocalDate hariIni) {
        StatusServis s = tentukanStatus(k, hariIni);
        String awal;
        switch (s) {
            case TERLAMBAT:
                awal = "[TERLAMBAT] ";
                break;
            case SEGERA:
                awal = "[SEGERA] ";
                break;
            default:
                awal = "[AMAN] ";
                break;
        }
        return awal + k.getNopol() + " - " + k.getMerkModel() + " (" + k.getPemilik() + "): "
                + alasan(k, hariIni);
    }

    /** Teks pengingat siap kirim ke pelanggan (WhatsApp/SMS). */
    public static String pesanUntukPelanggan(Kendaraan k, LocalDate hariIni) {
        return "Halo Bapak/Ibu " + k.getPemilik() + ", ini pengingat dari Shop & Drive Cempaka Putih. "
                + "Mobil " + k.getMerkModel() + " (" + k.getNopol() + ") sudah waktunya servis berkala ("
                + alasan(k, hariIni) + "). Silakan kunjungi kami di Jl. Cempaka Putih Raya No. 19, "
                + "Jakarta Pusat. Terima kasih.";
    }

    // ---------- Rekursi ----------
    /**
     * Proyeksi n jadwal servis berikutnya. Method pembungkus; pekerjaan dilakukan
     * oleh method rekursif proyeksiRekursif().
     */
    public static List<JadwalProyeksi> proyeksi(Kendaraan k, LocalDate hariIni, int jumlah) {
        List<JadwalProyeksi> hasil = new ArrayList<>();
        proyeksiRekursif(k.getKmServisTerakhir(), k.getTglServisTerakhir(),
                k.getIntervalKm(), k.getIntervalBulan(), rataKmPerHari(k, hariIni), 1, jumlah, hasil);
        return hasil;
    }

    private static void proyeksiRekursif(int km, LocalDate tgl, int intervalKm, int intervalBulan,
                                         double kmPerHari, int ke, int total, List<JadwalProyeksi> hasil) {
        if (ke > total) {            // kasus dasar (base case): berhenti
            return;
        }
        int kmBerikut = km + intervalKm;
        LocalDate byWaktu = tgl.plusMonths(intervalBulan);
        LocalDate byKm = tgl.plusDays(Math.round(intervalKm / kmPerHari));
        LocalDate tglBerikut = byKm.isBefore(byWaktu) ? byKm : byWaktu;
        hasil.add(new JadwalProyeksi(ke, kmBerikut, tglBerikut,
                ChecklistServis.butirJatuhTempo(km, kmBerikut)));
        proyeksiRekursif(kmBerikut, tglBerikut, intervalKm, intervalBulan, kmPerHari, ke + 1, total, hasil);
    }

    /** Total biaya servis secara rekursif (menjumlahkan elemen dari indeks ke akhir). */
    public static long totalBiaya(List<RiwayatServis> daftar, int indeks) {
        if (indeks >= daftar.size()) {
            return 0;
        }
        return daftar.get(indeks).getBiaya() + totalBiaya(daftar, indeks + 1);
    }
}
