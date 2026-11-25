package aquacontrol.util;

/** Error controlado que se traduce a una respuesta HTTP con el código indicado. */
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int estado;

    public ApiException(int estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }
}
