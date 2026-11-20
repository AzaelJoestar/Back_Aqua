package aquacontrol.dao;

import aquacontrol.config.Conexion;
import aquacontrol.util.ApiException;
import aquacontrol.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD genérico con JDBC. Cada DAO concreto solo declara su tabla, su llave primaria,
 * sus columnas y cómo convertir una fila en objeto (y viceversa).
 * Todas las consultas usan PreparedStatement (evita inyección SQL); los nombres de tabla
 * y columna salen siempre del código, nunca de la petición.
 * Cada operación tiene una versión que recibe Connection para poder agruparlas en una transacción.
 */
public abstract class BaseDao<T> {

    protected abstract String tabla();

    protected abstract String pk();

    /** Columnas editables (sin la llave primaria). */
    protected abstract List<Columna> columnas();

    protected abstract T mapear(ResultSet rs) throws SQLException;

    /** Valores del objeto en el mismo orden que columnas(). Enums como String. */
    protected abstract Object[] valores(T o);

    protected abstract void asignarId(T o, int id);

    // ---------- Leer ----------
    public List<T> listar() throws SQLException {
        try (Connection c = Conexion.get()) {
            return listar(c);
        }
    }

    public List<T> listar(Connection c) throws SQLException {
        String sql = "SELECT * FROM " + tabla() + " ORDER BY " + pk();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<T> r = new ArrayList<>();
            while (rs.next()) r.add(mapear(rs));
            return r;
        }
    }

    public T buscarPorId(int id) throws SQLException {
        try (Connection c = Conexion.get()) {
            return buscarPorId(c, id);
        }
    }

    public T buscarPorId(Connection c, int id) throws SQLException {
        String sql = "SELECT * FROM " + tabla() + " WHERE " + pk() + " = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // ---------- Crear ----------
    public T insertar(T o) throws SQLException {
        try (Connection c = Conexion.get()) {
            return insertar(c, o);
        }
    }

    /** Solo inserta las columnas con valor, para que apliquen los DEFAULT de la BD (fechas, estado, etc.). */
    public T insertar(Connection c, T o) throws SQLException {
        List<Columna> cols = columnas();
        Object[] vals = valores(o);
        List<Integer> usadas = new ArrayList<>();
        for (int i = 0; i < vals.length; i++) if (vals[i] != null) usadas.add(i);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tabla());
        if (usadas.isEmpty()) {
            sql.append(" DEFAULT VALUES");
        } else {
            StringBuilder nombres = new StringBuilder();
            StringBuilder marcas = new StringBuilder();
            for (int i : usadas) {
                if (nombres.length() > 0) { nombres.append(", "); marcas.append(", "); }
                nombres.append(cols.get(i).bd());
                marcas.append(cols.get(i).tipo() == Tipo.ENUM ? "?::" + tipoEnumBd(cols.get(i)) : "?");
            }
            sql.append(" (").append(nombres).append(") VALUES (").append(marcas).append(")");
        }
        sql.append(" RETURNING ").append(pk());

        int id;
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int n = 1;
            for (int i : usadas) enlazar(ps, n++, cols.get(i).tipo(), vals[i]);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
        asignarId(o, id);
        return buscarPorId(c, id);
    }

    // ---------- Reemplazar (PUT) ----------
    public boolean actualizar(int id, T o) throws SQLException {
        try (Connection c = Conexion.get()) {
            return actualizar(c, id, o);
        }
    }

    public boolean actualizar(Connection c, int id, T o) throws SQLException {
        List<Columna> cols = columnas();
        Object[] vals = valores(o);
        StringBuilder sets = new StringBuilder();
        for (Columna col : cols) {
            if (sets.length() > 0) sets.append(", ");
            sets.append(col.bd()).append(" = ").append(col.tipo() == Tipo.ENUM ? "?::" + tipoEnumBd(col) : "?");
        }
        String sql = "UPDATE " + tabla() + " SET " + sets + " WHERE " + pk() + " = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int n = 1;
            for (int i = 0; i < cols.size(); i++) enlazar(ps, n++, cols.get(i).tipo(), vals[i]);
            ps.setInt(n, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Modificar parcialmente (PATCH) ----------
    public boolean parchar(int id, JsonObject cambios) throws SQLException {
        try (Connection c = Conexion.get()) {
            return parchar(c, id, cambios);
        }
    }

    public boolean parchar(Connection c, int id, JsonObject cambios) throws SQLException {
        List<Columna> sel = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (Columna col : columnas()) {
            if (cambios.has(col.json())) {
                sel.add(col);
                vals.add(convertir(col.tipo(), cambios.get(col.json())));
            }
        }
        if (sel.isEmpty()) throw new ApiException(400, "No hay campos válidos para modificar");

        StringBuilder sets = new StringBuilder();
        for (Columna col : sel) {
            if (sets.length() > 0) sets.append(", ");
            sets.append(col.bd()).append(" = ").append(col.tipo() == Tipo.ENUM ? "?::" + tipoEnumBd(col) : "?");
        }
        String sql = "UPDATE " + tabla() + " SET " + sets + " WHERE " + pk() + " = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int n = 1;
            for (int i = 0; i < sel.size(); i++) enlazar(ps, n++, sel.get(i).tipo(), vals.get(i));
            ps.setInt(n, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Eliminar ----------
    public boolean eliminar(int id) throws SQLException {
        try (Connection c = Conexion.get()) {
            return eliminar(c, id);
        }
    }

    public boolean eliminar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla() + " WHERE " + pk() + " = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Utilidades ----------
    /** Nombre del tipo ENUM de PostgreSQL de una columna (se declara en el DAO que lo use). */
    protected String tipoEnumBd(Columna col) {
        throw new IllegalStateException("Falta tipoEnumBd para " + col.bd());
    }

    protected static void enlazar(PreparedStatement ps, int i, Tipo tipo, Object v) throws SQLException {
        if (v == null) {
            ps.setNull(i, tipo == Tipo.ENUM ? Types.VARCHAR : tipo.sqlType());
            return;
        }
        switch (tipo) {
            case TEXTO, ENUM -> ps.setString(i, (String) v);
            case ENTERO -> ps.setInt(i, (Integer) v);
            case DECIMAL -> ps.setBigDecimal(i, (BigDecimal) v);
            case BOOLEANO -> ps.setBoolean(i, (Boolean) v);
            case FECHA_HORA -> ps.setTimestamp(i, Timestamp.valueOf((LocalDateTime) v));
        }
    }

    private static Object convertir(Tipo tipo, JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        return switch (tipo) {
            case TEXTO, ENUM -> e.getAsString();
            case ENTERO -> e.getAsInt();
            case DECIMAL -> e.getAsBigDecimal();
            case BOOLEANO -> e.getAsBoolean();
            case FECHA_HORA -> Json.fecha(e.getAsString());
        };
    }

    protected static Integer entero(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected static Boolean booleano(ResultSet rs, String col) throws SQLException {
        boolean v = rs.getBoolean(col);
        return rs.wasNull() ? null : v;
    }

    protected static LocalDateTime fecha(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }
}
