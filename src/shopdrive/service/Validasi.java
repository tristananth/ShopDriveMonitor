package shopdrive.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import shopdrive.model.DataTidakValidException;

/**
 * Validasi dan konversi input teks dari form GUI.
 * Materi: String handling (trim, replace, regex), Exception handling (try-catch, throws),
 * Method overloading (dua versi parseAngka).
 */
public final class Validasi {
    public static final DateTimeFormatter FORMAT_INPUT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);   // tolak tanggal mustahil (31-02)
    private static final Pattern POLA_NOPOL = Pattern.compile("^([A-Z]{1,2})(\\d{1,4})([A-Z]{0,3})$");

    private Validasi() { }

    public static String wajibIsi(String teks, String namaField) throws DataTidakValidException {
        if (teks == null || teks.trim().isEmpty()) {
            throw new DataTidakValidException(namaField + " wajib diisi.");
        }
        return teks.trim();
    }

    /** Versi 1: angka bulat tidak negatif tanpa batas atas. */
    public static int parseAngka(String teks, String namaField) throws DataTidakValidException {
        return parseAngka(teks, namaField, 0, Integer.MAX_VALUE);
    }

    /** Versi 2 (overloading): angka bulat dengan batas minimum dan maksimum. */
    public static int parseAngka(String teks, String namaField, int min, int max)
            throws DataTidakValidException {
        String bersih = wajibIsi(teks, namaField).replace(".", "").replace(",", "");
        int nilai;
        try {
            nilai = Integer.parseInt(bersih);
        } catch (NumberFormatException e) {
            throw new DataTidakValidException(namaField + " harus berupa angka bulat.");
        }
        if (nilai < min || nilai > max) {
            throw new DataTidakValidException(namaField + " harus antara " + min + " dan " + max + ".");
        }
        return nilai;
    }

    public static LocalDate parseTanggal(String teks, String namaField) throws DataTidakValidException {
        String bersih = wajibIsi(teks, namaField);
        try {
            return LocalDate.parse(bersih, FORMAT_INPUT);
        } catch (DateTimeParseException e) {
            throw new DataTidakValidException(namaField + " harus berformat dd-MM-yyyy (contoh 16-11-2026).");
        }
    }

    /** Menormalkan nomor polisi menjadi format "B 1234 XYZ". */
    public static String normalisasiNopol(String teks) throws DataTidakValidException {
        String rapat = wajibIsi(teks, "Nomor polisi").toUpperCase().replaceAll("\\s+", "");
        Matcher m = POLA_NOPOL.matcher(rapat);
        if (!m.matches()) {
            throw new DataTidakValidException("Format nomor polisi tidak valid (contoh: B 1234 XYZ).");
        }
        String hasil = m.group(1) + " " + m.group(2);
        if (!m.group(3).isEmpty()) {
            hasil += " " + m.group(3);
        }
        return hasil;
    }

    /** Karakter pemisah CSV (;) dan baris baru dibersihkan agar file CSV tidak rusak. */
    public static String bersihkanCsv(String teks) {
        return teks.replace(';', ',').replace('\n', ' ').replace('\r', ' ').trim();
    }
}
