package co.edu.uniquindio.ProjectoFinal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

//Reportes avanzados del administrador y estadísticas de ocupación y cancelaciones.
//Cada método devuelve un texto con una línea por resultado, listo para mostrar en pantalla.
public class EstadisticaAvanzada {
    private Empresa empresa;

    public EstadisticaAvanzada(Empresa empresa){
        this.empresa= empresa;
    }

    //Auxiliares
    //Cuentan como ocupación las reservas Confirmadas, En curso y Finalizadas
    private boolean cuentaComoOcupacion(Reserva r){
        return r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.EN_CURSO || r.getEstado() == EstadoReserva.FINALIZADA;
    }

    private void validarRango(LocalDate desde, LocalDate hasta){
        if(desde == null || hasta == null || hasta.isBefore(desde)){
            throw new IllegalArgumentException("El rango de fechas no es válido");
        }
    }

    //Noches de una reserva que caen dentro del rango [desde, hasta] (ambos días incluidos)
    private int nochesEnRango(Reserva r, LocalDate desde, LocalDate hasta){
        LocalDate inicio= desde;
        if(r.getFechaEntrada().isAfter(desde)){
            inicio= r.getFechaEntrada();
        }
        LocalDate fin= hasta.plusDays(1);
        if(r.getFechaSalida().isBefore(fin)){
            fin= r.getFechaSalida();
        }
        long noches= ChronoUnit.DAYS.between(inicio, fin);
        if(noches < 0){
            return 0;
        }
        return (int) noches;
    }

    private int porcentaje(int parte, int total){
        if(total == 0){
            return 0;
        }
        return parte * 100 / total;
    }

    //Ocupación por apartamento
    public String ocupacionPorApartamento(LocalDate desde, LocalDate hasta){
        validarRango(desde, hasta);
        int nochesDelRango= (int) ChronoUnit.DAYS.between(desde, hasta.plusDays(1));
        String texto= "OCUPACIÓN POR APARTAMENTO (" + desde + " a " + hasta + ")\n\n";
        for(Apartamento a: empresa.getApartamentos()){
            int ocupadas= 0;
            for(Reserva r: empresa.getReservas()){
                if(cuentaComoOcupacion(r) && r.getApartamento().getCodigo().equals(a.getCodigo())){
                    ocupadas+= nochesEnRango(r, desde, hasta);
                }
            }
            texto+= a + ": " + ocupadas + " noches ocupadas de " + nochesDelRango + " (" + porcentaje(ocupadas, nochesDelRango) + " %)\n";
        }
        return texto;
    }

    //Ingresos por periodo: pagos recibidos menos devoluciones, según la fecha de cada movimiento
    public String ingresosPorPeriodo(LocalDate desde, LocalDate hasta){
        validarRango(desde, hasta);
        String texto= "INGRESOS POR PERIODO (" + desde + " a " + hasta + ")\n\n";
        int total= 0;
        for(Reserva r: empresa.getReservas()){
            int neto= 0;
            for(Pago p: r.getFolio().getPagos()){
                if(!p.fecha().isBefore(desde) && !p.fecha().isAfter(hasta)){
                    neto+= p.monto();
                }
            }
            if(neto != 0){
                texto+= r.getCodigo() + " (" + r.getCanal() + ", " + r.getApartamento().getCodigo() + "): " + Folio.dinero(neto) + "\n";
                total+= neto;
            }
        }
        texto+= "\nTOTAL: " + Folio.dinero(total);
        return texto;
    }

    //Reservas por canal, incluidas las canceladas y los no-show
    public String reservasPorCanal(){
        String texto= "RESERVAS POR CANAL\n\n";
        for(Canal c: Canal.values()){
            int total= 0;
            for(Reserva r: empresa.getReservas()){
                if(r.getCanal() == c){
                    total++;
                }
            }
            texto+= c + ": " + total + " reservas\n";
            for(EstadoReserva e: EstadoReserva.values()){
                int cantidad= 0;
                for(Reserva r: empresa.getReservas()){
                    if(r.getCanal() == c && r.getEstado() == e){
                        cantidad++;
                    }
                }
                texto+= "      " + e + ": " + cantidad + "\n";
            }
        }
        return texto;
    }

    //Saldos pendientes: reservas cuyo folio todavía debe dinero
    public String saldosPendientes(){
        String texto= "SALDOS PENDIENTES\n\n";
        int total= 0;
        for(Reserva r: empresa.getReservas()){
            int saldo= r.getFolio().calcularSaldo();
            if(saldo > 0){
                texto+= r.getCodigo() + " - " + r.getTitular().getNombre() + " (" + r.getEstado() + "): cargos " + Folio.dinero(r.getFolio().totalCargo())
                        + ", pagos " + Folio.dinero(r.getFolio().totalPago()) + ", SALDO " + Folio.dinero(saldo) + "\n";
                total+= saldo;
            }
        }
        texto+= "\nTOTAL PENDIENTE: " + Folio.dinero(total);
        return texto;
    }

