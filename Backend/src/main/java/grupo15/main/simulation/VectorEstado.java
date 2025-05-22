package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;

public class VectorEstado {
    private Integer iteracion;
    private String evento;
    private Float reloj;
    private Pc[] pcs;
    private Tecnico tecnico;
    private Integer acumAbandonos;
    private Integer colaAlumnos;
    private Alumno[] alumnos;
}
