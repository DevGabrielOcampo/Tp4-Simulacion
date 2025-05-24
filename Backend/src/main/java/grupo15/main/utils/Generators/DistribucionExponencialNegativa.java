package grupo15.main.utils.Generators;

public class DistribucionExponencialNegativa {
    static Float media = 2F;
    public static Float generadorExpNegativa(Double rnd) {
        if (media <= 0) {
            throw new IllegalArgumentException("La media no puede ser menor o igual a 0");
        }
        return (float) ((-media)*Math.log(1-rnd));
    }
}
