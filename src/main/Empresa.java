package co.edu.uniquindio.ProjectoFinal;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class Empresa implements Serializable {
    private String nit;
    private String nombre;
    private String ubicacion;

    private LinkedList<String> codigosExternos;
    private PoliticaCancelacion politicaCancelacion;
    private int consecutivoReservas;

    private final LinkedList<Apartamento> apartamentos;
    private final LinkedList<Reserva> reservas;
    private final LinkedList<Temporada> temporadas;
    private final LinkedList<Bloqueo> bloqueos;
    private final LinkedList<ConflictoCanal> conflictoCanales;
    private final LinkedList<Rol> roles;
    private final LinkedList<Novedad> novedades;

    public Empresa(String nit, String nombre, String ubicacion){
        this.nit= nit;
        this.nombre=nombre;
        this.ubicacion=ubicacion;

        this.codigosExternos= new LinkedList<>();
        //Política por defecto: más de 7 días se devuelve todo; entre 2 y 7 se retiene el 50 %
        this.politicaCancelacion= new PoliticaCancelacion(7, 2, 50);
        this.consecutivoReservas= 0;

        this.apartamentos= new LinkedList<>();
        this.reservas= new LinkedList<>();
        this.temporadas= new LinkedList<>();
        this.bloqueos= new LinkedList<>();
        this.conflictoCanales= new LinkedList<>();
        this.roles= new LinkedList<>();
        this.novedades= new LinkedList<>();
    }

    //Roles: huéspedes, recepcionistas y administrador
    public void registrarHuesped(Huesped h){
        registrarRol(h);
    }

    public void registrarRecepcionista(Recepcionista r){
        registrarRol(r);
    }

    public void registrarAdministrador(AdministradorAlojamiento a){
        registrarRol(a);
    }

    private void registrarRol(Rol nuevo){
        if(nuevo == null){
            throw new IllegalArgumentException("La persona no puede ser nula");
        }
        if(vacio(nuevo.getNombre()) || vacio(nuevo.getCedula()) || vacio(nuevo.getUsuario()) || vacio(nuevo.getContrasenia())){
            throw new IllegalArgumentException("El nombre, la cédula, el usuario y la contraseña son obligatorios");
        }
        for(Rol r: roles){
            if(r.getCedula().equals(nuevo.getCedula())){
                throw new IllegalArgumentException("Ya existe una persona con esa cédula");
            }
            if(r.getUsuario().equals(nuevo.getUsuario())){
                throw new IllegalArgumentException("Ya existe una persona con ese usuario");
            }
        }
        roles.add(nuevo);
    }

    //Desvinculación de un recepcionista: queda inactivo y ya no puede ingresar
    public void desvincularRecepcionista(String cedula){
        for(Rol r: roles){
            if(r instanceof Recepcionista && r.getCedula().equals(cedula)){
                ((Recepcionista) r).setActivo(false);
                return;
            }
        }
        throw new IllegalArgumentException("No existe un recepcionista con esa cédula");
    }

    //Seguridad y autenticación: devuelve la persona si el usuario y la contraseña son correctos
    public Rol autenticar(String usuario, String contrasenia){
        if(usuario == null || contrasenia == null){
            return null;
        }
        for(Rol r: roles){
            if(r.autenticar(usuario, contrasenia)){
                if(r instanceof Recepcionista && !((Recepcionista) r).getActivo()){
                    return null;
                }
                return r;
            }
        }
        return null;
    }

    public Huesped buscarHuesped(String cedula){
        for(Rol r: roles){
            if(r instanceof Huesped && r.getCedula().equals(cedula)){
                return (Huesped) r;
            }
        }
        return null;
    }

    public LinkedList<Huesped> getHuespedes(){
        LinkedList<Huesped> lista= new LinkedList<>();
        for(Rol r: roles){
            if(r instanceof Huesped){
                lista.add((Huesped) r);
            }
        }
        return lista;
    }

    public LinkedList<Recepcionista> getRecepcionistas(){
        LinkedList<Recepcionista> lista= new LinkedList<>();
        for(Rol r: roles){
            if(r instanceof Recepcionista){
                lista.add((Recepcionista) r);
            }
        }
        return lista;
    }

    //Apartamentos
    public void registrarApartamento(Apartamento a){
        if(a == null){
            throw new IllegalArgumentException("El apartamento no puede ser nulo");
        }
        if(vacio(a.getCodigo()) || vacio(a.getNombre())){
            throw new IllegalArgumentException("El código y el nombre del apartamento son obligatorios");
        }
        if(a.getCapacidadMaxima() <= 0){
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor que cero");
        }
        if(a.getNumeroDormitorios() < 0){
            throw new IllegalArgumentException("El número de dormitorios no puede ser negativo");
        }
        if(buscarApartamento(a.getCodigo()) != null){
            throw new IllegalArgumentException("Ya existe un apartamento con ese código");
        }
        apartamentos.add(a);
    }

    public Apartamento buscarApartamento(String codigo){
        for(Apartamento a: apartamentos){
            if(a.getCodigo().equals(codigo)){
                return a;
            }
        }
        return null;
    }

    //Eliminación lógica: el apartamento deja de estar activo, pero se conserva su historia
    public void eliminarApartamento(String codigo){
        Apartamento a= buscarApartamento(codigo);
        if(a == null){
            throw new IllegalArgumentException("No existe un apartamento con ese código");
        }
        for(Reserva r: reservas){
            if(r.estaActiva() && r.getApartamento().getCodigo().equals(codigo)){
                throw new IllegalStateException("No se puede eliminar: el apartamento tiene reservas activas");
            }
        }
        a.setActivo(false);
    }

    //Tarifas por apartamento. Un cambio de tarifa no afecta las reservas ya creadas (valor congelado)
    public void definirTarifa(Apartamento a, TemporadaAlojamiento temporada, int valor){
        if(a == null || temporada == null){
            throw new IllegalArgumentException("El apartamento y la temporada son obligatorios");
        }
        if(valor <= 0){
            throw new IllegalArgumentException("La tarifa debe ser mayor que cero");
        }
        for(Tarifa t: a.getTarifas()){
            if(t.getTemporada() == temporada){
                t.setValorPersonaNoche(valor);
                return;
            }
        }
        a.agregarTarifa(new Tarifa(valor, temporada));
    }

    //Temporadas
    public void registrarTemporada(Temporada t){
        if(t == null){
            throw new IllegalArgumentException("La temporada no puede ser nula");
        }
        if(t.getTipo() == TemporadaAlojamiento.BAJA){
            throw new IllegalArgumentException("La temporada baja es la base: cubre todas las fechas que no pertenezcan a otra temporada");
        }
        if(t.getFechaInicio() == null || t.getFechaFin() == null || t.getFechaFin().isBefore(t.getFechaInicio())){
            throw new IllegalArgumentException("El rango de fechas de la temporada no es válido");
        }
        for(Temporada otra: temporadas){
            if(!t.getFechaInicio().isAfter(otra.getFechaFin()) && !t.getFechaFin().isBefore(otra.getFechaInicio())){
                throw new IllegalArgumentException("La temporada se cruza con otra ya definida");
            }
        }
        temporadas.add(t);
    }

    public void eliminarTemporada(Temporada t){
        temporadas.remove(t);
    }

    //Bloqueos: rangos de fechas en que un apartamento no se puede vender
    public void registrarBloqueo(Bloqueo b){
        if(b == null || b.getApartamento() == null){
            throw new IllegalArgumentException("El bloqueo necesita un apartamento");
        }
        if(b.getFechaInicio() == null || b.getFechaFin() == null || b.getFechaFin().isBefore(b.getFechaInicio())){
            throw new IllegalArgumentException("El rango de fechas del bloqueo no es válido");
        }
        if(vacio(b.getMotivo())){
            throw new IllegalArgumentException("El bloqueo necesita un motivo");
        }
        //El rango del bloqueo incluye la fecha fin, por eso se compara contra el día siguiente
        if(chocaConReserva(b.getApartamento(), b.getFechaInicio(), b.getFechaFin().plusDays(1))){
            throw new IllegalStateException("El apartamento tiene reservas activas en esas fechas");
        }
        bloqueos.add(b);
    }

    public void eliminarBloqueo(Bloqueo b){
        bloqueos.remove(b);
    }

    public void registrarNovedad(Novedad n){
        if(n == null || vacio(n.getDescripcion())){
            throw new IllegalArgumentException("La novedad necesita una descripción");
        }
        novedades.add(n);
    }

    //Política de cancelación: solo afecta las reservas que se creen después
    public void configurarPoliticaCancelacion(int diasDevolucion, int diasRetencion, double porcentajeRetencion){
        if(diasDevolucion < 0 || diasRetencion < 0 || diasRetencion > diasDevolucion){
            throw new IllegalArgumentException("Los días de la política no son válidos (retención no puede superar a devolución)");
        }
        if(porcentajeRetencion < 0 || porcentajeRetencion > 100){
            throw new IllegalArgumentException("El porcentaje de retención debe estar entre 0 y 100");
        }
        this.politicaCancelacion= new PoliticaCancelacion(diasDevolucion, diasRetencion, porcentajeRetencion);
    }

    //Disponibles
    public LinkedList<Apartamento> buscarDisponibles(LocalDate entrada, LocalDate salida, int personas){
        validarFechas(entrada, salida);
        if(personas <= 0){
            throw new IllegalArgumentException("El número de personas debe ser mayor que cero");
        }
        LinkedList<Apartamento> disponibles= new LinkedList<>();
        for(Apartamento a: apartamentos){
            if(a.verificarDisponibilidad() && a.getCapacidadMaxima()>= personas && !hayConflicto(a, entrada, salida)){
                disponibles.add(a);
            }
        }
        return disponibles;
    }

    //La noche de la fecha de salida no se cobra ni se ocupa: del 10 al 12 de marzo son dos noches.
    //true si el apartamento tiene una reserva activa o si un bloqueo choca con las fechas
    public boolean hayConflicto(Apartamento a, LocalDate entrada, LocalDate salida){
        return chocaConReserva(a, entrada, salida) || chocaConBloqueo(a, entrada, salida);
    }

    private boolean chocaConReserva(Apartamento a, LocalDate entrada, LocalDate salida){
        for(Reserva r: reservas){
            if(r.getApartamento().getCodigo().equals(a.getCodigo()) && r.estaActiva()){
                if(entrada.isBefore(r.getFechaSalida()) && r.getFechaEntrada().isBefore(salida)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean chocaConBloqueo(Apartamento a, LocalDate entrada, LocalDate salida){
        for(Bloqueo b: bloqueos){
            if(b.getApartamento().getCodigo().equals(a.getCodigo())){
                if(b.getFechaInicio().isBefore(salida) && !b.getFechaFin().isBefore(entrada)){
                    return true;
                }
            }
        }
        return false;
    }

    //Valor estancia
    //El alojamiento cobra por persona, por noche, y los ocupantes menores de 6 años ocupan cupo pero no pagan.
    //La edad se calcula a la fecha de entrada.
    private int contarQuePagan(LinkedList<Ocupante> ocupantes, LocalDate entrada){
        int pagan= 0;
        for(Ocupante o: ocupantes){
            if(o.paga(entrada)){
                pagan++;
            }
        }
        return pagan;
    }

    private int tarifaDeLaNoche(Apartamento a, TemporadaAlojamiento tipo){
        int tarifa= a.getTarifa(tipo);
        if(tarifa <= 0){
            throw new IllegalStateException("El apartamento " + a.getCodigo() + " no tiene tarifa para la temporada " + tipo);
        }
        return tarifa;
    }

    private void validarCotizacion(Apartamento a, LocalDate entrada, LocalDate salida, LinkedList<Ocupante> ocupantes){
        validarFechas(entrada, salida);
        if(a == null || ocupantes == null){
            throw new IllegalArgumentException("El apartamento y los ocupantes son obligatorios");
        }
    }

    //El valor de la estancia es la suma, noche por noche, de la tarifa de la temporada de esa noche
    //multiplicada por el número de ocupantes de 6 años o más.
    public int cotizar(Apartamento a, LocalDate entrada, LocalDate salida, LinkedList<Ocupante> ocupantes){
        validarCotizacion(a, entrada, salida, ocupantes);
        int pagan= contarQuePagan(ocupantes, entrada);
        int total= 0;
        LocalDate noche= entrada;
        while(noche.isBefore(salida)){
            total+= tarifaDeLaNoche(a, tipoTemporada(noche)) * pagan;
            noche= noche.plusDays(1);
        }
        return total;
    }

    //Cotización: valor total y desglose noche por noche según la temporada (en texto, listo para mostrar)
    public String detalleCotizacion(Apartamento a, LocalDate entrada, LocalDate salida, LinkedList<Ocupante> ocupantes){
        validarCotizacion(a, entrada, salida, ocupantes);
        int pagan= contarQuePagan(ocupantes, entrada);
        String texto= a + "\nDel " + entrada + " al " + salida + "\n";
        texto+= "Ocupantes: " + ocupantes.size() + " | pagan (6 años o más): " + pagan + "\n\n";
        int total= 0;
        LocalDate noche= entrada;
        while(noche.isBefore(salida)){
            TemporadaAlojamiento tipo= tipoTemporada(noche);
            int tarifa= tarifaDeLaNoche(a, tipo);
            int valor= tarifa * pagan;
            texto+= noche + "  " + tipo + "  " + Folio.dinero(tarifa) + " x " + pagan + " = " + Folio.dinero(valor) + "\n";
            total+= valor;
            noche= noche.plusDays(1);
        }
        texto+= "\nTOTAL: " + Folio.dinero(total);
        return texto;
    }

    //Temporada baja (temporada base): cubre todas las fechas que no pertenezcan a otra temporada.
    public TemporadaAlojamiento tipoTemporada(LocalDate fecha){
        for(Temporada t: temporadas){
            if(t.contiene(fecha)){
                return t.getTipo();
            }
        }
        return TemporadaAlojamiento.BAJA;
    }

    //Reservas
    //Reserva portal: el huésped reserva por sí mismo desde la aplicación.
    public Reserva crearReservaPortal(Huesped titular, Apartamento a, LocalDate entrada, LocalDate salida, LinkedList<Ocupante> ocupantes){
        Reserva r= new ReservaPortal(generarCodigoReserva(), entrada, salida, a, titular, ocupantes, copiarPolitica());
        crearReserva(r);
        return r;
    }

    //Reserva directa: la recepción registra una reserva recibida por teléfono, mensajería o de manera presencial.
    public Reserva crearReservaDirecta(Huesped titular, Apartamento a, LocalDate entrada, LocalDate salida, LinkedList<Ocupante> ocupantes){
        Reserva r= new ReservaDirecta(generarCodigoReserva(), entrada, salida, a, titular, ocupantes, copiarPolitica());
        crearReserva(r);
        return r;
    }

    public void crearReserva(Reserva r){
        if(r == null){
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if(r instanceof ReservaExterna){
            throw new IllegalArgumentException("Las reservas externas se registran con registrarReservaExterna");
        }
        validarReserva(r);
        if(hayConflicto(r.getApartamento(), r.getFechaEntrada(), r.getFechaSalida())){
            throw new IllegalStateException("El apartamento no está disponible en esas fechas");
        }
        guardarReserva(r);
    }

    //Reserva externa: una que choca con una vigente se rechaza y se registra como conflicto de canal.
    //Devuelve true si se guardó y false si se rechazó por conflicto. Un mismo código externo no puede crear dos reservas
    public boolean registrarReservaExterna(ReservaExterna r){
        if(r == null){
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if(vacio(r.getCodigoExterno())){
            throw new IllegalArgumentException("La reserva externa necesita el código de la plataforma");
        }
        for(String codigo: codigosExternos){
            if(codigo.equals(r.getCodigoExterno())){
                throw new IllegalStateException("El código externo ya tiene creada una reserva");
            }
        }
        validarReserva(r);
        if(hayConflicto(r.getApartamento(), r.getFechaEntrada(), r.getFechaSalida())){
            conflictoCanales.add(new ConflictoCanal(r.getCodigoExterno(), r.getApartamento().getCodigo(), r.getFechaEntrada(), r.getFechaSalida(), "Choca con una reserva vigente o un bloqueo", LocalDateTime.now()));
            return false;
        }
        guardarReserva(r);
        codigosExternos.add(r.getCodigoExterno());
        return true;
    }

    public String generarCodigoReserva(){
        return "RES-" + String.format("%04d", consecutivoReservas + 1);
    }

    public Reserva buscarReserva(String codigo){
        for(Reserva r: reservas){
            if(r.getCodigo().equals(codigo)){
                return r;
            }
        }
        return null;
    }

    //Folio: pagos y cargos inalterables
    public void registrarPago(Reserva r, int monto, MedioPago medio){
        if(r == null || medio == null){
            throw new IllegalArgumentException("La reserva y el medio de pago son obligatorios");
        }
        if(monto <= 0){
            throw new IllegalArgumentException("El valor del pago debe ser mayor que cero");
        }
        if(!r.estaActiva()){
            throw new IllegalStateException("Solo se registran pagos en reservas activas");
        }
        if(monto > r.getFolio().calcularSaldo()){
            throw new IllegalArgumentException("El pago supera el saldo del folio (" + Folio.dinero(r.getFolio().calcularSaldo()) + ")");
        }
        r.getFolio().registrarPago(new Pago(monto, LocalDate.now(), medio));
        //Portal: la reserva pasa a Confirmada cuando se paga el anticipo del 30 %
        if(r.getEstado() == EstadoReserva.PENDIENTE && r.requiereAnticipo() && r.getFolio().totalPago() >= r.calcularAnticipo()){
            r.confirmar();
        }
    }

    public void registrarCargo(Reserva r, String concepto, int monto, TipoCargo tipo){
        if(r == null || tipo == null){
            throw new IllegalArgumentException("La reserva y el tipo de cargo son obligatorios");
        }
        if(vacio(concepto)){
            throw new IllegalArgumentException("El cargo necesita un concepto");
        }
        if(monto <= 0){
            throw new IllegalArgumentException("El valor del cargo debe ser mayor que cero");
        }
        if(!r.estaActiva()){
            throw new IllegalStateException("Solo se registran cargos en reservas activas");
        }
        r.getFolio().agregarCargo(new Cargo(concepto, monto, tipo));
    }

    //Validaciones de reservas nuevas (sin revisar choques de fechas)
    private void validarReserva(Reserva r){
        validarFechas(r.getFechaEntrada(), r.getFechaSalida());
        if(r.getOcupantes().isEmpty()){
            throw new IllegalArgumentException("Debe haber al menos un ocupante");
        }
        if(r.getOcupantes().size() > r.getApartamento().getCapacidadMaxima()){
            throw new IllegalArgumentException("Los ocupantes superan la capacidad del apartamento");
        }
        if(!r.getApartamento().verificarDisponibilidad()){
            throw new IllegalStateException("El apartamento no está disponible para la venta");
        }
        if(buscarReserva(r.getCodigo()) != null){
            throw new IllegalArgumentException("Ya existe una reserva con ese código");
        }
    }

    private void validarFechas(LocalDate entrada, LocalDate salida){
        if(entrada == null || salida == null){
            throw new IllegalArgumentException("Debes indicar la fecha de entrada y la de salida");
        }
        if(!salida.isAfter(entrada)){
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada (mínimo una noche)");
        }
        if(entrada.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("La entrada no puede ser anterior a hoy");
        }
    }

    //Valor congelado: el valor y la política de cancelación se guardan al crear la reserva; un cambio posterior de tarifas no afecta las reservas ya creadas
    private void guardarReserva(Reserva r){
        int valor= cotizar(r.getApartamento(), r.getFechaEntrada(), r.getFechaSalida(), r.getOcupantes());
        r.setValorEstancia(valor);
        reservas.add(r);
        consecutivoReservas++;
    }

    private PoliticaCancelacion copiarPolitica(){
        return new PoliticaCancelacion(politicaCancelacion.getDiasDevolucion(), politicaCancelacion.getDiasRetencion(), politicaCancelacion.getPorcentajeRetencion());
    }

    // Una reserva Pendiente que lleva más de 24 horas sin confirmarse pasa a Cancelada. Devuelve cuántas canceló
    public int cancelarPendientesVencidas(){
        LocalDateTime limite= LocalDateTime.now().minusHours(24);
        int canceladas= 0;
        for(Reserva r: reservas){
            if(r.getEstado() == EstadoReserva.PENDIENTE && r.getFechaCreacion().isBefore(limite)){
                r.cancelar();
                canceladas++;
            }
        }
        return canceladas;
    }

    public LinkedList<Reserva> historialHuesped(Huesped h){
        LinkedList<Reserva> historial= new LinkedList<>();
        for(Reserva r: reservas){
            if(r.getTitular().getCedula().equals(h.getCedula())){
                historial.add(r);
            }
        }
        return historial;
    }

    //Auxiliar
    private boolean vacio(String texto){
        return texto == null || texto.isBlank();
    }

    //Getters and setters
    public String getNit() {

        return nit;
    }
    public void setNit(String nit) {

        this.nit = nit;
    }
    public String getNombre() {

        return nombre;
    }
    public void setNombre(String nombre) {

        this.nombre = nombre;
    }
    public String getUbicacion() {

        return ubicacion;
    }
    public void setUbicacion(String ubicacion) {

        this.ubicacion = ubicacion;
    }

    public PoliticaCancelacion getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public LinkedList<String> getCodigosExternos() {
        return new LinkedList<>(codigosExternos);
    }

    public void setCodigosExternos(LinkedList<String> codigosExternos) {
        this.codigosExternos = codigosExternos;
    }

    public LinkedList<Apartamento> getApartamentos() {

        return new LinkedList<>(apartamentos);
    }
    public LinkedList<Reserva> getReservas() {

        return new LinkedList<>(reservas);
    }

    public LinkedList<Temporada> getTemporadas() {

        return new LinkedList<>(temporadas);
    }

    public LinkedList<Bloqueo> getBloqueos() {

        return new LinkedList<>(bloqueos);
    }
    public LinkedList<ConflictoCanal> getConflictoCanales(){

        return new LinkedList<>(conflictoCanales);
    }

    public LinkedList<Rol> getRoles() {
        return new LinkedList<>(roles);
    }

    public LinkedList<Novedad> getNovedades() {
        return new LinkedList<>(novedades);
    }
}
