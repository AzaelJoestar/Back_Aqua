package aquacontrol.modelos;


/** POJO de la tabla pipas. */
public class Pipa {
    private Integer idPipa;
    private String placa;
    private String conductor;
    private Integer capacidadLitros;
    private Boolean activa;

    public Pipa() {}

    public Integer getIdPipa() {
        return idPipa;
    }

    public void setIdPipa(Integer idPipa) {
        this.idPipa = idPipa;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public Integer getCapacidadLitros() {
        return capacidadLitros;
    }

    public void setCapacidadLitros(Integer capacidadLitros) {
        this.capacidadLitros = capacidadLitros;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
