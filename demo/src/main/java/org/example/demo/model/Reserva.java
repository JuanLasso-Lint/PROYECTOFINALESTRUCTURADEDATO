package org.example.pro_final_ed.model;

import org.example.pro_final_ed.model.Estructuras.ListaDoble;
import org.example.pro_final_ed.model.Estructuras.Pila;
import org.example.pro_final_ed.model.Exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reserva implements Comparable<Reserva> {
    private static int contadorIds = 1;

    private final int id;
    private final Huesped titular;
    private final Apartamento apartamentoAsignado;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final CanalReserva canal;
    private final LocalDateTime fechaCreacion;
    private EstadoReserva estado;

    private final ListaDoble<Huesped> acompanantes;
    private final ListaDoble<LocalDate> noches;
    private final ListaDoble<MovimientoFolio> folio;
    private final Pila<MovimientoFolio> historialFolio;

    public Reserva(Huesped titular, Apartamento apartamentoAsignado, LocalDate fechaEntrada,
                   LocalDate fechaSalida, CanalReserva canal) {
        if (titular == null) {
            throw new IllegalArgumentException("El huésped titular es obligatorio.");
        }
        if (apartamentoAsignado == null) {
            throw new IllegalArgumentException("El apartamento asignado es obligatorio.");
        }
        if (fechaEntrada == null || fechaSalida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida no pueden ser nulas.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new ReglaNegocioException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }

        this.id = contadorIds++;
        this.titular = titular;
        this.apartamentoAsignado = apartamentoAsignado;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.canal = canal != null ? canal : CanalReserva.DIRECTA;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = (this.canal == CanalReserva.DIRECTA)
                ? EstadoReserva.CONFIRMADA
                : EstadoReserva.PENDIENTE;

        this.acompanantes = new ListaDoble<>();
        this.noches = new ListaDoble<>();
        this.folio = new ListaDoble<>();
        this.historialFolio = new Pila<>();

        LocalDate actual = fechaEntrada;
        while (actual.isBefore(fechaSalida)) {
            noches.agregarAlFinal(actual);
            actual = actual.plusDays(1);
        }
    }

    public void agregarAcompanante(Huesped h) {
        if (h != null && !h.equals(titular) && !acompanantes.contiene(h)) {
            acompanantes.agregarAlFinal(h);
        }
    }

    public int getTotalPersonas() {
        return 1 + acompanantes.getTam();
    }

    public long getCantidadNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public void registrarMovimiento(String descripcion, double monto, boolean esCargo) {
        MovimientoFolio mov = new MovimientoFolio(descripcion, monto, esCargo);
        folio.agregarAlFinal(mov);
        historialFolio.apilar(mov);
    }

    public void registrarReembolsoOAnulacion(String descripcion, double monto) {
        MovimientoFolio mov = new MovimientoFolio(descripcion, monto, MovimientoFolio.TipoMovimiento.REEMBOLSO_O_ANULACION);
        folio.agregarAlFinal(mov);
        historialFolio.apilar(mov);
    }

    public MovimientoFolio deshacerUltimoMovimiento() {
        if (historialFolio.estaVacia()) {
            throw new ReglaNegocioException("No hay movimientos en el folio para deshacer.");
        }
        MovimientoFolio ultimo = historialFolio.desapilar();
        if (ultimo.getTipo() == MovimientoFolio.TipoMovimiento.PAGO) {
            registrarReembolsoOAnulacion("Anulación de: " + ultimo.getDescripcion(), ultimo.getMonto());
        } else if (ultimo.getTipo() == MovimientoFolio.TipoMovimiento.CARGO) {
            registrarMovimiento("Reverso cargo: " + ultimo.getDescripcion(), ultimo.getMonto(), false);
        }
        return ultimo;
    }

    public double calcularTotalPagadoNeto() {
        double pagado = 0.0;
        for (MovimientoFolio mov : folio) {
            if (mov.getTipo() == MovimientoFolio.TipoMovimiento.PAGO) {
                pagado += mov.getMonto();
            } else if (mov.getTipo() == MovimientoFolio.TipoMovimiento.REEMBOLSO_O_ANULACION) {
                pagado -= mov.getMonto();
            }
        }
        return Math.max(0.0, pagado);
    }

    public double calcularTotalCargos() {
        double cargos = 0.0;
        for (MovimientoFolio mov : folio) {
            if (mov.getTipo() == MovimientoFolio.TipoMovimiento.CARGO) {
                cargos += mov.getMonto();
            }
        }
        return cargos;
    }

    public boolean seSolapaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return fechaEntrada.isBefore(otraSalida) && otraEntrada.isBefore(fechaSalida);
    }

    public int getId() { return id; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamentoAsignado() { return apartamentoAsignado; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public CanalReserva getCanal() { return canal; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public ListaDoble<Huesped> getAcompanantes() { return acompanantes; }
    public ListaDoble<LocalDate> getNoches() { return noches; }
    public ListaDoble<MovimientoFolio> getFolio() { return folio; }

    @Override
    public int compareTo(Reserva o) {
        int cmpFecha = this.fechaEntrada.compareTo(o.fechaEntrada);
        if (cmpFecha != 0) return cmpFecha;
        int cmpApto = this.apartamentoAsignado.getCodigo().compareTo(o.apartamentoAsignado.getCodigo());
        if (cmpApto != 0) return cmpApto;
        return Integer.compare(this.id, o.id);
    }
}
