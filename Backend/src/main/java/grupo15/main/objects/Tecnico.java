package grupo15.main.objects;

import grupo15.main.states.EstadoPc;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tecnico implements Cloneable {

    @Builder.Default
    private EstadoPc estado = EstadoPc.LIBRE;

    // Parte relacionada con la llegada del tecnico
    private Float randomRegreso;
    private Float duracionDescanso;
    private Float tiempoRegreso;
    // Parámetros para las distribucion de regreso
    private Float base;  // 1h
    private Float rango;  // +-3min

    // Parte relacionada con la llegada del tecnico
    private Float randomMantenimiento;
    private Float duracionMantenimiento;
    private Float finMantenimiento;
    // Parámetros para las distribuciones uniformes en los mantenimientos
    private Float min;  // a
    private Float max;  // b

    private Pc ultimaPcMantenida;

    private Float acumTiempoOcioso;
    private Float acumTiempoTotal;
    private Float promedioTiempoOcioso;

    public Tecnico(EstadoPc estado, Float randomRegreso, Float duracionDescanso, Float tiempoRegreso, Float base, Float rango, Float randomMantenimiento, Float duracionMantenimiento, Float finMantenimiento, Float min, Float max, Pc ultimaPcMantenida, Float acumTiempoOcioso, Float acumTiempoTotal, Float promedioTiempoOcioso) {
        this.estado = estado;
        this.randomRegreso = randomRegreso;
        this.duracionDescanso = duracionDescanso;
        this.tiempoRegreso = tiempoRegreso;
        this.base = base;
        this.rango = rango;
        this.randomMantenimiento = randomMantenimiento;
        this.duracionMantenimiento = duracionMantenimiento;
        this.finMantenimiento = finMantenimiento;
        this.min = min;
        this.max = max;
        this.ultimaPcMantenida = ultimaPcMantenida;
        this.acumTiempoOcioso = acumTiempoOcioso;
        this.acumTiempoTotal = acumTiempoTotal;
        this.promedioTiempoOcioso = promedioTiempoOcioso;
    }

    public EstadoPc getEstado() {
        return estado;
    }

    public void setEstado(EstadoPc estado) {
        this.estado = estado;
    }

    public Float getRandomRegreso() {
        return randomRegreso;
    }

    public void setRandomRegreso(Float randomRegreso) {
        this.randomRegreso = randomRegreso;
    }

    public Float getDuracionDescanso() {
        return duracionDescanso;
    }

    public void setDuracionDescanso(Float duracionDescanso) {
        this.duracionDescanso = duracionDescanso;
    }

    public Float getTiempoRegreso() {
        return tiempoRegreso;
    }

    public void setTiempoRegreso(Float tiempoRegreso) {
        this.tiempoRegreso = tiempoRegreso;
    }

    public Float getBase() {
        return base;
    }

    public void setBase(Float base) {
        this.base = base;
    }

    public Float getRango() {
        return rango;
    }

    public void setRango(Float rango) {
        this.rango = rango;
    }

    public Float getRandomMantenimiento() {
        return randomMantenimiento;
    }

    public void setRandomMantenimiento(Float randomMantenimiento) {
        this.randomMantenimiento = randomMantenimiento;
    }

    public Float getDuracionMantenimiento() {
        return duracionMantenimiento;
    }

    public void setDuracionMantenimiento(Float duracionMantenimiento) {
        this.duracionMantenimiento = duracionMantenimiento;
    }

    public Float getFinMantenimiento() {
        return finMantenimiento;
    }

    public void setFinMantenimiento(Float finMantenimiento) {
        this.finMantenimiento = finMantenimiento;
    }

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Pc getUltimaPcMantenida() {
        return ultimaPcMantenida;
    }

    public void setUltimaPcMantenida(Pc ultimaPcMantenida) {
        this.ultimaPcMantenida = ultimaPcMantenida;
    }

    public Float getAcumTiempoOcioso() {
        return acumTiempoOcioso;
    }

    public void setAcumTiempoOcioso(Float acumTiempoOcioso) {
        this.acumTiempoOcioso = acumTiempoOcioso;
    }

    public Float getAcumTiempoTotal() {
        return acumTiempoTotal;
    }

    public void setAcumTiempoTotal(Float acumTiempoTotal) {
        this.acumTiempoTotal = acumTiempoTotal;
    }

    public Float getPromedioTiempoOcioso() {
        return promedioTiempoOcioso;
    }

    public void setPromedioTiempoOcioso(Float promedioTiempoOcioso) {
        this.promedioTiempoOcioso = promedioTiempoOcioso;
    }

    @Override
    public Tecnico clone() {
        try {
            Tecnico clone = (Tecnico) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}