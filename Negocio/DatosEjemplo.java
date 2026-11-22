package Negocio;

import java.time.LocalDate;

/**
 * Carga productos de demostración para no tener que teclearlos
 * cada vez que se ejecuta el programa.
 */
public class DatosEjemplo {

    public static void cargar(InventarioService inventario, LocalDate hoy) {
        // Perecederos: las fechas se calculan desde hoy, así el ejemplo no se desactualiza.
        inventario.registrarPerecedero("Leche entera 1L", 40, 0.85, 1.10, 10, hoy.plusDays(11));
        inventario.registrarPerecedero("Yogurt natural", 25, 0.60, 0.90, 5, hoy.plusDays(9));
        inventario.registrarPerecedero("Queso fresco", 8, 1.80, 2.60, 5, hoy.plusDays(4));  // por vencer
        inventario.registrarPerecedero("Pan de molde", 15, 1.20, 1.75, 5, hoy.plusDays(3)); // por vencer

        // No perecederos
        inventario.registrarNoPerecedero("Arroz 1 kg", 120, 0.80, 1.05, 30);
        inventario.registrarNoPerecedero("Aceite 1L", 60, 2.10, 2.75, 15);
        inventario.registrarNoPerecedero("Tornillo 1 pulgada", 150, 0.30, 0.50, 50);
        inventario.registrarNoPerecedero("Foco LED 9W", 6, 1.40, 2.25, 10); // stock bajo (6 < 10)
    }
}