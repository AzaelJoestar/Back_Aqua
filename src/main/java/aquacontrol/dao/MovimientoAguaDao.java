package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla movimientos_agua: CRUD con JDBC. */
public class MovimientoAguaDao extends BaseDao<MovimientoAgua> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_tanque", "idTanque", Tipo.ENTERO),
        new Columna("id_usuario", "idUsuario", Tipo.ENTERO),
        new Columna("id_registro", "idRegistro", Tipo.ENTERO),
        new Columna("tipo", "tipo", Tipo.ENUM),
        new Columna("litros", "litros", Tipo.ENTERO),
        new Columna("fecha", "fecha", Tipo.FECHA_HORA),
        new Columna("motivo", "motivo", Tipo.TEXTO));

    @Override
    protected String tabla() {
        return "movimientos_agua";
    }

    @Override
    protected String pk() {
        return "id_mov_agua";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected String tipoEnumBd(Columna col) {
        return "public.tipo_movimiento_agua";
    }

    @Override
    protected MovimientoAgua mapear(ResultSet rs) throws SQLException {
        MovimientoAgua o = new MovimientoAgua();
            o.setIdMovAgua(rs.getInt("id_mov_agua"));
            o.setIdTanque(entero(rs, "id_tanque"));
            o.setIdUsuario(entero(rs, "id_usuario"));
            o.setIdRegistro(entero(rs, "id_registro"));
            o.setTipo(TipoMovimientoAgua.desde(rs.getString("tipo")));
            o.setLitros(entero(rs, "litros"));
            o.setFecha(fecha(rs, "fecha"));
            o.setMotivo(rs.getString("motivo"));
        return o;
    }

    @Override
    protected Object[] valores(MovimientoAgua o) {
        return new Object[] {
            o.getIdTanque(),
            o.getIdUsuario(),
            o.getIdRegistro(),
            o.getTipo() == null ? null : o.getTipo().getValor(),
            o.getLitros(),
            o.getFecha(),
            o.getMotivo()
        };
    }

    @Override
    protected void asignarId(MovimientoAgua o, int id) {
        o.setIdMovAgua(id);
    }
}
