package aquacontrol.modelos;


/** POJO de la tabla tanques. */
public class Tanque {
    private Integer idTanque;
    private String nombre;
    private TipoTanque tipo;
    private Integer capacidadLitros;
    private Integer litrosActuales;
    private Integer litrosMinimos;

    public Tanque() {}

    public Integer getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(Integer idTanque) {
        this.idTanque = idTanque;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoTanque getTipo() {
        return tipo;
    }

    public void setTipo(TipoTanque tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidadLitros() {
        return capacidadLitros;
    }

    public void setCapacidadLitros(Integer capacidadLitros) {
        this.capacidadLitros = capacidadLitros;
    }

    public Integer getLitrosActuales() {
        return litrosActuales;
    }

    public void setLitrosActuales(Integer litrosActuales) {
        this.litrosActuales = litrosActuales;
    }

    public Integer getLitrosMinimos() {
        return litrosMinimos;
    }

    public void setLitrosMinimos(Integer litrosMinimos) {
        this.litrosMinimos = litrosMinimos;
    }
}
