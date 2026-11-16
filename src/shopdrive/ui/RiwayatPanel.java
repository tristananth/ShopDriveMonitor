package shopdrive.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import shopdrive.model.DataTidakValidException;
import shopdrive.model.Kendaraan;
import shopdrive.model.RiwayatServis;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;
import shopdrive.service.Validasi;
import shopdrive.service.Waktu;

/** Tab Riwayat Servis: mencatat servis yang baru selesai dan melihat histori per kendaraan. */
public class RiwayatPanel extends JPanel {
    private static final String SEMUA = "Semua kendaraan";

    private final ManajerData data;
    private final Runnable setelahUbah;

    private final JComboBox<Kendaraan> cmbKendaraan = new JComboBox<>();
    private final JTextField txtTanggal = new JTextField();
    private final JTextField txtKm = new JTextField();
    private final JComboBox<String> cmbJenis = new JComboBox<>(new String[]{
        "Servis Berkala 5.000 km", "Servis Berkala 10.000 km", "Servis Berkala 20.000 km",
        "Servis Berkala 40.000 km", "Ganti Oli Saja", "Servis Lainnya"});
    private final JTextField txtBiaya = new JTextField();
    private final JTextField txtCatatan = new JTextField();

    private final JComboBox<Object> cmbFilter = new JComboBox<>();
    private final DefaultTableModel model = Tema.modelTabel("Tanggal", "No. Polisi", "Km", "Jenis Servis", "Biaya", "Catatan");
    private final JTable tabel = new JTable(model);
    private final JLabel lblTotal = new JLabel(" ");
    private boolean sedangIsi = false;

    public RiwayatPanel(ManajerData data, Runnable setelahUbah) {
        this.data = data;
        this.setelahUbah = setelahUbah;
        setLayout(new BorderLayout(16, 0));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JPanel kiri = new JPanel(new BorderLayout(0, 8));
        kiri.setBackground(java.awt.Color.WHITE);
        kiri.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.GARIS),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        kiri.setPreferredSize(new Dimension(340, 0));
        kiri.add(Tema.judulBagian("Catat Servis Selesai"), BorderLayout.NORTH);

