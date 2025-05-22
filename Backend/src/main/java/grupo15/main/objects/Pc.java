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
