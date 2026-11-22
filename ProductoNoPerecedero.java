package Modelo;

import java.time.LocalDate;

public class ProductoNoPerecedero extends Producto {

    public ProductoNoPerecedero(String codigo, String nombre, int cantidad,
                                double costo, double precio, int stockMinimo) {
        super(codigo, nombre, cantidad, costo, precio, stockMinimo);
    }

    @Override
    public String getTipo() {
        return "No perecedero";
    }

    @Override
    public double calcularPrecioVenta(LocalDate hoy) {
        return getPrecio();
    }
}