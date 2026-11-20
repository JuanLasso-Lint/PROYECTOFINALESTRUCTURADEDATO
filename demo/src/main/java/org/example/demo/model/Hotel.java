package org.example.demo.model;

import org.example.demo.model.Estructuras.*;
import org.example.demo.model.Exception.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Iterator;


public class Hotel {


    public static final Comparator<Apartamento> CMP_PRECIO = Comparator.comparingDouble(Apartamento::getTarifaPorPersona);
    public static final Comparator<Apartamento> CMP_CAPACIDAD = Comparator.comparingInt(Apartamento::getCapacidadMaxima);
    public static final Comparator<Apartamento> CMP_DORMITORIOS = Comparator.comparingInt(Apartamento::getNumDormitorios);
    public static final Comparator<TareaLimpieza> CMP_PRIORIDAD_LIMPIEZA = Comparator.comparing(TareaLimpieza::isEntradaHoy).reversed().thenComparing(TareaLimpieza::getHoraLlegadaSiguiente).thenComparing(TareaLimpieza::getFechaSolicitud);



    private final ArbolBinarioBusqueda<Apartamento> catalogoApartamentos;
    private final ArbolBinarioBusqueda<Huesped> registroHuespedes;
    private final ArbolBinarioBusqueda<Empleado> empleados;

    private final ArbolBinarioBusqueda<ParClaveValor<String, ArbolBinarioBusqueda<Reserva>>> agendaPorApartamento;



    private final ListaDoble<Temporada> temporadas;
    private final ListaDoble<Reserva> historialGlobalReservas;

    private final ListaCircularSimple<Empleado> turnosRecepcion;


    private final Cola<Reserva> solicitudesExternas;
    private final ColaPrioridad<TareaLimpieza> colaLimpieza;
    private final Grafo<String> mapaEdificio;

    private int conflictosCanalExterno = 0;
    private int preparacionesCompletadas = 0;
    private long segundosTotalesPreparacion = 0;

    public Hotel() {
        this.catalogoApartamentos = new ArbolBinarioBusqueda<>();
        this.registroHuespedes = new ArbolBinarioBusqueda<>();
        this.empleados = new ArbolBinarioBusqueda<>();
        this.turnosRecepcion = new ListaCircularSimple<>();
        this.temporadas = new ListaDoble<>();
        this.agendaPorApartamento = new ArbolBinarioBusqueda<>();
        this.historialGlobalReservas = new ListaDoble<>();
        this.solicitudesExternas = new Cola<>();
        this.colaLimpieza = new ColaPrioridad<>(CMP_PRIORIDAD_LIMPIEZA);
        this.mapaEdificio = new Grafo<>();
    }



    private ArbolBinarioBusqueda<Reserva> obtenerAgenda(String codigoApto) {
        ParClaveValor<String, ArbolBinarioBusqueda<Reserva>> par =
                agendaPorApartamento.buscarElemento(new ParClaveValor<>(codigoApto, null));
        if (par == null) {
            ArbolBinarioBusqueda<Reserva> nueva = new ArbolBinarioBusqueda<>();
            agendaPorApartamento.insertar(new ParClaveValor<>(codigoApto, nueva));
            return nueva;
        }
        return par.getValor();
    }


//al registrar un apartamento se le crea de una vez su carpeta vacía en agendaPorApartamento
    public boolean registrarApartamento(Apartamento apto) {
        if (apto == null) throw new IllegalArgumentException("Apartamento nulo.");
        boolean insertado = catalogoApartamentos.insertar(apto);
        if (insertado) {
            obtenerAgenda(apto.getCodigo());
        }
        return insertado;
    }


    public boolean registrarHuesped(Huesped h) {
        if (h == null) throw new IllegalArgumentException("Huésped nulo.");
        return registroHuespedes.insertar(h);
    }


