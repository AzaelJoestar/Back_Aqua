package aquacontrol.modelos;

import java.time.LocalDateTime;

/** POJO de la tabla movimientos_inventario. */
public class MovimientoInventario {
    private Integer idMovimiento;
    private Integer idProducto;
    private TipoMovimientoInv tipo;
    private Integer cantidad;
    private LocalDateTime fecha;
    private String motivo;

    public MovimientoInventario() {}

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public TipoMovimientoInv getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoInv tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
