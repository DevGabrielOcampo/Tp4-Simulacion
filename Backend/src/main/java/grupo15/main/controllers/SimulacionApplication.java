package grupo15.main.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.Serializable;

@SpringBootApplication
@RestController
@RequestMapping("/simulacion")
@CrossOrigin(origins = "http://localhost:3000")
public class SimulacionApplication {

    //Iniciamos la aplicación Spring Boot
    public static void main(String[] args) {
        SpringApplication.run(SimulacionApplication.class, args);
    }

    //Controlador REST: Obtenemos los parametros del front
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
            @RequestParam("iteracionesMostrar") Float iteracionesMostrar) throws JsonProcessingException {


        // Creamos una instancia de la clase Simulación
        Simulacion simulacion = new Simulacion(
                5, // Cantidad de PCs
                minInscripcion,
                maxInscripcion,
                mediaLlegada,
                minMantenimiento,
                maxMantenimiento,
                baseRegresoTecnico,
                rangoRegresoTecnico
        );

        //Cambios para guardar solo las lineas necesarias
        List<Map<String, Object>> resultadosFiltrados;
        resultadosFiltrados = simulacion.simular(
                minutosSimulacion.doubleValue(),
                minutoDesde.doubleValue(),
                iteracionesMostrar.intValue()
        );

        return resultadosFiltrados;
    }
}

// Clase del estado equipo
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

// Clase del estado Alumno
enum EstadoAlumno implements Serializable {
    INSCRIBIENDOSE("inscribiendose"),
    ESPERANDO("esperando"),
    FINALIZO("finalizo"),
    SE_FUE("se_fue");

    private final String value;

