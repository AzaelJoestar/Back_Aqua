package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla pipas: CRUD con JDBC. */
public class PipaDao extends BaseDao<Pipa> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("placa", "placa", Tipo.TEXTO),
        new Columna("conductor", "conductor", Tipo.TEXTO),
        new Columna("capacidad_litros", "capacidadLitros", Tipo.ENTERO),
        new Columna("activa", "activa", Tipo.BOOLEANO));

    @Override
    protected String tabla() {
        return "pipas";
    }

    @Override
    protected String pk() {
        return "id_pipa";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Pipa mapear(ResultSet rs) throws SQLException {
        Pipa o = new Pipa();
            o.setIdPipa(rs.getInt("id_pipa"));
            o.setPlaca(rs.getString("placa"));
            o.setConductor(rs.getString("conductor"));
            o.setCapacidadLitros(entero(rs, "capacidad_litros"));
            o.setActiva(booleano(rs, "activa"));
        return o;
    }

    @Override
    protected Object[] valores(Pipa o) {
        return new Object[] {
            o.getPlaca(),
            o.getConductor(),
            o.getCapacidadLitros(),
            o.getActiva()
        };
    }

    @Override
    protected void asignarId(Pipa o, int id) {
        o.setIdPipa(id);
    }
}