    //si el empleado tiene cargo "Recepcionista", también entra al anillo de turnosRecepcion
    public boolean registrarEmpleado(Empleado emp) {
        if (emp == null) throw new IllegalArgumentException("Empleado nulo.");
        boolean insertado = empleados.insertar(emp);
        if (insertado && "Recepcionista".equalsIgnoreCase(emp.getCargo())) {
            turnosRecepcion.agregar(emp);
        }
        return insertado;
    }



    public void registrarTemporada(Temporada t) {
        if (t != null) temporadas.agregarAlFinal(t);
    }






    //Como los árboles ordenan los objetos usando compareTo (que solo mira el codigo o el documento), para buscar un apartamento
    // crea un Apartamento vacío poniendo únicamente el codigo buscado.
    public Apartamento buscarApartamento(String codigo) {
        if (codigo == null) return null;
        Apartamento clave = new Apartamento(codigo, "", 1, 1, 1, 1, "", 0, EstadoOperativo.PREPARADO, null);
        return catalogoApartamentos.buscarElemento(clave);
    }

    public Huesped buscarHuesped(String documento) {
        if (documento == null) return null;
        Huesped clave = new Huesped("", "", documento, LocalDate.now(), 0, "");
        return registroHuespedes.buscarElemento(clave);
    }







    public void actualizarTarifaApartamento(String codigo, double nuevaTarifa) {
        Apartamento apto = buscarApartamento(codigo);
        if (apto == null) throw new ReglaNegocioException("Apartamento no encontrado: " + codigo);
        if (nuevaTarifa <= 0) throw new ReglaNegocioException("La tarifa debe ser mayor a cero.");
        apto.setTarifaPorPersona(nuevaTarifa);
    }





//Avanza el puntero de la ListaCircularSimple
// al siguiente recepcionista y devuelve su nombre y código.
    public String rotarRecepcionista() {
        if (turnosRecepcion.estaVacia()) return "Sin recepcionistas";
        Empleado sig = turnosRecepcion.rotar();
        return sig.getNombre() + " (" + sig.getCodigo() + ")";
    }

    public String verTurnoActualRecepcion() {
        if (turnosRecepcion.estaVacia()) return "Sin recepcionistas";
        Empleado act = turnosRecepcion.getActual();
        return act.getNombre() + " (" + act.getCodigo() + ")";
    }






//Revisa al titular y a los acompañantes; si alguno tiene menos de 6 años
 //no paga y no se suma a personasQuePagan.
    public double cotizarEstanciaConMenores(Reserva reserva) {
        Apartamento apto = reserva.getApartamentoAsignado();
        double tarifaBase = apto.getTarifaPorPersona();
        int personasQuePagan = reserva.getTitular().esMenorDeSeisAnios() ? 0 : 1;

        for (Huesped ac : reserva.getAcompanantes()) {
            if (!ac.esMenorDeSeisAnios()) {
                personasQuePagan++;
            }
        }
        return liquidarNochesRecursivo(reserva.getNoches().iterator(), personasQuePagan, tarifaBase);
    }




//Recorre la lista de noches de la reserva una por una.
// En cada llamada mira si esa fecha cae dentro de alguna Temporada

    private double liquidarNochesRecursivo(Iterator<LocalDate> itNoches, int personasQuePagan, double tarifaBase) {
        if (!itNoches.hasNext()) return 0.0;
        LocalDate noche = itNoches.next();
        double mult = obtenerMultiplicadorTemporada(noche);
        double costoNoche = personasQuePagan * tarifaBase * mult;
        return costoNoche + liquidarNochesRecursivo(itNoches, personasQuePagan, tarifaBase);
    }

