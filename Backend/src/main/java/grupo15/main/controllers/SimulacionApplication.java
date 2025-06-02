package grupo15.main.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.io.Serializable;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@RequestMapping("/simulacion")
@CrossOrigin(origins = "http://localhost:3000")
public class SimulacionApplication {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(SimulacionApplication.class, args);
    }

    @GetMapping(value = "/run-parametros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> runSimulationWithParameters(
            @RequestParam("minInscripcion") Float minInscripcion,
            @RequestParam("maxInscripcion") Float maxInscripcion,
            @RequestParam("mediaLlegada") Float mediaLlegada,
            @RequestParam("minMantenimiento") Float minMantenimiento,
            @RequestParam("maxMantenimiento") Float maxMantenimiento,
            @RequestParam("baseRegresoTecnico") Float baseRegresoTecnico,
            @RequestParam("rangoRegresoTecnico") Float rangoRegresoTecnico,
            @RequestParam("minutosSimulacion") Float minutosSimulacion,
            @RequestParam("minutoDesde") Float minutoDesde,
            @RequestParam("iteracionesMostrar") Float iteracionesMostrar) {

// Validaciones de parámetros
        if (minutosSimulacion <= 0) {
            throw new IllegalArgumentException("Los minutos de simulación deben ser mayores a 0.");
        }
        if (minutoDesde < 0) {
            throw new IllegalArgumentException("El minuto 'desde' no puede ser negativo.");
        }
        if (minutoDesde >= minutosSimulacion) {
            throw new IllegalArgumentException("El minuto 'desde' debe ser menor que el total de minutos a simular.");
        }
        if (iteracionesMostrar <= 0) {
            throw new IllegalArgumentException("Las iteraciones a mostrar deben ser al menos 1.");
        }
        if (minInscripcion > maxInscripcion) {
            throw new IllegalArgumentException("El tiempo mínimo de inscripción no puede ser mayor que el máximo.");
        }
        if (minMantenimiento > maxMantenimiento) {
            throw new IllegalArgumentException("El tiempo mínimo de mantenimiento no puede ser mayor que el máximo.");
        }
        if (baseRegresoTecnico < 0) {
            throw new IllegalArgumentException("El tiempo base de regreso del técnico no puede ser negativo.");
        }
        if (rangoRegresoTecnico < 0) {
            throw new IllegalArgumentException("El rango de regreso del técnico no puede ser negativo.");
        }
        if (mediaLlegada <= 0) {
            throw new IllegalArgumentException("La media de llegada de alumnos debe ser positiva.");
        }

        Simulacion simulacion = new Simulacion(
                5,
                minInscripcion,
                maxInscripcion,
                mediaLlegada,
                minMantenimiento,
                maxMantenimiento,
                baseRegresoTecnico,
                rangoRegresoTecnico
        );

        StreamingResponseBody stream = output -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(output)) {
                writer.write("[\n");

                AtomicInteger iteracionesMostradas = new AtomicInteger(0);

                simulacion.simularStream(
                        minutosSimulacion.doubleValue(),
                        minutoDesde.doubleValue(),
                        iteracionesMostrar.intValue(),
                        estado -> {
                            try {
                                if (iteracionesMostradas.get() > 0) {
                                    writer.write(",\n");
                                }
                                writer.write(objectMapper.writeValueAsString(estado));
                                iteracionesMostradas.incrementAndGet();
                            } catch (IOException e) {
                                throw new RuntimeException("Error al escribir JSON", e);
                            }
                        }
                );

                writer.write("\n]");
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }
}

class Simulacion implements Serializable {
    private static final int RESET_INTERVAL = 500;
    private final List<Map<String, Object>> equipos;
    private final float infInscripcion;
    private final float supInscripcion;
    private final double mediaLlegada;
    private final float minMantenimiento;
    private final float maxMantenimiento;
    private final float baseRegresoTecnico;
    private final float rangoRegresoTecnico;

    private int cola;
    private double tiempoActual;
    private int contadorAlumnos;
    private final Map<String, String> estadoAlumnos;
    private final List<String> alumnosEnCola;
    private Double proximoRegresoTecnico;
    private int proximaComputadoraMantenimiento;
    private double proximaLlegada;

    private double tiempoOciosoTecnicoAcumulado;
    private double tiempoTrabajadoTecnicoAcumulado;
    private int cantidadMantenimientosCompletados;
    private int iteracionesDesdeUltimoReset;

