package grupo15.main.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.Serializable;

// Main Spring Boot Application class
@SpringBootApplication
@RestController
@RequestMapping("/simulacion")
@CrossOrigin(origins = "http://localhost:3000")
public class SimulacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulacionApplication.class, args);
    }

    // Nuevo endpoint para recibir todos los parámetros
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

        // Validaciones básicas antes de iniciar la simulación
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
        if (baseRegresoTecnico <= 0) {
            throw new IllegalArgumentException("El tiempo base de regreso del técnico debe ser positivo.");
        }
        if (rangoRegresoTecnico < 0) {
            throw new IllegalArgumentException("El rango de regreso del técnico no puede ser negativo.");
        }
        if (mediaLlegada <= 0) {
            throw new IllegalArgumentException("La media de llegada de alumnos debe ser positiva.");
        }


        Simulacion simulacion = new Simulacion(
                6, // Número fijo de equipos
                minInscripcion,
                maxInscripcion,
                mediaLlegada,
                minMantenimiento,
                maxMantenimiento,
                baseRegresoTecnico,
                rangoRegresoTecnico
        );

        List<Map<String, Object>> resultadosCompletos = simulacion.simular(minutosSimulacion.doubleValue());

        // Filtrar los resultados según los parámetros minutosDesde e iteracionesMostrar
        List<Map<String, Object>> resultadosFiltrados = new ArrayList<>();
        int count = 0;
        for (Map<String, Object> fila : resultadosCompletos) {
            double reloj = (Double) fila.get("Reloj");
            if (reloj >= minutoDesde) {
                resultadosFiltrados.add(fila);
                count++;
                if (count >= iteracionesMostrar) {
                    break; // Detener si ya se mostraron las iteraciones deseadas
                }
            }
        }

        return resultadosFiltrados;
    }
}

// Enum for Equipo State
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

// Enum for Alumno State
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

// Simulacion Class - Core logic
class Simulacion implements Serializable {
    private List<Map<String, Object>> equipos;
    private float infInscripcion; // Usar float para consistencia
    private float supInscripcion; // Usar float para consistencia
    private double mediaLlegada;
    private float minMantenimiento; // Nuevo parámetro
    private float maxMantenimiento; // Nuevo parámetro
    private float baseRegresoTecnico; // Nuevo parámetro
    private float rangoRegresoTecnico; // Nuevo parámetro

    private int cola;
    private double tiempoActual;
    private List<Map<String, Object>> resultados;
    private int contadorAlumnos;
    private Map<String, EstadoAlumno> estadoAlumnos;
    private Map<String, Double> tiempoLlegadaAlumnos;
    private Map<String, Double> tiempoInicioAtencion;
    private Map<String, Double> tiemposEspera;
    private List<String> alumnosEnCola;
    private double tiempoEsperaTotal;
    private int alumnosConEspera;
    private Double proximoMantenimiento;
    private boolean mantenimientoEnEsperas;
    private int proximaComputadoraMantenimiento;
    private double proximaLlegada;

    // Constructor antiguo (mantener por si se usa en otro lugar)
    public Simulacion() {
        this(6, 5f, 8f, 2.0f, 3f, 10f, 57f, 3f); // Valores por defecto para el constructor sin parámetros
    }

