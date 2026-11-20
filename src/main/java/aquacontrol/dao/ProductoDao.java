package aquacontrol.dao;

import aquacontrol.config.Conexion;
import aquacontrol.modelos.*;
import aquacontrol.util.ApiException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la tabla productos: CRUD con JDBC. */
public class ProductoDao extends BaseDao<Producto> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("nombre", "nombre", Tipo.TEXTO),
        new Columna("tipo", "tipo", Tipo.TEXTO),
        new Columna("precio", "precio", Tipo.DECIMAL),
        new Columna("stock", "stock", Tipo.ENTERO),
        new Columna("stock_minimo", "stockMinimo", Tipo.ENTERO),
        new Columna("activo", "activo", Tipo.BOOLEANO));

    @Override
    protected String tabla() {
        return "productos";
    }

    @Override
    protected String pk() {
        return "id_producto";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Producto mapear(ResultSet rs) throws SQLException {
        Producto o = new Producto();
            o.setIdProducto(rs.getInt("id_producto"));
            o.setNombre(rs.getString("nombre"));
            o.setTipo(rs.getString("tipo"));
            o.setPrecio(rs.getBigDecimal("precio"));
            o.setStock(entero(rs, "stock"));
            o.setStockMinimo(entero(rs, "stock_minimo"));
            o.setActivo(booleano(rs, "activo"));
        return o;
    }

    @Override
    protected Object[] valores(Producto o) {
        return new Object[] {
            o.getNombre(),
            o.getTipo(),
            o.getPrecio(),
            o.getStock(),
            o.getStockMinimo(),
            o.getActivo()
        };
    }

    @Override
    protected void asignarId(Producto o, int id) {
        o.setIdProducto(id);
    }

    /** Lee el producto bloqueando la fila hasta que termine la transacción (evita vender el mismo stock dos veces). */
    public Producto bloquear(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM productos WHERE id_producto = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Suma (o resta, si es negativo) unidades al stock. */
    public void cambiarStock(Connection c, int id, int delta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE productos SET stock = COALESCE(stock, 0) + ? WHERE id_producto = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
