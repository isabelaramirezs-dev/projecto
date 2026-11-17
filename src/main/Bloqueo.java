package co.edu.uniquindio.ProjectoFinal;

import java.io.Serializable;
import java.time.LocalDate;

public class Bloqueo implements Serializable {
    private Apartamento apartamento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;

    public Bloqueo(Apartamento apartamento, LocalDate fechaInicio, LocalDate fechaFin, String motivo) {
        this.apartamento = apartamento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "Bloqueo:" + apartamento.getCodigo() + "-" + fechaInicio + "-" + fechaFin + "-" + motivo;
    }

    //Un apartamento con un bloqueo sobre una noche no está disponible para esa noche.
    //El rango incluye la fecha de inicio y la de fin (igual que hayConflicto en Empresa)
    public boolean cubre(LocalDate noche) {
        if (noche == null) {
            return false;
        }
        return !noche.isBefore(fechaInicio) && !noche.isAfter(fechaFin);
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public void setApartamento(Apartamento apartamento) {
        this.apartamento = apartamento;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
