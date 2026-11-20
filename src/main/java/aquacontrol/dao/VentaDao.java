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
     * Registra una venta completa en UNA transacción: cabecera, detalles, descuento de stock y
     * movimientos de inventario. Los precios los toma de la BD (no del cliente). Si algo falla,
     * se deshace todo (rollback) y no queda nada a medias.
     */
    public Venta registrar(Venta peticion, int idUsuario) throws SQLException {
        if (peticion.getDetalles() == null || peticion.getDetalles().isEmpty())
            throw new ApiException(400, "La venta debe incluir al menos un producto");

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

                BigDecimal total = BigDecimal.ZERO;
                for (DetalleVenta d : peticion.getDetalles()) {
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
                        registrarMovimiento(c, p.getIdProducto(), TipoMovimientoInv.SALIDA, d.getCantidad(), "Venta #" + idVenta);
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

                try (PreparedStatement ps = c.prepareStatement("UPDATE ventas SET total = ? WHERE id_venta = ?")) {
                    ps.setBigDecimal(1, total);
                    ps.setInt(2, idVenta);
                    ps.executeUpdate();
                }
                c.commit();
                return buscarPorId(c, idVenta);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Elimina la venta con sus detalles y devuelve el stock, todo en una transacción. */
    public boolean eliminarCompleta(int id) throws SQLException {
        try (Connection c = Conexion.get()) {
            c.setAutoCommit(false);
            try {
                Venta v = buscarPorId(c, id);
                if (v == null) {
                    c.rollback();
                    return false;
                }
                for (DetalleVenta d : v.getDetalles()) {
                    if (d.getIdProducto() == null || d.getCantidad() == null) continue;
                    Producto p = productos.bloquear(c, d.getIdProducto());
                    if (p != null && controlaStock(p)) {
                        productos.cambiarStock(c, p.getIdProducto(), d.getCantidad());
                        registrarMovimiento(c, p.getIdProducto(), TipoMovimientoInv.ENTRADA, d.getCantidad(), "Cancelación de venta #" + id);
                    }
                }
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
