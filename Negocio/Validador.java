package Negocio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Convierte el texto que escribe el usuario en números o fechas.
 * Si algo falta o es inválido, lanza un error con un mensaje claro.
 */
public class Validador {

    // Comprueba que el campo no esté vacío y devuelve el texto sin espacios sobrantes.
    public static String requerido(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new InventarioException("El campo '" + campo + "' es obligatorio.");
        }
        return texto.trim();
    }

    // Texto -> número entero (para cantidad y stock mínimo).
    public static int aEntero(String texto, String campo) {
        String limpio = requerido(texto, campo);
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new InventarioException("El campo '" + campo + "' debe ser un número entero.");
        }
    }

    // Texto -> número decimal (para costo y precio). Acepta coma o punto
    public static double aDecimal(String texto, String campo) {
        String limpio = requerido(texto, campo).replace(',', '.');
        try {
            double valor = Double.parseDouble(limpio);
            if (!Double.isFinite(valor)) { // rechaza "NaN" e "Infinity"
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new InventarioException("El campo '" + campo + "' debe ser un número (ejemplo: 0.50).");
        }
    }

    // Texto -> fecha. Formato obligatorio: AAAA-MM-DD.
    public static LocalDate aFecha(String texto, String campo) {
        String limpio = requerido(texto, campo);
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new InventarioException("El campo '" + campo + "' debe tener formato AAAA-MM-DD (ejemplo: 2026-11-30).");
        }
    }
}
