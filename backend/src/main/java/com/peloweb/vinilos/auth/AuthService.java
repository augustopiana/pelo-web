package com.peloweb.vinilos.auth;

import com.peloweb.vinilos.auth.dto.AuthResponse;
import com.peloweb.vinilos.auth.dto.LoginRequest;
import com.peloweb.vinilos.auth.dto.MessageResponse;
import com.peloweb.vinilos.auth.dto.RegisterRequest;
import com.peloweb.vinilos.auth.dto.UsuarioDTO;
import com.peloweb.vinilos.domain.Usuario;
import com.peloweb.vinilos.domain.enums.RolUsuario;
import com.peloweb.vinilos.email.EmailSender;
import com.peloweb.vinilos.security.AuthUser;
import com.peloweb.vinilos.security.JwtService;
import com.peloweb.vinilos.user.UsuarioRepository;
import com.peloweb.vinilos.web.ApiException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailSender emailSender;
    private final GoogleVerifier google;
    private final String frontendUrl;

    public AuthService(UsuarioRepository usuarios,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       EmailSender emailSender,
                       GoogleVerifier google,
                       @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
        this.emailSender = emailSender;
        this.google = google;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setTelefono(req.telefono());
        u.setRol(RolUsuario.CLIENTE);
        u.setEmailVerificado(false);
        u.setCreatedAt(OffsetDateTime.now());
        usuarios.save(u);

        enviarVerificacion(u);
        return new MessageResponse("Registro exitoso. Te enviamos un email para verificar la cuenta.");
    }

    private void enviarVerificacion(Usuario u) {
        String token = jwt.generateVerification(u);
        String link = frontendUrl + "/verificar?token=" + token;
        emailSender.enviarVerificacion(u.getEmail(), u.getNombre(), link);
    }

    @Transactional
    public MessageResponse verify(String token) {
        Claims claims = parseOr(token, HttpStatus.BAD_REQUEST, "Token de verificacion invalido o vencido");
        if (!JwtService.TYPE_VERIFY.equals(jwt.type(claims))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token de verificacion invalido");
        }
        Usuario u = usuarios.findById(jwt.userId(claims))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));
        if (!u.isEmailVerificado()) {
            u.setEmailVerificado(true);
            usuarios.save(u);
        }
        return new MessageResponse("Email verificado. Ya podes operar.");
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        Usuario u = usuarios.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos"));
        if (u.getPasswordHash() == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos");
        }
        return tokens(u);
    }

    public AuthResponse refresh(String refreshToken) {
        Claims claims = parseOr(refreshToken, HttpStatus.UNAUTHORIZED, "Refresh token invalido o vencido");
        if (!JwtService.TYPE_REFRESH.equals(jwt.type(claims))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token invalido");
        }
        Usuario u = usuarios.findById(jwt.userId(claims))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return tokens(u);
    }

    @Transactional
    public AuthResponse google(String idToken) {
        GoogleVerifier.GooglePayload p = google.verify(idToken);
        if (!p.emailVerificado()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El email de Google no esta verificado");
        }
        String email = p.email().toLowerCase();
        Usuario u = usuarios.findByGoogleId(p.sub())
                .or(() -> usuarios.findByEmail(email))
                .orElseGet(Usuario::new);

        if (u.getId() == null) {
            u.setId(UUID.randomUUID());
            u.setNombre(p.nombre() != null ? p.nombre() : email);
            u.setEmail(email);
            u.setRol(RolUsuario.CLIENTE);
            u.setCreatedAt(OffsetDateTime.now());
        }
        // Google ya verifico el email; vinculamos el google_id.
        u.setGoogleId(p.sub());
        u.setEmailVerificado(true);
        usuarios.save(u);

        return tokens(u);
    }

    public UsuarioDTO me(AuthUser principal) {
        Usuario u = usuarios.findById(principal.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        return UsuarioDTO.from(u);
    }

    private Claims parseOr(String token, HttpStatus status, String mensaje) {
        try {
            return jwt.parse(token);
        } catch (Exception e) {
            throw new ApiException(status, mensaje);
        }
    }

    private AuthResponse tokens(Usuario u) {
        return new AuthResponse(jwt.generateAccess(u), jwt.generateRefresh(u), UsuarioDTO.from(u));
    }
}
