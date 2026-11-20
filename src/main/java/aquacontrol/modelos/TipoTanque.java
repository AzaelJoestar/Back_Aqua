package aquacontrol.modelos;

import com.google.gson.annotations.SerializedName;

/** Tipo ENUM de PostgreSQL. En la BD y en el JSON se escribe en minúsculas. */
public enum TipoTanque {
    @SerializedName("cruda") CRUDA("cruda"),
    @SerializedName("purificada") PURIFICADA("purificada");

    private final String valor;

    TipoTanque(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoTanque desde(String valor) {
        if (valor == null) return null;
        for (TipoTanque t : values()) if (t.valor.equals(valor)) return t;
        throw new IllegalArgumentException("Valor desconocido: " + valor);
    }
}
