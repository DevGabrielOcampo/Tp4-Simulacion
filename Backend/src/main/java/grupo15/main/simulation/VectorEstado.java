package grupo15.main.simulation;

import grupo15.main.objects.Alumno;
import grupo15.main.objects.Pc;
import grupo15.main.objects.Tecnico;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import grupo15.main.utils.Generators.*;

import java.util.List;

@Builder
@Getter
@Data
public class VectorEstado implements Cloneable{

    private Integer iteracion;
    @Builder.Default
    private String evento = "Inicialización";
    private Float reloj;
    private Float proximaLlegadaAlumno;
    private List<Pc> pcs;
    private Tecnico tecnico;
    private Integer acumAbandonos;
    private Integer colaAlumnos;
    private List<Alumno> alumnos;

    public VectorEstado(Integer iteracion, String evento, Float reloj, Float proximaLlegadaAlumno, List<Pc> pcs, Tecnico tecnico, Integer acumAbandonos, Integer colaAlumnos, List<Alumno> alumnos) {
        this.iteracion = iteracion;
        this.evento = evento;
        this.reloj = reloj;
        this.proximaLlegadaAlumno = proximaLlegadaAlumno;
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

    public Float getProximaLlegadaAlumno() {
        return proximaLlegadaAlumno;
    }

    public void setProximaLlegadaAlumno(Float proximaLlegadaAlumno) {
        this.proximaLlegadaAlumno = proximaLlegadaAlumno;
    }

    public List<Pc> getPcs() {
        return pcs;
    }

    public void setPcs(List<Pc> pcs) {
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

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    @Override
    public VectorEstado clone() {
        try {
            VectorEstado clone = (VectorEstado) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}