    public Simulacion(int equiposCount, float infInscripcion, float supInscripcion,
                      double mediaLlegada, float minMantenimiento, float maxMantenimiento,
                      float baseRegresoTecnico, float rangoRegresoTecnico) {
        this.equipos = new ArrayList<>();
        for (int i = 0; i < equiposCount; i++) {
            Map<String, Object> equipo = new HashMap<>();
            equipo.put("id", i + 1);
            equipo.put("estado", "Libre");
            equipo.put("fin_inscripcion", null);
            equipo.put("fin_mantenimiento", null);
            equipo.put("alumno_actual", null);
            this.equipos.add(equipo);
        }
        this.infInscripcion = infInscripcion;
        this.supInscripcion = supInscripcion;
        this.mediaLlegada = mediaLlegada;
        this.minMantenimiento = minMantenimiento;
        this.maxMantenimiento = maxMantenimiento;
        this.baseRegresoTecnico = baseRegresoTecnico;
        this.rangoRegresoTecnico = rangoRegresoTecnico;

        this.cola = 0;
        this.tiempoActual = 0;
        this.contadorAlumnos = 0;
        this.estadoAlumnos = new LinkedHashMap<>();
        this.alumnosEnCola = new ArrayList<>();
        this.proximoRegresoTecnico = null;
        this.proximaComputadoraMantenimiento = 0;
        this.proximaLlegada = 0;

        this.tiempoOciosoTecnicoAcumulado = 0;
        this.tiempoTrabajadoTecnicoAcumulado = 0;
        this.cantidadMantenimientosCompletados = 0;
        this.iteracionesDesdeUltimoReset = 0;
    }

