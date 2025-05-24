package grupo15.main.controllers;
// Forma parte del paquete grupo15.main.Controller

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
    public ResponseEntity<List<Float>> recibirParametros(@PathVariable List<Float> datos) {
        Float demora_inscripcion_a = 5.0f;
        Float demora_inscripcion_b = 8.0f;
        Float media_exponecial_llegada = 2.0f;
        Float demora_mantenimiento_a = 3.0f;
        Float demora_mantenimiento_b = 10f;
        Float regreso_tecnico_base = 60.0f;
        Float regreso_tecnico_rango = 3.0f;

        //demora_inscripcion_a = datos.get(0); // Parametro A de la distribuicion uniforme para las inscripciones
        //demora_inscripcion_b = datos.get(1); // Parametro B de la distribuicion uniforme para las inscripciones
        //media_exponecial_llegada = datos.get(2); // Media de la exponencial negativa de las llegada de los alumnos
        //demora_mantenimiento_a = datos.get(3); // Parametro A de la distribuicion uniforme del tiempo de mantenimiento
        //demora_mantenimiento_b = datos.get(4); // Parametro B de la distribuicion uniforme del tiempo de mantenimiento
        //regreso_tecnico_base = datos.get(5); // Tiempo base de regreso del tecnico (1h)
        //regreso_tecnico_rango = datos.get(6); // Rango de regreso del tecnico (+-3min)


        return null;
    }
}