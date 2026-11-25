package aquacontrol.modelos;

import java.time.LocalDateTime;

/** POJO de la tabla pendientes. */
public class Pendiente {
    private Integer idPendiente;
    private Integer idCliente;
    private String nombreTemporal;
    private String telefonoTemporal;
    private String tipo;
    private String descripcion;
    private String estado;
    private Integer pagado;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime fechaEntrega;
    private String notas;
    private Integer ventaGenerada;
    private String apellido;
    private String clave;
    private Integer cantidad;

    public Pendiente() {}

    public Integer getIdPendiente() {
        return idPendiente;
    }

    public void setIdPendiente(Integer idPendiente) {
        this.idPendiente = idPendiente;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreTemporal() {
        return nombreTemporal;
    }

    public void setNombreTemporal(String nombreTemporal) {
        this.nombreTemporal = nombreTemporal;
    }

    public String getTelefonoTemporal() {
        return telefonoTemporal;
    }

    public void setTelefonoTemporal(String telefonoTemporal) {
        this.telefonoTemporal = telefonoTemporal;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getPagado() {
        return pagado;
    }

    public void setPagado(Integer pagado) {
        this.pagado = pagado;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Integer getVentaGenerada() {
        return ventaGenerada;
    }

    public void setVentaGenerada(Integer ventaGenerada) {
        this.ventaGenerada = ventaGenerada;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
