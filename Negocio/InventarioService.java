package Negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import Modelo.Producto;
import Modelo.ProductoNoPerecedero;
import Modelo.ProductoPerecedero;

/**
 * Lógica del inventario: aquí viven las reglas del negocio.
 * La interfaz solo pedirá datos y llamará a estos métodos.
 */
public class InventarioService {

    // Restricciones del minimarket.
    public static final int CAPACIDAD_MAXIMA = 1000;          // unidades que caben en bodega
    public static final double PRESUPUESTO_INICIAL = 5000.00; // dinero disponible para compras

    // Aquí se guardan todos los productos. Es private: solo esta clase la modifica.
    private final List<Producto> productos = new ArrayList<>();

    // Cuántos productos se han registrado con éxito.
    private int totalRegistrados = 0;

    // Dinero que aún se puede gastar en compras, baja con cada registro o entrada.
    private double presupuestoDisponible = PRESUPUESTO_INICIAL;

    // Fecha "de hoy" con la que se evalúan los vencimientos, se pueden adelantar las fechas para simular.
    private LocalDate hoy;

    public InventarioService(LocalDate hoy) {
        this.hoy = hoy;
    }

    // RF1: registra un producto no perecedero y devuelve el producto creado.
    public Producto registrarNoPerecedero(String nombre, int cantidad, double costo,
                                          double precio, int stockMinimo) {
        validarNombreNoRepetido(nombre);
        Producto nuevo = new ProductoNoPerecedero(siguienteCodigo(), nombre,
                cantidad, costo, precio, stockMinimo);
        guardar(nuevo);
        return nuevo;
    }

    // RF1: registra un producto perecedero (este además lleva fecha de caducidad).
    public Producto registrarPerecedero(String nombre, int cantidad, double costo,
                                        double precio, int stockMinimo,
                                        LocalDate caducidad) {
        validarNombreNoRepetido(nombre);
        ProductoPerecedero nuevo = new ProductoPerecedero(siguienteCodigo(), nombre,
                cantidad, costo, precio, stockMinimo, caducidad);
        // Regla: un producto que ya caducó no se puede registrar.
        if (nuevo.estaVencido(hoy)) {
            throw new InventarioException("No se puede registrar un producto vencido (caducó el "
                    + nuevo.getFechaCaducidad() + ").");
        }
        guardar(nuevo);
        return nuevo;
    }

    // RF2: devuelve el inventario en modo solo lectura (nadie puede alterarlo desde fuera).
    public List<Producto> listar() {
        return Collections.unmodifiableList(productos);
    }

    // RF3: busca un producto por su código exacto; si no existe, lanza error.
    public Producto buscarPorCodigo(String codigo) {
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        throw new InventarioException("No existe un producto con código " + codigo.trim() + ".");
    }