    public void simularStream(double tiempoTotal, double minutoDesde, int iteracionesMostrar,
                              Consumer<Map<String, Object>> consumer) {
        int iteracionesMostradasCount = 0;
        int iteracion = 0;

// Inicialización
        double[] llegadaResult = generarTiempoLlegada();
        double rndLlegada = llegadaResult[0];
        double tiempoLlegada = llegadaResult[1];
        proximaLlegada = tiempoLlegada;

// Configurar primer mantenimiento
        Map<String, Object> primerEquipo = equipos.get(0);
        double[] mantResult = generarTiempoMantenimiento();
        primerEquipo.put("estado", "Mantenimiento");
        primerEquipo.put("fin_mantenimiento", tiempoActual + mantResult[1]);
        proximaComputadoraMantenimiento = 1;

// Procesar estado inicial
        if (tiempoActual >= minutoDesde) {
            consumer.accept(crearEstadoInicial(rndLlegada, tiempoLlegada, mantResult));
            iteracionesMostradasCount++;
        }

// Bucle principal
        while (tiempoActual < tiempoTotal && iteracion < 100000 && iteracionesMostradasCount < iteracionesMostrar) {
            iteracion++;
            iteracionesDesdeUltimoReset++;

// Limpieza periódica
            if (iteracionesDesdeUltimoReset % RESET_INTERVAL == 0) {
                estadoAlumnos.entrySet().removeIf(entry -> "AF".equals(entry.getValue()));
                alumnosEnCola.clear();
                alumnosEnCola.addAll(estadoAlumnos.entrySet().stream()
                        .filter(entry -> "EC".equals(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()));
                iteracionesDesdeUltimoReset = 0;
            }

            Object[] evento = obtenerProximoEvento();
            double tiempoEvento = (double) evento[1];
            if (tiempoEvento > tiempoTotal) break;

// Actualizar tiempo ocioso
            double duracion = tiempoEvento - tiempoActual;
            if (!isTechnicianBusyMaintaining() && (proximoRegresoTecnico == null || proximoRegresoTecnico <= tiempoActual)) {
                tiempoOciosoTecnicoAcumulado += duracion;
            }

            tiempoActual = tiempoEvento;
            Map<String, Object> estado = procesarEvento(evento, iteracion);

// Asegurar que todos los alumnos se muestren
            estadoAlumnos.forEach((id, estadoAlumno) -> {
                estado.put("Estado " + id, estadoAlumno);
                if ("SA".equals(estadoAlumno)) {
                    for (Map<String, Object> equipo : equipos) {
                        if (id.equals(equipo.get("alumno_actual"))) {
                            estado.put("Alumno M" + equipo.get("id"), id);
                        }
                    }
                }
            });

            if (tiempoActual >= minutoDesde && iteracionesMostradasCount < iteracionesMostrar) {
                consumer.accept(estado);
                iteracionesMostradasCount++;
            }
        }
    }

    private Map<String, Object> crearEstadoInicial(double rndLlegada, double tiempoLlegada, double[] mantResult) {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Iteracion", 0);
        estado.put("Evento", "Inicializacion");
        estado.put("Reloj", redondear(tiempoActual));
        estado.put("RND Llegada", redondear(rndLlegada));
        estado.put("Tiempo Llegada", redondear(tiempoLlegada));
        estado.put("Próxima Llegada", redondear(proximaLlegada));

        for (int i = 0; i < 5; i++) {
            Map<String, Object> equipo = equipos.get(i);
            estado.put("Máquina " + (i+1), equipo.get("estado"));
            estado.put("Fin Inscripción M" + (i+1), null);
            estado.put("Alumno M" + (i+1), null);
        }

        estado.put("RND Mantenimiento", redondear(mantResult[0]));
        estado.put("Tiempo Mantenimiento", redondear(mantResult[1]));
        estado.put("Fin Mantenimiento", redondear((Double)equipos.get(0).get("fin_mantenimiento")));
        estado.put("Máquina Mant.", 1);

        estado.put("Acum. Tiempo Trabajado Tec.", 0);
        estado.put("Tiempo Ocioso Tec.", 0);
        estado.put("Promedio Tiempo Ocioso Tec.", 0);
        estado.put("Cola", 0);

// Estados de alumnos
        estadoAlumnos.forEach((id, estadoAlumno) -> {
            estado.put("Estado " + id, estadoAlumno);
            if ("SA".equals(estadoAlumno)) {
                for (Map<String, Object> equipo : equipos) {
                    if (id.equals(equipo.get("alumno_actual"))) {
                        estado.put("Alumno M" + equipo.get("id"), id);
                    }
                }
            }
        });

        return estado;
    }

    private Map<String, Object> procesarEvento(Object[] evento, int iteracion) {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Iteracion", iteracion);
        estado.put("Reloj", redondear(tiempoActual));

        String tipoEvento = (String) evento[0];
        switch (tipoEvento) {
            case "llegada":
                return procesarLlegada(estado);
            case "fin_inscripcion":
                return procesarFinInscripcion((Map<String, Object>)evento[2], estado);
            case "fin_mantenimiento":
                return procesarFinMantenimiento((Map<String, Object>)evento[2], estado);
            case "regreso_tecnico":
                return procesarRegresoTecnico(estado);
            default:
                throw new IllegalArgumentException("Evento desconocido: " + tipoEvento);
        }
    }

    private Map<String, Object> procesarLlegada(Map<String, Object> estado) {
        double[] llegada = generarTiempoLlegada();
        estado.put("Evento", "Llegada Alumno A" + (++contadorAlumnos));
        estado.put("RND Llegada", llegada[0]);
        estado.put("Tiempo Llegada", llegada[1]);
        proximaLlegada = tiempoActual + llegada[1];

        String alumnoId = "A" + contadorAlumnos;
        if (cola >= 5) {
            estadoAlumnos.put(alumnoId, "AF");
            estado.put("Estado " + alumnoId, "AF");
        } else {
            Map<String, Object> equipoLibre = obtenerEquipoLibre();
            if (equipoLibre != null) {
                double[] inscripcion = generarTiempoInscripcion();
                equipoLibre.put("estado", "Ocupado");
                equipoLibre.put("fin_inscripcion", tiempoActual + inscripcion[1]);
                equipoLibre.put("alumno_actual", alumnoId);

                estado.put("Máquina", equipoLibre.get("id"));
                estado.put("RND Inscripción", inscripcion[0]);
                estado.put("Tiempo Inscripción", inscripcion[1]);
                estado.put("Fin Inscripción", redondear((Double)equipoLibre.get("fin_inscripcion")));
                estado.put("Alumno M" + equipoLibre.get("id"), alumnoId);

                estadoAlumnos.put(alumnoId, "SA");
                estado.put("Estado " + alumnoId, "SA");
            } else {
                cola++;
                estadoAlumnos.put(alumnoId, "EC");
                alumnosEnCola.add(alumnoId);
                estado.put("Estado " + alumnoId, "EC");
            }
        }
        estado.put("Cola", cola);
        return estado;
    }

    private Map<String, Object> procesarFinInscripcion(Map<String, Object> equipo, Map<String, Object> estado) {
        String alumnoId = (String) equipo.get("alumno_actual");
        estado.put("Evento", "Fin Inscripción " + alumnoId);

// Liberar equipo
        equipo.put("estado", "Libre");
        equipo.put("fin_inscripcion", null);
        equipo.put("alumno_actual", null);
        estadoAlumnos.put(alumnoId, "AF");
        estado.put("Estado " + alumnoId, "AF");

        estado.put("Máquina", equipo.get("id"));
        estado.put("Fin Inscripción", null);
        estado.put("Alumno M" + equipo.get("id"), null);

// Programar mantenimiento si es necesario
        if (proximoRegresoTecnico == null) {
            if (proximaComputadoraMantenimiento < 5 &&
                    equipo.get("id").equals(equipos.get(proximaComputadoraMantenimiento).get("id"))) {

                double[] mantResult = generarTiempoMantenimiento();
                equipo.put("estado", "Mantenimiento");
                equipo.put("fin_mantenimiento", tiempoActual + mantResult[1]);
                proximaComputadoraMantenimiento++;

                estado.put("Máquina Mant.", equipo.get("id"));
                estado.put("RND Mantenimiento", mantResult[0]);
                estado.put("Tiempo Mantenimiento", mantResult[1]);
                estado.put("Fin Mantenimiento", redondear((Double)equipo.get("fin_mantenimiento")));
            }
        }

// Atender siguiente alumno en cola si hay
        if (!alumnosEnCola.isEmpty()) {
            String siguienteAlumno = alumnosEnCola.remove(0);
            Map<String, Object> equipoLibre = obtenerEquipoLibre();
            if (equipoLibre != null) {
                double[] inscripcion = generarTiempoInscripcion();
                equipoLibre.put("estado", "Ocupado");
                equipoLibre.put("fin_inscripcion", tiempoActual + inscripcion[1]);
                equipoLibre.put("alumno_actual", siguienteAlumno);
                cola--;

                estado.put("Máquina", equipoLibre.get("id"));
                estado.put("RND Inscripción", inscripcion[0]);
                estado.put("Tiempo Inscripción", inscripcion[1]);
                estado.put("Fin Inscripción", redondear((Double)equipoLibre.get("fin_inscripcion")));
                estado.put("Alumno M" + equipoLibre.get("id"), siguienteAlumno);

                estadoAlumnos.put(siguienteAlumno, "SA");
                estado.put("Estado " + siguienteAlumno, "SA");
            }
        }

        estado.put("Cola", cola);
        estado.put("Acum. Tiempo Trabajado Tec.", redondear(tiempoTrabajadoTecnicoAcumulado));
        estado.put("Tiempo Ocioso Tec.", redondear(tiempoOciosoTecnicoAcumulado));
        return estado;
    }

    private Map<String, Object> procesarFinMantenimiento(Map<String, Object> equipo, Map<String, Object> estado) {
        estado.put("Evento", "Fin Mantenimiento M" + equipo.get("id"));

// Registrar tiempo trabajado
        double duracion = (double) equipo.get("fin_mantenimiento") - (tiempoActual - ((double)equipo.get("fin_mantenimiento") - tiempoActual));
        tiempoTrabajadoTecnicoAcumulado += duracion;
        cantidadMantenimientosCompletados++;

// Liberar equipo
        equipo.put("estado", "Libre");
        equipo.put("fin_mantenimiento", null);
        estado.put("Fin Mantenimiento", null);
        estado.put("Máquina Mant.", null);

// Programar próximo mantenimiento o regreso
        if (proximoRegresoTecnico == null) {
            if (proximaComputadoraMantenimiento < 5) {
                Map<String, Object> siguienteEquipo = equipos.get(proximaComputadoraMantenimiento);
                if ("Libre".equals(siguienteEquipo.get("estado"))) {
                    double[] mantResult = generarTiempoMantenimiento();
                    siguienteEquipo.put("estado", "Mantenimiento");
                    siguienteEquipo.put("fin_mantenimiento", tiempoActual + mantResult[1]);
                    proximaComputadoraMantenimiento++;

                    estado.put("Máquina Mant.", siguienteEquipo.get("id"));
                    estado.put("RND Mantenimiento", mantResult[0]);
                    estado.put("Tiempo Mantenimiento", mantResult[1]);
                    estado.put("Fin Mantenimiento", redondear((Double)siguienteEquipo.get("fin_mantenimiento")));
                }
            } else {
                double[] regresoResult = generarTiempoRegreso();
                proximoRegresoTecnico = tiempoActual + regresoResult[1];
                proximaComputadoraMantenimiento = 0;

                estado.put("RND Tiempo Vuelta", regresoResult[0]);
                estado.put("Tiempo Vuelta", regresoResult[1]);
                estado.put("Próximo Inicio Mantenimiento", redondear(proximoRegresoTecnico));
            }
        }

// Atender alumnos en cola si hay
        if (!alumnosEnCola.isEmpty() && "Libre".equals(equipo.get("estado"))) {
            String siguienteAlumno = alumnosEnCola.remove(0);
            double[] inscripcion = generarTiempoInscripcion();
            equipo.put("estado", "Ocupado");
            equipo.put("fin_inscripcion", tiempoActual + inscripcion[1]);
            equipo.put("alumno_actual", siguienteAlumno);
            cola--;

            estado.put("Máquina", equipo.get("id"));
            estado.put("RND Inscripción", inscripcion[0]);
            estado.put("Tiempo Inscripción", inscripcion[1]);
            estado.put("Fin Inscripción", redondear((Double)equipo.get("fin_inscripcion")));
            estado.put("Alumno M" + equipo.get("id"), siguienteAlumno);

            estadoAlumnos.put(siguienteAlumno, "SA");
            estado.put("Estado " + siguienteAlumno, "SA");
        }

        estado.put("Cola", cola);
        estado.put("Acum. Tiempo Trabajado Tec.", redondear(tiempoTrabajadoTecnicoAcumulado));
        estado.put("Tiempo Ocioso Tec.", redondear(tiempoOciosoTecnicoAcumulado));
        return estado;
    }

    private Map<String, Object> procesarRegresoTecnico(Map<String, Object> estado) {
        estado.put("Evento", "Regreso Técnico");
        proximoRegresoTecnico = null;

// Buscar siguiente equipo para mantenimiento
        Map<String, Object> siguienteEquipo = null;
        for (int i = proximaComputadoraMantenimiento; i < 5; i++) {
            if ("Libre".equals(equipos.get(i).get("estado"))) {
                siguienteEquipo = equipos.get(i);
                proximaComputadoraMantenimiento = i + 1;
                break;
            }
        }

        if (siguienteEquipo != null) {
            double[] mantResult = generarTiempoMantenimiento();
            siguienteEquipo.put("estado", "Mantenimiento");
            siguienteEquipo.put("fin_mantenimiento", tiempoActual + mantResult[1]);

            estado.put("Máquina Mant.", siguienteEquipo.get("id"));
            estado.put("RND Mantenimiento", mantResult[0]);
            estado.put("Tiempo Mantenimiento", mantResult[1]);
            estado.put("Fin Mantenimiento", redondear((Double)siguienteEquipo.get("fin_mantenimiento")));
        } else {
            proximaComputadoraMantenimiento = 0;
        }

        return estado;
    }

    private double[] generarTiempoLlegada() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = -mediaLlegada * Math.log(1 - rnd);
        return new double[]{redondear(rnd), redondear(tiempo)};
    }

