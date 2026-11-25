package aquacontrol.dao;

import aquacontrol.config.Conexion;
import aquacontrol.modelos.*;
import aquacontrol.util.ApiException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de la tabla usuarios: CRUD con JDBC. */
public class UsuarioDao extends BaseDao<Usuario> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("nombre", "nombre", Tipo.TEXTO),
        new Columna("apellido", "apellido", Tipo.TEXTO),
        new Columna("usuario", "usuario", Tipo.TEXTO),
        new Columna("password", "password", Tipo.TEXTO),
        new Columna("id_rol", "idRol", Tipo.ENTERO),
        new Columna("estado", "estado", Tipo.TEXTO),
        new Columna("reset_solicitado", "resetSolicitado", Tipo.ENTERO),
        new Columna("cambiar_password", "cambiarPassword", Tipo.ENTERO));

    @Override
    protected String tabla() {
        return "usuarios";
    }

    @Override
    protected String pk() {
        return "id_usuario";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected Usuario mapear(ResultSet rs) throws SQLException {
        Usuario o = new Usuario();
            o.setIdUsuario(rs.getInt("id_usuario"));
            o.setNombre(rs.getString("nombre"));
            o.setApellido(rs.getString("apellido"));
            o.setUsuario(rs.getString("usuario"));
            o.setPassword(rs.getString("password"));
            o.setIdRol(entero(rs, "id_rol"));
            o.setEstado(rs.getString("estado"));
            o.setResetSolicitado(entero(rs, "reset_solicitado"));
            o.setCambiarPassword(entero(rs, "cambiar_password"));
        return o;
    }

    @Override
    protected Object[] valores(Usuario o) {
        return new Object[] {
            o.getNombre(),
            o.getApellido(),
            o.getUsuario(),
            o.getPassword(),
            o.getIdRol(),
            o.getEstado(),
            o.getResetSolicitado(),
            o.getCambiarPassword()
        };
    }

    @Override
    protected void asignarId(Usuario o, int id) {
        o.setIdUsuario(id);
    }

    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void actualizarPassword(int id, String hash) throws SQLException {
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET password = ? WHERE id_usuario = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Cambio de contraseña hecho por el propio usuario: guarda el hash y quita la marca de "debe cambiarla". */
    public void cambiarPassword(int id, String hash) throws SQLException {
        try (Connection c = Conexion.get();
             PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET password = ?, cambiar_password = 0 WHERE id_usuario = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
