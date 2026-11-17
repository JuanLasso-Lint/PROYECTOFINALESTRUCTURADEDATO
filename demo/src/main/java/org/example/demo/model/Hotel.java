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


    private final ArbolBinarioBusqueda<Apartamento> catalogoApartamentos;
    private final ArbolBinarioBusqueda<Huesped> registroHuespedes;
    private final ArbolBinarioBusqueda<Empleado> empleados;
    private final ListaCircularSimple<Empleado> turnosRecepcion;
    private final ListaDoble<Temporada> temporadas;

    private final ArbolBinarioBusqueda<ParClaveValor<String, ArbolBinarioBusqueda<Reserva>>> agendaPorApartamento;
    private final ListaDoble<Reserva> historialGlobalReservas;
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

    public void actualizarTarifaApartamento(String codigo, double nuevaTarifa) {
        Apartamento apto = buscarApartamento(codigo);
        if (apto == null) throw new ReglaNegocioException("Apartamento no encontrado: " + codigo);
        if (nuevaTarifa <= 0) throw new ReglaNegocioException("La tarifa debe ser mayor a cero.");
        apto.setTarifaPorPersona(nuevaTarifa);
    }

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

    public boolean estaDisponible(Apartamento apto, LocalDate entrada, LocalDate salida) {
        if (apto.getEstadoOperativo() == EstadoOperativo.FUERA_DE_SERVICO) return false;
        ArbolBinarioBusqueda<Reserva> agenda = obtenerAgenda(apto.getCodigo());
        return !agenda.buscarRecursivo(r ->
                r.getEstado() != EstadoReserva.CANCELADA &&
                        r.getEstado() != EstadoReserva.FINALIZADA &&
                        r.getEstado() != EstadoReserva.NO_SHOW &&
                        r.seSolapaCon(entrada, salida)
        );
    }

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

    public boolean procesarSiguienteSolicitudExterna() {
        if (solicitudesExternas.estaVacia()) return false;
        Reserva sol = solicitudesExternas.desencolar();
        if (estaDisponible(sol.getApartamentoAsignado(), sol.getFechaEntrada(), sol.getFechaSalida())) {
            sol.setEstado(EstadoReserva.CONFIRMADA);
            obtenerAgenda(sol.getApartamentoAsignado().getCodigo()).insertar(sol);
            historialGlobalReservas.agregarAlFinal(sol);
            return true;
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

    public void registrarCheckIn(Reserva r) {
        if (r == null) throw new IllegalArgumentException("Reserva nula.");
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("No se puede realizar Check-In: la reserva está en estado " + r.getEstado() + " (debe estar CONFIRMADA).");
        }
        Apartamento apto = r.getApartamentoAsignado();
        if (apto.getEstadoOperativo() != EstadoOperativo.PREPARADO) {
            throw new ReglaNegocioException("El apartamento " + apto.getCodigo() + " no está PREPARADO (estado: " + apto.getEstadoOperativo() + ").");
        }
        r.setEstado(EstadoReserva.EN_CURSO);
        apto.setEstadoOperativo(EstadoOperativo.OCUPADO);
    }

    public void registrarCheckOut(Reserva r, boolean entradaHoy, LocalTime horaSalida) {
        if (r == null) throw new IllegalArgumentException("Reserva nula.");
        if (r.getEstado() != EstadoReserva.EN_CURSO) {
            throw new ReglaNegocioException("No se puede realizar Check-Out: la reserva está en estado " + r.getEstado() + " (debe estar EN_CURSO).");
        }
        if (horaSalida != null && horaSalida.isAfter(LocalTime.of(12, 0))) {
            double recargo = r.getApartamentoAsignado().getTarifaPorPersona() * 0.25;
            r.registrarMovimiento("Recargo Late Check-Out", recargo, true);
        }
        r.setEstado(EstadoReserva.FINALIZADA);
        Apartamento apto = r.getApartamentoAsignado();
        apto.setEstadoOperativo(EstadoOperativo.PENDENTE_DE_PREPARACION);
        encolarTareaPreparacion(apto.getCodigo(), entradaHoy);
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

    public ListaDoble<String> atenderSiguienteLimpieza(String nodoOrigen) {
        if (colaLimpieza.estaVacia()) {
            throw new ReglaNegocioException("No hay apartamentos pendientes en la cola de limpieza.");
        }
        TareaLimpieza tarea = colaLimpieza.verFrente();
        ListaDoble<String> ruta = mapaEdificio.dijkstraRutaMasCorta(nodoOrigen, tarea.getCodigoApartamento());

        colaLimpieza.desencolar();
        long segs = Math.max(1, Duration.between(tarea.getFechaSolicitud(), LocalDateTime.now()).getSeconds());
        segundosTotalesPreparacion += segs;
        preparacionesCompletadas++;

        Apartamento apto = buscarApartamento(tarea.getCodigoApartamento());
        if (apto != null && apto.getEstadoOperativo() != EstadoOperativo.FUERA_DE_SERVICO) {
            apto.setEstadoOperativo(EstadoOperativo.PREPARADO);
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
        mapaEdificio.eliminarArista(nodoA, nodoB);
        try {
            Conjunto<String> alcanzables = mapaEdificio.bfsAlcanzables(origen);
            for (String v : mapaEdificio) {
                if (!alcanzables.contiene(v)) {
                    inalcanzables.agregarAlFinal(v);
                }
            }
        } finally {
            mapaEdificio.agregarArista(nodoA, nodoB, pesoOriginal);
        }
        return inalcanzables;
    }

    public String agruparApartamentosPorBloqueBFS(String nodoOrigen) {
        Conjunto<String> alcanzables = mapaEdificio.bfsAlcanzables(nodoOrigen);
        ArbolBinarioBusqueda<ParClaveValor<Integer, ListaDoble<String>>> gruposPorBloque = new ArbolBinarioBusqueda<>();

        for (String nodo : alcanzables) {
            Apartamento apto = buscarApartamento(nodo);
            if (apto != null) {
                int bloque = apto.getBloque();
                ParClaveValor<Integer, ListaDoble<String>> par = gruposPorBloque.buscarElemento(new ParClaveValor<>(bloque, null));
                if (par == null) {
                    ListaDoble<String> lista = new ListaDoble<>();
                    lista.agregarAlFinal(apto.getCodigo() + " (" + apto.getNombre() + ")");
                    gruposPorBloque.insertar(new ParClaveValor<>(bloque, lista));
                } else {
                    par.getValor().agregarAlFinal(apto.getCodigo() + " (" + apto.getNombre() + ")");
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (ParClaveValor<Integer, ListaDoble<String>> entry : gruposPorBloque) {
            sb.append("Bloque ").append(entry.getClave()).append(": ").append(entry.getValor()).append(" ");
        }
        return sb.length() == 0 ? "Sin apartamentos agrupados." : sb.toString();
    }

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
        ListaDoble<Apartamento> candidatos = new ListaDoble<>();
        for (Apartamento a : catalogoApartamentos) {
            if (estaDisponible(a, entrada, salida)) candidatos.agregarAlFinal(a);
        }
        candidatos.ordenar(CMP_CAPACIDAD.reversed());

        ListaDoble<Apartamento> mejor = new ListaDoble<>();
        backtrack(candidatos, 0, personas, new ListaDoble<>(), mejor);
        return mejor;
    }

    private void backtrack(ListaDoble<Apartamento> cands, int idx, int faltantes,
                           ListaDoble<Apartamento> actual, ListaDoble<Apartamento> mejor) {
        if (faltantes <= 0) {
            if (mejor.estaVacia() || actual.getTam() < mejor.getTam()) {
                while (!mejor.estaVacia()) mejor.eliminar(mejor.obtener(0));
                for (Apartamento a : actual) mejor.agregarAlFinal(a);
            }
            return;
        }
        if (idx >= cands.getTam() || (!mejor.estaVacia() && actual.getTam() >= mejor.getTam())) return;

        Apartamento elegido = cands.obtener(idx);
        actual.agregarAlFinal(elegido);
        backtrack(cands, idx + 1, faltantes - elegido.getCapacidadMaxima(), actual, mejor);
        actual.eliminar(elegido);

        backtrack(cands, idx + 1, faltantes, actual, mejor);
    }

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




    public static final Comparator<Apartamento> CMP_PRECIO = Comparator.comparingDouble(Apartamento::getTarifaPorPersona);
    public static final Comparator<Apartamento> CMP_CAPACIDAD = Comparator.comparingInt(Apartamento::getCapacidadMaxima);
    public static final Comparator<Apartamento> CMP_DORMITORIOS = Comparator.comparingInt(Apartamento::getNumDormitorios);
    public static final Comparator<TareaLimpieza> CMP_PRIORIDAD_LIMPIEZA = Comparator.comparing(TareaLimpieza::isEntradaHoy).reversed().thenComparing(TareaLimpieza::getHoraLlegadaSiguiente).thenComparing(TareaLimpieza::getFechaSolicitud);


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