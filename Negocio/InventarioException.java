package Negocio;

/**
 * Error de negocio (regla del minimarket incumplida).
 * Lleva un mensaje claro para mostrárselo al usuario.
 */
public class InventarioException extends RuntimeException {

    public InventarioException(String mensaje) {
        super(mensaje); // guarda el mensaje; se lee luego con getMessage()
    }
}