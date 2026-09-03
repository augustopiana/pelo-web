package com.peloweb.vinilos.order;

import com.peloweb.vinilos.account.OrdenRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera el código de retiro (R-13): aleatorio, único, legible, sin caracteres ambiguos.
 * Alfabeto sin 0/O/1/I/L; 6 caracteres agrupados (ej. H7K-2P9).
 */
@Component
public class CodigoRetiroGenerator {

    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LARGO = 6;
    private static final int MAX_INTENTOS = 20;

    private final SecureRandom random = new SecureRandom();
    private final OrdenRepository ordenes;

    public CodigoRetiroGenerator(OrdenRepository ordenes) {
        this.ordenes = ordenes;
    }

    /** Genera un código único (reintenta ante colisión). */
    public String generar() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            String codigo = armar();
            if (!ordenes.existsByCodigoRetiro(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de retiro único");
    }

    private String armar() {
        StringBuilder sb = new StringBuilder(LARGO + 1);
        for (int i = 0; i < LARGO; i++) {
            if (i == LARGO / 2) {
                sb.append('-');
            }
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
