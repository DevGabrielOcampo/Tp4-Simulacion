package grupo15.main.objects;

import grupo15.main.states.EstadoAlumno;
import lombok.Builder;
import lombok.Data;

import java.util.Random;

@Data
@Builder
public class Alumno implements Cloneable {

    private static int contadorId = 0;
    private Integer id;

    private EstadoAlumno estado;


    // Parte relacionada con la llegada del tecnico
    private Random generador;
    private Float randomLlegada;
    private Float duracionLlegada;

    private Pc pcEnUso;

    // Parámetros para la distribucion exponencial negativa
    private Float media;

    public Alumno(Float media) {
        this.id = contadorId++;
        this.estado = EstadoAlumno.LLEGANDO;
        this.generador = new Random();
        this.randomLlegada = this.generador.nextFloat();
        this.duracionLlegada = tiempoProximaLlegada(this.randomLlegada, media);
        this.pcEnUso = null;
    }

    public Float tiempoProximaLlegada(Float random, Float media){
        return (float) ((-media)*Math.log(1-random));
    }

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

    public Random getGenerador() {
        return generador;
    }

    public void setGenerador(Random generador) {
        this.generador = generador;
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