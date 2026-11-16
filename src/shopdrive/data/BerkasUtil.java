package shopdrive.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitas berkas: backup data (Byte Stream) dan ekspor notifikasi (Character Stream).
 * Materi: Stream & File - InputStream/OutputStream, FileInputStream/FileOutputStream,
 * BufferedInputStream/BufferedOutputStream, FileWriter/BufferedWriter.
 */
public final class BerkasUtil {
    private BerkasUtil() { }

    /** Menyalin file byte per blok (cocok untuk file apa pun, termasuk biner). */
    public static void salinBerkas(File sumber, File tujuan) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(sumber));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(tujuan))) {
            byte[] buffer = new byte[4096];
            int terbaca;
            while ((terbaca = in.read(buffer)) != -1) {
                out.write(buffer, 0, terbaca);
            }
            out.flush();
        }
    }

    /** Backup semua file .csv di folderData ke folderData/backup/yyyyMMdd_HHmmss. */
    public static File backupData(File folderData) throws IOException {
        String cap = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File tujuan = new File(new File(folderData, "backup"), cap);
        if (!tujuan.mkdirs()) {
            throw new IOException("Tidak dapat membuat folder backup: " + tujuan);
        }
        File[] daftar = folderData.listFiles((dir, nama) -> nama.endsWith(".csv"));
        if (daftar != null) {
            for (File f : daftar) {
                salinBerkas(f, new File(tujuan, f.getName()));
            }
        }
        return tujuan;
    }

    /** Menulis daftar teks ke file .txt, satu baris per elemen (character stream). */
    public static void tulisBaris(File berkas, List<String> baris) throws IOException {
        File induk = berkas.getParentFile();
        if (induk != null) {
            induk.mkdirs();
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(berkas, StandardCharsets.UTF_8))) {
            for (String s : baris) {
                out.write(s);
                out.newLine();
            }
        }
    }
}
