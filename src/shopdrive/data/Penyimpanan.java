package shopdrive.data;

import java.io.IOException;
import java.util.List;
import shopdrive.model.Kendaraan;
import shopdrive.model.RiwayatServis;

/**
 * Interface penyimpanan data. Dengan interface, lapisan logika tidak bergantung pada
 * bentuk penyimpanan (CSV sekarang, bisa diganti database nanti).
 * Materi: Package dan Interface.
 */
public interface Penyimpanan {
    List<Kendaraan> muatKendaraan() throws IOException;

    void simpanKendaraan(List<Kendaraan> daftar) throws IOException;

    List<RiwayatServis> muatRiwayat() throws IOException;

    void simpanRiwayat(List<RiwayatServis> daftar) throws IOException;
}
