package aquacontrol.dao;

import java.sql.Types;

/** Tipo de dato de una columna; decide cómo se enlaza al PreparedStatement y cómo se lee del JSON. */
public enum Tipo {
    TEXTO(Types.VARCHAR),
    ENTERO(Types.INTEGER),
    DECIMAL(Types.NUMERIC),
    BOOLEANO(Types.BOOLEAN),
    FECHA_HORA(Types.TIMESTAMP),
    ENUM(Types.OTHER);

    private final int sqlType;

    Tipo(int sqlType) {
        this.sqlType = sqlType;
    }

    public int sqlType() {
        return sqlType;
    }
}
