package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import grupo15.main.states.EstadoPc;
import grupo15.main.states.EstadoTecnico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulacion {

    private VectorEstado estado;
    private List<VectorEstado> historial;

    public VectorEstado getEstado() {
        return estado;
    }

    public void setEstado(VectorEstado estado) {
        this.estado = estado;
    }

    public Simulacion(Float minInscripcion, Float maxInscripcion, Float mediaExponencialNegativa, Float minMantenimiento, Float maxMantenimiento, Float baseRegresoTecnico, Float rangoRegresoTecnico, Float minutosSimulacion, Float minutosDesde, Float iteracionesMostrar) {
        estado = inicializarEstado(minInscripcion, maxInscripcion, mediaExponencialNegativa, minMantenimiento, maxMantenimiento, baseRegresoTecnico, rangoRegresoTecnico, minutosSimulacion, minutosDesde, iteracionesMostrar);
    }

    private VectorEstado inicializarEstado(Float minInscripcion, Float maxInscripcion, Float mediaExponencialNegativa, Float minMantenimiento, Float maxMantenimiento, Float baseRegresoTecnico, Float rangoRegresoTecnico, Float minutosSimulacion, Float minutosDesde, Float iteracionesMostrar) {
        // Crear las 5 PC iniciales
        List<Pc> pcs = new ArrayList<>(); // Inicializamos una lista vacía
        for (int i = 0; i < 5; i++) {
            pcs.add(new Pc(
                    i + 1,         // ID de la PC (del 1 al 5)
                    EstadoPc.LIBRE,  // Estado inicial libre
                    0.0f,            // Random (Esto más que un RND debería ser el generador)
                    0.0f,            // Duración de inscripción (valor inicial, aplicar distribución luego)
                    minInscripcion,  // Parámetro min (definido más arriba)
                    maxInscripcion   // Parámetro max (definido más arriba)
            ));
        }

        // Crear el técnico inicial
        Tecnico tecnico = new Tecnico(
                EstadoTecnico.ESPERANDO_PARA_MANTENIMIENTO,       // Estado inicial
                0.0f,                 // Random de Regreso (Creo que esto mas que un RND deberia ser el generador para pedirle los numeros y agregar atributo para el rnd)
                0.0f,                 // Duración del descanso (a calcular después)
                baseRegresoTecnico,   // Base de regreso
                rangoRegresoTecnico,  // Rango de regreso (±3 min)
                0.0f,                 // Random de mantenimiento (a calcular después)
                0.0f,                 // Duración del mantenimiento inicial (valor arbitrario, ajustar con distribución)
                minMantenimiento,     // Min duración mantenimiento
                maxMantenimiento,     // Max duración mantenimiento
                null,                 // Última PC mantenida (aún no hizo mantenimiento)
                0.0f,                 // Acumulador tiempo ocioso
                0.0f,                 // Acumulador tiempo total
                0.0f                  // Promedio tiempo ocioso
        );
        tecnico.iniciarMantenimiento(pcs, 1);

        List<Alumno> alumnos = new ArrayList<>();
        Alumno alumnoInicial = new Alumno(mediaExponencialNegativa);
        alumnos.add(alumnoInicial);




        // Retornar el estado inicial
        return new VectorEstado(0, "Inicialización", 0.0f, alumnoInicial.getDuracionLlegada(), tecnico.getDuracionDescanso(), tecnico.getDuracionMantenimiento(),
                pcs.get(0).getDuracionInscripcion(), pcs.get(1).getDuracionInscripcion(), pcs.get(2).getDuracionInscripcion(), pcs.get(3).getDuracionInscripcion(), pcs.get(4).getDuracionInscripcion(),
                pcs,  tecnico, 0, 0, alumnos);
    }

    public List<VectorEstado> ejecutar(Float duracionMinutos, Float minutosDesde, Float iteracionesMostrar, Float mediaExponencialNegativa) {
        this.historial = new ArrayList<>();

        int iteracionesGuardadas = 0;

        while (estado.getReloj() < duracionMinutos) {
            // Avanzar la simulación una iteración
            avanzarSimulacion(mediaExponencialNegativa);
            estado.setIteracion(estado.getIteracion() + 1);

            // Condición para guardar
            if (estado.getReloj() >= minutosDesde && iteracionesGuardadas < iteracionesMostrar) {
                // Cloná el estado o guardá una copia si no querés mutar referencias
                historial.add(estado.clone()); // o new Estado(estado)
                iteracionesGuardadas++;
            }
        }

        return historial;
    }

    public void avanzarSimulacion(Float mediaExponencialNegativa) {
        Float proximoEvento = Float.MAX_VALUE;
        String tipoEvento = "";

        // Verificar próxima llegada de alumno
        if (estado.getProximaLlegadaAlumno() != null && estado.getProximaLlegadaAlumno() < proximoEvento) {
            proximoEvento = estado.getProximaLlegadaAlumno();
            tipoEvento = "Llegada Alumno";
        }

      // // Verificar próxima llegada del técnico
      // if (estado.getProximaLlegadaTecnico() != null && estado.getProximaLlegadaTecnico() < proximoEvento) {
      //     proximoEvento = estado.getProximaLlegadaTecnico();
      //     tipoEvento = "Llegada Técnico";
      // }

      // // Verificar fin de mantenimiento
      // if (estado.getFinMantenimiento() != null && estado.getFinMantenimiento() < proximoEvento) {
      //     proximoEvento = estado.getFinMantenimiento();
      //     tipoEvento = "Fin Mantenimiento";
      // }

      // // Verificar fin de inscripción de cada PC (del 1 al 5)
      // if (estado.getFinInscripcionPc1() != null && estado.getFinInscripcionPc1() < proximoEvento) {
      //     proximoEvento = estado.getFinInscripcionPc1();
      //     tipoEvento = "Fin Inscripcion Pc1";
      // }

      // if (estado.getFinInscripcionPc2() != null && estado.getFinInscripcionPc2() < proximoEvento) {
      //     proximoEvento = estado.getFinInscripcionPc2();
      //     tipoEvento = "Fin Inscripcion Pc2";
      // }

      // if (estado.getFinInscripcionPc3() != null && estado.getFinInscripcionPc3() < proximoEvento) {
      //     proximoEvento = estado.getFinInscripcionPc3();
      //     tipoEvento = "Fin Inscripcion Pc3";
      // }

      // if (estado.getFinInscripcionPc4() != null && estado.getFinInscripcionPc4() < proximoEvento) {
      //     proximoEvento = estado.getFinInscripcionPc4();
      //     tipoEvento = "Fin Inscripcion Pc4";
      // }

      // if (estado.getFinInscripcionPc5() != null && estado.getFinInscripcionPc5() < proximoEvento) {
      //     proximoEvento = estado.getFinInscripcionPc5();
      //     tipoEvento = "Fin Inscripcion Pc5";
      // }

        // Avanzar el reloj al tiempo del evento más próximo
        estado.setReloj(proximoEvento);
        estado.setEvento(tipoEvento);

        // Acá después podrías hacer switch para cada tipo de evento
        switch (tipoEvento) {
            case "Llegada Alumno":
                estado.setReloj(proximoEvento);

                Alumno nuevoAlumno = new Alumno(mediaExponencialNegativa);
                estado.setProximaLlegadaAlumno(nuevoAlumno.getDuracionLlegada() + estado.getReloj());

                List<Pc> pcs = estado.getPcs();
                boolean asignado = false;

                for (Pc pc : pcs) {
                    if (pc.getEstado() == EstadoPc.LIBRE) {
                        // Asignar alumno
                        pc.setEstado(EstadoPc.OCUPADA_POR_ALUMNO);
                        pc.generarDuracionInscripcion();

                        // Setear el fin de inscripción correspondiente en el estado
                        switch (pc.getId()) {
                            case 1:
                                estado.setFinInscripcionPc1(estado.getReloj() + pc.getDuracionInscripcion());
                                break;
                            case 2:
                                estado.setFinInscripcionPc2(estado.getReloj() + pc.getDuracionInscripcion());
                                break;
                            case 3:
                                estado.setFinInscripcionPc3(estado.getReloj() + pc.getDuracionInscripcion());
                                break;
                            case 4:
                                estado.setFinInscripcionPc4(estado.getReloj() + pc.getDuracionInscripcion());
                                break;
                            case 5:
                                estado.setFinInscripcionPc5(estado.getReloj() + pc.getDuracionInscripcion());
                                break;
                        }

                        asignado = true;
                        break; // Ya asignamos, salimos del loop
                    }
                }

                if (!asignado) {
                    // No hay PC libre → se suma a la cola
                    estado.setColaAlumnos(estado.getColaAlumnos() + 1);
                }

                estado.getAlumnos().add(nuevoAlumno); // Siempre se registra el alumno
                break;
//
         //   case "Llegada Técnico":
         //       estado.setReloj(100f);
         //       break;
         //   case "Fin Mantenimiento":
         //       estado.setReloj(100f);
         //       break;
         //   case "Fin Inscripcion Pc1":
         //   case "Fin Inscripcion Pc2":
         //   case "Fin Inscripcion Pc3":
         //   case "Fin Inscripcion Pc4":
         //   case "Fin Inscripcion Pc5":
         //       int pcId = Integer.parseInt(tipoEvento.replace("Fin Inscripcion Pc", ""));
         //       estado.setReloj(100f);
         //       break;
        }
    }



}

