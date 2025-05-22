package grupo15.main.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generadores {

    public static List<Float> generadorUniforme(float muestra, float a, float b) {
        List<Float> numerosUniformes = new ArrayList<>();
        Random random = new Random('1');

        for (int i = 0; i < muestra; i++) {
            float numeroAleatorio = random.nextFloat(); // Número aleatorio entre 0 y 1
            float numeroTransformado = numeroAleatorio * (b - a) + a; // Transformación al intervalo [a, b)

            // Redondear a 4 decimales
            float numeroRedondeado = Math.round(numeroTransformado * 10000f) / 10000f;
            numerosUniformes.add(numeroRedondeado);
        }

        return numerosUniformes;
    }

    public static List<Float> generadorExponencial(float muestra, float lambda){
        List<Float> numerosUniformes = new ArrayList<>();
        Random random = new Random('7');

        for (int i = 0; i < muestra; i++) {
            float numeroAleatorio = random.nextFloat(); // Número aleatorio entre 0 y 1
            float numeroTransformado = (float) (-Math.log(1 - numeroAleatorio) / lambda); // Transformación a distribucion exponencial

            // Redondear a 4 decimales
            float numeroRedondeado = Math.round(numeroTransformado * 10000f) / 10000f;
            numerosUniformes.add(numeroRedondeado);
        }
        return numerosUniformes;
    }



    public static List<Float> generadorNormal(float cantidad, float media, float desviacion) {
        List<Float> muestra = new ArrayList<>();
        Random random = new Random(2);
        float PI = (float) Math.PI;

        float i = 0.0f;
        while (i < cantidad) {
            float u1 = random.nextFloat();
            float u2 = random.nextFloat();

            if (u1 == 0.0f) u1 = 0.000001f;

            float z1 = (float) Math.sqrt(-2.0f * Math.log(u1)) * (float) Math.cos(2.0f * PI * u2);
            float valorNormal = media + desviacion * z1;

            // Redondear a 4 decimales
            valorNormal = (float) (Math.round(valorNormal * 10000.0f) / 10000.0f);

            muestra.add(valorNormal);
            i += 1.0f;
        }

        return muestra;
    }

}


