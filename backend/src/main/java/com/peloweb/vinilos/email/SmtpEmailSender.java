package com.peloweb.vinilos.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Mailer de produccion via SMTP (Spring Mail). Activo cuando app.mail.mode=smtp.
 * Requiere spring.mail.* configurado por entorno.
 */
@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void enviarVerificacion(String destinatario, String nombre, String linkVerificacion) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Verifica tu cuenta - pelo-web");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Verifica tu cuenta ingresando a este link:\n" + linkVerificacion + "\n\n"
                + "Si no creaste esta cuenta, ignora este mensaje.");
        mailSender.send(mensaje);
    }
}
