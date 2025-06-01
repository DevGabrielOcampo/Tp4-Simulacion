package grupo15.main.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.Serializable;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@RequestMapping("/simulacion")
@CrossOrigin(origins = "http://localhost:3000")
public class SimulacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulacionApplication.class, args);
    }

    @GetMapping("/run-parametros")
    public List<Map<String, Object>> runSimulationWithParameters(
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

        // Validaciones de parámetros (sin cambios)
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
                5, // Fixed number of machines
                minInscripcion,
                maxInscripcion,
                mediaLlegada,
                minMantenimiento,
                maxMantenimiento,
                baseRegresoTecnico,
                rangoRegresoTecnico
        );

        // La simulación ahora devuelve solo las filas filtradas, o una lista vacía si no se piden filas para mostrar.
        // La lógica de filtrado se mueve dentro del método simular para ser más eficiente.
        List<Map<String, Object>> resultadosFiltrados = simulacion.simular(
                minutosSimulacion.doubleValue(),
                minutoDesde.doubleValue(),
                iteracionesMostrar.intValue()
        );

        return resultadosFiltrados;
    }
}

enum EstadoEquipo implements Serializable {
    LIBRE("Libre"),
    OCUPADO("Ocupado"),
    MANTENIMIENTO("Mantenimiento");

    private final String value;

    EstadoEquipo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

enum EstadoAlumno implements Serializable {
    SIENDO_ATENDIDO("SA"),
    EN_COLA("EC"),
    ATENCION_FINALIZADA("AF");

    private final String value;

    EstadoAlumno(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

class Simulacion implements Serializable {
    private List<Map<String, Object>> equipos;
    private float infInscripcion;
    private float supInscripcion;
    private double mediaLlegada;
    private float minMantenimiento;
    private float maxMantenimiento;
    private float baseRegresoTecnico;
    private float rangoRegresoTecnico;

    private int cola;
    private double tiempoActual;
    // ELIMINADA: private List<Map<String, Object>> resultados; // Ya no se acumulan todos los resultados
    private int contadorAlumnos;
    private Map<String, EstadoAlumno> estadoAlumnos;
    private List<String> alumnosEnCola;
    private Double proximoRegresoTecnico;
    private int proximaComputadoraMantenimiento;
    private double proximaLlegada;

    // Estadísticas del técnico
    private double tiempoOciosoTecnicoAcumulado;
    private double tiempoTrabajadoTecnicoAcumulado;
    private int cantidadMantenimientosCompletados;

    public Simulacion(int equiposCount, Float infInscripcion, Float supInscripcion,
                      Float mediaLlegada, Float minMantenimiento, Float maxMantenimiento,
                      Float baseRegresoTecnico, Float rangoRegresoTecnico) {
        this.equipos = new ArrayList<>();
        for (int i = 0; i < equiposCount; i++) {
            Map<String, Object> equipo = new HashMap<>();
            equipo.put("id", i + 1);
            equipo.put("estado", EstadoEquipo.LIBRE);
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
        // this.resultados = new ArrayList<>(); // Ya no se inicializa
        this.contadorAlumnos = 0;
        this.estadoAlumnos = new HashMap<>();
        this.alumnosEnCola = new ArrayList<>();
        this.proximoRegresoTecnico = null;
        this.proximaComputadoraMantenimiento = 0;
        this.proximaLlegada = 0;

        this.tiempoOciosoTecnicoAcumulado = 0.0;
        this.tiempoTrabajadoTecnicoAcumulado = 0.0;
        this.cantidadMantenimientosCompletados = 0;
    }

    private double[] generarTiempoLlegada() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = -mediaLlegada * Math.log(1 - rnd);
        return new double[]{rnd, Math.round(tiempo * 100.0) / 100.0};
    }

    private double[] generarTiempoInscripcion() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = infInscripcion + (supInscripcion - infInscripcion) * rnd;
        return new double[]{rnd, Math.round(tiempo * 100.0) / 100.0};
    }

    private double[] generarTiempoMantenimiento() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempo = minMantenimiento + (maxMantenimiento - minMantenimiento) * rnd;
        return new double[]{rnd, Math.round(tiempo * 100.0) / 100.0};
    }

