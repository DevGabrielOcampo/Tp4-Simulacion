package grupo15.main.controllers;
// Forma parte del paquete grupo15.main.Controller

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import grupo15.main.simulation.Simulacion;
import grupo15.main.simulation.VectorEstado;
import grupo15.main.states.EstadoPc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.66:3000"})
// Permitimos el acceso CORS desde dos origenes distintos,
// para comunicarnos con el front

@RestController
@RequestMapping("/api")
public class Controller {
    @GetMapping("/vector/{datos}")
    // Recibe 4 floats, el primer representa la muestra, el segundo la distribucion, el tercero y cuarto los intervalos [A, B]
    // el quinto desviacion, el sexto media, el septimo lambda
    public List<VectorEstado> recibirParametros(@PathVariable List<Float> datos) {
        Float minInscripcion = datos.get(0); // Parametro A de la distribuicion uniforme para las inscripciones
        Float maxInscripcion = datos.get(1); // Parametro B de la distribuicion uniforme para las inscripciones
        Float mediaExponencialNegativa = datos.get(2); // Media de la exponencial negativa de las llegada de los alumnos
        Float minMantenimiento = datos.get(3); // Parametro A de la distribuicion uniforme del tiempo de mantenimiento
        Float maxMantenimiento = datos.get(4); // Parametro B de la distribuicion uniforme del tiempo de mantenimiento
        Float baseRegresoTecnico = datos.get(5); // Tiempo base de regreso del tecnico (1h)
        Float rangoRegresoTecnico = datos.get(6); // Rango de regreso del tecnico (+-3min)
        Float minutosSimulacion = datos.get(7);
        Float minutosDesde = datos.get(8);
        Float iteracionesMostrar = datos.get(9);

        Simulacion simulacion = new Simulacion(minInscripcion, maxInscripcion, mediaExponencialNegativa, minMantenimiento, maxMantenimiento, baseRegresoTecnico, rangoRegresoTecnico, minutosSimulacion, minutosDesde, iteracionesMostrar);
        return simulacion.ejecutar(minutosSimulacion, minutosDesde, iteracionesMostrar, mediaExponencialNegativa);

    }
}