package Interfaz;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import Modelo.Producto;
import Negocio.InventarioException;
import Negocio.InventarioService;
import Negocio.Validador;

/**
 * Menú de consola. Solo pide datos y muestra resultados:
 * las reglas del negocio las aplica InventarioService.
 */
public class MenuConsola {

    private final InventarioService inventario;
    private final Scanner scanner = new Scanner(System.in);

    public MenuConsola(InventarioService inventario) {
        this.inventario = inventario;
    }

    // Bucle principal: muestra el menú hasta que el usuario elija 0.
    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();
            // Cualquier error de las capas inferiores se muestra y el menú continúa.
            try {
                switch (opcion) {
                    case "1": registrarProducto(); break;
                    case "2": listarProductos(); break;
                    case "3": buscarPorCodigo(); break;
                    case "4": buscarPorNombre(); break;
                    case "5": mostrarEstado(); break;
                    case "6": registrarEntrada(); break;
                    case "7": registrarVenta(); break;
                    case "8": avanzarFecha(); break;
                    case "9": mostrarAlertas(); break;
                    case "10": mostrarValorInventario(); break;
                    case "11": eliminarProducto(); break;
                    case "0":
                        salir = true;
                        System.out.println("Hasta luego.");
                        break;
                    default:
                        System.out.println("ERROR: Opción no válida.");
                }
            } catch (InventarioException | IllegalArgumentException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("===== MINIMARKET EL VECINO =====");
        System.out.println("1. Registrar producto");
        System.out.println("2. Listar inventario");
        System.out.println("3. Buscar por código");
        System.out.println("4. Buscar por nombre");
        System.out.println("5. Ver bodega y presupuesto");
        System.out.println("6. Entrada de stock");
        System.out.println("7. Registrar venta");
        System.out.println("8. Avanzar fecha (simulación)");
        System.out.println("9. Ver alertas");
        System.out.println("10. Valor del inventario");
        System.out.println("11. Eliminar producto");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    // RF1: pide los datos como texto, los convierte con Validador y registra.
    private void registrarProducto() {
        System.out.print("Tipo de producto (1 = Perecedero, 2 = No perecedero): ");
        String tipo = scanner.nextLine().trim();
        if (!tipo.equals("1") && !tipo.equals("2")) {
            throw new InventarioException("Tipo de producto no válido: escriba 1 o 2.");
        }

        // Primero se leen todos los datos como texto (aunque estén vacíos)...
        String nombre = pedir("Nombre");
        String cantidad = pedir("Cantidad");
        String costo = pedir("Costo unitario");
        String precio = pedir("Precio de venta");
        String stockMinimo = pedir("Stock mínimo");
        String caducidad = tipo.equals("1") ? pedir("Fecha de caducidad (AAAA-MM-DD)") : null;

        // ...y después se convierten uno por uno; el primero inválido detiene el registro.
        String nombreOk = Validador.requerido(nombre, "nombre");
        int cantidadOk = Validador.aEntero(cantidad, "cantidad");
        double costoOk = Validador.aDecimal(costo, "costo");
        double precioOk = Validador.aDecimal(precio, "precio");
        int stockOk = Validador.aEntero(stockMinimo, "stock mínimo");

        Producto nuevo;
        if (tipo.equals("1")) {
            LocalDate fechaOk = Validador.aFecha(caducidad, "fecha de caducidad");
            nuevo = inventario.registrarPerecedero(nombreOk, cantidadOk, costoOk, precioOk, stockOk, fechaOk);
        } else {
            nuevo = inventario.registrarNoPerecedero(nombreOk, cantidadOk, costoOk, precioOk, stockOk);
        }
        System.out.println("Producto registrado con código " + nuevo.getCodigo() + ".");
    }

    // RF2: muestra todo el inventario en una tabla.
    private void listarProductos() {
        mostrarTabla(inventario.listar());
    }

    // RF3: busca un producto por su código.
    private void buscarPorCodigo() {
        String codigo = Validador.requerido(pedir("Código del producto"), "código");
        mostrarTabla(List.of(inventario.buscarPorCodigo(codigo)));
    }

    // RF3: busca productos cuyo nombre contenga el texto escrito.
    private void buscarPorNombre() {
        String texto = Validador.requerido(pedir("Texto a buscar en el nombre"), "nombre");
        mostrarTabla(inventario.buscarPorNombre(texto));
    }

    // Muestra cuánto espacio y presupuesto quedan.
    private void mostrarEstado() {
        System.out.println("Bodega: " + inventario.unidadesEnBodega() + " / "
                + InventarioService.CAPACIDAD_MAXIMA + " unidades (libres: "
                + inventario.espacioLibre() + ")");
        System.out.printf(Locale.US, "Presupuesto disponible: $%.2f de $%.2f%n",
                inventario.getPresupuestoDisponible(), InventarioService.PRESUPUESTO_INICIAL);
    }

    // RF4: suma unidades al stock de un producto que ya existe (compra a proveedor).
    private void registrarEntrada() {
        String codigo = Validador.requerido(pedir("Código del producto"), "código");
        int unidades = Validador.aEntero(pedir("Unidades que ingresan"), "unidades");
        Producto p = inventario.registrarEntrada(codigo, unidades);
        System.out.println("Entrada registrada: " + p.getNombre()
                + " ahora tiene " + p.getCantidad() + " unidades.");
    }

    // RF5: vende unidades; si el producto está por vencer, el servicio ya aplica el descuento.
    private void registrarVenta() {
        String codigo = Validador.requerido(pedir("Código del producto"), "código");
        int unidades = Validador.aEntero(pedir("Unidades a vender"), "unidades");
        Producto p = inventario.buscarPorCodigo(codigo);
        double precioUnitario = inventario.precioDeVenta(p);
        double total = inventario.registrarVenta(p.getCodigo(), unidades);
        System.out.printf(Locale.US, "Venta registrada: %s x%d a $%.2f c/u. Total: $%.2f. Stock restante: %d.%n",
                p.getNombre(), unidades, precioUnitario, total, p.getCantidad());
        if (precioUnitario < p.getPrecio()) {
            System.out.println("(Se aplicó el descuento por vencer.)");
        }
    }

    // Simula el paso del tiempo para poder ver productos que se vencen.
    private void avanzarFecha() {
        int dias = Validador.aEntero(pedir("Días a avanzar"), "días");
        inventario.avanzarDias(dias);
        System.out.println("Fecha del sistema: " + inventario.getHoy());
    }
    // RF6: muestra los productos vencidos, por vencer y con stock bajo.
    private void mostrarAlertas() {
        System.out.println("--- ALERTAS (fecha del sistema: " + inventario.getHoy() + ") ---");
        mostrarLista("Productos VENCIDOS", inventario.productosVencidos());
        mostrarLista("Productos POR VENCER (7 días o menos)", inventario.productosPorVencer());
        mostrarLista("Productos con STOCK BAJO", inventario.productosStockBajo());
    }

    // Imprime un título con la cantidad de productos y una línea por cada uno.
    private void mostrarLista(String titulo, List<Producto> lista) {
        System.out.println(titulo + " (" + lista.size() + "):");
        if (lista.isEmpty()) {
            System.out.println("  ninguno");
        }
        for (Producto p : lista) {
            System.out.println("  " + p.getCodigo() + " | " + p.getNombre()
                    + " | caduca: " + p.getCaducidadTexto()
                    + " | stock: " + p.getCantidad() + " (mínimo " + p.getStockMinimo() + ")");
        }
    }

    // RF7: muestra cuánto dinero hay invertido y cuánto se espera obtener al vender.
    private void mostrarValorInventario() {
        System.out.printf(Locale.US, "Valor del inventario al costo:   $%.2f%n", inventario.valorAlCosto());
        System.out.printf(Locale.US, "Valor de venta (precios de hoy): $%.2f%n", inventario.valorDeVenta());
        System.out.printf(Locale.US, "Utilidad estimada:               $%.2f%n", inventario.utilidadEstimada());
    }

    // RF8: elimina un producto sin stock o vencido.
    private void eliminarProducto() {
        String codigo = Validador.requerido(pedir("Código del producto"), "código");
        Producto p = inventario.eliminarProducto(codigo);
        System.out.println("Producto eliminado: " + p.getCodigo() + " - " + p.getNombre() + ".");
    }

    // Imprime una lista de productos como tabla (Locale.US para que los decimales usen punto).
    private void mostrarTabla(List<Producto> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay productos para mostrar.");
            return;
        }
        System.out.printf(Locale.US, "%-6s %-22s %-14s %6s %9s  %-11s%n",
                "Código", "Nombre", "Tipo", "Cant.", "Precio", "Caducidad");
        for (Producto p : lista) {
            System.out.printf(Locale.US, "%-6s %-22s %-14s %6d %9.2f  %-11s%n",
                    p.getCodigo(), p.getNombre(), p.getTipo(), p.getCantidad(),
                    p.getPrecio(), p.getCaducidadTexto());
        }
        System.out.println("Total: " + lista.size() + " producto(s).");
    }

    // Muestra una etiqueta y devuelve lo que el usuario escriba (puede venir vacío).
    private String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return scanner.nextLine();
    }
}