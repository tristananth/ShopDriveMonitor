package shopdrive.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import shopdrive.model.Kendaraan;
import shopdrive.model.RiwayatServis;
import shopdrive.service.Validasi;

/**
 * Implementasi Penyimpanan berbasis file CSV (pemisah titik koma).
 * Materi: Stream & File (Character Stream: FileReader, FileWriter, BufferedReader, BufferedWriter),
 * try-with-resources, String handling (split, trim), Exception handling,
 * break/continue pada perulangan.
 */
public class PenyimpananCsv implements Penyimpanan {
    private static final String HEADER_KENDARAAN =
            "nopol;pemilik;noHp;merkModel;tahun;kmSaatIni;kmServisTerakhir;tglServisTerakhir;intervalKm;intervalBulan";
    private static final String HEADER_RIWAYAT = "nopol;tanggal;km;jenisServis;biaya;catatan";

    private final File berkasKendaraan;
    private final File berkasRiwayat;

    public PenyimpananCsv(File folderData) {
        this.berkasKendaraan = new File(folderData, "kendaraan.csv");
        this.berkasRiwayat = new File(folderData, "riwayat.csv");
    }

    public File getBerkasKendaraan() { return berkasKendaraan; }
    public File getBerkasRiwayat() { return berkasRiwayat; }

    @Override
    public List<Kendaraan> muatKendaraan() throws IOException {
        List<Kendaraan> hasil = new ArrayList<>();
        if (!berkasKendaraan.exists()) {
            return hasil;                       // belum ada data: daftar kosong
        }
        try (BufferedReader in = new BufferedReader(new FileReader(berkasKendaraan, StandardCharsets.UTF_8))) {
            String baris;
            int nomor = 0;
            while ((baris = in.readLine()) != null) {
                nomor++;
                if (nomor == 1 || baris.trim().isEmpty()) {
                    continue;                   // lewati header & baris kosong
                }
                String[] f = baris.split(";", -1);
                try {
                    hasil.add(new Kendaraan(f[0], f[1], f[2], f[3],
                            Integer.parseInt(f[4]), Integer.parseInt(f[5]), Integer.parseInt(f[6]),
                            LocalDate.parse(f[7]), Integer.parseInt(f[8]), Integer.parseInt(f[9])));
                } catch (RuntimeException e) {
                    System.err.println("[PERINGATAN] kendaraan.csv baris " + nomor + " dilewati: " + e);
                }
            }
        }
        return hasil;
    }

    @Override
    public void simpanKendaraan(List<Kendaraan> daftar) throws IOException {
        berkasKendaraan.getParentFile().mkdirs();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(berkasKendaraan, StandardCharsets.UTF_8))) {
            out.write(HEADER_KENDARAAN);
            out.newLine();
            for (Kendaraan k : daftar) {
                out.write(String.join(";",
                        Validasi.bersihkanCsv(k.getNopol()), Validasi.bersihkanCsv(k.getPemilik()),
                        Validasi.bersihkanCsv(k.getNoHp()), Validasi.bersihkanCsv(k.getMerkModel()),
                        String.valueOf(k.getTahun()), String.valueOf(k.getKmSaatIni()),
                        String.valueOf(k.getKmServisTerakhir()), k.getTglServisTerakhir().toString(),
                        String.valueOf(k.getIntervalKm()), String.valueOf(k.getIntervalBulan())));
                out.newLine();
            }
        }
    }

    @Override
    public List<RiwayatServis> muatRiwayat() throws IOException {
        List<RiwayatServis> hasil = new ArrayList<>();
        if (!berkasRiwayat.exists()) {
            return hasil;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(berkasRiwayat, StandardCharsets.UTF_8))) {
            String baris;
            int nomor = 0;
            while ((baris = in.readLine()) != null) {
                nomor++;
                if (nomor == 1 || baris.trim().isEmpty()) {
                    continue;
                }
                String[] f = baris.split(";", -1);
                try {
                    hasil.add(new RiwayatServis(f[0], LocalDate.parse(f[1]), Integer.parseInt(f[2]),
                            f[3], Long.parseLong(f[4]), f.length > 5 ? f[5] : ""));
                } catch (RuntimeException e) {
                    System.err.println("[PERINGATAN] riwayat.csv baris " + nomor + " dilewati: " + e);
                }
            }
        }
        return hasil;
    }

    @Override
    public void simpanRiwayat(List<RiwayatServis> daftar) throws IOException {
        berkasRiwayat.getParentFile().mkdirs();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(berkasRiwayat, StandardCharsets.UTF_8))) {
            out.write(HEADER_RIWAYAT);
            out.newLine();
            for (RiwayatServis r : daftar) {
                out.write(String.join(";", Validasi.bersihkanCsv(r.getNopol()), r.getTanggal().toString(),
                        String.valueOf(r.getKm()), Validasi.bersihkanCsv(r.getJenisServis()),
                        String.valueOf(r.getBiaya()), Validasi.bersihkanCsv(r.getCatatan())));
                out.newLine();
            }
        }
    }
}
