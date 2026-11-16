package shopdrive.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import shopdrive.model.DataTidakValidException;
import shopdrive.service.ManajerData;
import shopdrive.service.Validasi;
import shopdrive.service.Waktu;

/** Panel utama aplikasi: header, tab-tab fitur, dan status bar. Dipisah dari JFrame agar mudah diuji. */
public class AplikasiPanel extends JPanel {
    private static final DateTimeFormatter TGL_PANJANG =
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    private final ManajerData data;
    private final DashboardPanel dashboard;
    private final KendaraanPanel kendaraan;
    private final RiwayatPanel riwayat;
    private final ProyeksiPanel proyeksi;
    private final JTabbedPane tab = new JTabbedPane();
    private final JTextField txtTanggal = new JTextField(9);
    private final JLabel lblHariIni = new JLabel();
    private final JButton btnNotif = Tema.tombol("Notifikasi (0)", Tema.MERAH);
    private final JLabel lblStatus = new JLabel(" ");

    public AplikasiPanel(ManajerData data, File folderData) {
        this.data = data;
        setLayout(new BorderLayout());
        dashboard = new DashboardPanel(data, folderData);
        kendaraan = new KendaraanPanel(data, this::refreshSemua);
        riwayat = new RiwayatPanel(data, this::refreshSemua);
        proyeksi = new ProyeksiPanel(data);

        add(buatHeader(), BorderLayout.NORTH);
        tab.setFont(Tema.FONT_TEBAL);
        tab.addTab("Dashboard Monitoring", dashboard);
        tab.addTab("Data Kendaraan", kendaraan);
        tab.addTab("Riwayat Servis", riwayat);
        tab.addTab("Proyeksi & Checklist", proyeksi);
        add(tab, BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GARIS), BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        lblStatus.setFont(Tema.FONT_BIASA);
        lblStatus.setForeground(Tema.TEKS_REDUP);
        status.add(lblStatus, BorderLayout.WEST);
        add(status, BorderLayout.SOUTH);

        data.tambahPendengar(this::segarkanTampilan);
        // Pemeriksaan otomatis tiap 60 detik: tanggal berganti / data berubah -> status dihitung ulang.
        new Timer(60_000, e -> {
            if (!Waktu.isSimulasi()) {
                refreshSemua();
            }
        }).start();
        segarkanTampilan();
    }

    private JPanel buatHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Tema.NAVY);
        h.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel kiri = new JPanel(new BorderLayout());
        kiri.setOpaque(false);
        JLabel judul = new JLabel("Sistem Monitoring Servis Berkala Mobil");
        judul.setFont(Tema.FONT_JUDUL);
        judul.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Shop & Drive Cempaka Putih  •  Jl. Cempaka Putih Raya No. 19, Jakarta Pusat");
        sub.setFont(Tema.FONT_BIASA);
        sub.setForeground(new Color(0xBF, 0xCC, 0xDF));
        kiri.add(judul, BorderLayout.NORTH);
        kiri.add(sub, BorderLayout.SOUTH);
        h.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        kanan.setOpaque(false);
        JPanel barisAtas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barisAtas.setOpaque(false);
        lblHariIni.setForeground(Color.WHITE);
        lblHariIni.setFont(Tema.FONT_TEBAL);
        JLabel l = new JLabel("Tanggal acuan:");
        l.setForeground(new Color(0xBF, 0xCC, 0xDF));
        txtTanggal.setPreferredSize(new Dimension(95, 28));
        JButton btnTerapkan = Tema.tombol("Terapkan", Tema.BIRU);
        JButton btnHariIni = Tema.tombol("Hari ini", Tema.BIRU);
        btnTerapkan.addActionListener(e -> terapkanTanggal());
        btnHariIni.addActionListener(e -> {
            Waktu.setAcuan(null);
            refreshSemua();
        });
        btnNotif.addActionListener(e -> tampilkanNotifikasi());
        barisAtas.add(lblHariIni);
        kanan.add(l);
        kanan.add(txtTanggal);
        kanan.add(btnTerapkan);
        kanan.add(btnHariIni);
        kanan.add(btnNotif);
        JPanel kolomKanan = new JPanel(new BorderLayout(0, 2));
        kolomKanan.setOpaque(false);
        kolomKanan.add(barisAtas, BorderLayout.NORTH);
        kolomKanan.add(kanan, BorderLayout.CENTER);
        h.add(kolomKanan, BorderLayout.EAST);
        return h;
    }

    private void terapkanTanggal() {
        try {
            LocalDate t = Validasi.parseTanggal(txtTanggal.getText(), "Tanggal acuan");
            Waktu.setAcuan(t);
            refreshSemua();
        } catch (DataTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Menyegarkan seluruh tab (dipanggil saat data atau tanggal acuan berubah). */
    public void refreshSemua() {
        segarkanTampilan();
    }

    private void segarkanTampilan() {
        LocalDate hariIni = Waktu.hariIni();
        txtTanggal.setText(hariIni.format(Validasi.FORMAT_INPUT));
        lblHariIni.setText(hariIni.format(TGL_PANJANG) + (Waktu.isSimulasi() ? " (simulasi)  " : "  "));
        dashboard.refresh();
        kendaraan.refresh();
        riwayat.refresh();
        proyeksi.refresh();
        int n = dashboard.jumlahPerluPerhatian();
        btnNotif.setText("Notifikasi (" + n + ")");
        btnNotif.setBackground(n > 0 ? Tema.MERAH : Tema.HIJAU);
        lblStatus.setText("Data tersimpan otomatis di folder data/ (CSV)  •  Status dihitung ulang tiap data atau tanggal berubah");
    }

    public void tampilkanNotifikasi() {
        if (dashboard.jumlahPerluPerhatian() == 0) {
            JOptionPane.showMessageDialog(this, "Semua kendaraan masih dalam jadwal servis yang aman.",
                    "Notifikasi Servis", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, dashboard.teksPopup(), "Notifikasi Servis Berkala",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public DashboardPanel getDashboard() { return dashboard; }
    public KendaraanPanel getKendaraan() { return kendaraan; }
    public ProyeksiPanel getProyeksi() { return proyeksi; }
    public JTabbedPane getTab() { return tab; }
    public ManajerData getData() { return data; }
}