        JPanel form = Tema.panelForm();
        Tema.barisForm(form, 0, "Kendaraan", cmbKendaraan);
        Tema.barisForm(form, 1, "Tanggal servis", txtTanggal);
        Tema.barisForm(form, 2, "Odometer (km)", txtKm);
        Tema.barisForm(form, 3, "Jenis servis", cmbJenis);
        Tema.barisForm(form, 4, "Biaya (Rp)", txtBiaya);
        Tema.barisForm(form, 5, "Catatan", txtCatatan);
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(form, BorderLayout.NORTH);
        kiri.add(pembungkus, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(0, 6));
        bawah.setOpaque(false);
        JButton btnCatat = Tema.tombol("Catat Servis", Tema.HIJAU);
        btnCatat.addActionListener(e -> aksiCatat());
        JLabel info = new JLabel("<html><body style='width:290px'>Mencatat servis akan memperbarui km &amp; tanggal servis terakhir kendaraan, sehingga status di Dashboard dihitung ulang.</body></html>");
        info.setFont(Tema.FONT_BIASA);
        info.setForeground(Tema.TEKS_REDUP);
        bawah.add(btnCatat, BorderLayout.NORTH);
        bawah.add(info, BorderLayout.CENTER);
        kiri.add(bawah, BorderLayout.SOUTH);
        add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new BorderLayout(0, 6));
        kanan.setOpaque(false);
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        atas.setOpaque(false);
        atas.add(Tema.judulBagian("Riwayat Servis"));
        atas.add(new JLabel("   Tampilkan:"));
        cmbFilter.setPreferredSize(new Dimension(240, 28));
        atas.add(cmbFilter);
        kanan.add(atas, BorderLayout.NORTH);
        Tema.gayaTabel(tabel);
        Tema.lebarKolom(tabel, 100, 100, 80, 170, 110, 260);
        tabel.getColumnModel().getColumn(2).setCellRenderer(new Tema.RendererKanan());
        tabel.getColumnModel().getColumn(4).setCellRenderer(new Tema.RendererKanan());
        kanan.add(new JScrollPane(tabel), BorderLayout.CENTER);
        lblTotal.setFont(Tema.FONT_TEBAL);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        kanan.add(lblTotal, BorderLayout.SOUTH);
        add(kanan, BorderLayout.CENTER);

        cmbFilter.addActionListener(e -> { if (!sedangIsi) tampilkanTabel(); });
        cmbKendaraan.addActionListener(e -> { if (!sedangIsi) isiOtomatis(); });
        refresh();
    }

    public void refresh() {
        sedangIsi = true;
        Kendaraan lamaForm = (Kendaraan) cmbKendaraan.getSelectedItem();
        Object lamaFilter = cmbFilter.getSelectedItem();
        cmbKendaraan.removeAllItems();
        cmbFilter.removeAllItems();
        cmbFilter.addItem(SEMUA);
        for (Kendaraan k : data.getKendaraan()) {
            cmbKendaraan.addItem(k);
            cmbFilter.addItem(k);
        }
        pulihkanPilihan(lamaForm, lamaFilter);
        sedangIsi = false;
        if (txtTanggal.getText().isEmpty()) {
            txtTanggal.setText(Waktu.hariIni().format(Validasi.FORMAT_INPUT));
        }
        if (txtKm.getText().isEmpty()) {
            isiOtomatis();
        }
        tampilkanTabel();
    }

    private void pulihkanPilihan(Kendaraan lamaForm, Object lamaFilter) {
        for (int i = 0; i < cmbKendaraan.getItemCount(); i++) {
            if (lamaForm != null && cmbKendaraan.getItemAt(i).getNopol().equals(lamaForm.getNopol())) {
                cmbKendaraan.setSelectedIndex(i);
                break;
            }
        }
        if (lamaFilter instanceof Kendaraan) {
            for (int i = 1; i < cmbFilter.getItemCount(); i++) {
                if (((Kendaraan) cmbFilter.getItemAt(i)).getNopol().equals(((Kendaraan) lamaFilter).getNopol())) {
                    cmbFilter.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /** Odometer default = jarak tempuh terbaru kendaraan terpilih. */
    private void isiOtomatis() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        if (k != null) {
            txtKm.setText(String.valueOf(k.getKmSaatIni()));
        }
    }

    private void tampilkanTabel() {
        model.setRowCount(0);
        Object pilihan = cmbFilter.getSelectedItem();
        List<RiwayatServis> daftar = (pilihan instanceof Kendaraan)
                ? data.riwayatDari(((Kendaraan) pilihan).getNopol())
                : data.getRiwayat();
        List<RiwayatServis> urut = new java.util.ArrayList<>(daftar);
        urut.sort((a, b) -> b.getTanggal().compareTo(a.getTanggal()));   // terbaru di atas
        for (RiwayatServis r : urut) {
            model.addRow(new Object[]{r.getTanggal().format(Validasi.FORMAT_INPUT), r.getNopol(),
                PerhitunganServis.formatKm(r.getKm()), r.getJenisServis(),
                PerhitunganServis.formatRupiah(r.getBiaya()), r.getCatatan()});
        }
        lblTotal.setText(daftar.size() + " catatan  |  Total biaya: "
                + PerhitunganServis.formatRupiah(PerhitunganServis.totalBiaya(daftar, 0)));
    }

    private void aksiCatat() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        try {
            if (k == null) {
                throw new DataTidakValidException("Belum ada kendaraan terdaftar.");
            }
            LocalDate tgl = Validasi.parseTanggal(txtTanggal.getText(), "Tanggal servis");
            if (tgl.isAfter(Waktu.hariIni())) {
                throw new DataTidakValidException("Tanggal servis tidak boleh di masa depan.");
            }
            int km = Validasi.parseAngka(txtKm.getText(), "Odometer");
            long biaya = Validasi.parseAngka(txtBiaya.getText(), "Biaya");
            String jenis = Validasi.wajibIsi(String.valueOf(cmbJenis.getSelectedItem()), "Jenis servis");
            data.catatServis(new RiwayatServis(k.getNopol(), tgl, km, jenis, biaya, txtCatatan.getText().trim()));
            txtBiaya.setText("");
            txtCatatan.setText("");
            JOptionPane.showMessageDialog(this, "Servis " + k.getNopol() + " tercatat. Jadwal servis berikutnya dihitung ulang.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            setelahUbah.run();
        } catch (DataTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}
