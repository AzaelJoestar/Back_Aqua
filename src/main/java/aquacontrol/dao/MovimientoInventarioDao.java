package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla movimientos_inventario: CRUD con JDBC. */
public class MovimientoInventarioDao extends BaseDao<MovimientoInventario> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_producto", "idProducto", Tipo.ENTERO),
        new Columna("tipo", "tipo", Tipo.ENUM),
        new Columna("cantidad", "cantidad", Tipo.ENTERO),
        new Columna("fecha", "fecha", Tipo.FECHA_HORA),
        new Columna("motivo", "motivo", Tipo.TEXTO));

    @Override
    protected String tabla() {
        return "movimientos_inventario";
    }

    @Override
    protected String pk() {
        return "id_movimiento";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected String tipoEnumBd(Columna col) {
        return "public.tipo_movimiento_inv";
    }

    @Override
    protected MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario o = new MovimientoInventario();
            o.setIdMovimiento(rs.getInt("id_movimiento"));
            o.setIdProducto(entero(rs, "id_producto"));
            o.setTipo(TipoMovimientoInv.desde(rs.getString("tipo")));
            o.setCantidad(entero(rs, "cantidad"));
            o.setFecha(fecha(rs, "fecha"));
            o.setMotivo(rs.getString("motivo"));
        return o;
    }

    @Override
    protected Object[] valores(MovimientoInventario o) {
        return new Object[] {
            o.getIdProducto(),
            o.getTipo() == null ? null : o.getTipo().getValor(),
            o.getCantidad(),
            o.getFecha(),
            o.getMotivo()
        };
    }

    @Override
    protected void asignarId(MovimientoInventario o, int id) {
        o.setIdMovimiento(id);
    }
}
