package com.peloweb.vinilos.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mailer de desarrollo: no envia nada, loguea el link de verificacion en la consola.
 * Activo cuando app.mail.mode=log (default).
 */
@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void enviarVerificacion(String destinatario, String nombre, String linkVerificacion) {
        log.info("""

                ==================== [DEV] Email de verificacion ====================
                Para : {} <{}>
                Link : {}
                =====================================================================""",
                nombre, destinatario, linkVerificacion);
    }
}
