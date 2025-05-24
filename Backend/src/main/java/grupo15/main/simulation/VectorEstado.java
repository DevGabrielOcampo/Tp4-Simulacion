package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import grupo15.main.utils.Generators.*;

@Builder
@Getter
@Data
public class VectorEstado {

    private Integer iteracion;
    @Builder.Default
    private String evento = "Inicialización";
    private Float reloj;
    private Pc[] pcs;
    private Tecnico tecnico;
    private Integer acumAbandonos;
    private Integer colaAlumnos;
    private Alumno[] alumnos;

    public VectorEstado(Integer iteracion, String evento, Float reloj, Pc[] pcs, Tecnico tecnico, Integer acumAbandonos, Integer colaAlumnos, Alumno[] alumnos) {
        this.iteracion = iteracion;
        this.evento = evento;
        this.reloj = reloj;
        this.pcs = pcs;
        this.tecnico = tecnico;
        this.acumAbandonos = acumAbandonos;
        this.colaAlumnos = colaAlumnos;
        this.alumnos = alumnos;
    }

    public Integer getIteracion() {
        return iteracion;
    }

    public void setIteracion(Integer iteracion) {
        this.iteracion = iteracion;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public Float getReloj() {
        return reloj;
    }

    public void setReloj(Float reloj) {
        this.reloj = reloj;
    }

    public Pc[] getPcs() {
        return pcs;
    }

    public void setPcs(Pc[] pcs) {
        this.pcs = pcs;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public Integer getAcumAbandonos() {
        return acumAbandonos;
    }

    public void setAcumAbandonos(Integer acumAbandonos) {
        this.acumAbandonos = acumAbandonos;
    }

    public Integer getColaAlumnos() {
        return colaAlumnos;
    }

    public void setColaAlumnos(Integer colaAlumnos) {
        this.colaAlumnos = colaAlumnos;
    }

    public Alumno[] getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(Alumno[] alumnos) {
        this.alumnos = alumnos;
    }
}