package shopdrive.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import shopdrive.data.BerkasUtil;
import shopdrive.model.Kendaraan;
import shopdrive.model.StatusServis;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;
import shopdrive.service.Waktu;

/** Tab Dashboard: ringkasan status, tabel monitoring semua mobil, dan notifikasi servis otomatis. */
public class DashboardPanel extends JPanel {
    private static final DateTimeFormatter TGL = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private final ManajerData data;
    private final File folderData;
    private final JLabel lblTotal = new JLabel("0");
    private final JLabel lblAman = new JLabel("0");
    private final JLabel lblSegera = new JLabel("0");
    private final JLabel lblTerlambat = new JLabel("0");
    private final DefaultTableModel model = Tema.modelTabel("No. Polisi", "Pemilik", "Kendaraan", "Km Saat Ini",
            "Sisa Km", "Batas Waktu", "Sisa Hari", "Estimasi Servis", "Status");
    private final JTable tabel = new JTable(model);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{"Semua Status", "Aman", "Segera Servis", "Terlambat"});
    private final JTextField txtCari = new JTextField(14);
    private final DefaultListModel<String> modelNotif = new DefaultListModel<>();
    private final JList<String> daftarNotif = new JList<>(modelNotif);
    private final JLabel lblJudulNotif = Tema.judulBagian("Notifikasi Servis Otomatis");

    public DashboardPanel(ManajerData data, File folderData) {
        this.data = data;
        this.folderData = folderData;
        setLayout(new BorderLayout(0, 12));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        // Kartu ringkasan
        JPanel kartu = new JPanel(new GridLayout(1, 4, 14, 0));
        kartu.setOpaque(false);
        kartu.add(Tema.kartu("Total kendaraan terdata", lblTotal, Tema.BIRU));
        kartu.add(Tema.kartu("Aman", lblAman, Tema.HIJAU));
        kartu.add(Tema.kartu("Segera servis (≤ 500 km / 14 hari)", lblSegera, Tema.AMBER));
        kartu.add(Tema.kartu("Terlambat servis", lblTerlambat, Tema.MERAH));
        add(kartu, BorderLayout.NORTH);

        // Tabel + filter
        JPanel atas = new JPanel(new BorderLayout(0, 6));
        atas.setOpaque(false);
        JPanel barFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barFilter.setOpaque(false);
        barFilter.add(Tema.judulBagian("Monitoring Kendaraan"));
        barFilter.add(new JLabel("   Filter:"));
        cmbFilter.setPreferredSize(new java.awt.Dimension(150, 28));
        barFilter.add(cmbFilter);
        barFilter.add(new JLabel("Cari:"));
        barFilter.add(txtCari);
        atas.add(barFilter, BorderLayout.NORTH);

        Tema.gayaTabel(tabel);
        tabel.setRowHeight(24);
        Tema.lebarKolom(tabel, 95, 120, 190, 90, 80, 100, 80, 110, 110);
        tabel.getColumnModel().getColumn(3).setCellRenderer(new Tema.RendererKanan());
        tabel.getColumnModel().getColumn(4).setCellRenderer(new Tema.RendererSisa());
        tabel.getColumnModel().getColumn(6).setCellRenderer(new Tema.RendererSisa());
        tabel.getColumnModel().getColumn(8).setCellRenderer(new Tema.RendererStatus());
        atas.add(new JScrollPane(tabel), BorderLayout.CENTER);

        // Notifikasi
        JPanel bawah = new JPanel(new BorderLayout(0, 4));
        bawah.setOpaque(false);
        JPanel barNotif = new JPanel(new BorderLayout());
        barNotif.setOpaque(false);
        barNotif.add(lblJudulNotif, BorderLayout.WEST);
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        JButton btnEkspor = Tema.tombol("Ekspor Pengingat Pelanggan (.txt)", Tema.BIRU);
        JButton btnBackup = Tema.tombol("Backup Data (.csv)", Tema.NAVY);
        btnEkspor.addActionListener(e -> eksporPengingat());
        btnBackup.addActionListener(e -> backupData());
        tombol.add(btnEkspor);
        tombol.add(btnBackup);
        barNotif.add(tombol, BorderLayout.EAST);
        bawah.add(barNotif, BorderLayout.NORTH);
        daftarNotif.setCellRenderer(new RendererNotifikasi());
        daftarNotif.setFixedCellHeight(24);
        bawah.add(new JScrollPane(daftarNotif), BorderLayout.CENTER);

        JSplitPane belah = new JSplitPane(JSplitPane.VERTICAL_SPLIT, atas, bawah);
        belah.setResizeWeight(0.62);
        belah.setBorder(null);
        belah.setOpaque(false);
        add(belah, BorderLayout.CENTER);

        cmbFilter.addActionListener(e -> refresh());
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        refresh();
    }

    /** Menghitung ulang semua tampilan berdasarkan data dan tanggal acuan saat ini. */
    public void refresh() {
        LocalDate hariIni = Waktu.hariIni();
        List<Kendaraan> semua = data.getKendaraan();

        int[] r = PerhitunganServis.ringkasan(semua, hariIni);
        lblTotal.setText(String.valueOf(semua.size()));
        lblAman.setText(String.valueOf(r[StatusServis.AMAN.getPrioritas()]));
        lblSegera.setText(String.valueOf(r[StatusServis.SEGERA.getPrioritas()]));
        lblTerlambat.setText(String.valueOf(r[StatusServis.TERLAMBAT.getPrioritas()]));

        model.setRowCount(0);
        String kata = txtCari.getText().trim().toLowerCase();
        int pilihanFilter = cmbFilter.getSelectedIndex();   // 0 = semua, 1..3 = status
        modelNotif.clear();

        for (Kendaraan k : PerhitunganServis.urutkanUrgensi(semua, hariIni)) {
            StatusServis s = PerhitunganServis.tentukanStatus(k, hariIni);
            if (s != StatusServis.AMAN) {
                modelNotif.addElement(PerhitunganServis.pesanNotifikasi(k, hariIni));
            }
            boolean lolosFilter = (pilihanFilter == 0) || (s.ordinal() == pilihanFilter - 1);
            boolean lolosCari = kata.isEmpty()
                    || k.getNopol().toLowerCase().contains(kata)
                    || k.getPemilik().toLowerCase().contains(kata)
                    || k.getMerkModel().toLowerCase().contains(kata);
            if (!lolosFilter || !lolosCari) {
                continue;
            }
            model.addRow(new Object[]{
                k.getNopol(), k.getPemilik(), k.getMerkModel(),
                PerhitunganServis.formatKm(k.getKmSaatIni()),
                PerhitunganServis.formatKm(PerhitunganServis.sisaKm(k)),
                PerhitunganServis.jatuhTempoTanggal(k).format(TGL),
                PerhitunganServis.sisaHari(k, hariIni),
                PerhitunganServis.estimasiServis(k, hariIni).format(TGL),
                s
            });
        }
        if (modelNotif.isEmpty()) {
            modelNotif.addElement("[AMAN] Tidak ada kendaraan yang perlu diservis saat ini.");
        }
        lblJudulNotif.setText("Notifikasi Servis Otomatis (" + (r[1] + r[2]) + " perlu perhatian) - acuan "
                + hariIni.format(TGL));
    }

    /** Jumlah kendaraan berstatus SEGERA + TERLAMBAT. */
    public int jumlahPerluPerhatian() {
        int[] r = PerhitunganServis.ringkasan(data.getKendaraan(), Waktu.hariIni());
        return r[1] + r[2];
    }

    /** Teks HTML untuk popup notifikasi. */
    public String teksPopup() {
        LocalDate hariIni = Waktu.hariIni();
        StringBuilder sb = new StringBuilder("<html><body style='width:520px'>");
        sb.append("<b>Per ").append(hariIni.format(TGL)).append(", ").append(jumlahPerluPerhatian())
          .append(" kendaraan perlu perhatian:</b><br><br>");
        for (Kendaraan k : PerhitunganServis.urutkanUrgensi(data.getKendaraan(), hariIni)) {
            StatusServis s = PerhitunganServis.tentukanStatus(k, hariIni);
            if (s == StatusServis.AMAN) {
                continue;
            }
            String warna = (s == StatusServis.TERLAMBAT) ? "#C62828" : "#B26A00";
            sb.append("<span style='color:").append(warna).append("'><b>").append(s.getLabel().toUpperCase())
              .append("</b></span> &nbsp;").append(k.getNopol()).append(" - ").append(k.getMerkModel())
              .append(" (").append(k.getPemilik()).append(")<br>&nbsp;&nbsp;&nbsp;")
              .append(PerhitunganServis.alasan(k, hariIni)).append("<br>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    // ---------- Aksi berkas ----------
    public void eksporPengingat() {
        LocalDate hariIni = Waktu.hariIni();
        List<String> baris = new ArrayList<>();
        baris.add("PENGINGAT SERVIS BERKALA - SHOP & DRIVE CEMPAKA PUTIH");
        baris.add("Tanggal acuan: " + hariIni);
        baris.add("");
        int nomor = 0;
        for (Kendaraan k : PerhitunganServis.urutkanUrgensi(data.getKendaraan(), hariIni)) {
            if (PerhitunganServis.tentukanStatus(k, hariIni) == StatusServis.AMAN) {
                continue;
            }
            nomor++;
            baris.add(nomor + ". " + k.getPemilik() + " (" + k.getNoHp() + ")");
            baris.add("   " + PerhitunganServis.pesanUntukPelanggan(k, hariIni));
            baris.add("");
        }
        File tujuan = new File(folderData, "export/pengingat_" + hariIni + ".txt");
        try {
            BerkasUtil.tulisBaris(tujuan, baris);
            JOptionPane.showMessageDialog(this, "Pengingat untuk " + nomor + " pelanggan disimpan di:\n"
                    + tujuan.getPath(), "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menulis file: " + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void backupData() {
        try {
            File hasil = BerkasUtil.backupData(folderData);
            JOptionPane.showMessageDialog(this, "Backup berhasil disimpan di:\n" + hasil.getPath(),
                    "Backup Data", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Backup gagal: " + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Memberi warna pada baris notifikasi sesuai tingkat urgensi. */
    private static class RendererNotifikasi implements javax.swing.ListCellRenderer<String> {
        public Component getListCellRendererComponent(JList<? extends String> list, String nilai, int indeks,
                                                      boolean dipilih, boolean fokus) {
            JLabel l = new JLabel(nilai);
            l.setOpaque(true);
            l.setFont(Tema.FONT_BIASA);
            l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            Color fg = Tema.HIJAU;
            if (nilai.startsWith("[TERLAMBAT]")) {
                fg = Tema.MERAH;
            } else if (nilai.startsWith("[SEGERA]")) {
                fg = new Color(0xB2, 0x6A, 0x00);
            }
            l.setForeground(dipilih ? Color.WHITE : fg);
            l.setBackground(dipilih ? Tema.BIRU : (indeks % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xF9, 0xFB)));
            return l;
        }
    }
}
