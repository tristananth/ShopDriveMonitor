package shopdrive;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import javax.swing.SwingUtilities;
import shopdrive.data.PenyimpananCsv;
import shopdrive.model.Kendaraan;
import shopdrive.model.StatusServis;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;
import shopdrive.service.Waktu;
import shopdrive.ui.MainFrame;
import shopdrive.ui.Tema;

/**
 * Titik masuk program (method main).
 *   java -cp out shopdrive.Main             -> mode GUI
 *   java -cp out shopdrive.Main --console   -> mode teks (memakai predefined stream System.out/System.err)
 * Opsi tambahan: --tanggal=2026-11-16 untuk mensimulasikan tanggal hari ini.
 */
public class Main {
    public static final File FOLDER_DATA = new File(System.getProperty("data.dir", "data"));

    public static void main(String[] args) {
        boolean konsol = false;
        for (String arg : args) {
            if (arg.equals("--console")) {
                konsol = true;
            } else if (arg.startsWith("--tanggal=")) {
                try {
                    Waktu.setAcuan(LocalDate.parse(arg.substring("--tanggal=".length())));
                } catch (RuntimeException e) {
                    System.err.println("Format tanggal salah, gunakan yyyy-MM-dd. Memakai tanggal sistem.");
                }
            }
        }

        ManajerData data;
        try {
            data = new ManajerData(new PenyimpananCsv(FOLDER_DATA));
        } catch (IOException e) {
            System.err.println("Gagal membaca data: " + e.getMessage());
            return;
        }

        if (konsol) {
            jalankanKonsol(data);
        } else {
            final ManajerData dataFinal = data;
            SwingUtilities.invokeLater(() -> {
                Tema.pasang();
                new MainFrame(dataFinal, FOLDER_DATA).setVisible(true);
            });
        }
    }

    /** Mode teks: mencetak notifikasi servis ke konsol. */
    private static void jalankanKonsol(ManajerData data) {
        LocalDate hariIni = Waktu.hariIni();
        System.out.println("=== Monitoring Servis Berkala - Shop & Drive Cempaka Putih ===");
        System.out.println("Tanggal acuan: " + hariIni);
        for (Kendaraan k : PerhitunganServis.urutkanUrgensi(data.getKendaraan(), hariIni)) {
            if (PerhitunganServis.tentukanStatus(k, hariIni) != StatusServis.AMAN) {
                System.out.println(PerhitunganServis.pesanNotifikasi(k, hariIni));
            }
        }
        int[] r = PerhitunganServis.ringkasan(data.getKendaraan(), hariIni);
        System.out.println("Ringkasan -> Aman: " + r[0] + ", Segera: " + r[1] + ", Terlambat: " + r[2]);
    }
}
