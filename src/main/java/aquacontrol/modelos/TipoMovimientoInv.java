package aquacontrol.modelos;

import com.google.gson.annotations.SerializedName;

/** Tipo ENUM de PostgreSQL. En la BD y en el JSON se escribe en minúsculas. */
public enum TipoMovimientoInv {
    @SerializedName("entrada") ENTRADA("entrada"),
    @SerializedName("salida") SALIDA("salida");

    private final String valor;

    TipoMovimientoInv(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoMovimientoInv desde(String valor) {
        if (valor == null) return null;
        for (TipoMovimientoInv t : values()) if (t.valor.equals(valor)) return t;
        throw new IllegalArgumentException("Valor desconocido: " + valor);
    }
}
