package co.edu.uniquindio.ProjectoFinal;

import java.io.Serializable;

public record Cargo (String concepto, int monto, TipoCargo tipo) implements IAcumulable, Serializable {

    @Override
    public String toString (){
        return "Cargo:" + concepto + "-" + monto;
    }

}
