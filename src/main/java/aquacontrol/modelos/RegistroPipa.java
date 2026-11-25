package aquacontrol.modelos;

import java.time.LocalDateTime;

/** POJO de la tabla registro_pipas. */
public class RegistroPipa {
    private Integer idRegistro;
    private Integer idPipa;
    private Integer idUsuario;
    private Integer idCliente;
    private Integer idTanque;
    private TipoMovimientoAgua tipo;
    private Integer litros;
    private LocalDateTime fecha;
    private String notas;

    public RegistroPipa() {}

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Integer getIdPipa() {
        return idPipa;
    }

    public void setIdPipa(Integer idPipa) {
        this.idPipa = idPipa;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(Integer idTanque) {
        this.idTanque = idTanque;
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

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
