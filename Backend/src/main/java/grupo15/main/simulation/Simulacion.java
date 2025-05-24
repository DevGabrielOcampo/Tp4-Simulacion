package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import grupo15.main.states.EstadoPc;

import java.util.Arrays;

public class Simulacion {
    private VectorEstado estado;

    public Simulacion(Float minInscripcion, Float maxInscripcion, Float mediaExponencialNegativa, Float minMantenimiento, Float maxMantenimiento, Float baseRegresoTecnico, Float rangoRegresoTecnico, Float minutosSimulacion, Float minutosDesde, Float iteracionesMostrar) {
        estado = inicializarEstado(minInscripcion, maxInscripcion, mediaExponencialNegativa, minMantenimiento, maxMantenimiento, baseRegresoTecnico, rangoRegresoTecnico, minutosSimulacion, minutosDesde, iteracionesMostrar);
    }

    private VectorEstado inicializarEstado(Float minInscripcion, Float maxInscripcion, Float mediaExponencialNegativa, Float minMantenimiento, Float maxMantenimiento, Float baseRegresoTecnico, Float rangoRegresoTecnico, Float minutosSimulacion, Float minutosDesde, Float iteracionesMostrar) {
        // Crear las 5 PC iniciales
        Pc[] pcs = new Pc[5];
        for (int i = 0; i < 5; i++) {
            pcs[i] = new Pc(
                    i + 1,        // ID de la PC (del 1 al 5)
                    EstadoPc.LIBRE,  // Estado inicial libre
                    0.0f,            // Random  (Creo que esto mas que un RND deberia ser el generador para pedirle los numeros y agregar atributo para el rnd)
                    0.0f,            // Duración de inscripción (valor inicial, aplicar distribución luego)
                    0.0f,           // Fin de inscripción (pendiente de ajuste según lógica)
                    minInscripcion,               // Parámetro min (definido más arriba)
                    maxInscripcion                // Parámetro max (definido más arriba)
            ); // Valores inventados para probar, hay que acomodar lógica

        }

        // Crear el técnico inicial
        Tecnico tecnico = new Tecnico(
                EstadoPc.LIBRE,       // Estado inicial
                0.0f,                 // Random de Regreso (Creo que esto mas que un RND deberia ser el generador para pedirle los numeros y agregar atributo para el rnd)
                0.0f,                 // Duración del descanso (a calcular después)
                0.0f,                 // Tiempo de regreso
                baseRegresoTecnico,   // Base de regreso
                rangoRegresoTecnico,  // Rango de regreso (±3 min)
                0.0f,                 // Random de mantenimiento (a calcular después)
                0.0f,                 // Duración del mantenimiento inicial (valor arbitrario, ajustar con distribución)
                0.0f,                 // Fin de mantenimiento (a calcular después)
                minMantenimiento,     // Min duración mantenimiento
                maxMantenimiento,     // Max duración mantenimiento
                null,                 // Última PC mantenida (aún no hizo mantenimiento)
                0.0f,                 // Acumulador tiempo ocioso
                0.0f,                 // Acumulador tiempo total
                0.0f                  // Promedio tiempo ocioso
        ); // Valores inventados para probar, hay que acomodar lógica



        // Retornar el estado inicial
        return new VectorEstado(0, "Inicialización", 0.0f, pcs, tecnico, 0, 0, new Alumno[0]);
    }

    public void ejecutar() {
        mostrarEstado();
    }

    private void mostrarEstado() {
        System.out.println("======================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================");
        System.out.printf("%-12s %-20s %-10s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s ",
                "Iteración", "Evento", "Reloj",
                "Rnd Alumno", "Tiempo Llegada", "Próx. Llegada",
                "Rnd Técnico", "Tiempo Regreso", "Próx. Regreso",
                "Abandonos", "Cola Alumnos",
                "Estado Técnico", "Fin Mantenimiento", "ABC");

        for (int i = 1; i <= 5; i++) {
            System.out.printf("%-15s %-15s %-15s ", "PC"+i, "RND", "Fin Inscripción");
        }

        System.out.printf("%-15s %-15s %-15s ", "Alumno", "Estado", "PC Ocupada");

        System.out.println();
        System.out.println("======================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================");

        // Imprimir datos en la misma línea
        System.out.printf("%-12d %-20s %-10.2f %-15.2f %-15.2f %-15.2f %-15.2f %-15.2f %-15.2f %-15d %-15d %-15s %-15.2f ",
                estado.getIteracion(), estado.getEvento(), estado.getReloj(),
                estado.getReloj(), estado.getReloj(), estado.getReloj(), // Cualquier fruta para que ande
                estado.getTecnico().getRandomRegreso(), estado.getTecnico().getDuracionDescanso(), estado.getTecnico().getTiempoRegreso(),
                estado.getAcumAbandonos(), estado.getColaAlumnos(),
                estado.getTecnico().getEstado(), estado.getTecnico().getFinMantenimiento());

        for (Pc pc : estado.getPcs()) {
            System.out.printf("%-15.2f %-15.2f %-15.2f ", pc.getRandom(), pc.getDuracionInscripcion(), pc.getFinInscripcion());
        }

        for (Alumno alumno : estado.getAlumnos()) {
            System.out.printf("%-15d %-15s %-15s ", alumno.getId(), alumno.getEstado(),
                    (alumno.getPcEnUso() != null ? alumno.getPcEnUso().getId() : "-"));
        }

        System.out.println();
        System.out.println("======================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================");
    }
}

