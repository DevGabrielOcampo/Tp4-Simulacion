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
    private Float proximaLlegadaTecnico;
    private Float finMantenimiento;
    private Float finInscripcionPc1;
    private Float finInscripcionPc2;
    private Float finInscripcionPc3;
    private Float finInscripcionPc4;
    private Float finInscripcionPc5;
    private List<Pc> pcs;
    private Tecnico tecnico;
    private Integer acumAbandonos;
    private Integer colaAlumnos;
    private List<Alumno> alumnos;

    public VectorEstado(Integer iteracion, String evento, Float reloj, Float proximaLlegadaAlumno, Float proximaLlegadaTecnico, Float finMantenimiento, Float finInscripcionPc1, Float finInscripcionPc2, Float finInscripcionPc3, Float finInscripcionPc4, Float finInscripcionPc5, List<Pc> pcs, Tecnico tecnico, Integer acumAbandonos, Integer colaAlumnos, List<Alumno> alumnos) {
        this.iteracion = iteracion;
        this.evento = evento;
        this.reloj = reloj;
        this.proximaLlegadaAlumno = proximaLlegadaAlumno;
        this.proximaLlegadaTecnico = proximaLlegadaTecnico;
        this.finMantenimiento = finMantenimiento;
        this.finInscripcionPc1 = finInscripcionPc1;
        this.finInscripcionPc2 = finInscripcionPc2;
        this.finInscripcionPc3 = finInscripcionPc3;
        this.finInscripcionPc4 = finInscripcionPc4;
        this.finInscripcionPc5 = finInscripcionPc5;
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

    public Float getProximaLlegadaTecnico() {
        return proximaLlegadaTecnico;
    }

    public void setProximaLlegadaTecnico(Float proximaLlegadaTecnico) {
        this.proximaLlegadaTecnico = proximaLlegadaTecnico;
    }

    public Float getFinMantenimiento() {
        return finMantenimiento;
    }

    public void setFinMantenimiento(Float finMantenimiento) {
        this.finMantenimiento = finMantenimiento;
    }

    public Float getFinInscripcionPc1() {
        return finInscripcionPc1;
    }

    public void setFinInscripcionPc1(Float finInscripcionPc1) {
        this.finInscripcionPc1 = finInscripcionPc1;
    }

    public Float getFinInscripcionPc2() {
        return finInscripcionPc2;
    }

    public void setFinInscripcionPc2(Float finInscripcionPc2) {
        this.finInscripcionPc2 = finInscripcionPc2;
    }

    public Float getFinInscripcionPc3() {
        return finInscripcionPc3;
    }

    public void setFinInscripcionPc3(Float finInscripcionPc3) {
        this.finInscripcionPc3 = finInscripcionPc3;
    }

    public Float getFinInscripcionPc4() {
        return finInscripcionPc4;
    }

    public void setFinInscripcionPc4(Float finInscripcionPc4) {
        this.finInscripcionPc4 = finInscripcionPc4;
    }

    public Float getFinInscripcionPc5() {
        return finInscripcionPc5;
    }

    public void setFinInscripcionPc5(Float finInscripcionPc5) {
        this.finInscripcionPc5 = finInscripcionPc5;
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