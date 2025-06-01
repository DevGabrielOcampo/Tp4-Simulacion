package grupo15.main.PruebaMati;

public class Main {
    public static void main(String[] args) {
        Simulador sim = new Simulador(500, 10, 30); // Simular hasta t=500, mostrar eventos del 10 al 30
        sim.simular();
    }
}