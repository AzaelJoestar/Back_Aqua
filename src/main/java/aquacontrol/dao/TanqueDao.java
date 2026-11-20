package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla tanques: CRUD con JDBC. */
public class TanqueDao extends BaseDao<Tanque> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("nombre", "nombre", Tipo.TEXTO),
        new Columna("tipo", "tipo", Tipo.ENUM),
        new Columna("capacidad_litros", "capacidadLitros", Tipo.ENTERO),
        new Columna("litros_actuales", "litrosActuales", Tipo.ENTERO),
        new Columna("litros_minimos", "litrosMinimos", Tipo.ENTERO));

    @Override
    protected String tabla() {
        return "tanques";
    }

    @Override
    protected String pk() {
        return "id_tanque";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected String tipoEnumBd(Columna col) {
        return "public.tipo_tanque";
    }

    @Override
    protected Tanque mapear(ResultSet rs) throws SQLException {
        Tanque o = new Tanque();
            o.setIdTanque(rs.getInt("id_tanque"));
            o.setNombre(rs.getString("nombre"));
            o.setTipo(TipoTanque.desde(rs.getString("tipo")));
            o.setCapacidadLitros(entero(rs, "capacidad_litros"));
            o.setLitrosActuales(entero(rs, "litros_actuales"));
            o.setLitrosMinimos(entero(rs, "litros_minimos"));
        return o;
    }

    @Override
    protected Object[] valores(Tanque o) {
        return new Object[] {
            o.getNombre(),
            o.getTipo() == null ? null : o.getTipo().getValor(),
            o.getCapacidadLitros(),
            o.getLitrosActuales(),
            o.getLitrosMinimos()
        };
    }

    @Override
    protected void asignarId(Tanque o, int id) {
        o.setIdTanque(id);
    }
}