    private double[] generarTiempoRegreso() {
        double rnd = ThreadLocalRandom.current().nextDouble();
        double tiempoRegreso = baseRegresoTecnico - rangoRegresoTecnico + (2 * rangoRegresoTecnico) * rnd;
        if (tiempoRegreso < 0) {
            tiempoRegreso = 0;
        }
        return new double[]{rnd, Math.round(tiempoRegreso * 100.0) / 100.0};
    }

    private Map<String, Object> obtenerEquipoLibre() {
        for (Map<String, Object> equipo : equipos) {
            if (equipo.get("estado") == EstadoEquipo.LIBRE && equipo.get("fin_mantenimiento") == null) {
                return equipo;
            }
        }
        return null;
    }

    private boolean isTechnicianBusyMaintaining() {
        for (Map<String, Object> equipo : equipos) {
            if (equipo.get("estado") == EstadoEquipo.MANTENIMIENTO) {
                return true;
            }
        }
        return false;
    }

    private void actualizarEstadoAlumno(String idAlumno, EstadoAlumno estado) {
        if (!estadoAlumnos.containsKey(idAlumno) || !estadoAlumnos.get(idAlumno).equals(estado)) {
            estadoAlumnos.put(idAlumno, estado);

            if (estado == EstadoAlumno.EN_COLA) {
                alumnosEnCola.add(idAlumno);
            } else if (estado == EstadoAlumno.SIENDO_ATENDIDO) {
                alumnosEnCola.remove(idAlumno);
            }
        }
    }

    private void agregarEstadosAlumnos(Map<String, Object> estadoActual) {
        estadoActual.put("Cola", cola);

        int currentMaxAlumnos = 0;
        for (int i = 1; i <= contadorAlumnos; i++) {
            String alumnoId = "A" + i;
            if (estadoAlumnos.containsKey(alumnoId)) {
                estadoActual.put("Estado " + alumnoId, estadoAlumnos.get(alumnoId).getValue());
                currentMaxAlumnos = i;
            } else {
                estadoActual.put("Estado " + alumnoId, null);
            }
        }
        estadoActual.put("max_alumnos", currentMaxAlumnos);
    }

    private Object[] obtenerProximoEvento() {
        List<Object[]> eventos = new ArrayList<>();
        eventos.add(new Object[]{"llegada", proximaLlegada});

        if (proximoRegresoTecnico != null) {
            eventos.add(new Object[]{"regreso_tecnico", proximoRegresoTecnico});
        }

        for (Map<String, Object> equipo : equipos) {
            if (equipo.get("fin_mantenimiento") instanceof Double) {
                eventos.add(new Object[]{"fin_mantenimiento", (Double) equipo.get("fin_mantenimiento"), equipo});
            }
            if (equipo.get("fin_inscripcion") instanceof Double) {
                eventos.add(new Object[]{"fin_inscripcion", (Double) equipo.get("fin_inscripcion"), equipo});
            }
        }

        if (eventos.isEmpty()) {
            throw new RuntimeException("No hay eventos futuros definidos, la simulación no puede continuar.");
        }

        eventos.sort(Comparator.comparingDouble(o -> (double) o[1]));
        return eventos.get(0);
    }

