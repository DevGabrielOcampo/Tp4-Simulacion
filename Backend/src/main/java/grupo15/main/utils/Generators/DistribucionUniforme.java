package grupo15.main.utils.Generators;

public class DistribucionUniforme {
    public static Float generadorUniforme(Float rnd, Float a, Float b) {
        if (!esValido(a, b)) {
            throw new IllegalArgumentException("El valor de a debe ser menor que el valor de b");
        }
        return a + rnd*(b-a);
    }

    public static boolean esValido(Float a, Float b) {
        return a < b;
    }
}