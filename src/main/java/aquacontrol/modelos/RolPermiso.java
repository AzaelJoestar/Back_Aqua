package aquacontrol.modelos;


/** POJO de la tabla roles_permisos. */
public class RolPermiso {
    private Integer idRolPermiso;
    private String rolNombre;
    private String rolDescripcion;
    private Boolean rolActivo;
    private String permisoClave;
    private String permisoModulo;
    private String permisoDescripcion;

    public RolPermiso() {}

    public Integer getIdRolPermiso() {
        return idRolPermiso;
    }

    public void setIdRolPermiso(Integer idRolPermiso) {
        this.idRolPermiso = idRolPermiso;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getRolDescripcion() {
        return rolDescripcion;
    }

    public void setRolDescripcion(String rolDescripcion) {
        this.rolDescripcion = rolDescripcion;
    }

    public Boolean getRolActivo() {
        return rolActivo;
    }

    public void setRolActivo(Boolean rolActivo) {
        this.rolActivo = rolActivo;
    }

    public String getPermisoClave() {
        return permisoClave;
    }

    public void setPermisoClave(String permisoClave) {
        this.permisoClave = permisoClave;
    }

    public String getPermisoModulo() {
        return permisoModulo;
    }

    public void setPermisoModulo(String permisoModulo) {
        this.permisoModulo = permisoModulo;
    }

    public String getPermisoDescripcion() {
        return permisoDescripcion;
    }

    public void setPermisoDescripcion(String permisoDescripcion) {
        this.permisoDescripcion = permisoDescripcion;
    }
}