    // Nuevo constructor para la simulación con parámetros
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
        this.resultados = new ArrayList<>();
        this.contadorAlumnos = 0;
        this.estadoAlumnos = new HashMap<>();
        this.tiempoLlegadaAlumnos = new HashMap<>();
        this.tiempoInicioAtencion = new HashMap<>();
        this.tiemposEspera = new HashMap<>();
        this.alumnosEnCola = new ArrayList<>();
        this.tiempoEsperaTotal = 0;
        this.alumnosConEspera = 0;
        this.proximoMantenimiento = null;
        this.mantenimientoEnEsperas = false;
        this.proximaComputadoraMantenimiento = 0;
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
        // Asegurarse de que el tiempo de regreso no sea negativo
        if (tiempoRegreso < 0) {
            tiempoRegreso = 0;
        }
        return new double[]{rnd, Math.round(tiempoRegreso * 100.0) / 100.0};
    }

    private Map<String, Object> obtenerEquipoLibre() {
        for (Map<String, Object> equipo : equipos) {
            if (equipo.get("estado") == EstadoEquipo.LIBRE) {
                return equipo;
            }
        }
        return null;
    }

    private void actualizarEstadoAlumno(String idAlumno, EstadoAlumno estado, double tiempoActual) {
        if (!estadoAlumnos.containsKey(idAlumno)) {
            estadoAlumnos.put(idAlumno, estado);
            tiempoLlegadaAlumnos.put(idAlumno, tiempoActual);
            if (estado == EstadoAlumno.EN_COLA) {
                alumnosEnCola.add(idAlumno);
                tiemposEspera.put(idAlumno, 0.0);
                alumnosConEspera++;
            }
        } else {
            EstadoAlumno anteriorEstado = estadoAlumnos.get(idAlumno);
            estadoAlumnos.put(idAlumno, estado);

            if (estado == EstadoAlumno.SIENDO_ATENDIDO && anteriorEstado == EstadoAlumno.EN_COLA) {
                alumnosEnCola.remove(idAlumno);
                double tiempoEspera = tiempoActual - tiempoLlegadaAlumnos.get(idAlumno);
                tiemposEspera.put(idAlumno, tiempoEspera);
                if (tiempoEspera > 0) {
                    tiempoEsperaTotal += tiempoEspera;
                }
            }
        }
    }

    private double calcularTiempoEspera(String idAlumno) {
        if (estadoAlumnos.containsKey(idAlumno)) {
            if (estadoAlumnos.get(idAlumno) == EstadoAlumno.EN_COLA) {
                double tiempoEspera = tiempoActual - tiempoLlegadaAlumnos.get(idAlumno);
                return Math.round(tiempoEspera * 100.0) / 100.0;
            }
            return Math.round(tiemposEspera.getOrDefault(idAlumno, 0.0) * 100.0) / 100.0;
        }
        return 0.0;
    }

    private double[] calcularEstadisticasEspera() {
        if (alumnosConEspera > 0) {
            double acumulado = Math.round(tiempoEsperaTotal * 100.0) / 100.0;
            double promedio = Math.round((acumulado / alumnosConEspera) * 100.0) / 100.0;
            return new double[]{acumulado, promedio};
        }
        return new double[]{0.0, 0.0};
    }

    private void agregarEstadosAlumnos(Map<String, Object> estadoActual) {
        for (int i = 1; i <= contadorAlumnos; i++) {
            String alumnoId = "A" + i;
            if (estadoAlumnos.containsKey(alumnoId)) {
                estadoActual.put("Estado " + alumnoId, estadoAlumnos.get(alumnoId).getValue());
                estadoActual.put("Tiempo Espera " + alumnoId, calcularTiempoEspera(alumnoId));
            } else {
                // Asegurarse de que todos los alumnos aparezcan, incluso si no han llegado
                estadoActual.put("Estado " + alumnoId, "N/A");
                estadoActual.put("Tiempo Espera " + alumnoId, "N/A");
            }
        }

        double[] estadisticas = calcularEstadisticasEspera();
        estadoActual.put("Tiempo Espera Acumulado", estadisticas[0]);
        estadoActual.put("Tiempo Espera Promedio", estadisticas[1]);
    }

    private Object[] obtenerProximoEvento() {
        List<Object[]> eventos = new ArrayList<>();
        eventos.add(new Object[]{"llegada", proximaLlegada});

        // Asegurarse de que proximoMantenimiento no sea null antes de agregarlo
        if (proximoMantenimiento != null) {
            eventos.add(new Object[]{"inicio_mantenimiento", proximoMantenimiento});
        }

        for (Map<String, Object> equipo : equipos) {
            // Asegurarse de que fin_mantenimiento no sea null antes de agregarlo
            if (equipo.get("fin_mantenimiento") != null) {
                eventos.add(new Object[]{"fin_mantenimiento", (Double) equipo.get("fin_mantenimiento"), equipo});
            }
            // Asegurarse de que fin_inscripcion no sea null antes de agregarlo
            if (equipo.get("fin_inscripcion") != null) {
                eventos.add(new Object[]{"fin_inscripcion", (Double) equipo.get("fin_inscripcion"), equipo});
            }
        }

        // Si no hay eventos futuros definidos (esto no debería pasar si la simulación es correcta)
        if (eventos.isEmpty()) {
            throw new RuntimeException("No hay eventos futuros definidos, la simulación no puede continuar.");
        }

        eventos.sort(Comparator.comparingDouble(o -> (double) o[1]));
        return eventos.get(0);
    }

    public List<Map<String, Object>> simular(double tiempoTotal) {
        double[] llegadaResult = generarTiempoLlegada();
        double rndLlegada = llegadaResult[0];
        double tiempoLlegada = llegadaResult[1];

        tiempoActual = 0;
        proximaLlegada = tiempoLlegada; // La primera llegada se genera inmediatamente
        contadorAlumnos = 0; // Se inicializa en 0, se incrementa al llegar el primer alumno

        // Inicializar el primer mantenimiento (si no hay equipos libres, se pospondrá)
        double[] mantResult = generarTiempoMantenimiento();
        double rndMant = mantResult[0];
        double tiempoMant = mantResult[1];
        proximoMantenimiento = tiempoMant; // La primera máquina entra en mantenimiento después de este tiempo

        // Si el primer equipo está libre, lo ponemos en mantenimiento
        Map<String, Object> primerEquipo = equipos.get(0);
        if (primerEquipo.get("estado") == EstadoEquipo.LIBRE) {
            primerEquipo.put("estado", EstadoEquipo.MANTENIMIENTO);
            primerEquipo.put("fin_mantenimiento", tiempoActual + tiempoMant); // Se marca el fin de mantenimiento
            proximaComputadoraMantenimiento = 1; // La siguiente máquina a mantener sería la 2
        } else {
            // Si el primer equipo no está libre (lo cual no debería pasar en la inicialización),
            // el mantenimiento se programará pero no se iniciará
            proximaComputadoraMantenimiento = 0; // Todavía no hemos mantenido ninguna máquina
            // proximoMantenimiento ya está establecido al primer tiempo de mantenimiento
        }


        Map<String, Object> estadoInicial = new LinkedHashMap<>();
        estadoInicial.put("Evento", "Inicializacion");
        estadoInicial.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estadoInicial.put("RND Llegada", Math.round(rndLlegada * 100.0) / 100.0);
        estadoInicial.put("Tiempo Llegada", Math.round(tiempoLlegada * 100.0) / 100.0);
        estadoInicial.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0); // Próxima llegada absoluta
        estadoInicial.put("Máquina", "N/A");
        estadoInicial.put("RND Inscripción", "N/A");
        estadoInicial.put("Tiempo Inscripción", "N/A");
        estadoInicial.put("Fin Inscripción", "N/A");
        estadoInicial.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0); // RND del primer mantenimiento
        estadoInicial.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0); // Tiempo del primer mantenimiento
        estadoInicial.put("Fin Mantenimiento", Math.round((Double) primerEquipo.getOrDefault("fin_mantenimiento", "N/A") * 100.0) / 100.0); // Fin del primer mantenimiento si inició
        estadoInicial.put("Próximo Inicio Mantenimiento", proximoMantenimiento != null ? Math.round(proximoMantenimiento * 100.0) / 100.0 : "N/A"); // Cuando ocurrirá el próximo evento de mantenimiento
        estadoInicial.put("RND Tiempo Vuelta", "N/A");
        estadoInicial.put("Tiempo Vuelta", "N/A");

        for (int i = 0; i < equipos.size(); i++) {
            estadoInicial.put("Máquina " + (i + 1), ((EstadoEquipo) equipos.get(i).get("estado")).getValue());
        }
        estadoInicial.put("Cola", cola);
        estadoInicial.put("max_alumnos", contadorAlumnos); // Se inicializa aquí para que React sepa el número de alumnos
        agregarEstadosAlumnos(estadoInicial);
        resultados.add(estadoInicial);


        while (tiempoActual < tiempoTotal) {
            Object[] evento = obtenerProximoEvento();
            tiempoActual = (double) evento[1]; // Actualiza el reloj al tiempo del próximo evento
            String tipoEvento = (String) evento[0];

            Map<String, Object> estado = null;

            if (tipoEvento.equals("llegada")) {
                double[] newLlegadaResult = generarTiempoLlegada();
                rndLlegada = newLlegadaResult[0];
                tiempoLlegada = newLlegadaResult[1];
                proximaLlegada = tiempoActual + tiempoLlegada; // Programa la próxima llegada
                contadorAlumnos++;
                String idActual = "A" + contadorAlumnos;
                estado = procesarLlegada(idActual, rndLlegada, tiempoLlegada);
            } else if (tipoEvento.equals("inicio_mantenimiento")) {
                estado = procesarInicioMantenimiento();
                if (estado == null) {
                    continue; // No se pudo iniciar el mantenimiento, pasar al siguiente evento
                }
            } else if (tipoEvento.equals("fin_mantenimiento")) {
                Map<String, Object> equipo = (Map<String, Object>) evento[2];
                estado = procesarFinMantenimiento(equipo);
            } else if (tipoEvento.equals("fin_inscripcion")) {
                Map<String, Object> equipo = (Map<String, Object>) evento[2];
                estado = procesarFinInscripcion(equipo);
            }

            if (estado != null) {
                // Es importante copiar los estados de los alumnos *antes* de que se modifiquen
                // por los eventos subsiguientes del mismo reloj si los hubiera.
                // Sin embargo, para esta simulación discreta, cada evento ocurre en un tiempo.
                // Si el mismo tiempo se procesa varias veces, solo la última copia será la final.
                // El contadorAlumnos se actualiza para cada fila que se añade.
                estado.put("max_alumnos", contadorAlumnos);
                resultados.add(estado);
            }
        }

        // Ajustar el contadorAlumnos final si es necesario (ej. para el rendering de la tabla)
        // Ya se está actualizando en cada fila, así que esto es un poco redundante aquí.
        // Lo importante es que cada fila tenga el 'max_alumnos' correcto para su momento.
        // No es necesario iterar de nuevo sobre `resultados` para añadir `max_alumnos`.
        // Este valor se establece en cada fila al momento de su creación.

        // Sort by 'Reloj' (redundante si ya se obtiene el evento más cercano en cada paso,
        // pero asegura el orden final)
        resultados.sort(Comparator.comparingDouble(o -> (double) o.get("Reloj")));
        return resultados;
    }

    private Map<String, Object> procesarLlegada(String idAlumno, double rndLlegada, double tiempoLlegada) {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Evento", "Llegada Alumno " + idAlumno);
        estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estado.put("RND Llegada", Math.round(rndLlegada * 100.0) / 100.0);
        estado.put("Tiempo Llegada", Math.round(tiempoLlegada * 100.0) / 100.0);
        estado.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);
        estado.put("Máquina", "N/A"); // Se actualizará si se asigna una máquina
        estado.put("RND Inscripción", "N/A");
        estado.put("Tiempo Inscripción", "N/A");
        estado.put("Fin Inscripción", "N/A");
        estado.put("RND Mantenimiento", "N/A"); // N/A para este tipo de evento
        estado.put("Tiempo Mantenimiento", "N/A"); // N/A para este tipo de evento
        estado.put("Fin Mantenimiento", "N/A"); // N/A para este tipo de evento
        estado.put("Próximo Inicio Mantenimiento", proximoMantenimiento != null ? Math.round(proximoMantenimiento * 100.0) / 100.0 : "N/A");
        estado.put("RND Tiempo Vuelta", "N/A");
        estado.put("Tiempo Vuelta", "N/A");


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
            actualizarEstadoAlumno(idAlumno, EstadoAlumno.SIENDO_ATENDIDO, tiempoActual);
        } else {
            cola++;
            actualizarEstadoAlumno(idAlumno, EstadoAlumno.EN_COLA, tiempoActual);
        }

        for (int i = 0; i < equipos.size(); i++) {
            estado.put("Máquina " + (i + 1), ((EstadoEquipo) equipos.get(i).get("estado")).getValue());
        }
        estado.put("Cola", cola);
        agregarEstadosAlumnos(estado);
        return estado;
    }

    private Map<String, Object> procesarInicioMantenimiento() {
        // En este punto, 'proximoMantenimiento' ya es el tiempo actual
        // Si no hay equipos libres o ya hay un mantenimiento en espera para un equipo libre, no hacemos nada
        Map<String, Object> equipoLibre = obtenerEquipoLibre();
        if (equipoLibre == null) {
            // No hay equipos libres para iniciar el mantenimiento ahora, se pospone implícitamente
            // al siguiente evento que libere una máquina. El 'proximoMantenimiento' se actualizará
            // cuando una máquina se libere o se complete un ciclo de mantenimiento.
            // Es crucial que el 'obtenerProximoEvento' tenga una forma de reevaluar esto.
            // Para simplificar, si no hay equipos libres, no se genera una fila para este evento,
            // y se espera a que se libere una máquina.
            return null; // No se puede iniciar mantenimiento ahora, no se genera una fila
        }

        double[] mantResult = generarTiempoMantenimiento();
        double rndMant = mantResult[0];
        double tiempoMant = mantResult[1];

        equipoLibre.put("estado", EstadoEquipo.MANTENIMIENTO);
        equipoLibre.put("fin_mantenimiento", tiempoActual + tiempoMant);
        // ProximoMantenimiento ya no se necesita aquí, ya que el evento de inicio ya ocurrió
        proximoMantenimiento = null; // Se establecerá el próximo inicio de mantenimiento al finalizar este o el siguiente ciclo

        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Evento", "Inicio Mantenimiento M" + equipoLibre.get("id"));
        estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estado.put("RND Llegada", "N/A");
        estado.put("Tiempo Llegada", "N/A");
        estado.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);
        estado.put("Máquina", equipoLibre.get("id"));
        estado.put("RND Inscripción", "N/A");
        estado.put("Tiempo Inscripción", "N/A");
        estado.put("Fin Inscripción", "N/A");
        estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
        estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
        estado.put("Fin Mantenimiento", Math.round((Double) equipoLibre.get("fin_mantenimiento") * 100.0) / 100.0);
        estado.put("Próximo Inicio Mantenimiento", proximoMantenimiento != null ? Math.round(proximoMantenimiento * 100.0) / 100.0 : "N/A");
        estado.put("RND Tiempo Vuelta", "N/A");
        estado.put("Tiempo Vuelta", "N/A");
        estado.put("Cola", cola);

        for (int i = 0; i < equipos.size(); i++) {
            estado.put("Máquina " + (i + 1), ((EstadoEquipo) equipos.get(i).get("estado")).getValue());
        }
        agregarEstadosAlumnos(estado);
        return estado;
    }

    private Map<String, Object> procesarFinMantenimiento(Map<String, Object> equipo) {
        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_mantenimiento", null);
        // mantenimientoEnEsperas ya no se usa, el sistema ahora busca equipos libres
        // para el mantenimiento basándose en proximoMantenimiento.

        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Evento", "Fin Mantenimiento M" + equipo.get("id"));
        estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estado.put("RND Llegada", "N/A");
        estado.put("Tiempo Llegada", "N/A");
        estado.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);
        estado.put("Máquina", equipo.get("id")); // Máquina que finalizó mantenimiento
        estado.put("RND Inscripción", "N/A");
        estado.put("Tiempo Inscripción", "N/A");
        estado.put("Fin Inscripción", "N/A");
        estado.put("RND Mantenimiento", "N/A"); // Se usará si se programa el siguiente
        estado.put("Tiempo Mantenimiento", "N/A"); // Se usará si se programa el siguiente
        estado.put("Fin Mantenimiento", "N/A"); // N/A para este evento, se libera la máquina
        estado.put("Cola", cola);

        // Se intenta atender al siguiente alumno en cola si hay
        if (!alumnosEnCola.isEmpty()) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];
            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.SIENDO_ATENDIDO, tiempoActual);
            cola--;
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipo.get("fin_inscripcion") * 100.0) / 100.0);
        }

        // Programar el próximo mantenimiento o el regreso del técnico
        if (proximaComputadoraMantenimiento < equipos.size()) {
            // Aún quedan máquinas por mantener en el ciclo actual
            Map<String, Object> siguienteEquipo = equipos.get(proximaComputadoraMantenimiento);
            proximaComputadoraMantenimiento++; // Mover al siguiente índice

            // Si el siguiente equipo está libre, lo programamos para mantenimiento inmediatamente
            if (siguienteEquipo.get("estado") == EstadoEquipo.LIBRE) {
                double[] mantResult = generarTiempoMantenimiento();
                double rndMant = mantResult[0];
                double tiempoMant = mantResult[1];
                siguienteEquipo.put("estado", EstadoEquipo.MANTENIMIENTO);
                siguienteEquipo.put("fin_mantenimiento", tiempoActual + tiempoMant);

                // Actualizar los campos de mantenimiento en el estado actual para la máquina que entra en mantenimiento
                estado.put("Máquina", siguienteEquipo.get("id")); // La máquina que AHORA entra en mantenimiento
                estado.put("RND Mantenimiento", Math.round(rndMant * 100.0) / 100.0);
                estado.put("Tiempo Mantenimiento", Math.round(tiempoMant * 100.0) / 100.0);
                estado.put("Fin Mantenimiento", Math.round((Double) siguienteEquipo.get("fin_mantenimiento") * 100.0) / 100.0);
            } else {
                // Si el siguiente equipo no está libre, se necesita programar el próximo inicio de mantenimiento
                // para cuando se libere un equipo o cuando el técnico esté disponible.
                // En este modelo, el técnico va en serie, así que simplemente lo dejamos para cuando se libere.
                // Para evitar un bucle de N/A en la tabla, simplemente no mostramos RND/Tiempo/Fin Mantenimiento aquí.
                // El proximoMantenimiento se establecerá cuando se necesite.
            }

        } else {
            // Todas las computadoras han sido mantenidas, programar el regreso del técnico para el próximo ciclo
            double[] regresoResult = generarTiempoRegreso();
            double rndVuelta = regresoResult[0];
            double tiempoVuelta = regresoResult[1];
            proximoMantenimiento = tiempoActual + tiempoVuelta; // Programar el inicio del próximo ciclo
            proximaComputadoraMantenimiento = 0; // Reiniciar para el próximo ciclo

            estado.put("RND Tiempo Vuelta", Math.round(rndVuelta * 100.0) / 100.0);
            estado.put("Tiempo Vuelta", Math.round(tiempoVuelta * 100.0) / 100.0);
            estado.put("Próximo Inicio Mantenimiento", Math.round(proximoMantenimiento * 100.0) / 100.0);
        }

        estado.put("Próximo Inicio Mantenimiento", proximoMantenimiento != null ? Math.round(proximoMantenimiento * 100.0) / 100.0 : "N/A");


        for (int i = 0; i < equipos.size(); i++) {
            estado.put("Máquina " + (i + 1), ((EstadoEquipo) equipos.get(i).get("estado")).getValue());
        }
        agregarEstadosAlumnos(estado);
        return estado;
    }

    private Map<String, Object> procesarFinInscripcion(Map<String, Object> equipo) {
        String alumnoFinalizado = (String) equipo.get("alumno_actual");
        actualizarEstadoAlumno(alumnoFinalizado, EstadoAlumno.ATENCION_FINALIZADA, tiempoActual);
        equipo.put("estado", EstadoEquipo.LIBRE);
        equipo.put("fin_inscripcion", null);
        equipo.put("alumno_actual", null);

        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("Evento", "Fin Inscripción " + alumnoFinalizado);
        estado.put("Reloj", Math.round(tiempoActual * 100.0) / 100.0);
        estado.put("RND Llegada", "N/A");
        estado.put("Tiempo Llegada", "N/A");
        estado.put("Próxima Llegada", Math.round(proximaLlegada * 100.0) / 100.0);
        estado.put("Máquina", equipo.get("id"));
        estado.put("RND Inscripción", "N/A");
        estado.put("Tiempo Inscripción", "N/A");
        estado.put("Fin Inscripción", "N/A");
        estado.put("RND Mantenimiento", "N/A");
        estado.put("Tiempo Mantenimiento", "N/A");
        estado.put("Fin Mantenimiento", "N/A");
        estado.put("Próximo Inicio Mantenimiento", proximoMantenimiento != null ? Math.round(proximoMantenimiento * 100.0) / 100.0 : "N/A");
        estado.put("RND Tiempo Vuelta", "N/A");
        estado.put("Tiempo Vuelta", "N/A");
        estado.put("Cola", cola);

        // Atender al siguiente alumno en cola si hay
        if (!alumnosEnCola.isEmpty()) {
            String siguienteAlumno = alumnosEnCola.get(0);
            double[] insResult = generarTiempoInscripcion();
            double rndIns = insResult[0];
            double tiempoIns = insResult[1];
            equipo.put("estado", EstadoEquipo.OCUPADO);
            equipo.put("fin_inscripcion", tiempoActual + tiempoIns);
            equipo.put("alumno_actual", siguienteAlumno);
            actualizarEstadoAlumno(siguienteAlumno, EstadoAlumno.SIENDO_ATENDIDO, tiempoActual);
            cola--;
            estado.put("RND Inscripción", Math.round(rndIns * 100.0) / 100.0);
            estado.put("Tiempo Inscripción", Math.round(tiempoIns * 100.0) / 100.0);
            estado.put("Fin Inscripción", Math.round((Double) equipo.get("fin_inscripcion") * 100.0) / 100.0);
        }

        for (int i = 0; i < equipos.size(); i++) {
            estado.put("Máquina " + (i + 1), ((EstadoEquipo) equipos.get(i).get("estado")).getValue());
        }
        agregarEstadosAlumnos(estado);
        return estado;
    }
}