    private double[] generarTiempoInscripcion() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = infInscripcion + (supInscripcion - infInscripcion) * rnd;
        return new double[]{redondear(rnd), redondear(tiempo)};
    }

    private double[] generarTiempoMantenimiento() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = minMantenimiento + (maxMantenimiento - minMantenimiento) * rnd;
        return new double[]{redondear(rnd), redondear(tiempo)};
    }

    private double[] generarTiempoRegreso() {
        double rnd = Math.random() < 0.5 ? -1 : 1;
        double tiempo = baseRegresoTecnico + rangoRegresoTecnico * rnd;
        return new double[]{redondear(rnd), redondear(Math.max(tiempo, 0))};
    }

    private Object[] obtenerProximoEvento() {
        List<Object[]> eventos = new ArrayList<>();
        eventos.add(new Object[]{"llegada", proximaLlegada});

        if (proximoRegresoTecnico != null) {
            eventos.add(new Object[]{"regreso_tecnico", proximoRegresoTecnico});
        }

        equipos.forEach(equipo -> {
            if (equipo.get("fin_mantenimiento") instanceof Double) {
                eventos.add(new Object[]{"fin_mantenimiento", equipo.get("fin_mantenimiento"), equipo});
            }
            if (equipo.get("fin_inscripcion") instanceof Double) {
                eventos.add(new Object[]{"fin_inscripcion", equipo.get("fin_inscripcion"), equipo});
            }
        });

        return eventos.stream()
                .min(Comparator.comparingDouble(e -> (double)e[1]))
                .orElseThrow(() -> new RuntimeException("No hay eventos futuros"));
    }

    private Map<String, Object> obtenerEquipoLibre() {
        return equipos.stream()
                .filter(e -> "Libre".equals(e.get("estado")) && e.get("fin_mantenimiento") == null)
                .findFirst()
                .orElse(null);
    }

    private boolean isTechnicianBusyMaintaining() {
        return equipos.stream().anyMatch(e -> "Mantenimiento".equals(e.get("estado")));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}