    // RF3: busca por parte del nombre, sin importar mayúsculas o minúsculas.
    public List<Producto> buscarPorNombre(String texto) {
        List<Producto> resultado = new ArrayList<>();
        String buscado = texto.trim().toLowerCase();
        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // RF4: suma unidades a un producto existente (compra a proveedor).
    // Debe respetar las mismas restricciones de bodega y presupuesto que un registro nuevo.
    public Producto registrarEntrada(String codigo, int unidades) {
        Producto p = buscarPorCodigo(codigo);
        if (p.estaVencido(hoy)) {
            throw new InventarioException("No se puede ingresar stock a un producto vencido ("
                    + p.getNombre() + ").");
        }
        if (unidades <= 0) {
            throw new InventarioException("Las unidades a ingresar deben ser mayores que cero.");
        }
        double costoTotal = validarCompra(unidades, p.getCosto());
        p.aumentarStock(unidades);
        cobrar(costoTotal);
        return p;
    }

    // RF5: vende unidades al precio de hoy (con descuento si está por vencer).
    // Devuelve el total cobrado al cliente.
    public double registrarVenta(String codigo, int unidades) {
        Producto p = buscarPorCodigo(codigo);
        if (p.estaVencido(hoy)) {
            throw new InventarioException("No se puede vender un producto vencido ("
                    + p.getNombre() + ").");
        }
        p.reducirStock(unidades); // valida unidades > 0 y que alcance el stock
        return redondear(unidades * precioDeVenta(p));
    }

    // Precio unitario de venta de hoy. Cada tipo de producto lo calcula a su manera (polimorfismo).
    public double precioDeVenta(Producto p) {
        return p.calcularPrecioVenta(hoy);
    }

    // RF8: elimina un producto. Solo se permite si no tiene stock o si ya venció (descarte).
    // El código eliminado NO se reutiliza, porque totalRegistrados nunca baja.
    public Producto eliminarProducto(String codigo) {
        Producto p = buscarPorCodigo(codigo);
        if (p.getCantidad() > 0 && !p.estaVencido(hoy)) {
            throw new InventarioException("No se puede eliminar '" + p.getNombre()
                    + "': todavía tiene " + p.getCantidad()
                    + " unidad(es) vigentes. Solo se eliminan productos sin stock o vencidos.");
        }
        productos.remove(p);
        return p;
    }

    // Adelanta la fecha del sistema para simular el paso de los días.
    public void avanzarDias(int dias) {
        if (dias <= 0) {
            throw new InventarioException("Los días a avanzar deben ser mayores que cero.");
        }
        hoy = hoy.plusDays(dias);
    }

    public LocalDate getHoy() {
        return hoy;
    }

    // Suma las unidades de todos los productos, lo que hay en bodega.
    public int unidadesEnBodega() {
        int total = 0;
        for (Producto p : productos) {
            total += p.getCantidad();
        }
        return total;
    }

    // Unidades que todavía caben en la bodega.
    public int espacioLibre() {
        return CAPACIDAD_MAXIMA - unidadesEnBodega();
    }

    public double getPresupuestoDisponible() {
        return presupuestoDisponible;
    }

    // RF6: productos cuya fecha de caducidad ya pasó.
    public List<Producto> productosVencidos() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.estaVencido(hoy)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // RF6: productos que vencen en 7 días o menos (y todavía no han vencido).
    public List<Producto> productosPorVencer() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.estaPorVencer(hoy)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // RF6: productos con stock igual o menor al mínimo.
    public List<Producto> productosStockBajo() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (p.tieneStockBajo()) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // RF7: dinero invertido en el inventario actual (unidades x costo).
    public double valorAlCosto() {
        double total = 0;
        for (Producto p : productos) {
            total += p.getCantidad() * p.getCosto();
        }
        return redondear(total);
    }

    // RF7: dinero que se obtendría vendiendo todo a los precios de hoy.
    // Cada producto calcula su precio a su manera: los vencidos valen 0 y los
    // que están por vencer ya traen su descuento (polimorfismo).
    public double valorDeVenta() {
        double total = 0;
        for (Producto p : productos) {
            total += p.getCantidad() * precioDeVenta(p);
        }
        return redondear(total);
    }

    // RF7: ganancia estimada si se vende todo (valor de venta - valor al costo).
    public double utilidadEstimada() {
        return redondear(valorDeVenta() - valorAlCosto());
    }

    // Calcula el próximo código SIN gastarlo todavía.
    private String siguienteCodigo() {
        return String.format("P%03d", totalRegistrados + 1);
    }

    // Guarda un producto nuevo si cumple bodega y presupuesto,
    // y recién ahí descuenta el costo y gasta el código.
    private void guardar(Producto nuevo) {
        double costoTotal = validarCompra(nuevo.getCantidad(), nuevo.getCosto());
        productos.add(nuevo);
        totalRegistrados++;
        cobrar(costoTotal);
    }

    // Revisa que unidades quepan en la bodega y que el costo no pase del presupuesto.
    // Si todo está bien, devuelve el costo total de la compra.
    private double validarCompra(int unidades, double costoUnitario) {
        // Restricción de espacio: las unidades nuevas deben caber en la bodega.
        if (unidades > espacioLibre()) {
            throw new InventarioException("No hay espacio en bodega: capacidad máxima "
                    + CAPACIDAD_MAXIMA + " unidades, espacio libre " + espacioLibre()
                    + " y se intentan registrar " + unidades + ".");
        }
        // Restricción de presupuesto: el costo total no puede superar lo disponible.
        double costoTotal = redondear(unidades * costoUnitario);
        if (costoTotal > presupuestoDisponible) {
            throw new InventarioException("Presupuesto insuficiente: el costo total es $"
                    + dinero(costoTotal) + " y solo quedan $" + dinero(presupuestoDisponible) + ".");
        }
        return costoTotal;
    }

    // Descuenta una compra del presupuesto disponible.
    private void cobrar(double costoTotal) {
        presupuestoDisponible = redondear(presupuestoDisponible - costoTotal);
    }

    // Regla: no puede haber dos productos con el mismo nombre.
    private void validarNombreNoRepetido(String nombre) {
        if (nombre == null) {
            return; // el constructor de Producto avisará que el nombre es obligatorio
        }
        for (Producto p : productos) {
            if (p.getNombre().equalsIgnoreCase(nombre.trim())) {
                throw new InventarioException("Ya existe un producto llamado '"
                        + p.getNombre() + "' (código " + p.getCodigo() + ").");
            }
        }
    }

    // Redondea a 2 decimales para evitar restos raros de los cálculos con dinero.
    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // Da formato de dinero con 2 decimales y punto (ejemplo: 4643.20).
    private static String dinero(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}