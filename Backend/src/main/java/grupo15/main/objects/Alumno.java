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