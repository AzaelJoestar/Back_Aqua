package aquacontrol.modelos;

import java.time.LocalDateTime;

/** POJO de la tabla movimientos_agua. */
public class MovimientoAgua {
    private Integer idMovAgua;
    private Integer idTanque;
    private Integer idUsuario;
    private Integer idRegistro;
    private TipoMovimientoAgua tipo;
    private Integer litros;
    private LocalDateTime fecha;
    private String motivo;

    public MovimientoAgua() {}

    public Integer getIdMovAgua() {
        return idMovAgua;
    }

    public void setIdMovAgua(Integer idMovAgua) {
        this.idMovAgua = idMovAgua;
    }

    public Integer getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(Integer idTanque) {
        this.idTanque = idTanque;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public TipoMovimientoAgua getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoAgua tipo) {
        this.tipo = tipo;
    }

    public Integer getLitros() {
        return litros;
    }

    public void setLitros(Integer litros) {
        this.litros = litros;
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
