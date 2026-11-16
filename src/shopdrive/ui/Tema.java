package shopdrive.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import shopdrive.model.StatusServis;

/** Warna, font, dan helper pembuat komponen agar tampilan konsisten. */
public final class Tema {
    public static final Color NAVY = new Color(0x12, 0x2B, 0x4A);
    public static final Color BIRU = new Color(0x1F, 0x5F, 0xA8);
    public static final Color LATAR = new Color(0xF3, 0xF5, 0xF9);
    public static final Color GARIS = new Color(0xD5, 0xDB, 0xE5);
    public static final Color TEKS_REDUP = new Color(0x5B, 0x67, 0x7A);
    public static final Color HIJAU = new Color(0x2E, 0x8B, 0x57);
    public static final Color AMBER = new Color(0xE0, 0x8A, 0x00);
    public static final Color MERAH = new Color(0xC6, 0x28, 0x28);

    public static final Font FONT_JUDUL = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_ANGKA = new Font("SansSerif", Font.BOLD, 30);
    public static final Font FONT_BIASA = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_TEBAL = new Font("SansSerif", Font.BOLD, 13);

    private Tema() { }

    /** Memasang look-and-feel Nimbus dengan warna dasar tema. */
    public static void pasang() {
        try {
            UIManager.put("nimbusBase", NAVY);
            UIManager.put("nimbusBlueGrey", new Color(0x6B, 0x7C, 0x93));
            UIManager.put("nimbusSelectionBackground", BIRU);
            UIManager.put("control", LATAR);
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Look and feel Nimbus tidak tersedia, memakai bawaan.");
        }
    }

    public static Color warnaStatus(StatusServis s) {
        switch (s) {
            case TERLAMBAT: return MERAH;
            case SEGERA: return AMBER;
            default: return HIJAU;
        }
    }

    public static JButton tombol(String teks, Color warna) {
        JButton b = new JButton(teks);
        b.setFont(FONT_TEBAL);
        b.setBackground(warna);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setMargin(new Insets(6, 12, 6, 12));
        return b;
    }

    /** Kartu ringkasan: judul kecil di atas, angka besar di bawah, garis aksen di kiri. */
    public static JPanel kartu(String judul, JLabel angka, Color aksen) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 2));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, aksen),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GARIS),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14))));
        JLabel j = new JLabel(judul);
        j.setForeground(TEKS_REDUP);
        j.setFont(FONT_BIASA);
        angka.setFont(FONT_ANGKA);
        angka.setForeground(aksen);
        p.add(j, java.awt.BorderLayout.NORTH);
        p.add(angka, java.awt.BorderLayout.CENTER);
        return p;
    }

    public static JLabel judulBagian(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(NAVY);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    /** Tabel tidak bisa diedit dengan gaya seragam. */
    public static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolomKe) {
                return false;
            }
        };
    }

    public static void gayaTabel(JTable t) {
        t.setRowHeight(26);
        t.setFont(FONT_BIASA);
        t.setGridColor(GARIS);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(FONT_TEBAL);
        t.getTableHeader().setReorderingAllowed(false);
    }

    public static void lebarKolom(JTable t, int... lebar) {
        for (int i = 0; i < lebar.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Renderer sel status berupa "pil" berwarna. */
    public static class RendererStatus extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean dipilih,
                                                       boolean fokus, int baris, int kolom) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(tabel, "", dipilih, fokus, baris, kolom);
            if (nilai instanceof StatusServis) {
                StatusServis s = (StatusServis) nilai;
                l.setText(s.getLabel());
                l.setOpaque(true);
                l.setBackground(warnaStatus(s));
                l.setForeground(Color.WHITE);
                l.setFont(FONT_TEBAL);
            }
            l.setHorizontalAlignment(SwingConstants.CENTER);
            return l;
        }
    }

    /** Renderer angka: rata kanan, merah tebal bila negatif (sisa km/hari sudah lewat). */
    public static class RendererSisa extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean dipilih,
                                                       boolean fokus, int baris, int kolom) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
            l.setHorizontalAlignment(SwingConstants.RIGHT);
            boolean negatif = nilai != null && nilai.toString().startsWith("-");
            if (!dipilih) {
                l.setForeground(negatif ? MERAH : Color.BLACK);
            }
            l.setFont(negatif ? FONT_TEBAL : FONT_BIASA);
            return l;
        }
    }

    public static class RendererKanan extends DefaultTableCellRenderer {
        public RendererKanan() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    // ---------- Helper form ----------
    public static void barisForm(JPanel panel, int baris, String label, JComponent isi) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = baris;
        c.insets = new Insets(4, 0, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        JLabel l = new JLabel(label);
        l.setFont(FONT_BIASA);
        panel.add(l, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        isi.setPreferredSize(new Dimension(190, 28));
        panel.add(isi, c);
    }

    public static JPanel panelForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }
}