    EstadoAlumno(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

// Clase del Personal Mantenimiento -> Creo que no la usamos
enum PersonalMantenimiento implements Serializable {
    LIBRE("Libre"),
    OCUPADO("Ocupado"), //Lo usamos cuando se encuentra trabajando
    MANTENIMIENTO("Mantenimiento");

    private final String value;

    PersonalMantenimiento(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

// Clase simulación
class Simulacion implements Serializable {
    private final List<Map<String, Object>> equipos;
    private final float infInscripcion;
    private final float supInscripcion;
    private final double mediaLlegada;
    private final float minMantenimiento;
    private final float maxMantenimiento;
    private final float baseRegresoTecnico;
    private final float rangoRegresoTecnico;

    private int cola;
    private int acumAbandonos;
    private final List<String> alumnosEnCola;
    private double tiempoActual;
    private final List<Map<String, Object>> resultados;
    private int contadorAlumnos;
    private final Map<String, EstadoAlumno> estadoAlumnos;
    private Double proximoRegresoTecnico;
    private int proximaComputadoraMantenimiento;
    private double proximaLlegada;

    // Estadísticas del técnico
    private double tiempoOciosoTecnicoAcumulado;
    private double tiempoTrabajadoTecnicoAcumulado;
    private int cantidadMantenimientosCompletados;

    //Constructor de la clase
    public Simulacion(int equiposCount, Float infInscripcion, Float supInscripcion,
                      Float mediaLlegada, Float minMantenimiento, Float maxMantenimiento,
                      Float baseRegresoTecnico, Float rangoRegresoTecnico) {
        //inicializamos la lista de pc
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
        //Asigna los valores recibidos en el constructor a las variables correspondientes
        this.infInscripcion = infInscripcion;
        this.supInscripcion = supInscripcion;
        this.mediaLlegada = mediaLlegada;
        this.minMantenimiento = minMantenimiento;
        this.maxMantenimiento = maxMantenimiento;
        this.baseRegresoTecnico = baseRegresoTecnico;
        this.rangoRegresoTecnico = rangoRegresoTecnico;

        //Inicializa variables para el estado de la simulación
        this.cola = 0;
        this.acumAbandonos = 0;
        this.tiempoActual = 0;
        this.resultados = new ArrayList<>();
        this.contadorAlumnos = 0;
        this.estadoAlumnos = new HashMap<>();
        this.alumnosEnCola = new ArrayList<>();
        this.proximoRegresoTecnico = null;
        this.proximaComputadoraMantenimiento = 0;
        this.proximaLlegada = 0;

        //Inicializa las estadísticas del técnico en 0
        this.tiempoOciosoTecnicoAcumulado = 0.0;
        this.tiempoTrabajadoTecnicoAcumulado = 0.0; // Inicializado
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
            //if (equipo.get("estado") == EstadoEquipo.LIBRE && equipo.get("fin_mantenimiento") == null)
            if (equipo.get("estado") == EstadoEquipo.LIBRE){
                return equipo;
            }
        }
        return null;
    }

    // Verificamos si el técnico está ocupado (realizando mantenimiento)
    // Si hay algún equipo en mantenimiento, el técnico está ocupado
    private boolean esTecnicoOcupadoManteniendo() {
        for (Map<String, Object> equipo : equipos) {
            if (equipo.get("estado") == EstadoEquipo.MANTENIMIENTO) {
                return true;
            }
        }
        return false;
    }

    private void actualizarEstadoAlumno(String idAlumno, EstadoAlumno estado) {
        //Verificamos si:
        //El alumno no tiene un estado registrado, o
        //Su estado actual es diferente al nuevo
        if (!estadoAlumnos.containsKey(idAlumno) || !estadoAlumnos.get(idAlumno).equals(estado)) {
            //Actualizamos el nuevo estado del alumno
            estadoAlumnos.put(idAlumno, estado);

            //Actualizamos la cola de de los alumnos
            if (estado == EstadoAlumno.ESPERANDO) {
                alumnosEnCola.add(idAlumno);
            } else if (estado == EstadoAlumno.INSCRIBIENDOSE) {
                alumnosEnCola.remove(idAlumno);
            }
        }
    }

    private void agregarEstadosAlumnos(Map<String, Object> estadoActual) {
        estadoActual.put("Cola", cola);

        int cantidadAlumnos = 0;
        for (int i = 1; i <= contadorAlumnos; i++) {
            String alumnoId = "A" + i;
            if (estadoAlumnos.containsKey(alumnoId)) {
                estadoActual.put("Estado " + alumnoId, estadoAlumnos.get(alumnoId).getValue());
                cantidadAlumnos = i;
            } else {
                estadoActual.put("Estado " + alumnoId, null);
            }
        }
        estadoActual.put("max_alumnos", cantidadAlumnos);
    }

    //Procesamos el siguiente evento
    private Object[] obtenerProximoEvento() {
        List<Object[]> eventos = new ArrayList<>();

        // Ver este evento, unn alumno llega cuando lo indica proximaLlegada
        eventos.add(new Object[]{"Llegada Alumno", proximaLlegada});

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

        //Ordena todos los eventos por su tiempo (índice 1)
        eventos.sort(Comparator.comparingDouble(o -> (double) o[1]));
        //Devuelve el evento más próximo
        return eventos.get(0);
    }

    //Metodo principal donde se realiza toda la simulación
    public List<Map<String, Object>> simular(double tiempoTotal, double minutoDesde, int iteracionesMostrar) throws JsonProcessingException {
        double[] llegadaResult = generarTiempoLlegada();
        double rndLlegada = llegadaResult[0];
        double tiempoLlegada = llegadaResult[1];

        tiempoActual = 0;
        proximaLlegada = tiempoLlegada;
        contadorAlumnos = 0;

        tiempoOciosoTecnicoAcumulado = 0.0;
        tiempoTrabajadoTecnicoAcumulado = 0.0;
        cantidadMantenimientosCompletados = 0;

        int iteracion = 0;
        int filasGuardadas = 0;
        int maxIteraciones = 100000;

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
        estadoInicial.put("Iteracion", iteracion);
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
        resultados.add(estadoInicial);


        while (tiempoActual < tiempoTotal && iteracion < maxIteraciones) {
            Map<String, Object> estadoAnterior = resultados.get(resultados.size() - 1);
            Object[] evento = obtenerProximoEvento();
            double proximoTiempoEvento = (double) evento[1];
            if (proximoTiempoEvento > tiempoTotal) break;

            double duracionIntervalo = proximoTiempoEvento - tiempoActual;
            if (duracionIntervalo > 0 && !esTecnicoOcupadoManteniendo()) {
                if (proximoRegresoTecnico == null || proximoRegresoTecnico <= tiempoActual) {
                    tiempoOciosoTecnicoAcumulado += duracionIntervalo;
                }
            }

            tiempoOciosoTecnicoAcumulado = Math.max(0, tiempoOciosoTecnicoAcumulado);
            tiempoActual = proximoTiempoEvento;
            String tipoEvento = (String) evento[0];

            Map<String, Object> estado = new LinkedHashMap<>();

            estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);

            //Copiar el estado anterior al nuevo estado, pero filtrando ciertos campos que deben
            // recalcularse o reiniciarse en cada iteración.
            for (Map.Entry<String, Object> entry : estadoAnterior.entrySet()) {
                if (!entry.getKey().equals("Evento") && !entry.getKey().equals("Reloj")
                        // No copiar RNDs/Tiempos específicos de eventos, se recalcularán o resetearán
                        && !entry.getKey().equals("RND Llegada")
                        && !entry.getKey().equals("Tiempo Llegada")
                        && !entry.getKey().equals("RND Inscripción")
                        && !entry.getKey().equals("Tiempo Inscripción")
                        && !entry.getKey().equals("Máquina")
                        && !entry.getKey().equals("RND Mantenimiento")
                        && !entry.getKey().equals("Tiempo Mantenimiento")
                        && !entry.getKey().equals("Fin Mantenimiento") // No copiar el global, se recalcula si aplica
                        && !entry.getKey().equals("Máquina Mant.")
                        && !entry.getKey().equals("RND Tiempo Vuelta")
                        && !entry.getKey().equals("Tiempo Vuelta")
                        // NO resetear las estadísticas acumuladas, se actualizan incrementalmente
                        && !entry.getKey().equals("Acum. Tiempo Trabajado Tec.")
                        && !entry.getKey().equals("Promedio Tiempo Trabajado Tec.")
                        && !entry.getKey().equals("Tiempo Ocioso Tec.")
                        && !entry.getKey().equals("Promedio Tiempo Ocioso Tec.")
                ) {
                    estado.put(entry.getKey(), entry.getValue());
                }
            }

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
                case "Llegada Alumno":
                    //Generamos un nuevo tiempo (proximaLlegada)
                    double[] nuevaLlegada = generarTiempoLlegada();
                    rndLlegada = nuevaLlegada[0];
                    tiempoLlegada = nuevaLlegada[1];
                    proximaLlegada = tiempoActual + tiempoLlegada;
                    estado.put("Próxima Llegada", proximaLlegada);

                    contadorAlumnos++;
                    String idActualAlumno = "A" + contadorAlumnos;
                    procesarLlegada(idActualAlumno, rndLlegada, tiempoLlegada, estado);
                    break;

                case "fin_inscripcion":
                    Map<String, Object> equipoFinInscrip = (Map<String, Object>) evento[2];
                    procesarFinInscripcion(equipoFinInscrip, estado);
                    break;

                case "fin_mantenimiento":
                    Map<String, Object> equipoFinMant = (Map<String, Object>) evento[2];
                    procesarFinMantenimiento(equipoFinMant, estado);
                    break;



                case "regreso_tecnico":
                    procesarRegresoTecnico(estado);
                    break;
            }

            //actualizarEstadisticasTecnico(estado);
            estado.put("Evento", tipoEvento);
            agregarEstadosAlumnos(estado);

            //Condición para guardar desde el minuto que se pasa hasta la cantidad de iteraciones
            if (tiempoActual >= minutoDesde && filasGuardadas < iteracionesMostrar) {
                resultados.add(estado);
                filasGuardadas++;
            }

            iteracion++;
            //Número de fila de la simulación
            estado.put("Iteracion", iteracion);
        }

        //Pruebas para imprimir por consola
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));

