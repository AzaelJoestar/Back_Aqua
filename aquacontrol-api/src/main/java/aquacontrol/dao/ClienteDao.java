package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla clientes: CRUD con JDBC. */
public class ClienteDao extends BaseDao<Cliente> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("nombre", "nombre", Tipo.TEXTO),
        new Columna("apellido", "apellido", Tipo.TEXTO),
        new Columna("telefono", "telefono", Tipo.TEXTO),
        new Columna("referencia", "referencia", Tipo.TEXTO),
        new Columna("notas", "notas", Tipo.TEXTO));

    @Override
    protected String tabla() {
        return "clientes";
    }

    @Override
    protected String pk() {
        return "id_cliente";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Cliente mapear(ResultSet rs) throws SQLException {
        Cliente o = new Cliente();
            o.setIdCliente(rs.getInt("id_cliente"));
            o.setNombre(rs.getString("nombre"));
            o.setApellido(rs.getString("apellido"));
            o.setTelefono(rs.getString("telefono"));
            o.setReferencia(rs.getString("referencia"));
            o.setNotas(rs.getString("notas"));
        return o;
    }

    @Override
    protected Object[] valores(Cliente o) {
        return new Object[] {
            o.getNombre(),
            o.getApellido(),
            o.getTelefono(),
            o.getReferencia(),
            o.getNotas()
        };
    }

    @Override
    protected void asignarId(Cliente o, int id) {
        o.setIdCliente(id);
    }
}
