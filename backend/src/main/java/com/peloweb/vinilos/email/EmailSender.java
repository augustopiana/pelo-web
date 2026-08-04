package com.peloweb.vinilos.email;

/** Envio de emails. Implementacion segun app.mail.mode (log en dev, smtp en prod). */
public interface EmailSender {

    void enviarVerificacion(String destinatario, String nombre, String linkVerificacion);
}
