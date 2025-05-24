package grupo15.main.objects;

import grupo15.main.states.EstadoPc;
import lombok.Builder;
import lombok.Data;

@Data // Da los getters, setters y el toString
@Builder // Este da el constructor
public class Pc implements Cloneable {

    private Integer id;

    @Builder.Default
    private EstadoPc estado = EstadoPc.LIBRE;

    private Float random;

    private Float duracionInscripcion;

    private Float finInscripcion;

    // Parámetros para las distribuciones uniformes
    private Float min;  // a
    private Float max;  // b

    public Pc(Integer id, EstadoPc estado, Float random, Float duracionInscripcion, Float finInscripcion, Float min, Float max) {
        this.id = id;
        this.estado = estado;
        this.random = random;
        this.duracionInscripcion = duracionInscripcion;
        this.finInscripcion = finInscripcion;
        this.min = min;
        this.max = max;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EstadoPc getEstado() {
        return estado;
    }

    public void setEstado(EstadoPc estado) {
        this.estado = estado;
    }

    public Float getRandom() {
        return random;
    }

    public void setRandom(Float random) {
        this.random = random;
    }

    public Float getDuracionInscripcion() {
        return duracionInscripcion;
    }

    public void setDuracionInscripcion(Float duracionInscripcion) {
        this.duracionInscripcion = duracionInscripcion;
    }

    public Float getFinInscripcion() {
        return finInscripcion;
    }

    public void setFinInscripcion(Float finInscripcion) {
        this.finInscripcion = finInscripcion;
    }

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    @Override
    public Pc clone() {
        try {
            Pc clone = (Pc) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
