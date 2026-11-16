package shopdrive.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import shopdrive.data.PenyimpananCsv;
import shopdrive.service.ManajerData;
import shopdrive.service.Waktu;
import shopdrive.ui.AplikasiPanel;
import shopdrive.ui.Tema;

/**
 * Alat bantu pengembang (bukan bagian aplikasi): merender GUI ke file PNG secara headless,
 * dipakai untuk membuat tangkapan layar pada laporan dan presentasi.
 * Pemakaian: java -Djava.awt.headless=true -cp out shopdrive.tools.AmbilGambar folderData folderKeluaran
 */
public class AmbilGambar {
    public static void main(String[] args) throws Exception {
        File folderData = new File(args[0]);
        File keluar = new File(args[1]);
        keluar.mkdirs();
        Waktu.setAcuan(LocalDate.of(2026, 11, 16));
        Tema.pasang();
        ManajerData data = new ManajerData(new PenyimpananCsv(folderData));
        AplikasiPanel app = new AplikasiPanel(data, folderData);
        String[] nama = {"dashboard", "kendaraan", "riwayat", "proyeksi"};
        app.getKendaraan().pilihBaris(1);
        app.getProyeksi().pilihKendaraan(1);
        for (int i = 0; i < nama.length; i++) {
            app.getTab().setSelectedIndex(i);
            simpan(app, new File(keluar, nama[i] + ".png"), 1240, 780);
        }
        System.out.println("Selesai: " + keluar.getAbsolutePath());
        System.exit(0);
    }

    private static void simpan(AplikasiPanel app, File berkas, int lebar, int tinggi) throws Exception {
        app.setSize(lebar, tinggi);
        tataTotal(app);
        tataTotal(app);
        BufferedImage img = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, lebar, tinggi);
        app.paint(g);
        g.dispose();
        ImageIO.write(img, "png", berkas);
    }

    private static void tataTotal(Container c) {
        if (c instanceof JSplitPane) {
            JSplitPane sp = (JSplitPane) c;
            sp.setDividerLocation(sp.getResizeWeight());
        }
        if (c instanceof JScrollPane) {
            Component v = ((JScrollPane) c).getViewport().getView();
            if (v instanceof JTable && (((JScrollPane) c).getColumnHeader() == null
                    || ((JScrollPane) c).getColumnHeader().getView() == null)) {
                ((JScrollPane) c).setColumnHeaderView(((JTable) v).getTableHeader());
            }
        }
        c.doLayout();
        for (Component k : c.getComponents()) {
            if (k instanceof Container) {
                tataTotal((Container) k);
            }
        }
    }
}