    public List<Map<String, Object>> simular(double tiempoTotal, double minutoDesde, int iteracionesMostrar) {
        List<Map<String, Object>> resultadosFiltrados = new ArrayList<>();
        int iteracionesMostradasCount = 0;
        Map<String, Object> estadoAnterior = null; // Para copiar el estado

        double[] llegadaResult = generarTiempoLlegada();
        double rndLlegada = llegadaResult[0];
        double tiempoLlegada = llegadaResult[1];

        tiempoActual = 0;
        proximaLlegada = tiempoLlegada;
        contadorAlumnos = 0;

        tiempoOciosoTecnicoAcumulado = 0.0;
        tiempoTrabajadoTecnicoAcumulado = 0.0;
        cantidadMantenimientosCompletados = 0;

        // --- Inicialización del primer mantenimiento ---
        Map<String, Object> primerEquipoMantenimiento = equipos.get(0);
        double[] mantResult = generarTiempoMantenimiento();
        double rndMantInicial = mantResult[0];
        double tiempoMantInicial = mantResult[1];

        primerEquipoMantenimiento.put("estado", EstadoEquipo.MANTENIMIENTO);
        primerEquipoMantenimiento.put("fin_mantenimiento", tiempoActual + tiempoMantInicial);
        primerEquipoMantenimiento.put("duracion_mantenimiento_actual", tiempoMantInicial);
        proximaComputadoraMantenimiento = 1;
        proximoRegresoTecnico = null;

        Map<String, Object> estadoInicial = new LinkedHashMap<>();
        estadoInicial.put("Evento", "Inicializacion");
        estadoInicial.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estadoInicial.put("RND Llegada", Math.round(rndLlegada * 100.0) / 100.0);
        estadoInicial.put("Tiempo Llegada", Math.round(tiempoLlegada * 100.0) / 100.0);
        estadoInicial.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);

        estadoInicial.put("Máquina", null);
        estadoInicial.put("RND Inscripción", null);
        estadoInicial.put("Tiempo Inscripción", null);
        estadoInicial.put("Fin Inscripción", null);

        estadoInicial.put("Máquina Mant.", primerEquipoMantenimiento.get("id"));
        estadoInicial.put("RND Mantenimiento", Math.round(rndMantInicial * 100.0) / 100.0);
        estadoInicial.put("Tiempo Mantenimiento", Math.round(tiempoMantInicial * 100.0) / 100.0);
        estadoInicial.put("Fin Mantenimiento", Math.round((Double) primerEquipoMantenimiento.get("fin_mantenimiento") * 100.0) / 100.0);
        estadoInicial.put("RND Tiempo Vuelta", null);
        estadoInicial.put("Tiempo Vuelta", null);
        estadoInicial.put("Próximo Inicio Mantenimiento", null);

        estadoInicial.put("Acum. Tiempo Trabajado Tec.", 0.0);
        estadoInicial.put("Promedio Tiempo Trabajado Tec.", 0.0);
        estadoInicial.put("Tiempo Ocioso Tec.", 0.0);
        estadoInicial.put("Promedio Tiempo Ocioso Tec.", 0.0);

        for (int i = 0; i < equipos.size(); i++) {
            Map<String, Object> equipo = equipos.get(i);
            estadoInicial.put("Máquina " + (i + 1), ((EstadoEquipo) equipo.get("estado")).getValue());
            estadoInicial.put("Fin Inscripción M" + (i + 1), null);
            estadoInicial.put("Fin Mantenimiento M" + (i + 1), equipo.get("fin_mantenimiento") != null ? Math.round((Double) equipo.get("fin_mantenimiento") * 100.0) / 100.0 : null);
            estadoInicial.put("Alumno M" + (i + 1), null);
        }

        agregarEstadosAlumnos(estadoInicial);

        // Agregamos el estado inicial si cumple con los criterios de minutoDesde
        if (tiempoActual >= minutoDesde && iteracionesMostradasCount < iteracionesMostrar) {
            resultadosFiltrados.add(estadoInicial);
            iteracionesMostradasCount++;
        }
        estadoAnterior = estadoInicial;


