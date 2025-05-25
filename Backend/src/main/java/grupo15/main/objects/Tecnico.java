package grupo15.main.objects;

import grupo15.main.states.EstadoPc;
import grupo15.main.states.EstadoTecnico;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Random;

@Data
@Builder
public class Tecnico implements Cloneable {

    @Builder.Default
    private EstadoTecnico estado = EstadoTecnico.ESPERANDO_PARA_MANTENIMIENTO;

    // Parte relacionada con la llegada del tecnico
    private Random randomRegreso;
    private Float numRandomRegreso;
    private Float duracionDescanso;
    // Parámetros para las distribucion de regreso
    private Float base;  // 1h
    private Float rango;  // +-3min

    // Parte relacionada con el tiempo de mantenimiento
    private Random randomMantenimieto;
    private Float numRandomMantenimiento;
    private Float duracionMantenimiento;
    // Parámetros para las distribuciones uniformes en los mantenimientos
    private Float min;  // a
    private Float max;  // b

    private Pc ultimaPcMantenida;

    private Float acumTiempoOcioso;
    private Float acumTiempoTotal;
    private Float promedioTiempoOcioso;

    public void iniciarMantenimiento(List<Pc> pcs, Integer numPcMantener) {
        if(this.estado == EstadoTecnico.ESPERANDO_PARA_MANTENIMIENTO){
            for (Pc pc : pcs) {
                if (pc.getId() == numPcMantener && pc.getEstado() == EstadoPc.LIBRE) {
                    calcularTiempoMantenimiento();
                    this.ultimaPcMantenida = pc; // Asigna la PC al técnico
                    this.estado = EstadoTecnico.EN_MANTENIMIENTO;
                    pc.setEstado(EstadoPc.EN_MANTENIMIENTO); // Cambia el estado de la PC
                    return;
                }
            }
        }
    }

    public void calcularTiempoMantenimiento(){
        this.numRandomMantenimiento = this.randomMantenimieto.nextFloat();
        this.duracionMantenimiento = this.min + this.numRandomMantenimiento*(this.max-this.min);
    }

    public Tecnico(EstadoTecnico estado, Float numRandomRegreso, Float duracionDescanso, Float base, Float rango, Float numRandomMantenimiento, Float duracionMantenimiento, Float min, Float max, Pc ultimaPcMantenida, Float acumTiempoOcioso, Float acumTiempoTotal, Float promedioTiempoOcioso) {
        this.estado = estado;
        this.randomRegreso = new Random();
        this.numRandomRegreso = numRandomRegreso;
        this.duracionDescanso = duracionDescanso;
        this.base = base;
        this.rango = rango;
        this.randomMantenimieto = new Random();
        this.numRandomMantenimiento = numRandomMantenimiento;
        this.duracionMantenimiento = duracionMantenimiento;
        this.min = min;
        this.max = max;
        this.ultimaPcMantenida = ultimaPcMantenida;
        this.acumTiempoOcioso = acumTiempoOcioso;
        this.acumTiempoTotal = acumTiempoTotal;
        this.promedioTiempoOcioso = promedioTiempoOcioso;
    }

    public EstadoTecnico getEstado() {
        return estado;
    }

    public void setEstado(EstadoTecnico estado) {
        this.estado = estado;
    }

    public Random getRandomRegreso() {
        return randomRegreso;
    }

    public void setRandomRegreso(Random randomRegreso) {
        this.randomRegreso = randomRegreso;
    }

    public Float getNumRandomRegreso() {
        return numRandomRegreso;
    }

    public void setNumRandomRegreso(Float numRandomRegreso) {
        this.numRandomRegreso = numRandomRegreso;
    }

    public Float getDuracionDescanso() {
        return duracionDescanso;
    }

    public void setDuracionDescanso(Float duracionDescanso) {
        this.duracionDescanso = duracionDescanso;
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

    public Random getRandomMantenimieto() {
        return randomMantenimieto;
    }

    public void setRandomMantenimieto(Random randomMantenimieto) {
        this.randomMantenimieto = randomMantenimieto;
    }

    public Float getNumRandomMantenimiento() {
        return numRandomMantenimiento;
    }

    public void setNumRandomMantenimiento(Float numRandomMantenimiento) {
        this.numRandomMantenimiento = numRandomMantenimiento;
    }

    public Float getDuracionMantenimiento() {
        return duracionMantenimiento;
    }

    public void setDuracionMantenimiento(Float duracionMantenimiento) {
        this.duracionMantenimiento = duracionMantenimiento;
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