package Interfaz;

import java.time.LocalDate;

import Negocio.DatosEjemplo;
import Negocio.InventarioService;

/** Punto de entrada: crea el servicio, carga datos de ejemplo y arranca el menú. */
public class Main {
    public static void main(String[] args) {
        LocalDate hoy = LocalDate.now(); // fecha real del computador
        InventarioService inventario = new InventarioService(hoy);

        // Quita o comenta esta línea si quieres empezar con el inventario vacío.
        DatosEjemplo.cargar(inventario, hoy);

        new MenuConsola(inventario).iniciar();
    }
}