        // Bucle principal de simulación
        while (tiempoActual < tiempoTotal) {
            if (iteracionesMostradasCount >= iteracionesMostrar && tiempoActual >= minutoDesde) {
                // Si ya mostramos suficientes iteraciones y estamos en el rango deseado, podemos salir
                break;
            }

            Object[] evento = obtenerProximoEvento();
            double proximoTiempoEvento = (double) evento[1];

            if (proximoTiempoEvento > tiempoTotal) {
                break;
            }

            double duracionIntervalo = proximoTiempoEvento - tiempoActual;

            if (duracionIntervalo > 0) {
                if (!isTechnicianBusyMaintaining()) {
                    if (proximoRegresoTecnico == null || proximoRegresoTecnico <= tiempoActual) {
                        tiempoOciosoTecnicoAcumulado += duracionIntervalo;
                    }
                }
            }
            tiempoOciosoTecnicoAcumulado = Math.max(0, tiempoOciosoTecnicoAcumulado);

            tiempoActual = proximoTiempoEvento;
            String tipoEvento = (String) evento[0];

            Map<String, Object> estado = new LinkedHashMap<>();
            estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);

            // Copiar los estados de las máquinas y alumnos de la fila anterior
            // Esto es crucial para la eficiencia: solo creamos una nueva fila de resultados si la vamos a mostrar.
            // Si no se va a mostrar, los estados de las máquinas y alumnos se mantienen internamente en los objetos.
            if (estadoAnterior != null) {
                for (Map.Entry<String, Object> entry : estadoAnterior.entrySet()) {
                    if (!entry.getKey().equals("Evento") && !entry.getKey().equals("Reloj")
                            && !entry.getKey().equals("RND Llegada") && !entry.getKey().equals("Tiempo Llegada")
                            && !entry.getKey().equals("RND Inscripción") && !entry.getKey().equals("Tiempo Inscripción")
                            && !entry.getKey().equals("Máquina") && !entry.getKey().equals("RND Mantenimiento")
                            && !entry.getKey().equals("Tiempo Mantenimiento") && !entry.getKey().equals("Máquina Mant.")
                            && !entry.getKey().equals("RND Tiempo Vuelta") && !entry.getKey().equals("Tiempo Vuelta")
                            // No copiar las estadísticas acumuladas directamente, se actualizan incrementalmente
                            && !entry.getKey().equals("Acum. Tiempo Trabajado Tec.")
                            && !entry.getKey().equals("Promedio Tiempo Trabajado Tec.")
                            && !entry.getKey().equals("Tiempo Ocioso Tec.")
                            && !entry.getKey().equals("Promedio Tiempo Ocioso Tec.")
                    ) {
                        estado.put(entry.getKey(), entry.getValue());
                    }
                }
            }


            // Resetear campos específicos del evento para la fila actual
            estado.put("Máquina", null);
            estado.put("RND Inscripción", null);
            estado.put("Tiempo Inscripción", null);
            estado.put("Fin Inscripción", null);
            estado.put("Máquina Mant.", null);
            estado.put("RND Mantenimiento", null);
            estado.put("Tiempo Mantenimiento", null);
            estado.put("Fin Mantenimiento", null);
            estado.put("RND Tiempo Vuelta", null);
            estado.put("Tiempo Vuelta", null);
            estado.put("Próximo Inicio Mantenimiento", proximoRegresoTecnico != null ? Math.round(proximoRegresoTecnico * 100.0) / 100.0 : null);


            switch (tipoEvento) {
                case "llegada":
                    double[] newLlegadaResult = generarTiempoLlegada();
                    rndLlegada = newLlegadaResult[0];
                    tiempoLlegada = newLlegadaResult[1];
                    proximaLlegada = tiempoActual + tiempoLlegada;
                    contadorAlumnos++;
                    String idActualAlumno = "A" + contadorAlumnos;
                    procesarLlegada(idActualAlumno, rndLlegada, tiempoLlegada, estado);
                    break;
                case "fin_mantenimiento":
                    Map<String, Object> equipoFinMant = (Map<String, Object>) evento[2];
                    procesarFinMantenimiento(equipoFinMant, estado);
                    break;
                case "fin_inscripcion":
                    Map<String, Object> equipoFinInsc = (Map<String, Object>) evento[2];
                    procesarFinInscripcion(equipoFinInsc, estado);
                    break;
                case "regreso_tecnico":
                    procesarRegresoTecnico(estado);
                    break;
            }

