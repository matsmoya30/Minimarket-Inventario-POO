package Modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductoPerecedero extends Producto {

    public static final int DIAS_POR_VENCER = 7;
    public static final double DESCUENTO = 0.20;

    private LocalDate fechaCaducidad;

    public ProductoPerecedero(String codigo, String nombre, int cantidad,
                              double costo, double precio, int stockMinimo,
                              LocalDate fechaCaducidad) {
        super(codigo, nombre, cantidad, costo, precio, stockMinimo);
        if (fechaCaducidad == null) {
            throw new IllegalArgumentException("La fecha de caducidad es obligatoria.");
        }
        this.fechaCaducidad = fechaCaducidad;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, fechaCaducidad);
    }

    @Override
    public boolean estaVencido(LocalDate hoy) {
        return fechaCaducidad.isBefore(hoy);
    }

    @Override
    public boolean estaPorVencer(LocalDate hoy) {
        return !estaVencido(hoy) && diasParaVencer(hoy) <= DIAS_POR_VENCER;
    }

    @Override
    public String getCaducidadTexto() {
        return fechaCaducidad.toString(); // formato AAAA-MM-DD
    }

    @Override
    public String getTipo() {
        return "Perecedero";
    }

    @Override
    public double calcularPrecioVenta(LocalDate hoy) {
        if (estaVencido(hoy)) {
            return 0.0;
        }
        if (estaPorVencer(hoy)) {
            double conDescuento = getPrecio() * (1 - DESCUENTO);
            return Math.round(conDescuento * 100.0) / 100.0;
        }
        return getPrecio();
    }
}