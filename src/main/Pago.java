package co.edu.uniquindio.ProjectoFinal;

import java.io.Serializable;
import java.time.LocalDate;

public record Pago (int monto, LocalDate fecha, MedioPago medioPago) implements IAcumulable, Serializable {

    @Override
    public String toString (){
        return "Pago:" + monto + "-" + fecha;
    }

}