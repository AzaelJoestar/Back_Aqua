package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla registro_pipas: CRUD con JDBC. */
public class RegistroPipaDao extends BaseDao<RegistroPipa> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("id_pipa", "idPipa", Tipo.ENTERO),
        new Columna("id_usuario", "idUsuario", Tipo.ENTERO),
        new Columna("id_cliente", "idCliente", Tipo.ENTERO),
        new Columna("id_tanque", "idTanque", Tipo.ENTERO),
        new Columna("tipo", "tipo", Tipo.ENUM),
        new Columna("litros", "litros", Tipo.ENTERO),
        new Columna("fecha", "fecha", Tipo.FECHA_HORA),
        new Columna("notas", "notas", Tipo.TEXTO));

    @Override
    protected String tabla() {
        return "registro_pipas";
    }

    @Override
    protected String pk() {
        return "id_registro";
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
    protected RegistroPipa mapear(ResultSet rs) throws SQLException {
        RegistroPipa o = new RegistroPipa();
            o.setIdRegistro(rs.getInt("id_registro"));
            o.setIdPipa(entero(rs, "id_pipa"));
            o.setIdUsuario(entero(rs, "id_usuario"));
            o.setIdCliente(entero(rs, "id_cliente"));
            o.setIdTanque(entero(rs, "id_tanque"));
            o.setTipo(TipoMovimientoAgua.desde(rs.getString("tipo")));
            o.setLitros(entero(rs, "litros"));
            o.setFecha(fecha(rs, "fecha"));
            o.setNotas(rs.getString("notas"));
        return o;
    }

    @Override
    protected Object[] valores(RegistroPipa o) {
        return new Object[] {
            o.getIdPipa(),
            o.getIdUsuario(),
            o.getIdCliente(),
            o.getIdTanque(),
            o.getTipo() == null ? null : o.getTipo().getValor(),
            o.getLitros(),
            o.getFecha(),
            o.getNotas()
        };
    }

    @Override
    protected void asignarId(RegistroPipa o, int id) {
        o.setIdRegistro(id);
    }
}
