package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import grupo15.main.utils.Generators.*;

@Builder
@Getter
@Data
public class VectorEstado {

    private Integer iteracion;
    @Builder.Default
    private String evento = "Inicialización";
    private Float reloj;
    private Pc[] pcs;
    private Tecnico tecnico;
    private Integer acumAbandonos;
    private Integer colaAlumnos;
    private Alumno[] alumnos;

}