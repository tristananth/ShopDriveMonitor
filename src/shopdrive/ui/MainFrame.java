package shopdrive.ui;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;

/** Jendela utama aplikasi. */
public class MainFrame extends JFrame {
    private final AplikasiPanel isi;

    public MainFrame(ManajerData data, File folderData) {
        super("Monitoring Servis Berkala - Shop & Drive Cempaka Putih");
        isi = new AplikasiPanel(data, folderData);
        setContentPane(isi);
        setJMenuBar(buatMenu());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new java.awt.Dimension(1100, 680));
        setLocationRelativeTo(null);
        // Notifikasi otomatis saat aplikasi dibuka
        SwingUtilities.invokeLater(() -> {
            if (isi.getDashboard().jumlahPerluPerhatian() > 0) {
                isi.tampilkanNotifikasi();
            }
        });
    }

    private JMenuBar buatMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Berkas");
        JMenuItem backup = new JMenuItem("Backup Data (.csv)");
        JMenuItem ekspor = new JMenuItem("Ekspor Pengingat Pelanggan (.txt)");
        JMenuItem keluar = new JMenuItem("Keluar");
        backup.addActionListener(e -> isi.getDashboard().backupData());
        ekspor.addActionListener(e -> isi.getDashboard().eksporPengingat());
        keluar.addActionListener(e -> dispose());
        file.add(backup);
        file.add(ekspor);
        file.addSeparator();
        file.add(keluar);

        JMenu bantuan = new JMenu("Bantuan");
        JMenuItem aturan = new JMenuItem("Aturan Status Servis");
        JMenuItem tentang = new JMenuItem("Tentang Aplikasi");
        aturan.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "<html><body style='width:380px'><b>Aturan status servis</b><br><br>"
                + "<b>Terlambat</b>: jarak tempuh melewati batas interval ATAU tanggal servis berikutnya sudah lewat.<br>"
                + "<b>Segera Servis</b>: sisa jarak ≤ " + PerhitunganServis.AMBANG_KM_SEGERA
                + " km ATAU sisa waktu ≤ " + PerhitunganServis.AMBANG_HARI_SEGERA + " hari.<br>"
                + "<b>Aman</b>: selain kondisi di atas.<br><br>"
                + "Batas jarak = km servis terakhir + interval km.<br>"
                + "Batas waktu = tanggal servis terakhir + interval bulan.</body></html>",
                "Aturan Status Servis", JOptionPane.INFORMATION_MESSAGE));
        tentang.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistem Monitoring Servis Berkala Mobil Berbasis Java GUI\n"
                + "Studi kasus: Shop & Drive Cempaka Putih, Jakarta Pusat\n"
                + "Tugas Besar Dasar Pemrograman Java - Tema 3 (Kelompok 9)",
                "Tentang", JOptionPane.INFORMATION_MESSAGE));
        bantuan.add(aturan);
        bantuan.add(tentang);

        bar.add(file);
        bar.add(bantuan);
        return bar;
    }
}
