package aquacontrol.dao;

import aquacontrol.modelos.*;
import java.sql.*;
import java.util.List;

/** DAO de la tabla roles_permisos: CRUD con JDBC. */
public class RolPermisoDao extends BaseDao<RolPermiso> {
    private static final List<Columna> COLUMNAS = List.of(
        new Columna("rol_nombre", "rolNombre", Tipo.TEXTO),
        new Columna("rol_descripcion", "rolDescripcion", Tipo.TEXTO),
        new Columna("rol_activo", "rolActivo", Tipo.BOOLEANO),
        new Columna("permiso_clave", "permisoClave", Tipo.TEXTO),
        new Columna("permiso_modulo", "permisoModulo", Tipo.TEXTO),
        new Columna("permiso_descripcion", "permisoDescripcion", Tipo.TEXTO));

    @Override
    protected String tabla() {
        return "roles_permisos";
    }

    @Override
    protected String pk() {
        return "id_rol_permiso";
    }

    @Override
    protected List<Columna> columnas() {
        return COLUMNAS;
    }

    @Override
    protected RolPermiso mapear(ResultSet rs) throws SQLException {
        RolPermiso o = new RolPermiso();
            o.setIdRolPermiso(rs.getInt("id_rol_permiso"));
            o.setRolNombre(rs.getString("rol_nombre"));
            o.setRolDescripcion(rs.getString("rol_descripcion"));
            o.setRolActivo(booleano(rs, "rol_activo"));
            o.setPermisoClave(rs.getString("permiso_clave"));
            o.setPermisoModulo(rs.getString("permiso_modulo"));
            o.setPermisoDescripcion(rs.getString("permiso_descripcion"));
        return o;
    }

    @Override
    protected Object[] valores(RolPermiso o) {
        return new Object[] {
            o.getRolNombre(),
            o.getRolDescripcion(),
            o.getRolActivo(),
            o.getPermisoClave(),
            o.getPermisoModulo(),
            o.getPermisoDescripcion()
        };
    }

    @Override
    protected void asignarId(RolPermiso o, int id) {
        o.setIdRolPermiso(id);
    }
}
