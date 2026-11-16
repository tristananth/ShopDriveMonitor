package shopdrive.tools;

import java.io.File;
import java.time.LocalDate;
import shopdrive.data.BerkasUtil;
import shopdrive.data.PenyimpananCsv;
import shopdrive.model.*;
import shopdrive.service.*;

/** Uji fungsional sederhana tanpa GUI: java -cp out shopdrive.tools.UjiFungsi */
public class UjiFungsi {
    static int lulus = 0, gagal = 0;
    static void cek(String nama, boolean ok) {
        System.out.println((ok ? "[LULUS] " : "[GAGAL] ") + nama);
        if (ok) lulus++; else gagal++;
    }
    public static void main(String[] a) throws Exception {
        LocalDate h = LocalDate.of(2026, 11, 16);
        Waktu.setAcuan(h);
        Kendaraan aman = new Kendaraan("B 1 AA", "x", "0", "m", 2020, 10000, 9000, LocalDate.of(2026, 10, 1), 5000, 6);
        cek("T1 status AMAN", PerhitunganServis.tentukanStatus(aman, h) == StatusServis.AMAN);
        aman.setKmSaatIni(13700);   // sisa 300 km
        cek("T2 status SEGERA (sisa km <= 500)", PerhitunganServis.tentukanStatus(aman, h) == StatusServis.SEGERA);
        aman.setKmSaatIni(14200);   // sisa -200
        cek("T3 status TERLAMBAT (km lewat)", PerhitunganServis.tentukanStatus(aman, h) == StatusServis.TERLAMBAT);
        Kendaraan lama = new Kendaraan("B 2 AA", "x", "0", "m", 2020, 9100, 9000, LocalDate.of(2026, 4, 1), 5000, 6);
        cek("T4 status TERLAMBAT (tanggal lewat)", PerhitunganServis.tentukanStatus(lama, h) == StatusServis.TERLAMBAT);
        cek("T5 overloading tentukanStatus(int,long)", PerhitunganServis.tentukanStatus(1000, 100) == StatusServis.AMAN);
        cek("T6 checklist 47.350 -> 52.350 memuat oli & 10.000 km, tanpa 20.000 km",
            ChecklistServis.butirJatuhTempo(47350, 52350).contains("Ganti oli mesin")
            && !ChecklistServis.butirJatuhTempo(47350, 52350).contains("Ganti minyak rem"));
        cek("T7 proyeksi rekursif menghasilkan 5 jadwal", PerhitunganServis.proyeksi(aman, h, 5).size() == 5);
        String err = "";
        try { Validasi.parseAngka("abc", "Km"); } catch (DataTidakValidException e) { err = e.getMessage(); }
        cek("T8 validasi angka non-numerik ditolak", err.contains("angka"));
        err = "";
        try { Validasi.parseTanggal("31-02-2026", "Tgl"); } catch (DataTidakValidException e) { err = e.getMessage(); }
        cek("T9 validasi tanggal salah ditolak", err.contains("dd-MM-yyyy"));
        cek("T10 normalisasi nopol 'b1234kzr' -> 'B 1234 KZR'", Validasi.normalisasiNopol("b1234kzr").equals("B 1234 KZR"));

        File tmp = new File(System.getProperty("java.io.tmpdir"), "uji_sd_" + System.nanoTime());
        tmp.mkdirs();
        BerkasUtil.salinBerkas(new File("data/kendaraan.csv"), new File(tmp, "kendaraan.csv"));
        BerkasUtil.salinBerkas(new File("data/riwayat.csv"), new File(tmp, "riwayat.csv"));
        ManajerData m = new ManajerData(new PenyimpananCsv(tmp));
        cek("T11 muat CSV: 10 kendaraan, 13 riwayat", m.getKendaraan().size() == 10 && m.getRiwayat().size() == 13);
        m.catatServis(new RiwayatServis("B 5123 WQA", h, 19000, "Servis Berkala 5.000 km", 500000, "uji"));
        cek("T12 catat servis -> B 5123 WQA kembali AMAN",
            PerhitunganServis.tentukanStatus(m.cari("B 5123 WQA"), h) == StatusServis.AMAN);
        ManajerData m2 = new ManajerData(new PenyimpananCsv(tmp));
        cek("T13 data persisten setelah dimuat ulang", m2.cari("B 5123 WQA").getKmServisTerakhir() == 19000);
        err = "";
        try { m.perbaruiJarakTempuh("B 5123 WQA", 100); } catch (DataTidakValidException e) { err = e.getMessage(); }
        cek("T14 odometer lebih kecil ditolak", err.contains("tidak boleh lebih kecil"));
        File bk = BerkasUtil.backupData(tmp);
        cek("T15 backup byte-stream membuat 2 file csv", bk.listFiles().length == 2);
        System.out.println("Lulus " + lulus + ", gagal " + gagal);
    }
}