    private double obtenerMultiplicadorTemporada(LocalDate fecha) {
        for (Temporada t : temporadas) {
            if (t.aplicaA(fecha)) return t.getMultiplicador();
        }
        return 1.0;
    }






//Primero revisa que el apartamento no esté FUERA_DE_SERVICO
    public boolean estaDisponible(Apartamento apto, LocalDate entrada, LocalDate salida) {
        if (apto.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICO) return false;

//entra al árbol de reservas de ese apartamento y usa buscarRecursivo con una condición lambda:
        ArbolBinarioBusqueda<Reserva> agenda = obtenerAgenda(apto.getCodigo());
        //alguna reserva activa uyas fechas se crucen (seSolapaCon), devuelve false
        return !agenda.buscarRecursivo(r ->
                r.getEstado() != EstadoReserva.CANCELADA &&
                        r.getEstado() != EstadoReserva.FINALIZADA &&
                        r.getEstado() != EstadoReserva.NO_SHOW &&
                        r.seSolapaCon(entrada, salida)
        );
    }



    //Verifica que el apartamento funcione, que no se supere capacidadMaxima y que las fechas estén libres.
    // Si la reserva viene por canal EXTERNA, la manda a hacer fila en solicitudesExternas
    public void validarYRegistrarReserva(Reserva reserva) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no puede ser nula.");
        Apartamento apto = reserva.getApartamentoAsignado();

        if (apto.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICO) {
            throw new ReglaNegocioException("El apartamento " + apto.getCodigo() + " está fuera de servicio.");
        }
        if (reserva.getTotalPersonas() > apto.getCapacidadMaxima()) {
            throw new ReglaNegocioException("El grupo (" + reserva.getTotalPersonas() + ") supera la capacidad máxima (" + apto.getCapacidadMaxima() + ").");
        }
        if (!estaDisponible(apto, reserva.getFechaEntrada(), reserva.getFechaSalida())) {
            throw new ReglaNegocioException("El apartamento " + apto.getCodigo() + " ya tiene una reserva solapada en esas fechas.");
        }

