package shopdrive.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import shopdrive.model.DataTidakValidException;
import shopdrive.model.Kendaraan;
import shopdrive.service.ManajerData;
import shopdrive.service.PerhitunganServis;
import shopdrive.service.Validasi;
import shopdrive.service.Waktu;

/** Tab Data Kendaraan: input jarak tempuh & tanggal servis terakhir, tambah/ubah/hapus mobil. */
public class KendaraanPanel extends JPanel {
    private final ManajerData data;
    private final Runnable setelahUbah;

    private final JTextField txtNopol = new JTextField();
    private final JTextField txtPemilik = new JTextField();
    private final JTextField txtHp = new JTextField();
    private final JTextField txtModel = new JTextField();
    private final JTextField txtTahun = new JTextField();
    private final JTextField txtKmSaatIni = new JTextField();
    private final JTextField txtKmTerakhir = new JTextField();
    private final JTextField txtTglTerakhir = new JTextField();
    private final JComboBox<String> cmbIntervalKm = new JComboBox<>(new String[]{"3000", "5000", "7500", "10000"});
    private final JComboBox<String> cmbIntervalBulan = new JComboBox<>(new String[]{"3", "6", "12"});

    private final DefaultTableModel model = Tema.modelTabel("No. Polisi", "Pemilik", "Kendaraan",
            "Tahun", "Km Saat Ini", "Km Servis Akhir", "Tgl Servis Akhir", "Interval");
    private final JTable tabel = new JTable(model);
    private final JLabel lblPesan = new JLabel(" ");
    private boolean sedangIsiForm = false;

    public KendaraanPanel(ManajerData data, Runnable setelahUbah) {
        this.data = data;
        this.setelahUbah = setelahUbah;
        setLayout(new BorderLayout(16, 0));
        setBackground(Tema.LATAR);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        // ----- Form kiri -----
        JPanel kiri = new JPanel(new BorderLayout(0, 8));
        kiri.setBackground(java.awt.Color.WHITE);
        kiri.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.GARIS),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        kiri.setPreferredSize(new Dimension(340, 0));
        kiri.add(Tema.judulBagian("Form Data Kendaraan"), BorderLayout.NORTH);

        JPanel form = Tema.panelForm();
        cmbIntervalKm.setSelectedItem("5000");
        cmbIntervalBulan.setSelectedItem("6");
        txtTglTerakhir.setToolTipText("Format dd-MM-yyyy");
        Tema.barisForm(form, 0, "No. Polisi", txtNopol);
        Tema.barisForm(form, 1, "Pemilik", txtPemilik);
        Tema.barisForm(form, 2, "No. HP", txtHp);
        Tema.barisForm(form, 3, "Merk / Model", txtModel);
        Tema.barisForm(form, 4, "Tahun", txtTahun);
        Tema.barisForm(form, 5, "Jarak tempuh saat ini (km)", txtKmSaatIni);
        Tema.barisForm(form, 6, "Km servis terakhir", txtKmTerakhir);
        Tema.barisForm(form, 7, "Tgl servis terakhir", txtTglTerakhir);
        Tema.barisForm(form, 8, "Interval servis (km)", cmbIntervalKm);
        Tema.barisForm(form, 9, "Interval servis (bulan)", cmbIntervalBulan);
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(form, BorderLayout.NORTH);
        kiri.add(pembungkus, BorderLayout.CENTER);

        JPanel bawahKiri = new JPanel(new BorderLayout(0, 8));
        bawahKiri.setOpaque(false);
        JPanel tombol = new JPanel(new GridLayout(3, 2, 6, 6));
        tombol.setOpaque(false);
        JButton btnTambah = Tema.tombol("Tambah Baru", Tema.HIJAU);
        JButton btnUbah = Tema.tombol("Simpan", Tema.BIRU);
        JButton btnKm = Tema.tombol("Perbarui Km", Tema.AMBER);
        JButton btnHapus = Tema.tombol("Hapus Data", Tema.MERAH);
        JButton btnBersih = Tema.tombol("Bersihkan", Tema.NAVY);
        tombol.add(btnTambah);
        tombol.add(btnUbah);
        tombol.add(btnKm);
        tombol.add(btnHapus);
        tombol.add(btnBersih);
        bawahKiri.add(tombol, BorderLayout.CENTER);
        lblPesan.setFont(Tema.FONT_BIASA);
        bawahKiri.add(lblPesan, BorderLayout.SOUTH);
        kiri.add(bawahKiri, BorderLayout.SOUTH);
        add(kiri, BorderLayout.WEST);

