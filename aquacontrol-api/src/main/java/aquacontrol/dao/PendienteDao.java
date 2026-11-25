package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla pendientes: CRUD con JDBC. */
public class PendienteDao extends BaseDao<Pendiente> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_cliente", "idCliente", Tipo.ENTERO),
        new Columna("nombre_temporal", "nombreTemporal", Tipo.TEXTO),
        new Columna("telefono_temporal", "telefonoTemporal", Tipo.TEXTO),
        new Columna("tipo", "tipo", Tipo.TEXTO),
        new Columna("descripcion", "descripcion", Tipo.TEXTO),
        new Columna("estado", "estado", Tipo.TEXTO),
        new Columna("pagado", "pagado", Tipo.ENTERO),
        new Columna("fecha_recepcion", "fechaRecepcion", Tipo.FECHA_HORA),
        new Columna("fecha_entrega", "fechaEntrega", Tipo.FECHA_HORA),
        new Columna("notas", "notas", Tipo.TEXTO),
        new Columna("venta_generada", "ventaGenerada", Tipo.ENTERO),
        new Columna("apellido", "apellido", Tipo.TEXTO),
        new Columna("clave", "clave", Tipo.TEXTO),
        new Columna("cantidad", "cantidad", Tipo.ENTERO));

    @Override
    protected String tabla() {
        return "pendientes";
    }

    @Override
    protected String pk() {
        return "id_pendiente";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Pendiente mapear(ResultSet rs) throws SQLException {
        Pendiente o = new Pendiente();
            o.setIdPendiente(rs.getInt("id_pendiente"));
            o.setIdCliente(entero(rs, "id_cliente"));
            o.setNombreTemporal(rs.getString("nombre_temporal"));
            o.setTelefonoTemporal(rs.getString("telefono_temporal"));
            o.setTipo(rs.getString("tipo"));
            o.setDescripcion(rs.getString("descripcion"));
            o.setEstado(rs.getString("estado"));
            o.setPagado(entero(rs, "pagado"));
            o.setFechaRecepcion(fecha(rs, "fecha_recepcion"));
            o.setFechaEntrega(fecha(rs, "fecha_entrega"));
            o.setNotas(rs.getString("notas"));
            o.setVentaGenerada(entero(rs, "venta_generada"));
            o.setApellido(rs.getString("apellido"));
            o.setClave(rs.getString("clave"));
            o.setCantidad(entero(rs, "cantidad"));
        return o;
    }

    @Override
    protected Object[] valores(Pendiente o) {
        return new Object[] {
            o.getIdCliente(),
            o.getNombreTemporal(),
            o.getTelefonoTemporal(),
            o.getTipo(),
            o.getDescripcion(),
            o.getEstado(),
            o.getPagado(),
            o.getFechaRecepcion(),
            o.getFechaEntrega(),
            o.getNotas(),
            o.getVentaGenerada(),
            o.getApellido(),
            o.getClave(),
            o.getCantidad()
        };
    }

    @Override
    protected void asignarId(Pendiente o, int id) {
        o.setIdPendiente(id);
    }
}
