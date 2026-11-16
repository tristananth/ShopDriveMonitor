package shopdrive.model;

/** Status kebutuhan servis sebuah kendaraan. */
public enum StatusServis {
    AMAN("Aman", 0),
    SEGERA("Segera Servis", 1),
    TERLAMBAT("Terlambat", 2);

    private final String label;
    private final int prioritas;

    StatusServis(String label, int prioritas) {
        this.label = label;
        this.prioritas = prioritas;
    }

    public String getLabel() { return label; }
    public int getPrioritas() { return prioritas; }
}
