package com.peloweb.vinilos.config;

import com.peloweb.vinilos.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Seguridad stateless con JWT.
 * Publico: health, endpoints de /auth y el catalogo de lectura (GET).
 * El resto exige autenticacion; los endpoints de panel exigiran ADMIN (milestones siguientes).
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin autenticacion valida -> 401 (el front lo usa para renovar/redirigir).
                // Autenticado pero sin permiso -> 403 (no dispara refresh/logout en el front).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                        .accessDeniedHandler((request, response, denied) ->
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/actuator/health").permitAll()
                        .requestMatchers("/auth/register", "/auth/login", "/auth/verify",
                                "/auth/refresh", "/auth/google").permitAll()
                        // Catalogo publico (solo lectura).
                        .requestMatchers(HttpMethod.GET, "/vinilos", "/vinilos/**", "/generos").permitAll()
                        // Gestion del dueno: escritura de vinilos, fotos y generos -> ADMIN.
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/vinilos", "/vinilos/*/fotos", "/generos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/vinilos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/vinilos/*/pausar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/vinilos/*/fotos/*").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
