package shopdrive.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import shopdrive.model.JadwalProyeksi;
import shopdrive.model.Kendaraan;
import shopdrive.model.StatusServis;
import shopdrive.service.ChecklistServis;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;
import shopdrive.service.Waktu;

/** Tab Proyeksi & Checklist: perkiraan jadwal servis berikutnya (rekursif) dan pekerjaan yang jatuh tempo. */
public class ProyeksiPanel extends JPanel {
    private static final DateTimeFormatter TGL = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    private final ManajerData data;
    private final JComboBox<Kendaraan> cmbKendaraan = new JComboBox<>();
    private final JSpinner spJumlah = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
    private final JLabel lblInfo = new JLabel(" ");
    private final DefaultTableModel model = Tema.modelTabel("Servis ke-", "Perkiraan Km", "Perkiraan Tanggal", "Jumlah Pekerjaan");
    private final JTable tabel = new JTable(model);
    private final JTextArea areaDetail = new JTextArea();
    private List<JadwalProyeksi> hasil = new java.util.ArrayList<>();
    private boolean sedangIsi = false;

    public ProyeksiPanel(ManajerData data) {
        this.data = data;
        setLayout(new BorderLayout(16, 10));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JPanel atas = new JPanel(new BorderLayout(0, 6));
        atas.setOpaque(false);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(Tema.judulBagian("Proyeksi Jadwal Servis"));
        bar.add(new JLabel("   Kendaraan:"));
        cmbKendaraan.setPreferredSize(new Dimension(290, 28));
        bar.add(cmbKendaraan);
        bar.add(new JLabel("Jumlah proyeksi:"));
        bar.add(spJumlah);
        JButton btn = Tema.tombol("Hitung Proyeksi", Tema.BIRU);
        btn.addActionListener(e -> hitung());
        bar.add(btn);
        atas.add(bar, BorderLayout.NORTH);
        lblInfo.setFont(Tema.FONT_BIASA);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 0));
        atas.add(lblInfo, BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        // Kiri: tabel proyeksi + detail pekerjaan
        Tema.gayaTabel(tabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(0).setCellRenderer(new Tema.RendererKanan());
        tabel.getColumnModel().getColumn(1).setCellRenderer(new Tema.RendererKanan());
        tabel.getColumnModel().getColumn(3).setCellRenderer(new Tema.RendererKanan());
        areaDetail.setEditable(false);
        areaDetail.setFont(Tema.FONT_BIASA);
        areaDetail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JPanel panelDetail = new JPanel(new BorderLayout(0, 4));
        panelDetail.setOpaque(false);
        panelDetail.add(Tema.judulBagian("Pekerjaan yang jatuh tempo pada servis terpilih"), BorderLayout.NORTH);
        panelDetail.add(new JScrollPane(areaDetail), BorderLayout.CENTER);
        JSplitPane kiri = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabel), panelDetail);
        kiri.setResizeWeight(0.5);
        kiri.setBorder(null);
        kiri.setOpaque(false);

        // Kanan: tabel acuan interval (isi dari array 2D)
        DefaultTableModel modelAcuan = Tema.modelTabel("Pekerjaan", "Setiap (km)");
        for (int i = 0; i < ChecklistServis.jumlahButir(); i++) {
            modelAcuan.addRow(new Object[]{ChecklistServis.namaButir(i), PerhitunganServis.formatKm(ChecklistServis.intervalButir(i))});
        }
        JTable tabelAcuan = new JTable(modelAcuan);
        Tema.gayaTabel(tabelAcuan);
        Tema.lebarKolom(tabelAcuan, 260, 80);
        tabelAcuan.getColumnModel().getColumn(1).setCellRenderer(new Tema.RendererKanan());
        JPanel kanan = new JPanel(new BorderLayout(0, 4));
        kanan.setOpaque(false);
        kanan.setPreferredSize(new Dimension(390, 0));
        kanan.add(Tema.judulBagian("Acuan Interval Pekerjaan Berkala"), BorderLayout.NORTH);
        kanan.add(new JScrollPane(tabelAcuan), BorderLayout.CENTER);
        JLabel cat = new JLabel("<html>Acuan umum; sesuaikan dengan buku servis tiap mobil.</html>");
        cat.setFont(Tema.FONT_BIASA);
        cat.setForeground(Tema.TEKS_REDUP);
        kanan.add(cat, BorderLayout.SOUTH);

        add(kiri, BorderLayout.CENTER);
        add(kanan, BorderLayout.EAST);

        cmbKendaraan.addActionListener(e -> { if (!sedangIsi) hitung(); });
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanDetail();
            }
        });
        refresh();
    }

    public void refresh() {
        sedangIsi = true;
        Kendaraan lama = (Kendaraan) cmbKendaraan.getSelectedItem();
        cmbKendaraan.removeAllItems();
        for (Kendaraan k : data.getKendaraan()) {
            cmbKendaraan.addItem(k);
        }
        for (int i = 0; i < cmbKendaraan.getItemCount(); i++) {
            if (lama != null && cmbKendaraan.getItemAt(i).getNopol().equals(lama.getNopol())) {
                cmbKendaraan.setSelectedIndex(i);
                break;
            }
        }
        sedangIsi = false;
        hitung();
    }

    public void pilihKendaraan(int indeks) {
        cmbKendaraan.setSelectedIndex(indeks);
    }

    private void hitung() {
        model.setRowCount(0);
        areaDetail.setText("");
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        if (k == null) {
            lblInfo.setText("Belum ada kendaraan.");
            return;
        }
        LocalDate hariIni = Waktu.hariIni();
        StatusServis s = PerhitunganServis.tentukanStatus(k, hariIni);
        lblInfo.setText("<html><b>" + k.getNopol() + "</b> &nbsp;|&nbsp; status: <b>" + s.getLabel()
                + "</b> &nbsp;|&nbsp; servis terakhir " + k.getTglServisTerakhir().format(TGL) + " pada "
                + PerhitunganServis.formatKm(k.getKmServisTerakhir()) + " km &nbsp;|&nbsp; laju rata-rata "
                + String.format(Locale.forLanguageTag("id-ID"), "%.1f", PerhitunganServis.rataKmPerHari(k, hariIni))
                + " km/hari &nbsp;|&nbsp; <i>asumsi: setiap servis dilakukan tepat pada jatuh temponya</i></html>");
        int jumlah = (Integer) spJumlah.getValue();
        hasil = PerhitunganServis.proyeksi(k, hariIni, jumlah);
        for (JadwalProyeksi j : hasil) {
            model.addRow(new Object[]{j.getUrutan(), PerhitunganServis.formatKm(j.getPerkiraanKm()),
                j.getPerkiraanTanggal().format(TGL), j.getPekerjaan().size()});
        }
        if (!hasil.isEmpty()) {
            tabel.setRowSelectionInterval(0, 0);
        }
    }

    private void tampilkanDetail() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= hasil.size()) {
            return;
        }
        JadwalProyeksi j = hasil.get(baris);
        StringBuilder sb = new StringBuilder();
        sb.append("Servis ke-").append(j.getUrutan()).append(" (± ").append(PerhitunganServis.formatKm(j.getPerkiraanKm()))
          .append(" km, ").append(j.getPerkiraanTanggal().format(TGL)).append("):\n");
        int no = 1;
        for (String p : j.getPekerjaan()) {
            sb.append("  ").append(no++).append(". ").append(p).append('\n');
        }
        areaDetail.setText(sb.toString());
        areaDetail.setCaretPosition(0);
    }
}
