package aquacontrol.modelos;


/** POJO de la tabla usuarios. */
public class Usuario {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String usuario;
    private String password;
    private Integer idRol;
    private String estado;
    private Integer resetSolicitado;
    private Integer cambiarPassword;

    public Usuario() {}

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getResetSolicitado() {
        return resetSolicitado;
    }

    public void setResetSolicitado(Integer resetSolicitado) {
        this.resetSolicitado = resetSolicitado;
    }

    public Integer getCambiarPassword() {
        return cambiarPassword;
    }

    public void setCambiarPassword(Integer cambiarPassword) {
        this.cambiarPassword = cambiarPassword;
    }
}
