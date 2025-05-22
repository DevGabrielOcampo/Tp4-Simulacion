package grupo15.main.objects;

import grupo15.main.states.EstadoPc;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tecnico implements Cloneable {

    @Builder.Default
    private EstadoPc estado = EstadoPc.LIBRE;

    // Parte relacionada con la llegada del tecnico
    private Float randomLlegada;
    private Float duracionDescanso;
    private Float tiempoRegreso;
    // Parámetros para las distribucion de regreso
    private Float base;  // 1h
    private Float rango;  // +-3min

    // Parte relacionada con la llegada del tecnico
    private Float randomMantenimiento;
    private Float duracionMantenimiento;
    private Float finMantenimiento;
    // Parámetros para las distribuciones uniformes en los mantenimientos
    private Float min;  // a
    private Float max;  // b

    private Pc ultimaPcMantenida;

    private Float acumTiempoOcioso;
    private Float acumTiempoTotal;
    private Float promedioTiempoOcioso;



    @Override
    public Tecnico clone() {
        try {
            Tecnico clone = (Tecnico) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}