        if (reserva.getCanal() == CanalReserva.EXTERNA) {
            solicitudesExternas.encolar(reserva);
        } else {
            obtenerAgenda(apto.getCodigo()).insertar(reserva);
            historialGlobalReservas.agregarAlFinal(reserva);
        }
    }



    //Saca la solicitud más antigua de la cola
    public boolean procesarSiguienteSolicitudExterna() {
        if (solicitudesExternas.estaVacia()) return false;
        Reserva sol = solicitudesExternas.desencolar();

//revisar si las fechas siguen libres confirma
        if (estaDisponible(sol.getApartamentoAsignado(), sol.getFechaEntrada(), sol.getFechaSalida())) {
            sol.setEstado(EstadoReserva.CONFIRMADA);
            obtenerAgenda(sol.getApartamentoAsignado().getCodigo()).insertar(sol);
            historialGlobalReservas.agregarAlFinal(sol);
            return true;

        //si ya se ocuparon, la cancela y suma 1 a conflictosCanalExterno.
        } else {
            sol.setEstado(EstadoReserva.CANCELADA);
            conflictosCanalExterno++;
            historialGlobalReservas.agregarAlFinal(sol);
            return false;
        }
    }



    public void pagarAnticipoPortal(Reserva reserva, double monto, double totalEstancia) {
        if (monto < totalEstancia * 0.30) {
            throw new ReglaNegocioException("El anticipo mínimo debe ser del 30% ($" + String.format("%.0f", totalEstancia * 0.30) + ").");
        }
        reserva.registrarMovimiento("Anticipo confirmado", monto, false);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
    }



    //Recorre el historial y cancela automáticamente las reservas PENDIENTE
    // que lleven más de 24 horas sin pagarse, liberándolas de la agenda.
    public int cancelarReservasVencidas(LocalDateTime corte) {
        int canceladas = 0;
        for (Reserva r : historialGlobalReservas) {
            if (r.getEstado() == EstadoReserva.PENDIENTE &&
                    ChronoUnit.HOURS.between(r.getFechaCreacion(), corte) >= 24) {
                r.setEstado(EstadoReserva.CANCELADA);
                obtenerAgenda(r.getApartamentoAsignado().getCodigo()).eliminar(r);
                canceladas++;
            }
        }
        return canceladas;
    }


    public double calcularTotalPagadoFolio(Reserva reserva) {
        return reserva.calcularTotalPagadoNeto();
    }




    //Calcula cuántos días faltan para el viaje:
    // si faltan 7 días o más devuelve el 100% de lo pagado;
    // si faltan entre 2 y 6 días devuelve el 50%;
    // y si falta menos, 0%
    public double previsualizarReembolso(Reserva reserva, LocalDate fechaCancelacion) {
        long diasAnticipacion = ChronoUnit.DAYS.between(fechaCancelacion, reserva.getFechaEntrada());
        double pagadoNeto = calcularTotalPagadoFolio(reserva);
        double porcentajeReembolso = diasAnticipacion >= 7 ? 1.0 : (diasAnticipacion >= 2 ? 0.5 : 0.0);
        return pagadoNeto * porcentajeReembolso;
    }

    public double cancelarReserva(Reserva reserva, LocalDate fechaCancelacion) {
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ReglaNegocioException("La reserva ya está " + reserva.getEstado() + ".");
        }
        long diasAnticipacion = ChronoUnit.DAYS.between(fechaCancelacion, reserva.getFechaEntrada());
        double pagadoNeto = calcularTotalPagadoFolio(reserva);
        double porcentajeReembolso = diasAnticipacion >= 7 ? 1.0 : (diasAnticipacion >= 2 ? 0.5 : 0.0);
        double reembolso = pagadoNeto * porcentajeReembolso;

        reserva.setEstado(EstadoReserva.CANCELADA);
        obtenerAgenda(reserva.getApartamentoAsignado().getCodigo()).eliminar(reserva);

        if (reembolso > 0) {
            reserva.registrarReembolsoOAnulacion("Reembolso por cancelación (" + (int) (porcentajeReembolso * 100) + "%)", reembolso);
        }
        return reembolso;
    }




    //Exige que la reserva esté CONFIRMADA y el apartamento PREPARADO.
    public void registrarCheckIn(Reserva r) {
        if (r == null) throw new IllegalArgumentException("Reserva nula.");
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("No se puede realizar Check-In: la reserva está en estado " + r.getEstado() + " (debe estar CONFIRMADA).");
        }
        Apartamento apto = r.getApartamentoAsignado();
        if (apto.getEstadoOperativo() != EstadoOperativo.PREPARADO) {
            throw new ReglaNegocioException("El apartamento " + apto.getCodigo() + " no está PREPARADO (estado: " + apto.getEstadoOperativo() + ").");
        }

       //Cambia la reserva a EN_CURSO y el apartamento a OCUPADO.
        r.setEstado(EstadoReserva.EN_CURSO);
        apto.setEstadoOperativo(EstadoOperativo.OCUPADO);
    }



    //Si el huésped sale después de las 12:00 m.,
    // le suma al folio un cargo del 25% por Late Check-Out.
    public void registrarCheckOut(Reserva r, boolean entradaHoy, LocalTime horaSalida) {
        if (r == null) throw new IllegalArgumentException("Reserva nula.");
        if (r.getEstado() != EstadoReserva.EN_CURSO) {
            throw new ReglaNegocioException("No se puede realizar Check-Out: la reserva está en estado " + r.getEstado() + " (debe estar EN_CURSO).");
        }
        if (horaSalida != null && horaSalida.isAfter(LocalTime.of(12, 0))) {
            double recargo = r.getApartamentoAsignado().getTarifaPorPersona() * 0.25;
            r.registrarMovimiento("Recargo Late Check-Out", recargo, true);
        }

        //, marca el apartamento como PENDENTE_DE_PREPARACION
        // y mete una TareaLimpieza
        r.setEstado(EstadoReserva.FINALIZADA);
        Apartamento apto = r.getApartamentoAsignado();
        apto.setEstadoOperativo(EstadoOperativo.PENDENTE_DE_PREPARACION);
        encolarTareaPreparacion(apto.getCodigo(), entradaHoy); //si llega otro huésped ese mismo día para darle prioridad máxima).
    }



    private boolean tieneEntradaHoy(String codigoApto) {
        LocalDate hoy = LocalDate.now();
        for (Reserva r : obtenerAgenda(codigoApto)) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA && r.getFechaEntrada().equals(hoy)) {
                return true;
            }
        }
        return false;
    }





    private void encolarTareaPreparacion(String codigoApto, boolean forzarEntradaHoy) {
        boolean entradaHoy = forzarEntradaHoy || tieneEntradaHoy(codigoApto);
        TareaLimpieza tarea = new TareaLimpieza(codigoApto, entradaHoy);
        if (!colaLimpieza.contiene(tarea)) {
            colaLimpieza.encolar(tarea);
        }
    }


    //ira cuál es la tarea más urgente al frente de colaLimpieza
    public ListaDoble<String> atenderSiguienteLimpieza(String nodoOrigen) {
        if (colaLimpieza.estaVacia()) {
            throw new ReglaNegocioException("No hay apartamentos pendientes en la cola de limpieza.");
        }
        TareaLimpieza tarea = colaLimpieza.verFrente();
//le pide al grafo mapaEdificio la ruta más corta con Dijkstra desde donde está el empleado
        ListaDoble<String> ruta = mapaEdificio.dijkstraRutaMasCorta(nodoOrigen, tarea.getCodigoApartamento());

        colaLimpieza.desencolar(); //saca la tarea de la cola y mide el tiempo
        long segs = Math.max(1, Duration.between(tarea.getFechaSolicitud(), LocalDateTime.now()).getSeconds());
        segundosTotalesPreparacion += segs;
        preparacionesCompletadas++;

        Apartamento apto = buscarApartamento(tarea.getCodigoApartamento());
        if (apto != null && apto.getEstadoOperativo() != EstadoOperativo.FUERA_DE_SERVICO) {
            apto.setEstadoOperativo(EstadoOperativo.PREPARADO); //cambia esl estado del apartamentp a PREPARADo
        }
        return ruta;
    }

    public void cambiarEstadoApartamento(String codigo, EstadoOperativo nuevoEstado, String motivo) {
        Apartamento apto = buscarApartamento(codigo);
        if (apto == null) throw new ReglaNegocioException("Apartamento no encontrado: " + codigo);
        if (nuevoEstado == EstadoOperativo.FUERA_DE_SERVICO && (motivo == null || motivo.trim().isEmpty())) {
            throw new ReglaNegocioException("Debe indicar un motivo para marcar el apartamento fuera de servicio.");
        }
        apto.setEstadoOperativo(nuevoEstado);
        apto.setMotivo(nuevoEstado == EstadoOperativo.FUERA_DE_SERVICO ? motivo : null);

        if (nuevoEstado == EstadoOperativo.PENDENTE_DE_PREPARACION || nuevoEstado == EstadoOperativo.EN_PREPARACION) {
            encolarTareaPreparacion(codigo, false);
        }
    }







    public ListaDoble<String> detectarInalcanzablesSiCierraPasillo(String origen, String nodoA, String nodoB) {
        Double pesoOriginal = mapaEdificio.obtenerPesoArista(nodoA, nodoB);
        if (pesoOriginal == null) {
            throw new ReglaNegocioException("No existe un pasillo directo entre " + nodoA + " y " + nodoB + ".");
        }


        ListaDoble<String> inalcanzables = new ListaDoble<>();
        mapaEdificio.eliminarArista(nodoA, nodoB); //Borra temporalmente una conexión en el grafo


        //va desde la recepcion para ver a que apartamentos si se puede llegar
        try {
            Conjunto<String> alcanzables = mapaEdificio.bfsAlcanzables(origen);
            for (String v : mapaEdificio) {
                if (!alcanzables.contiene(v)) {
                    inalcanzables.agregarAlFinal(v);
                }
            }
        } finally {
            mapaEdificio.agregarArista(nodoA, nodoB, pesoOriginal); // Y guada los quee quedaron aislados
        }
        return inalcanzables;
    }



    //recorrer los apartamentos alcanzables y los va metiendo en un árbol temporal ordenado por número de bloque
    public String agruparApartamentosPorBloqueBFS(String nodoOrigen) {


        //Guarda en un Conjunto sin duplicados
        // todos los nodos conectados a los que se puede llegar.
        Conjunto<String> alcanzables = mapaEdificio.bfsAlcanzables(nodoOrigen);
        //e cada nodo almacenará un par clave-valor: la clave es el número de bloque (Integer)
        // y el valor es una lista doble con los apartamentos de ese bloque.
        ArbolBinarioBusqueda<ParClaveValor<Integer, ListaDoble<String>>> gruposPorBloque = new ArbolBinarioBusqueda<>();

        for (String nodo : alcanzables) { //Recorre uno por uno cada nodo obtenido
            Apartamento apto = buscarApartamento(nodo); //mira ese nodo corresponde a un apartamento registrado.
            if (apto != null) { //procesar únicamente nodos que sean apartamentos
                int bloque = apto.getBloque();



                ParClaveValor<Integer, ListaDoble<String>> par = gruposPorBloque.buscarElemento(new ParClaveValor<>(bloque, null));


                if (par == null) { //Entra aquí si es el primer apartamento encontrado de ese bloque.
                    ListaDoble<String> lista = new ListaDoble<>(); //Crea una nueva lista doblemente enlazada vacía para agrupar los apartamentos de este bloque.
                    lista.agregarAlFinal(apto.getCodigo() + " (" + apto.getNombre() + ")"); //lo guarda al final de la nueva lista.

                    //nserta en el árbol un nuevo nodo con el número de bloque como clave y la lista recién creada como valor.
                    gruposPorBloque.insertar(new ParClaveValor<>(bloque, lista));
                } else { //ncontró un nodo previo con ese mismo número de bloque.
                    par.getValor().agregarAlFinal(apto.getCodigo() + " (" + apto.getNombre() + ")");
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        //usando su iterador inorden (el que revisaste al inicio), por lo que entrega cada entry ordenado ascendentemente por número de bloque
        for (ParClaveValor<Integer, ListaDoble<String>> entry : gruposPorBloque) {
            sb.append("Bloque ").append(entry.getClave()).append(": ").append(entry.getValor()).append(" ");
        }

        //si no se encontró ningún apartamento alcanzable devuelve el mensaje por defecto, y en caso contrario retorna 0todo el texto acumulado.
        return sb.length() == 0 ? "Sin apartamentos agrupados." : sb.toString();
    }








//Usa BFS para recorrer los apartamentos alcanzables
// y los va metiendo en un árbol temporal ordenado por número de bloque (

    public ListaDoble<Apartamento> buscarDisponiblesOrdenados(LocalDate entrada, LocalDate salida, int personas, String criterio) {
        ListaDoble<Apartamento> resultado = new ListaDoble<>();
        for (Apartamento a : catalogoApartamentos) {
            if (a.getCapacidadMaxima() >= personas && estaDisponible(a, entrada, salida)) {
                resultado.agregarAlFinal(a);
            }
        }
        if ("PRECIO".equalsIgnoreCase(criterio)) {
            resultado.ordenar(CMP_PRECIO);
        } else if ("CAPACIDAD".equalsIgnoreCase(criterio)) {
            resultado.ordenar(CMP_CAPACIDAD);
        } else {
            resultado.ordenar(CMP_DORMITORIOS);
        }
        return resultado;
    }





    public ListaDoble<Apartamento> asignarGrupoGrandeBacktracking(LocalDate entrada, LocalDate salida, int personas) {


        //lista vacía para guardar
        // los apartamentos que pueden usarse en esas fechas.
        ListaDoble<Apartamento> candidatos = new ListaDoble<>();
        for (Apartamento a : catalogoApartamentos) {
            if (estaDisponible(a, entrada, salida)) candidatos.agregarAlFinal(a);
        //i el apartamento no tiene reservas cruzadas en esas fechas, lo agrega a la lista
        }
        candidatos.ordenar(CMP_CAPACIDAD.reversed());//Ordena los candidatos de mayor a menor capacidad


        ListaDoble<Apartamento> mejor = new ListaDoble<>();//ira fuardando la mejor combinacion

        //Inicia la busqueda
        backtrack(candidatos, 0, personas, new ListaDoble<>(), mejor);

        //Al terminar todas las combinaciones posibles, devuelve la mejor lista encontrada
        return mejor;
    }



    private void backtrack(ListaDoble<Apartamento> cands, int idx, int faltantes,
                           ListaDoble<Apartamento> actual, ListaDoble<Apartamento> mejor) {


        if (faltantes <= 0) { //Evalua si las condiciones de la lista ya cibren o superan  el total de perona

            //mira sia la solucion encontrada es valida o si usa menos apartamentos
            if (mejor.estaVacia() || actual.getTam() < mejor.getTam()) {


                //Vacía el contenido anterior de mejor eliminando su primer elemento repetidamente.
                while (!mejor.estaVacia()) mejor.eliminar(mejor.obtener(0));
               //opia uno a uno los apartamentos de actual dentro de mejor
                for (Apartamento a : actual) mejor.agregarAlFinal(a);
            }
            return;
        }

        //Detiene la exploración de esta rama si ocurre cualquiera de dos cosas
        //Ya no quedan más apartamentos por revisar y aún faltan personas por ubicar.
        //ya existe una solución guardada en mejor y la ruta actual no es mejor que la guardada
        if (idx >= cands.getTam() || (!mejor.estaVacia() && actual.getTam() >= mejor.getTam())) return;

        Apartamento elegido = cands.obtener(idx);
        actual.agregarAlFinal(elegido);

        //vanza recursivamente al siguiente apartamento ,
        // restando la capacidad máxima del apartamento elegido a las personas
        backtrack(cands, idx + 1, faltantes - elegido.getCapacidadMaxima(), actual, mejor);
       //saca elegido de actual para dejar la lista limpia antes de probar el otro camino.
        actual.eliminar(elegido);


        //Avanza al siguiente apartamento (idx + 1) sin haber usado elegido, manteniendo intacta la cantidad de personas faltantes.
        backtrack(cands, idx + 1, faltantes, actual, mejor);
    }







//Recorre el catálogo de apartamentos y el historial de reservas contando
// estados, canales,  conflictos, tiempos promedio de limpieza
// e ingresos netos recaudados.


    public String generarReporteEstadisticas() {
        int totalAptos = catalogoApartamentos.getTam();
        int preparados = 0, ocupados = 0, limpieza = 0, fuera = 0;
        for (Apartamento a : catalogoApartamentos) {
            switch (a.getEstadoOperativo()) {
                case PREPARADO -> preparados++;
                case OCUPADO -> ocupados++;
                case PENDENTE_DE_PREPARACION, EN_PREPARACION -> limpieza++;
                case FUERA_DE_SERVICO -> fuera++;
            }
        }

        double ingresosNetos = 0.0;
        int confirmadas = 0, enCurso = 0, finalizadas = 0, canceladas = 0, noShow = 0;
        int resPortal = 0, resDirecta = 0, resExterna = 0;
        for (Reserva r : historialGlobalReservas) {
            ingresosNetos += r.calcularTotalPagadoNeto();
            switch (r.getEstado()) {
                case CONFIRMADA -> confirmadas++;
                case EN_CURSO -> enCurso++;
                case FINALIZADA -> finalizadas++;
                case CANCELADA -> canceladas++;
                case NO_SHOW -> noShow++;
                default -> {}
            }
            switch (r.getCanal()) {
                case PORTAL -> resPortal++;
                case DIRECTA -> resDirecta++;
                case EXTERNA -> resExterna++;
            }
        }

        double promedioPrepSeg = preparacionesCompletadas == 0 ? 0.0 : (double) segundosTotalesPreparacion / preparacionesCompletadas;

        return "====================================================== "
                + "REPORTE OPERATIVO Y FINANCIERO - APARTA-SUITES UQ "
                + "====================================================== "
                + "1. INVENTARIO DE APARTAMENTOS (Total: " + totalAptos + ") "
                + "   • Preparados: " + preparados + ""
                + "   • Ocupados: " + ocupados + ""
                + "   • En Limpieza / Preparación: " + limpieza + ""
                + "   • Fuera de Servicio: " + fuera + " "
                + "2. ESTADO DE RESERVAS "
                + "   • Confirmadas: " + confirmadas + " | En Curso: " + enCurso + " | Finalizadas: " + finalizadas
                + " | Canceladas: " + canceladas + " | No-Show: " + noShow + " "
                + "3. RESERVAS POR CANAL Y CONFLICTOS "
                + "   • Portal: " + resPortal + " | Directa: " + resDirecta + " | Externa: " + resExterna + " "
                + "   • Conflictos rechazados en canal externo: " + conflictosCanalExterno + " "
                + "4. RENDIMIENTO DE PREPARACIÓN "
                + "   • Preparaciones atendidas: " + preparacionesCompletadas + " | Tiempo promedio: " + String.format("%.1f", promedioPrepSeg) + " seg "
                + "5. INGRESOS NETOS RECAUDADOS (Pagos - Anulaciones/Reembolsos) "
                + "   • Total Neto en Caja: $" + String.format("%,.0f", ingresosNetos) + " "
                + "====================================================== ";
    }














    public void cargarEscenarioDesdeArchivo(String ruta) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                String[] p = linea.split(";");
                switch (p[0].toUpperCase()) {
                    case "APTO" -> registrarApartamento(new Apartamento(
                            p[1], p[2], Integer.parseInt(p[6]), Integer.parseInt(p[3]),
                            Integer.parseInt(p[5]), Integer.parseInt(p[4]), p[7],
                            Double.parseDouble(p[8]), EstadoOperativo.PREPARADO, null));
                    case "PASILLO" -> mapaEdificio.agregarArista(p[1], p[2], Double.parseDouble(p[3]));
                    case "HUESPED" -> registrarHuesped(new Huesped(
                            p[1], p[2], p[3], LocalDate.parse(p[4]), Integer.parseInt(p[5]), p[6]));
                    case "EMPLEADO" -> registrarEmpleado(new Empleado(p[1], p[2], p[3]));
                }
            }
        }
    }





    public ArbolBinarioBusqueda<Apartamento> getCatalogoApartamentos() { return catalogoApartamentos; }
    public ArbolBinarioBusqueda<Huesped> getRegistroHuespedes() { return registroHuespedes; }
    public ArbolBinarioBusqueda<Empleado> getEmpleados() { return empleados; }
    public ListaCircularSimple<Empleado> getTurnosRecepcion() { return turnosRecepcion; }
    public ListaDoble<Temporada> getTemporadas() { return temporadas; }
    public ArbolBinarioBusqueda<ParClaveValor<String, ArbolBinarioBusqueda<Reserva>>> getAgendaPorApartamento() { return agendaPorApartamento; }
    public ListaDoble<Reserva> getHistorialGlobalReservas() { return historialGlobalReservas; }
    public Cola<Reserva> getSolicitudesExternas() { return solicitudesExternas; }
    public ColaPrioridad<TareaLimpieza> getColaLimpieza() { return colaLimpieza; }
    public Grafo<String> getMapaEdificio() { return mapaEdificio; }
}