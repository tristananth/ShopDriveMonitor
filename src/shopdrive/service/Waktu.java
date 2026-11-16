package shopdrive.service;

import java.time.LocalDate;

/**
 * Penyedia "tanggal hari ini". Dibuat terpisah agar tanggal acuan bisa disimulasikan
 * (berguna untuk demo notifikasi tanpa mengubah jam sistem).
 */
public final class Waktu {
    private static LocalDate acuan = null;

    private Waktu() { }

    public static LocalDate hariIni() {
        return (acuan != null) ? acuan : LocalDate.now();
    }

    /** Isi dengan null untuk kembali memakai tanggal sistem. */
    public static void setAcuan(LocalDate tanggal) {
        acuan = tanggal;
    }

    public static boolean isSimulasi() {
        return acuan != null;
    }
}
