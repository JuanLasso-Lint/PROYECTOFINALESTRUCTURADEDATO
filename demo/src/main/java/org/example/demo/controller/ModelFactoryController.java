package org.example.demo.controller;



import org.example.demo.model.*;
import org.example.demo.model.Estructuras.ListaDoble;
import org.example.demo.model.Estructuras.Pila;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ModelFactoryController {

    private static ModelFactoryController instance;
    private Hotel hotel;
    private final Pila<String> historialNavegacion;
    private String vistaActual;

    private ModelFactoryController() {
        this.hotel = new Hotel();
        this.historialNavegacion = new Pila<>();
        this.vistaActual = "inicio-view.fxml";
        inicializarDatosPorDefecto();
    }



    public static ModelFactoryController getInstance() {
        if (instance == null) {
            instance = new ModelFactoryController();
        }
        return instance;
    }

    private void inicializarDatosPorDefecto() {
        hotel.registrarTemporada(new Temporada("Temporada Baja", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 5, 31), 0.9));
        hotel.registrarTemporada(new Temporada("Temporada Media", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 11, 30), 1.0));
        hotel.registrarTemporada(new Temporada("Temporada Alta", LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 14), 1.35));

        Apartamento a101 = new Apartamento("101", "Suite Jardin", 1, 1, 2, 4, "Terraza privada", 120000, EstadoOperativo.PREPARADO, null);
        Apartamento a102 = new Apartamento("102", "Estudio Ejecutivo", 1, 1, 1, 2, "Vista calle", 95000, EstadoOperativo.PREPARADO, null);
        Apartamento a201 = new Apartamento("201", "Penthouse Familiar", 1, 2, 3, 6, "Balcon panoramico", 150000, EstadoOperativo.PREPARADO, null);
        Apartamento a202 = new Apartamento("202", "Duplex Confort", 2, 2, 1, 3, "Cocina equipada", 110000, EstadoOperativo.PREPARADO, null);
        hotel.registrarApartamento(a101);
        hotel.registrarApartamento(a102);
        hotel.registrarApartamento(a201);
        hotel.registrarApartamento(a202);

        Huesped h1 = new Huesped("Carlos", "Mendez", "115", LocalDate.of(1992, 5, 14), 300123456, "carlos@mail.com", "fotos/carlos.png");
        Huesped h2 = new Huesped("Laura", "Gomez", "220", LocalDate.of(1988, 11, 20), 310987654, "laura@mail.com", "fotos/laura.png");
        Huesped h3 = new Huesped("Mateo", "Rios", "330", LocalDate.of(2022, 3, 10), 320555444, "mateo@mail.com", null);
        hotel.registrarHuesped(h1);
        hotel.registrarHuesped(h2);
        hotel.registrarHuesped(h3);

        hotel.registrarEmpleado(new Empleado("Ana Torres", "E01", "Recepcionista"));
        hotel.registrarEmpleado(new Empleado("Luis Perez", "E02", "Recepcionista"));
        hotel.registrarEmpleado(new Empleado("Marta Diaz", "E03", "Limpieza"));

        hotel.getMapaEdificio().agregarVertice("Recepcion");
        hotel.getMapaEdificio().agregarVertice("CuartoAseo");
        hotel.getMapaEdificio().agregarVertice("EscalerasP1");
        hotel.getMapaEdificio().agregarVertice("101");
        hotel.getMapaEdificio().agregarVertice("102");
        hotel.getMapaEdificio().agregarVertice("EscalerasP2");
        hotel.getMapaEdificio().agregarVertice("201");
        hotel.getMapaEdificio().agregarVertice("202");

        hotel.getMapaEdificio().agregarArista("Recepcion", "EscalerasP1", 5);
        hotel.getMapaEdificio().agregarArista("Recepcion", "CuartoAseo", 8);
        hotel.getMapaEdificio().agregarArista("EscalerasP1", "101", 4);
        hotel.getMapaEdificio().agregarArista("101", "102", 3);
        hotel.getMapaEdificio().agregarArista("EscalerasP1", "EscalerasP2", 10);
        hotel.getMapaEdificio().agregarArista("EscalerasP2", "201", 4);
        hotel.getMapaEdificio().agregarArista("201", "202", 3);
        hotel.getMapaEdificio().agregarArista("CuartoAseo", "EscalerasP1", 6);

        Reserva r1 = new Reserva(h1, a101, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), CanalReserva.DIRECTA);
        r1.registrarMovimiento("Alojamiento inicial", 360000, true);
        r1.registrarMovimiento("Abono inicial", 180000, false);
        hotel.validarYRegistrarReserva(r1);
    }

    public void cargarEscenarioInicial() throws IOException {
        this.hotel = new Hotel();
        hotel.registrarTemporada(new Temporada("Temporada Baja", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 5, 31), 0.9));
        hotel.registrarTemporada(new Temporada("Temporada Media", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 11, 30), 1.0));
        hotel.registrarTemporada(new Temporada("Temporada Alta", LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 14), 1.35));

        InputStream is = getClass().getResourceAsStream("/org/example/pro_final_ed/escenario_prueba.txt");
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty() || linea.startsWith("#")) continue;
                    String[] p = linea.split(";");
                    switch (p[0].toUpperCase()) {

                        case "APTO" -> hotel.registrarApartamento(new Apartamento(
                                p[1], p[2], Integer.parseInt(p[6]), Integer.parseInt(p[3]),
                                Integer.parseInt(p[5]), Integer.parseInt(p[4]), p[7],
                                Double.parseDouble(p[8]), EstadoOperativo.PREPARADO, null));
                        case "PASILLO" -> hotel.getMapaEdificio().agregarArista(p[1], p[2], Double.parseDouble(p[3]));
                        case "HUESPED" -> hotel.registrarHuesped(new Huesped(
                                p[1], p[2], p[3], LocalDate.parse(p[4]), Integer.parseInt(p[5]), p[6]));
                        case "EMPLEADO" -> hotel.registrarEmpleado(new Empleado(p[1], p[2], p[3]));



                    }
                }
            }
        } else {
            hotel.cargarEscenarioDesdeArchivo("demo/escenario_de_prueba.txt");
        }
    }

    public void registrarApartamento(String codigo, String nombre, int piso, int capacidad,
                                     int dormitorios, int bloque, String caracteristica, double tarifa) {
        Apartamento nuevo = new Apartamento(codigo, nombre, bloque, piso, dormitorios,
                capacidad, caracteristica, tarifa, EstadoOperativo.PREPARADO, null);
        if (!hotel.registrarApartamento(nuevo)) {
            throw new IllegalArgumentException("Ya existe un apartamento con el código " + codigo);
        }

        hotel.getMapaEdificio().agregarVertice(codigo);
        String nodoEscalera = "EscalerasP" + piso;
        boolean esPisoNuevo = !hotel.getMapaEdificio().contieneVertice(nodoEscalera);

        hotel.getMapaEdificio().agregarVertice(nodoEscalera);
        if (esPisoNuevo && piso > 2) {
            String escaleraAnterior = "EscalerasP" + (piso - 1);
            hotel.getMapaEdificio().agregarVertice(escaleraAnterior);
            hotel.getMapaEdificio().agregarArista(escaleraAnterior, nodoEscalera, 10.0);
        }
        hotel.getMapaEdificio().agregarArista(nodoEscalera, codigo, 4.0);
    }

    public void registrarHuesped(String nombre, String apellido, String documento,
                                 LocalDate fechaNacimiento, int telefono, String correo, String rutaFoto) {
        Huesped nuevo = new Huesped(nombre, apellido, documento, fechaNacimiento, telefono, correo, rutaFoto);
        if (!hotel.registrarHuesped(nuevo)) {
            throw new IllegalArgumentException("Ya existe un huésped con documento " + documento);
        }
    }

    public void registrarEmpleado(String nombre, String codigo, String cargo) {
        Empleado nuevo = new Empleado(nombre, codigo, cargo);
        if (!hotel.registrarEmpleado(nuevo)) {
            throw new IllegalArgumentException("Ya existe un trabajador con código " + codigo);
        }
    }

    public void registrarTemporada(String nombre, LocalDate inicio, LocalDate fin, double multiplicador) {
        hotel.registrarTemporada(new Temporada(nombre, inicio, fin, multiplicador));
    }

    public void actualizarTarifaApartamento(String codigo, double nuevaTarifa) {
        hotel.actualizarTarifaApartamento(codigo, nuevaTarifa);
    }

    public void registrarNavegacion(String nuevaVistaFxml) {
        if (vistaActual != null && !vistaActual.equals(nuevaVistaFxml)) {
            historialNavegacion.apilar(vistaActual);
        }
        vistaActual = nuevaVistaFxml;
    }

    public String regresarVistaAnterior() {
        if (historialNavegacion.estaVacia()) return null;
        vistaActual = historialNavegacion.desapilar();
        return vistaActual;
    }

    public boolean puedeRegresar() { return !historialNavegacion.estaVacia(); }
    public Hotel getHotel() { return hotel; }

    public ListaDoble<Apartamento> buscarDisponibles(LocalDate entrada, LocalDate salida, int personas, String criterio) {
        return hotel.buscarDisponiblesOrdenados(entrada, salida, personas, criterio);
    }

    public ListaDoble<Apartamento> asignarGrupoGrande(LocalDate entrada, LocalDate salida, int personas) {
        return hotel.asignarGrupoGrandeBacktracking(entrada, salida, personas);
    }

    public double cotizar(Reserva reserva) { return hotel.cotizarEstanciaConMenores(reserva); }
    public void crearReserva(Reserva reserva) { hotel.validarYRegistrarReserva(reserva); }
    public void pagarAnticipoPortal(Reserva reserva, double monto, double total) { hotel.pagarAnticipoPortal(reserva, monto, total); }
    public double previsualizarReembolso(Reserva reserva, LocalDate fecha) { return hotel.previsualizarReembolso(reserva, fecha); }
    public double cancelarReserva(Reserva reserva, LocalDate fechaCancelacion) { return hotel.cancelarReserva(reserva, fechaCancelacion); }
    public boolean procesarExterna() { return hotel.procesarSiguienteSolicitudExterna(); }
    public int cancelarVencidas() { return hotel.cancelarReservasVencidas(LocalDateTime.now().plusHours(25)); }
    public void checkIn(Reserva r) { hotel.registrarCheckIn(r); }
    public void checkOut(Reserva r, boolean entradaHoy, LocalTime hora) { hotel.registrarCheckOut(r, entradaHoy, hora); }
    public ListaDoble<String> atenderLimpieza(String origen) { return hotel.atenderSiguienteLimpieza(origen); }
    public void cambiarEstadoApartamento(String codigo, EstadoOperativo estado, String motivo) { hotel.cambiarEstadoApartamento(codigo, estado, motivo); }
    public String rotarTurno() { return hotel.rotarRecepcionista(); }
    public String turnoActual() { return hotel.verTurnoActualRecepcion(); }
    public ListaDoble<String> simularCierrePasillo(String origen, String a, String b) { return hotel.detectarInalcanzablesSiCierraPasillo(origen, a, b); }
    public String agruparPorBloque(String origen) { return hotel.agruparApartamentosPorBloqueBFS(origen); }
    public String obtenerEstadisticas() { return hotel.generarReporteEstadisticas(); }
}