    //Apartamentos con más novedades (de mayor a menor)
    public String apartamentosConMasNovedades(){
        LinkedList<Apartamento> apartamentos= empresa.getApartamentos();
        LinkedList<Integer> cantidades= new LinkedList<>();
        for(Apartamento a: apartamentos){
            int cantidad= 0;
            for(Novedad n: empresa.getNovedades()){
                if(n.getApartamento().getCodigo().equals(a.getCodigo())){
                    cantidad++;
                }
            }
            cantidades.add(cantidad);
        }
        String texto= "APARTAMENTOS CON MÁS NOVEDADES\n\n";
        //Se busca varias veces el mayor y se marca como usado (-1)
        for(int veces= 0; veces < apartamentos.size(); veces++){
            int posicionMayor= 0;
            for(int i= 1; i < cantidades.size(); i++){
                if(cantidades.get(i) > cantidades.get(posicionMayor)){
                    posicionMayor= i;
                }
            }
            if(cantidades.get(posicionMayor) <= 0){
                break;
            }
            texto+= apartamentos.get(posicionMayor) + ": " + cantidades.get(posicionMayor) + " novedades\n";
            cantidades.set(posicionMayor, -1);
        }
        return texto;
    }

    //Ocupación por temporada: noches ocupadas frente a noches disponibles
    public String ocupacionPorTemporada(LocalDate desde, LocalDate hasta){
        validarRango(desde, hasta);
        int apartamentosActivos= 0;
        for(Apartamento a: empresa.getApartamentos()){
            if(a.isActivo()){
                apartamentosActivos++;
            }
        }
        TemporadaAlojamiento[] tipos= TemporadaAlojamiento.values();
        int[] ocupadas= new int[tipos.length];
        int[] disponibles= new int[tipos.length];
        LocalDate noche= desde;
        while(!noche.isAfter(hasta)){
            int posicion= empresa.tipoTemporada(noche).ordinal();
            disponibles[posicion]+= apartamentosActivos;
            for(Reserva r: empresa.getReservas()){
                if(cuentaComoOcupacion(r) && !noche.isBefore(r.getFechaEntrada()) && noche.isBefore(r.getFechaSalida())){
                    ocupadas[posicion]++;
                }
            }
            noche= noche.plusDays(1);
        }
        String texto= "OCUPACIÓN POR TEMPORADA (" + desde + " a " + hasta + ")\n\n";
        for(int i= 0; i < tipos.length; i++){
            texto+= tipos[i] + ": " + ocupadas[i] + " noches ocupadas de " + disponibles[i] + " (" + porcentaje(ocupadas[i], disponibles[i]) + " %)\n";
        }
        return texto;
    }

    //Cancelaciones y no-show: cuántas hay, por canal, cuánto se devolvió y cuánto se retuvo
    public String comportamientoCancelaciones(){
        int total= 0;
        int canceladas= 0;
        int noShow= 0;
        int devuelto= 0;
        int retenido= 0;
        int sumaDias= 0;
        int conFecha= 0;
        for(Reserva r: empresa.getReservas()){
            total++;
            if(r.getEstado() == EstadoReserva.CANCELADA){
                canceladas++;
                if(r.getFechaCancelacion() != null){
                    sumaDias+= (int) ChronoUnit.DAYS.between(r.getFechaCancelacion(), r.getFechaEntrada());
                    conFecha++;
                }
            }
            if(r.getEstado() == EstadoReserva.NO_SHOW){
                noShow++;
            }
            if(r.getEstado() == EstadoReserva.CANCELADA || r.getEstado() == EstadoReserva.NO_SHOW){
                devuelto+= r.getDevolucion();
                for(Cargo c: r.getFolio().getCargos()){
                    if(c.tipo() == TipoCargo.PENALIDAD){
                        retenido+= c.monto();
                    }
                }
            }
        }
        String texto= "COMPORTAMIENTO DE LAS CANCELACIONES\n\n";
        texto+= "Reservas totales: " + total + "\n";
        texto+= "Canceladas: " + canceladas + " (" + porcentaje(canceladas, total) + " %)\n";
        texto+= "No-show: " + noShow + " (" + porcentaje(noShow, total) + " %)\n";
        for(Canal c: Canal.values()){
            int porCanal= 0;
            for(Reserva r: empresa.getReservas()){
                if(r.getCanal() == c && r.getEstado() == EstadoReserva.CANCELADA){
                    porCanal++;
                }
            }
            texto+= "Canceladas por el canal " + c + ": " + porCanal + "\n";
        }
        texto+= "Total devuelto: " + Folio.dinero(devuelto) + "\n";
        texto+= "Total retenido: " + Folio.dinero(retenido) + "\n";
        if(conFecha > 0){
            texto+= "Antelación promedio al cancelar: " + (sumaDias / conFecha) + " días\n";
        }
        return texto;
    }
}