            // Actualizar estados de máquinas y alumnos después de procesar el evento
            for (int i = 0; i < equipos.size(); i++) {
                Map<String, Object> currentEquipo = equipos.get(i);
                estado.put("Máquina " + (i + 1), ((EstadoEquipo) currentEquipo.get("estado")).getValue());
                Double finInsc = (Double) currentEquipo.get("fin_inscripcion");
                estado.put("Fin Inscripción M" + (i + 1), finInsc != null ? Math.round(finInsc * 100.0) / 100.0 : null);
                Double finMant = (Double) currentEquipo.get("fin_mantenimiento");
                estado.put("Fin Mantenimiento M" + (i + 1), finMant != null ? Math.round(finMant * 100.0) / 100.0 : null);
                estado.put("Alumno M" + (i + 1), currentEquipo.get("alumno_actual"));
            }

            estado.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);
            estado.put("Próximo Inicio Mantenimiento", proximoRegresoTecnico != null ? Math.round(proximoRegresoTecnico * 100.0) / 100.0 : null);

            // Actualizar estadísticas del técnico para la fila actual
            estado.put("Tiempo Ocioso Tec.", Math.round(tiempoOciosoTecnicoAcumulado * 100.0) / 100.0);
            estado.put("Promedio Tiempo Ocioso Tec.", cantidadMantenimientosCompletados > 0 ? Math.round((tiempoOciosoTecnicoAcumulado / cantidadMantenimientosCompletados) * 100.0) / 100.0 : 0.0);
            estado.put("Acum. Tiempo Trabajado Tec.", Math.round(tiempoTrabajadoTecnicoAcumulado * 100.0) / 100.0);
            estado.put("Promedio Tiempo Trabajado Tec.", cantidadMantenimientosCompletados > 0 ? Math.round((tiempoTrabajadoTecnicoAcumulado / cantidadMantenimientosCompletados) * 100.0) / 100.0 : 0.0);

            agregarEstadosAlumnos(estado);

