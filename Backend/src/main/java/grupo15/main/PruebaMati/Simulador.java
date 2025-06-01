package grupo15.main.PruebaMati;

import java.util.*;

public class Simulador {
    private int i, j;
    private double tiempoMaximo;
    private List<VectorEstado> vectorEstado = new ArrayList<>();
    private PriorityQueue<Evento> eventos = new PriorityQueue<>(Comparator.comparingDouble(e -> e.tiempo));
    private double tiempoActual = 0;
    private int nroEvento = 0;

    public Simulador(double tiempoMaximo, int i, int j) {
        this.tiempoMaximo = tiempoMaximo;
        this.i = i;
        this.j = j;
    }

    public void simular() {
        inicializarPrimerosEventos();

        while (!eventos.isEmpty() && tiempoActual <= tiempoMaximo) {
            Evento actual = eventos.poll();
            tiempoActual = actual.tiempo;
            nroEvento++;

            procesarEvento(actual);

            if (nroEvento >= i && nroEvento <= j) {
                guardarVectorEstado(actual);
            }
            /* Eliminamos la referencia de la fila */
            actual=null;
        }

        mostrarResultados();
    }

    private void guardarVectorEstado(Evento evento) {
        String pcs = "...";
        String tecnico = "...";
        String eventosFuturos = "...";
        String stats = "...";

        VectorEstado fila = new VectorEstado(nroEvento, tiempoActual, evento.tipo.toString(),
                pcs, tecnico, eventosFuturos, stats);
        vectorEstado.add(fila);
    }

    private void mostrarResultados() {
        for (VectorEstado fila : vectorEstado) {
            System.out.println(fila);
        }
    }

    private void procesarEvento(Evento e) {
        // Lógica de evento
    }

    private void inicializarPrimerosEventos() {
        eventos.add(new Evento(Evento.Tipo.LLEGADA_ALUMNO, generarTiempoLlegada()));
        eventos.add(new Evento(Evento.Tipo.NUEVO_MANTENIMIENTO, 0));
    }

    private double generarTiempoLlegada() {
        return -2 * Math.log(1 - Math.random());
    }

    private double generarTiempoInscripcion() {
        return 5 + Math.random() * 3;
    }

    private double generarTiempoMantenimiento() {
        return 3 + Math.random() * 7;
    }

    private double generarTiempoEntreMantenimientos() {
        return 60 + Math.random() * 120;
    }
}