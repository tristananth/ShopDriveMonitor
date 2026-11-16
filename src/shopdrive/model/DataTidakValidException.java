package shopdrive.model;

/**
 * Exception khusus (custom exception) untuk input yang tidak valid.
 * Materi: Exception Handling (Basic exception, try-catch).
 */
public class DataTidakValidException extends Exception {
    public DataTidakValidException(String pesan) {
        super(pesan);
    }
}