            // Solo agrega a resultadosFiltrados si cumple con los criterios de minutoDesde e iteracionesMostrar
            if (tiempoActual >= minutoDesde && iteracionesMostradasCount < iteracionesMostrar) {
                resultadosFiltrados.add(estado);
                iteracionesMostradasCount++;
            }
            estadoAnterior = estado; // Guardamos el estado actual para la próxima iteración (si se va a mostrar)
        }

        // Si se necesitan más iteraciones después del tiempoTotal, se podrían agregar lógicas aquí,
        // pero para mantener la eficiencia, el bucle se detiene.

        // El orden se mantiene porque se agregan cronológicamente, no es necesario un sort final si ya se agregan en orden
        return resultadosFiltrados;
    }

    private void procesarLlegada(String idAlumno, double rndLlegada, double tiempoLlegada, Map<String, Object> estado) {
        estado.put("Evento", "Llegada Alumno " + idAlumno);
        estado.put("RND Llegada", Math.round(rndLlegada * 100.0) / 100.0);
        estado.put("Tiempo Llegada", Math.round(tiempoLlegada * 100.0) / 100.0);

        Map<String, Object> equipoLibre = obtenerEquipoLibre();
        if (equipoLibre != null) {
            equipoLibre.put("estado", EstadoEquipo.OCUPADO);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];
            equipoLibre.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipoLibre.put("alumno_actual", idAlumno);

            estado.put("Máquina", equipoLibre.get("id"));
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipoLibre.get("fin_inscripcion") * 100.0) / 100.0);

            actualizarEstadoAlumno(idAlumno, EstadoAlumno.SIENDO_ATENDIDO);
        } else {
            cola++;
            actualizarEstadoAlumno(idAlumno, EstadoAlumno.EN_COLA);
        }
    }

    private void procesarFinMantenimiento(Map<String, Object> equipo, Map<String, Object> estado) {
        estado.put("Evento", "Fin Mantenimiento M" + equipo.get("id"));

        double prevMantDuration = (double) equipo.getOrDefault("duracion_mantenimiento_actual", 0.0);
        if (prevMantDuration > 0) {
            tiempoTrabajadoTecnicoAcumulado += prevMantDuration;
            cantidadMantenimientosCompletados++;
        }

        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_mantenimiento", null);
        equipo.put("duracion_mantenimiento_actual", null);

        estado.put("Fin Mantenimiento", null);
        estado.put("Máquina Mant.", null);

        boolean technicianTookMachine = false;
        if (proximoRegresoTecnico == null) {
            Map<String, Object> siguienteEquipoMantenimiento = null;
            if (proximaComputadoraMantenimiento < equipos.size()) {
                siguienteEquipoMantenimiento = equipos.get(proximaComputadoraMantenimiento);
            }

            if (siguienteEquipoMantenimiento != null && siguienteEquipoMantenimiento.get("estado") != EstadoEquipo.OCUPADO) {
                double[] mantResult = generarTiempoMantenimiento();
                double rndMant = mantResult[0];
                double tiempoMant = mantResult[1];

                siguienteEquipoMantenimiento.put("estado", EstadoEquipo.MANTENIMIENTO);
                siguienteEquipoMantenimiento.put("fin_mantenimiento", tiempoActual + tiempoMant);
                siguienteEquipoMantenimiento.put("duracion_mantenimiento_actual", tiempoMant);
                proximaComputadoraMantenimiento++;
                technicianTookMachine = true;

                estado.put("Máquina Mant.", siguienteEquipoMantenimiento.get("id"));
                estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
                estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
                estado.put("Fin Mantenimiento", Math.round((Double) siguienteEquipoMantenimiento.get("fin_mantenimiento") * 100.0) / 100.0);
            }
        }

        if (!technicianTookMachine && proximoRegresoTecnico == null) {
            if (proximaComputadoraMantenimiento >= equipos.size() || isTechnicianBusyMaintaining()) {
                double[] regresoResult = generarTiempoRegreso();
                double rndVuelta = regresoResult[0];
                double tiempoVuelta = regresoResult[1];
                proximoRegresoTecnico = tiempoActual + tiempoVuelta;
                proximaComputadoraMantenimiento = 0;

                estado.put("RND Tiempo Vuelta", Math.round(rndVuelta * 100.0) / 100.0);
                estado.put("Tiempo Vuelta", Math.round(tiempoVuelta * 100.0) / 100.0);
                estado.put("Próximo Inicio Mantenimiento", Math.round(proximoRegresoTecnico * 100.0) / 100.0);
            }
        }

        if (!alumnosEnCola.isEmpty() && equipo.get("estado") == EstadoEquipo.LIBRE) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];

            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.SIENDO_ATENDIDO);
            cola--;

            estado.put("Máquina", equipo.get("id"));
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipo.get("fin_inscripcion") * 100.0) / 100.0);
        } else if (equipo.get("estado") == EstadoEquipo.LIBRE) {
            estado.put("Máquina", equipo.get("id"));
        }
    }

    private void procesarFinInscripcion(Map<String, Object> equipo, Map<String, Object> estado) {
        String alumnoFinalizado = (String) equipo.get("alumno_actual");
        actualizarEstadoAlumno(alumnoFinalizado, EstadoAlumno.ATENCION_FINALIZADA);

        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_inscripcion", null);
        equipo.put("alumno_actual", null);

        estado.put("Evento", "Fin Inscripción " + alumnoFinalizado);
        estado.put("Máquina", equipo.get("id"));
        estado.put("Fin Inscripción", null);

        boolean technicianTookMachine = false;
        if (proximoRegresoTecnico == null) {
            if (proximaComputadoraMantenimiento < equipos.size() &&
                    equipo.get("id").equals(equipos.get(proximaComputadoraMantenimiento).get("id")) &&
                    !isTechnicianBusyMaintaining()) {

                double[] mantResult = generarTiempoMantenimiento();
                double rndMant = mantResult[0];
                double tiempoMant = mantResult[1];

                equipo.put("estado", EstadoEquipo.MANTENIMIENTO);
                equipo.put("fin_mantenimiento", tiempoActual + tiempoMant);
                equipo.put("duracion_mantenimiento_actual", tiempoMant);
                // Estos atributos no son necesarios en el objeto equipo si solo se usan una vez
                // equipo.put("rnd_mantenimiento", rndMant);
                // equipo.put("tiempo_mantenimiento", tiempoMant);

                proximaComputadoraMantenimiento++;
                technicianTookMachine = true;

                estado.put("Máquina Mant.", equipo.get("id"));
                estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
                estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
                estado.put("Fin Mantenimiento", Math.round((Double) equipo.get("fin_mantenimiento") * 100.0) / 100.0);
            }
        }

        if (!technicianTookMachine && !alumnosEnCola.isEmpty()) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];

            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.SIENDO_ATENDIDO);
            cola--;

            estado.put("Máquina", equipo.get("id"));
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipo.get("fin_inscripcion") * 100.0) / 100.0);
        } else if (equipo.get("estado") == EstadoEquipo.LIBRE && proximoRegresoTecnico == null && !isTechnicianBusyMaintaining()) {
            // Caso especial: si la máquina se liberó y el técnico no la tomó,
            // y el técnico está ocioso y no en regreso, podría iniciar un mantenimiento si esa es la siguiente máquina.
            // Esta lógica ya está cubierta en `procesarFinMantenimiento` que maneja la prioridad del técnico.
            // Aquí, si no hay alumnos en cola y el técnico no la toma, la máquina simplemente queda libre.
            estado.put("Máquina", equipo.get("id"));
        }
    }

    private void procesarRegresoTecnico(Map<String, Object> estado) {
        estado.put("Evento", "Regreso Técnico");
        estado.put("RND Tiempo Vuelta", null); // Ya se generó antes
        estado.put("Tiempo Vuelta", null); // Ya se generó antes
        proximoRegresoTecnico = null; // El técnico ya regresó

        // Al regresar, el técnico debe tomar la próxima máquina en mantenimiento si está libre
        Map<String, Object> siguienteEquipoMantenimiento = null;
        if (proximaComputadoraMantenimiento < equipos.size()) {
            siguienteEquipoMantenimiento = equipos.get(proximaComputadoraMantenimiento);
        }

        if (siguienteEquipoMantenimiento != null && siguienteEquipoMantenimiento.get("estado") == EstadoEquipo.LIBRE) {
            double[] mantResult = generarTiempoMantenimiento();
            double rndMant = mantResult[0];
            double tiempoMant = mantResult[1];

            siguienteEquipoMantenimiento.put("estado", EstadoEquipo.MANTENIMIENTO);
            siguienteEquipoMantenimiento.put("fin_mantenimiento", tiempoActual + tiempoMant);
            siguienteEquipoMantenimiento.put("duracion_mantenimiento_actual", tiempoMant);
            proximaComputadoraMantenimiento++;

            estado.put("Máquina Mant.", siguienteEquipoMantenimiento.get("id"));
            estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
            estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
            estado.put("Fin Mantenimiento", Math.round((Double) siguienteEquipoMantenimiento.get("fin_mantenimiento") * 100.0) / 100.0);
            estado.put("Próximo Inicio Mantenimiento", null); // No hay próximo regreso ya que acaba de empezar un mantenimiento
        } else {
            // Si no hay máquina libre o no es el momento de tomar una, el técnico queda ocioso
            estado.put("Próximo Inicio Mantenimiento", null); // Si no toma una, no hay un próximo inicio de mantenimiento programado
        }
    }
}