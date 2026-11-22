package Modelo;

import java.time.LocalDate;

/**
 * Clase base de todos los productos del minimarket.
 * Es abstracta: no existe un "producto genérico", siempre es
 * perecedero o no perecedero.
 */
public abstract class Producto {

    // atributos privados ( Encapsulamiento)
    private String codigo;
    private String nombre;
    private int cantidad;
    private double costo;
    private double precio;
    private int stockMinimo;

    public Producto(String codigo, String nombre, int cantidad,
                    double costo, double precio, int stockMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        if (costo <= 0) {
            throw new IllegalArgumentException("El costo debe ser mayor que cero.");
        }
        if (precio < costo) {
            throw new IllegalArgumentException("El precio no puede ser menor que el costo.");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.cantidad = cantidad;
        this.costo = costo;
        this.precio = precio;
        this.stockMinimo = stockMinimo;
    }

    // Getters lectura controlada de los datos
    public String getCodigo()   { return codigo; }
    public String getNombre()   { return nombre; }
    public int getCantidad()    { return cantidad; }
    public double getCosto()    { return costo; }
    public double getPrecio()   { return precio; }
    public int getStockMinimo() { return stockMinimo; }

    // Polimorfismo, cada subclase define su propia versión
    public abstract String getTipo();
    public abstract double calcularPrecioVenta(LocalDate hoy);

    // Texto de caducidad para las tablas. Los no perecederos no tienen, por eso "-".
    // ProductoPerecedero lo sobrescribe con su fecha real.
    public String getCaducidadTexto() {
        return "-";
    }
    // Sube el stock. Es final: ninguna subclase puede cambiar cómo se modifica la cantidad.
    public final void aumentarStock(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades a ingresar deben ser mayores que cero.");
        }
        cantidad += unidades;
    }

    // Baja el stock (venta). No deja vender más de lo que hay.
    public final void reducirStock(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades a vender deben ser mayores que cero.");
        }
        if (unidades > cantidad) {
            throw new IllegalArgumentException("Stock insuficiente: hay " + cantidad
                    + " unidad(es) y se piden " + unidades + ".");
        }
        cantidad -= unidades;
    }

    public boolean estaVencido(LocalDate hoy) {
        return false;
    }

    public boolean estaPorVencer(LocalDate hoy) {
        return false;
    }
    @Override
    public String toString() {
        return codigo + " | " + nombre + " | " + getTipo()
                + " | cant: " + cantidad + " | $" + precio;
    }
    // se hace la siguiente pregunta: El stock está en el mínimo o por debajo? Si es así, hay que reponer.
    public boolean tieneStockBajo() {
        return cantidad <= stockMinimo;
    }
}