package com.peloweb.vinilos.auth;

import com.peloweb.vinilos.auth.dto.AuthResponse;
import com.peloweb.vinilos.auth.dto.GoogleLoginRequest;
import com.peloweb.vinilos.auth.dto.LoginRequest;
import com.peloweb.vinilos.auth.dto.MessageResponse;
import com.peloweb.vinilos.auth.dto.RefreshRequest;
import com.peloweb.vinilos.auth.dto.RegisterRequest;
import com.peloweb.vinilos.auth.dto.UsuarioDTO;
import com.peloweb.vinilos.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @GetMapping("/verify")
    public MessageResponse verify(@RequestParam String token) {
        return auth.verify(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return auth.refresh(req.refreshToken());
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest req) {
        return auth.google(req.idToken());
    }

    @GetMapping("/me")
    public UsuarioDTO me(@AuthenticationPrincipal AuthUser principal) {
        return auth.me(principal);
    }
}
