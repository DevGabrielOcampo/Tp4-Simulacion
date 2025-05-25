package grupo15.main.objects;

import grupo15.main.states.EstadoPc;
import lombok.Builder;
import lombok.Data;

import java.util.Random;

@Data // Da los getters, setters y el toString
@Builder // Este da el constructor
public class Pc implements Cloneable {

    private Integer id;

    @Builder.Default
    private EstadoPc estado = EstadoPc.LIBRE;

    private Random randomInscripcion;
    private Float numRandomInscripcion;
    private Float duracionInscripcion;

    // Parámetros para las distribuciones uniformes
    private Float min;  // a
    private Float max;  // b

    public void generarDuracionInscripcion(){
        this.numRandomInscripcion = this.randomInscripcion.nextFloat();
        this.duracionInscripcion = this.min + this.numRandomInscripcion*(this.max-this.min);
    }

    public Pc(Integer id, EstadoPc estado, Float numRandomInscripcion, Float duracionInscripcion, Float min, Float max) {
        this.id = id;
        this.estado = estado;
        this.randomInscripcion = new Random();
        this.numRandomInscripcion = numRandomInscripcion;
        this.duracionInscripcion = duracionInscripcion;
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

    public Random getRandomInscripcion() {
        return randomInscripcion;
    }

    public void setRandomInscripcion(Random randomInscripcion) {
        this.randomInscripcion = randomInscripcion;
    }

    public Float getNumRandomInscripcion() {
        return numRandomInscripcion;
    }

    public void setNumRandomInscripcion(Float numRandomInscripcion) {
        this.numRandomInscripcion = numRandomInscripcion;
    }

    public Float getDuracionInscripcion() {
        return duracionInscripcion;
    }

    public void setDuracionInscripcion(Float duracionInscripcion) {
        this.duracionInscripcion = duracionInscripcion;
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
