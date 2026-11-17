package co.edu.uniquindio.ProjectoFinal;

import java.io.Serializable;
import java.util.LinkedList;

public class Folio implements Serializable {

    private final LinkedList<Cargo> cargos;
    private final LinkedList<Pago> pagos;

    public Folio(){
        this.cargos= new LinkedList<>();
        this.pagos= new LinkedList<>();
    }

    public void agregarCargo(Cargo c){
        if (c == null){
            System.out.println("El cargo no puede ser nulo");
            return;
        }
        cargos.add(c);
    }

    public void registrarPago(Pago p){
        if (p == null){
            System.out.println("El pago no puede ser nulo");
            return;
        }
        pagos.add(p);
    }

    public int totalCargo(){
        int total=0;
        for (Cargo c: cargos){
            total+= c.monto();
        }
        return total;
    }
    public int totalPago(){
        int total=0;
        for (Pago p: pagos){
            total+=p.monto();
        }
        return total;
    }
    //Los valores se manejan en pesos colombianos, sin decimales. Ejemplo: 1500000 se muestra como $1.500.000
    public static String dinero(int valor){
        return "$" + String.format("%,d", valor).replace(',', '.');
    }

    //El saldo es la diferencia entre cargos y pagos
    public int calcularSaldo(){

        return totalCargo()- totalPago();
    }
    public int calcularTotal(){

        return totalCargo();
    }

    //Devuelve una copia porque los cargos y pagos de un folio no se modifican ni se eliminan.
    public LinkedList<Cargo> getCargos() {
        return new LinkedList<>(cargos);
    }

    public LinkedList<Pago> getPagos() {
        return new LinkedList<>(pagos);
    }
}
