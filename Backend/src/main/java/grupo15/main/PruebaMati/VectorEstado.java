package grupo15.main.PruebaMati;

public class VectorEstado {
    int nroEvento;
    double tiempo;
    String tipoEvento;
    String estadoPCs;
    String estadoTecnico;
    String eventosFuturos;
    String estadisticas;

    public VectorEstado(int nroEvento, double tiempo, String tipoEvento, String estadoPCs,
                        String estadoTecnico, String eventosFuturos, String estadisticas) {
        this.nroEvento = nroEvento;
        this.tiempo = tiempo;
        this.tipoEvento = tipoEvento;
        this.estadoPCs = estadoPCs;
        this.estadoTecnico = estadoTecnico;
        this.eventosFuturos = eventosFuturos;
        this.estadisticas = estadisticas;
    }

    @Override
    public String toString() {
        return String.format("Evento #%d | t=%.2f | %s | PCs: %s | Técnico: %s | Eventos: %s | Stats: %s",
                nroEvento, tiempo, tipoEvento, estadoPCs, estadoTecnico, eventosFuturos, estadisticas);
    }
}