package aquacontrol.dao;

import aquacontrol.config.Conexion;
import aquacontrol.modelos.*;
import aquacontrol.util.ApiException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la tabla ventas: CRUD con JDBC. */
public class VentaDao extends BaseDao<Venta> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_cliente", "idCliente", Tipo.ENTERO),
        new Columna("id_usuario", "idUsuario", Tipo.ENTERO),
        new Columna("fecha", "fecha", Tipo.FECHA_HORA),
        new Columna("total", "total", Tipo.DECIMAL));

    @Override
    protected String tabla() {
        return "ventas";
    }

    @Override
    protected String pk() {
        return "id_venta";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Venta mapear(ResultSet rs) throws SQLException {
        Venta o = new Venta();
            o.setIdVenta(rs.getInt("id_venta"));
            o.setIdCliente(entero(rs, "id_cliente"));
            o.setIdUsuario(entero(rs, "id_usuario"));
            o.setFecha(fecha(rs, "fecha"));
            o.setTotal(rs.getBigDecimal("total"));
        return o;
    }

    @Override
    protected Object[] valores(Venta o) {
        return new Object[] {
            o.getIdCliente(),
            o.getIdUsuario(),
            o.getFecha(),
            o.getTotal()
        };
    }

    @Override
    protected void asignarId(Venta o, int id) {
        o.setIdVenta(id);
    }

    private final ProductoDao productos = new ProductoDao();
    private final DetalleVentaDao detalles = new DetalleVentaDao();
    private final MovimientoInventarioDao movimientos = new MovimientoInventarioDao();

    /** Las ventas incluyen su lista de detalles al consultarlas por id. */
    @Override
    public Venta buscarPorId(Connection c, int id) throws SQLException {
        Venta v = super.buscarPorId(c, id);
        if (v != null) v.setDetalles(detalles.porVenta(c, id));
        return v;
    }

    /** Los servicios (Llenado) no tienen existencias físicas, no descuentan stock. */
    private static boolean controlaStock(Producto p) {
        return p.getTipo() == null || !p.getTipo().toLowerCase().startsWith("llenado");
    }

    private void registrarMovimiento(Connection c, int idProducto, TipoMovimientoInv tipo, int cantidad, String motivo) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setIdProducto(idProducto);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setFecha(LocalDateTime.now());
        m.setMotivo(motivo);
        movimientos.insertar(c, m);
    }

    /**
     * Guarda las líneas de una venta: valida producto y stock, descuenta existencias,
     * registra el movimiento de inventario y devuelve el total. Los precios salen de la BD.
     */
    private BigDecimal aplicarLineas(Connection c, int idVenta, List<DetalleVenta> lineas, String motivo) throws SQLException {
        if (lineas == null || lineas.isEmpty())
            throw new ApiException(400, "La venta debe incluir al menos un producto");

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : lineas) {
            if (d.getIdProducto() == null || d.getCantidad() == null || d.getCantidad() <= 0)
                throw new ApiException(400, "Cada detalle necesita idProducto y una cantidad mayor que 0");

            Producto p = productos.bloquear(c, d.getIdProducto());
            if (p == null || !Boolean.TRUE.equals(p.getActivo()))
                throw new ApiException(400, "El producto " + d.getIdProducto() + " no existe o está inactivo");

            if (controlaStock(p)) {
                int stock = p.getStock() == null ? 0 : p.getStock();
                if (stock < d.getCantidad())
                    throw new ApiException(409, "Stock insuficiente de " + p.getNombre());
                productos.cambiarStock(c, p.getIdProducto(), -d.getCantidad());
                registrarMovimiento(c, p.getIdProducto(), TipoMovimientoInv.SALIDA, d.getCantidad(), motivo);
            }

            BigDecimal precio = p.getPrecio() == null ? BigDecimal.ZERO : p.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(d.getCantidad()));
            DetalleVenta linea = new DetalleVenta();
            linea.setIdVenta(idVenta);
            linea.setIdProducto(p.getIdProducto());
            linea.setCantidad(d.getCantidad());
            linea.setPrecioUnitario(precio);
            linea.setSubtotal(subtotal);
            detalles.insertar(c, linea);
            total = total.add(subtotal);
        }
        return total;
    }

    /** Devuelve al stock las unidades de las líneas indicadas (cancelación o edición de una venta). */
    private void devolverStock(Connection c, List<DetalleVenta> lineas, String motivo) throws SQLException {
        for (DetalleVenta d : lineas) {
            if (d.getIdProducto() == null || d.getCantidad() == null) continue;
            Producto p = productos.bloquear(c, d.getIdProducto());
            if (p != null && controlaStock(p)) {
                productos.cambiarStock(c, p.getIdProducto(), d.getCantidad());
                registrarMovimiento(c, p.getIdProducto(), TipoMovimientoInv.ENTRADA, d.getCantidad(), motivo);
            }
        }
    }

    private void actualizarCabecera(Connection c, int idVenta, Integer idCliente, BigDecimal total) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE ventas SET id_cliente = ?, total = ? WHERE id_venta = ?")) {
            if (idCliente == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, idCliente);
            ps.setBigDecimal(2, total);
            ps.setInt(3, idVenta);
            ps.executeUpdate();
        }
    }

    /**
     * POST: registra una venta completa en UNA transacción (cabecera, detalles, stock, movimientos).
     * Si algo falla se deshace todo (rollback).
     */
    public Venta registrar(Venta peticion, int idUsuario) throws SQLException {
        try (Connection c = Conexion.get()) {
            c.setAutoCommit(false);
            try {
                Venta cabecera = new Venta();
                cabecera.setIdCliente(peticion.getIdCliente());
                cabecera.setIdUsuario(idUsuario);
                cabecera.setFecha(LocalDateTime.now());
                cabecera.setTotal(BigDecimal.ZERO);
                cabecera = insertar(c, cabecera);
                int idVenta = cabecera.getIdVenta();

                BigDecimal total = aplicarLineas(c, idVenta, peticion.getDetalles(), "Venta #" + idVenta);
                actualizarCabecera(c, idVenta, peticion.getIdCliente(), total);
                c.commit();
                return buscarPorId(c, idVenta);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * PUT/PATCH: reemplaza los productos (y el cliente) de una venta ya registrada.
     * En una sola transacción: devuelve el stock de las líneas anteriores, aplica las nuevas
     * (con las mismas validaciones que registrar) y recalcula el total. Devuelve null si no existe.
     */
    public Venta reemplazar(int id, Venta peticion) throws SQLException {
        try (Connection c = Conexion.get()) {
            c.setAutoCommit(false);
            try {
                Venta actual = buscarPorId(c, id);
                if (actual == null) {
                    c.rollback();
                    return null;
                }
                devolverStock(c, actual.getDetalles(), "Edición de venta #" + id);
                detalles.eliminarPorVenta(c, id);
                BigDecimal total = aplicarLineas(c, id, peticion.getDetalles(), "Venta #" + id + " (editada)");
                actualizarCabecera(c, id, peticion.getIdCliente(), total);
                c.commit();
                return buscarPorId(c, id);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** DELETE: elimina la venta con sus detalles y devuelve el stock, todo en una transacción. */
    public boolean eliminarCompleta(int id) throws SQLException {
        try (Connection c = Conexion.get()) {
            c.setAutoCommit(false);
            try {
                Venta v = buscarPorId(c, id);
                if (v == null) {
                    c.rollback();
                    return false;
                }
                devolverStock(c, v.getDetalles(), "Cancelación de venta #" + id);
                detalles.eliminarPorVenta(c, id);
                eliminar(c, id);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