        // ----- Tabel kanan -----
        JPanel kanan = new JPanel(new BorderLayout(0, 6));
        kanan.setOpaque(false);
        kanan.add(Tema.judulBagian("Daftar Kendaraan Pelanggan (klik baris untuk mengubah)"), BorderLayout.NORTH);
        Tema.gayaTabel(tabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tema.lebarKolom(tabel, 95, 110, 150, 60, 85, 110, 110, 125);
        tabel.getColumnModel().getColumn(4).setCellRenderer(new Tema.RendererKanan());
        tabel.getColumnModel().getColumn(5).setCellRenderer(new Tema.RendererKanan());
        kanan.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(kanan, BorderLayout.CENTER);

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !sedangIsiForm) {
                isiFormDariBaris();
            }
        });
        btnKm.setToolTipText("Perbarui hanya jarak tempuh (odometer) kendaraan terpilih");
        btnTambah.addActionListener(e -> aksiTambah());
        btnUbah.addActionListener(e -> aksiUbah());
        btnKm.addActionListener(e -> aksiPerbaruiKm());
        btnHapus.addActionListener(e -> aksiHapus());
        btnBersih.addActionListener(e -> bersihkanForm());
        refresh();
    }

    public void refresh() {
        String dipilih = nopolTerpilih();
        sedangIsiForm = true;
        model.setRowCount(0);
        List<Kendaraan> semua = data.getKendaraan();
        for (Kendaraan k : semua) {
            model.addRow(new Object[]{k.getNopol(), k.getPemilik(), k.getMerkModel(), k.getTahun(),
                PerhitunganServis.formatKm(k.getKmSaatIni()), PerhitunganServis.formatKm(k.getKmServisTerakhir()),
                k.getTglServisTerakhir().format(Validasi.FORMAT_INPUT),
                PerhitunganServis.formatKm(k.getIntervalKm()) + " km / " + k.getIntervalBulan() + " bln"});
        }
        if (dipilih != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(dipilih)) {
                    tabel.setRowSelectionInterval(i, i);
                    break;                         // hentikan loop setelah ketemu
                }
            }
        }
        sedangIsiForm = false;
    }

    /** Memilih satu baris (dipakai juga oleh alat pengambil tangkapan layar). */
    public void pilihBaris(int indeks) {
        tabel.setRowSelectionInterval(indeks, indeks);
    }

    private String nopolTerpilih() {
        int baris = tabel.getSelectedRow();
        return (baris >= 0) ? (String) model.getValueAt(baris, 0) : null;
    }

    private void isiFormDariBaris() {
        String nopol = nopolTerpilih();
        if (nopol == null) {
            return;
        }
        Kendaraan k = data.cari(nopol);
        if (k == null) {
            return;
        }
        txtNopol.setText(k.getNopol());
        txtNopol.setEnabled(false);               // nopol adalah kunci data, tidak boleh diubah
        txtPemilik.setText(k.getPemilik());
        txtHp.setText(k.getNoHp());
        txtModel.setText(k.getMerkModel());
        txtTahun.setText(String.valueOf(k.getTahun()));
        txtKmSaatIni.setText(String.valueOf(k.getKmSaatIni()));
        txtKmTerakhir.setText(String.valueOf(k.getKmServisTerakhir()));
        txtTglTerakhir.setText(k.getTglServisTerakhir().format(Validasi.FORMAT_INPUT));
        cmbIntervalKm.setSelectedItem(String.valueOf(k.getIntervalKm()));
        cmbIntervalBulan.setSelectedItem(String.valueOf(k.getIntervalBulan()));
        pesan("Mengubah data " + k.getNopol(), Tema.TEKS_REDUP);
    }

    private void bersihkanForm() {
        tabel.clearSelection();
        for (JTextField t : new JTextField[]{txtNopol, txtPemilik, txtHp, txtModel, txtTahun,
                txtKmSaatIni, txtKmTerakhir, txtTglTerakhir}) {
            t.setText("");
        }
        txtNopol.setEnabled(true);
        cmbIntervalKm.setSelectedItem("5000");
        cmbIntervalBulan.setSelectedItem("6");
        pesan(" ", Tema.TEKS_REDUP);
    }

    /** Membaca dan memvalidasi seluruh isian form menjadi objek Kendaraan. */
    private Kendaraan bacaForm() throws DataTidakValidException {
        String nopol = Validasi.normalisasiNopol(txtNopol.getText());
        String pemilik = Validasi.wajibIsi(txtPemilik.getText(), "Nama pemilik");
        String hp = Validasi.wajibIsi(txtHp.getText(), "No. HP");
        String merk = Validasi.wajibIsi(txtModel.getText(), "Merk / model");
        int tahunMaks = Waktu.hariIni().getYear() + 1;
        int tahun = Validasi.parseAngka(txtTahun.getText(), "Tahun", 1990, tahunMaks);
        int kmSaatIni = Validasi.parseAngka(txtKmSaatIni.getText(), "Jarak tempuh saat ini");
        int kmTerakhir = Validasi.parseAngka(txtKmTerakhir.getText(), "Km servis terakhir");
        LocalDate tgl = Validasi.parseTanggal(txtTglTerakhir.getText(), "Tanggal servis terakhir");
        int intervalKm = Validasi.parseAngka(String.valueOf(cmbIntervalKm.getSelectedItem()), "Interval km", 1000, 50000);
        int intervalBulan = Validasi.parseAngka(String.valueOf(cmbIntervalBulan.getSelectedItem()), "Interval bulan", 1, 24);
        if (kmTerakhir > kmSaatIni) {
            throw new DataTidakValidException("Km servis terakhir tidak boleh melebihi jarak tempuh saat ini.");
        }
        if (tgl.isAfter(Waktu.hariIni())) {
            throw new DataTidakValidException("Tanggal servis terakhir tidak boleh di masa depan.");
        }
        return new Kendaraan(nopol, pemilik, hp, merk, tahun, kmSaatIni, kmTerakhir, tgl, intervalKm, intervalBulan);
    }

    private void aksiTambah() {
        try {
            Kendaraan k = bacaForm();
            data.tambahKendaraan(k);
            pesan("Kendaraan " + k.getNopol() + " berhasil ditambahkan.", Tema.HIJAU);
            bersihkanForm();
            pesan("Kendaraan " + k.getNopol() + " berhasil ditambahkan.", Tema.HIJAU);
            setelahUbah.run();
        } catch (DataTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiUbah() {
        if (nopolTerpilih() == null) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan pada tabel terlebih dahulu.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Kendaraan k = bacaForm();
            data.ubahKendaraan(k);
            pesan("Perubahan " + k.getNopol() + " tersimpan.", Tema.HIJAU);
            setelahUbah.run();
        } catch (DataTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Fitur inti: pelanggan datang/menelepon dan odometer terbaru dimasukkan. */
    private void aksiPerbaruiKm() {
        String nopol = nopolTerpilih();
        if (nopol == null) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan pada tabel, lalu isi 'Jarak tempuh saat ini'.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int km = Validasi.parseAngka(txtKmSaatIni.getText(), "Jarak tempuh saat ini");
            data.perbaruiJarakTempuh(nopol, km);
            pesan("Jarak tempuh " + nopol + " diperbarui: " + PerhitunganServis.formatKm(km) + " km.", Tema.HIJAU);
            setelahUbah.run();
        } catch (DataTidakValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiHapus() {
        String nopol = nopolTerpilih();
        if (nopol == null) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan pada tabel terlebih dahulu.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int ya = JOptionPane.showConfirmDialog(this,
                "Hapus " + nopol + " beserta seluruh riwayat servisnya?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ya != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            data.hapusKendaraan(nopol);
            bersihkanForm();
            pesan("Kendaraan " + nopol + " dihapus.", Tema.MERAH);
            setelahUbah.run();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pesan(String teks, java.awt.Color warna) {
        lblPesan.setText(teks);
        lblPesan.setForeground(warna);
    }
}
