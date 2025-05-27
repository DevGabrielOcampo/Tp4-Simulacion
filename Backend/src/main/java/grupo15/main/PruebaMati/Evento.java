package grupo15.main.PruebaMati;

public class Evento {
    enum Tipo { LLEGADA_ALUMNO, FIN_INSCRIPCION, FIN_MANTENIMIENTO, NUEVO_MANTENIMIENTO }
    Tipo tipo;
    double tiempo;

    public Evento(Tipo tipo, double tiempo) {
        this.tipo = tipo;
        this.tiempo = tiempo;
    }
}