        //Si pasamos minuto desde distinto a cero, elimina la primera linea de inicialización
        if (minutoDesde != 0) {
            resultados.removeFirst();
        }
        return resultados;
    }


    private void procesarLlegada(String idAlumno, double rndLlegada, double tiempoLlegada, Map<String, Object> estado) {
        estado.put("Evento", "Llegada Alumno " + idAlumno);
        estado.put("RND Llegada", Math.round(rndLlegada * 100.0) / 100.0);
        estado.put("Tiempo Llegada", Math.round(tiempoLlegada * 100.0) / 100.0);

        //Comprueba si la cola tiene lugar
        if (cola < 5){
            cola++;
            actualizarEstadoAlumno(idAlumno, EstadoAlumno.ESPERANDO);
        }
        else {
            //Implementar logica para cuando se va el alumno
            acumAbandonos++;
        }


        //Chequear esta parte
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
            // Estos se actualizan en el bucle principal for (int i = 0; i < equipos.size(); i++)
            // estado.put("Fin Inscripción M" + equipoLibre.get("id"), Math.round((Double) equipoLibre.get("fin_inscripcion") * 100.0) / 100.0);
            // estado.put("Alumno M" + equipoLibre.get("id"), idAlumno);

            actualizarEstadoAlumno(idAlumno, EstadoAlumno.INSCRIBIENDOSE);
        }
    }

   private void procesarFinMantenimiento(Map<String, Object> equipo, Map<String, Object> estado) {
        estado.put("Evento", "Fin Mantenimiento M" + equipo.get("id"));

        double prevMantDuration = (double) equipo.getOrDefault("duracion_mantenimiento_actual", 0.0);
        if (prevMantDuration > 0) {
            tiempoTrabajadoTecnicoAcumulado += prevMantDuration; // Acumular tiempo trabajado
            cantidadMantenimientosCompletados++;
        }

        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_mantenimiento", null);
        equipo.put("duracion_mantenimiento_actual", null);

        // Los campos de mantenimiento en el estado global solo se rellenan si se inicia un nuevo mantenimiento AHORA
        estado.put("Fin Mantenimiento", null); // Resetear el global, se actualiza si se inicia uno nuevo
        estado.put("Máquina Mant.", null); // Resetear el global, se actualiza si se inicia uno nuevo

        // Lógica de transición del técnico (prioridad: siguiente mantenimiento si hay o descanso)
        // Solo si el técnico NO está en descanso
        boolean technicianTookMachine = false;
        if (proximoRegresoTecnico == null) {
            // Buscar la próxima máquina para mantenimiento según el orden
            Map<String, Object> siguienteEquipoMantenimiento = null;
            if (proximaComputadoraMantenimiento < equipos.size()) {
                siguienteEquipoMantenimiento = equipos.get(proximaComputadoraMantenimiento);
            }

            // Si hay una siguiente máquina y no está ocupada por un alumno
            if (siguienteEquipoMantenimiento != null && siguienteEquipoMantenimiento.get("estado") != EstadoEquipo.OCUPADO) {
                double[] mantResult = generarTiempoMantenimiento();
                double rndMant = mantResult[0];
                double tiempoMant = mantResult[1];

                siguienteEquipoMantenimiento.put("estado", EstadoEquipo.MANTENIMIENTO);
                siguienteEquipoMantenimiento.put("fin_mantenimiento", tiempoActual + tiempoMant);
                siguienteEquipoMantenimiento.put("duracion_mantenimiento_actual", tiempoMant);
                proximaComputadoraMantenimiento++;
                technicianTookMachine = true; // El técnico tomó la máquina

                estado.put("Máquina Mant.", siguienteEquipoMantenimiento.get("id"));
                estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
                estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
                estado.put("Fin Mantenimiento", Math.round((Double) siguienteEquipoMantenimiento.get("fin_mantenimiento") * 100.0) / 100.0);
            }
        }

        // Si el técnico NO tomó una máquina y ya no hay más máquinas pendientes en el ciclo actual,
        // o si está en tiempo de regreso (proximoRegresoTecnico != null y > tiempoActual), entonces irse de descanso.
        // Si proximaComputadoraMantenimiento >= equipos.size() significa que se completó un ciclo de mantenimiento.
        if (!technicianTookMachine && proximoRegresoTecnico == null) { // Si no inició un mantenimiento Y no está ya en regreso
            if (proximaComputadoraMantenimiento >= equipos.size() || esTecnicoOcupadoManteniendo()) { // Si ya recorrió todas o si hay máquinas en mantenimiento
                // El técnico ha terminado un ciclo de mantenimiento o no hay más máquinas libres para mantenimiento, se va de descanso
                double[] regresoResult = generarTiempoRegreso();
                double rndVuelta = regresoResult[0];
                double tiempoVuelta = regresoResult[1];
                proximoRegresoTecnico = tiempoActual + tiempoVuelta;
                proximaComputadoraMantenimiento = 0; // Reiniciar el contador para el próximo ciclo

                estado.put("RND Tiempo Vuelta", Math.round(rndVuelta * 100.0) / 100.0);
                estado.put("Tiempo Vuelta", Math.round(tiempoVuelta * 100.0) / 100.0);
                estado.put("Próximo Inicio Mantenimiento", Math.round(proximoRegresoTecnico * 100.0) / 100.0);
            }
        }


        // Lógica de atención de alumno si la máquina se liberó y no fue tomada por el técnico
        if (!alumnosEnCola.isEmpty() && equipo.get("estado") == EstadoEquipo.LIBRE) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];

            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.INSCRIBIENDOSE);
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
        actualizarEstadoAlumno(alumnoFinalizado, EstadoAlumno.FINALIZO);
        cola--;

        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_inscripcion", null);
        equipo.put("alumno_actual", null);

        estado.put("Evento", "Fin Inscripción " + alumnoFinalizado);
        estado.put("Máquina", equipo.get("id")); // La máquina en la que finalizó la inscripción
        estado.put("Fin Inscripción", null); // Se actualiza solo si una nueva inscripción inicia en esta fila

        boolean technicianTookMachine = false;
        // Si el técnico NO está en descanso
        if (proximoRegresoTecnico == null) {
            // Si la máquina que se liberó es la siguiente en el orden de mantenimiento
            // Y el técnico no está ya ocupado en otra máquina de mantenimiento
            if (proximaComputadoraMantenimiento < equipos.size() &&
                    equipo.get("id").equals(equipos.get(proximaComputadoraMantenimiento).get("id")) &&
                    !esTecnicoOcupadoManteniendo()) { // Asegurarse de que el técnico no está ocupado

                double[] mantResult = generarTiempoMantenimiento();
                double rndMant = mantResult[0];
                double tiempoMant = mantResult[1];

                equipo.put("estado", EstadoEquipo.MANTENIMIENTO);
                equipo.put("fin_mantenimiento", tiempoActual + tiempoMant);
                equipo.put("duracion_mantenimiento_actual", tiempoMant);
                proximaComputadoraMantenimiento++;
                technicianTookMachine = true;

                estado.put("Máquina Mant.", equipo.get("id"));
                estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
                estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
                estado.put("Fin Mantenimiento", Math.round((Double) equipo.get("fin_mantenimiento") * 100.0) / 100.0);
            }
        }

        // Si el técnico no tomó la máquina, intentar atender un alumno en cola
        if (!technicianTookMachine && !alumnosEnCola.isEmpty()) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];

            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.INSCRIBIENDOSE);


            estado.put("Máquina", equipo.get("id"));
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipo.get("fin_inscripcion") * 100.0) / 100.0);
        } else if (equipo.get("estado") == EstadoEquipo.LIBRE) { // Si la máquina se liberó y no fue tomada por nadie
            estado.put("Máquina", equipo.get("id"));
        }
    }

    private void procesarRegresoTecnico(Map<String, Object> estado) {
        estado.put("Evento", "Regreso Técnico");
        proximoRegresoTecnico = null; // El técnico ya regresó

        // Si hay una máquina siguiente en el ciclo de mantenimiento y no está ocupada por un alumno
        if (proximaComputadoraMantenimiento < equipos.size()) {
            Map<String, Object> siguienteEquipoMantenimiento = equipos.get(proximaComputadoraMantenimiento);

            if (siguienteEquipoMantenimiento.get("estado") != EstadoEquipo.OCUPADO) {
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
            }
        }
    }
}