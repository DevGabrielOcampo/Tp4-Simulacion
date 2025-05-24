package grupo15.main.objects;

import grupo15.main.states.EstadoAlumno;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Alumno implements Cloneable {

    private Integer id;

    private EstadoAlumno estado;

    // Parte relacionada con la llegada del tecnico
    private Float randomLlegada;
    private Float duracionLlegada;
    private Float tiempoLlegada;

    private Pc pcEnUso;

    // Parámetros para la distribucion exponencial negativa
    private Float media;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EstadoAlumno getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlumno estado) {
        this.estado = estado;
    }

    public Float getRandomLlegada() {
        return randomLlegada;
    }

    public void setRandomLlegada(Float randomLlegada) {
        this.randomLlegada = randomLlegada;
    }

    public Float getDuracionLlegada() {
        return duracionLlegada;
    }

    public void setDuracionLlegada(Float duracionLlegada) {
        this.duracionLlegada = duracionLlegada;
    }

    public Float getTiempoLlegada() {
        return tiempoLlegada;
    }

    public void setTiempoLlegada(Float tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public Pc getPcEnUso() {
        return pcEnUso;
    }

    public void setPcEnUso(Pc pcEnUso) {
        this.pcEnUso = pcEnUso;
    }

    public Float getMedia() {
        return media;
    }

    public void setMedia(Float media) {
        this.media = media;
    }

    @Override
    public Alumno clone() {
        try {
            Alumno clone = (Alumno) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}