package aquacontrol.dao;

import aquacontrol.config.Conexion;
import aquacontrol.modelos.*;
import aquacontrol.util.ApiException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la tabla detalle_venta: CRUD con JDBC. */
public class DetalleVentaDao extends BaseDao<DetalleVenta> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_venta", "idVenta", Tipo.ENTERO),
        new Columna("id_producto", "idProducto", Tipo.ENTERO),
        new Columna("cantidad", "cantidad", Tipo.ENTERO),
        new Columna("precio_unitario", "precioUnitario", Tipo.DECIMAL),
        new Columna("subtotal", "subtotal", Tipo.DECIMAL));

    @Override
    protected String tabla() {
        return "detalle_venta";
    }

    @Override
    protected String pk() {
        return "id_detalle";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected DetalleVenta mapear(ResultSet rs) throws SQLException {
        DetalleVenta o = new DetalleVenta();
            o.setIdDetalle(rs.getInt("id_detalle"));
            o.setIdVenta(entero(rs, "id_venta"));
            o.setIdProducto(entero(rs, "id_producto"));
            o.setCantidad(entero(rs, "cantidad"));
            o.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
            o.setSubtotal(rs.getBigDecimal("subtotal"));
        return o;
    }

    @Override
    protected Object[] valores(DetalleVenta o) {
        return new Object[] {
            o.getIdVenta(),
            o.getIdProducto(),
            o.getCantidad(),
            o.getPrecioUnitario(),
            o.getSubtotal()
        };
    }

    @Override
    protected void asignarId(DetalleVenta o, int id) {
        o.setIdDetalle(id);
    }

    public List<DetalleVenta> porVenta(Connection c, int idVenta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM detalle_venta WHERE id_venta = ? ORDER BY id_detalle")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetalleVenta> r = new ArrayList<>();
                while (rs.next()) r.add(mapear(rs));
                return r;
            }
        }
    }

    public void eliminarPorVenta(Connection c, int idVenta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM detalle_venta WHERE id_venta = ?")) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }
    }
}
