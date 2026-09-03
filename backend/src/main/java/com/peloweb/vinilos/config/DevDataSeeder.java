package com.peloweb.vinilos.config;

import com.peloweb.vinilos.catalog.FotoViniloRepository;
import com.peloweb.vinilos.catalog.GeneroRepository;
import com.peloweb.vinilos.catalog.ViniloRepository;
import com.peloweb.vinilos.domain.FotoVinilo;
import com.peloweb.vinilos.domain.Genero;
import com.peloweb.vinilos.domain.Usuario;
import com.peloweb.vinilos.domain.Vinilo;
import com.peloweb.vinilos.domain.enums.EstadoDisco;
import com.peloweb.vinilos.domain.enums.EstadoVinilo;
import com.peloweb.vinilos.domain.enums.Formato;
import com.peloweb.vinilos.domain.enums.RolUsuario;
import com.peloweb.vinilos.user.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Carga datos de prueba solo en el perfil dev, y solo si la base esta vacia.
 * Sirve para navegar/buscar/filtrar el catalogo (criterio de salida de M1) y
 * para tener cuentas con las que loguearse. No corre en prod.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UsuarioRepository usuarios;
    private final GeneroRepository generos;
    private final ViniloRepository vinilos;
    private final FotoViniloRepository fotos;
    private final PasswordEncoder encoder;

    private final Map<String, Genero> generosPorNombre = new HashMap<>();
    private int contadorFotos = 0;

    public DevDataSeeder(UsuarioRepository usuarios, GeneroRepository generos, ViniloRepository vinilos,
                         FotoViniloRepository fotos, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.generos = generos;
        this.vinilos = vinilos;
        this.fotos = fotos;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seedGeneros();
        seedUsuarios();
        seedVinilos();
    }

    private void seedGeneros() {
        if (generos.count() > 0) {
            generos.findAll().forEach(g -> generosPorNombre.put(g.getNombre(), g));
            return;
        }
        for (String nombre : new String[]{"Rock", "Jazz", "Folklore", "Pop", "Blues", "Electrónica", "Tango"}) {
            Genero g = new Genero();
            g.setId(UUID.randomUUID());
            g.setNombre(nombre);
            generosPorNombre.put(nombre, generos.save(g));
        }
        log.info("[seed] {} generos creados", generosPorNombre.size());
    }

    private void seedUsuarios() {
        crearUsuarioSiFalta("admin@pelo-web.local", "Dueño", "admin1234", RolUsuario.ADMIN);
        crearUsuarioSiFalta("cliente@pelo-web.local", "Cliente Demo", "cliente1234", RolUsuario.CLIENTE);
    }

    private void crearUsuarioSiFalta(String email, String nombre, String password, RolUsuario rol) {
        if (usuarios.existsByEmail(email)) {
            return;
        }
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setRol(rol);
        u.setEmailVerificado(true);
        u.setCreatedAt(OffsetDateTime.now());
        usuarios.save(u);
        log.info("[seed] usuario {} ({})", email, rol);
    }

    private void seedVinilos() {
        if (vinilos.count() > 0) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        int i = 0;

        addVinilo("Thriller", "Michael Jackson", "Pop", 1982, "Epic", "Edición original 🇺🇸",
                EstadoDisco.NEAR_MINT, new BigDecimal("25000"), 10, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("The Dark Side of the Moon", "Pink Floyd", "Rock", 1973, "Harvest", "Reedición 🇬🇧",
                EstadoDisco.VG_PLUS, new BigDecimal("32000"), 15, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Kind of Blue", "Miles Davis", "Jazz", 1959, "Columbia", "Edición 🇺🇸",
                EstadoDisco.VG, new BigDecimal("28000"), 10, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Nevermind", "Nirvana", "Rock", 1991, "DGC", "Edición 🇦🇷",
                EstadoDisco.MINT, new BigDecimal("30000"), 12, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Back in Black", "AC/DC", "Rock", 1980, "Atlantic", "Edición original 🇦🇷 1980",
                EstadoDisco.VG_PLUS_PLUS, new BigDecimal("27000"), 10, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Rumours", "Fleetwood Mac", "Rock", 1977, "Warner", "Edición 🇺🇸",
                EstadoDisco.GOOD, new BigDecimal("22000"), 8, EstadoVinilo.VENDIDO, now.minusDays(5), now.minusDays(i++));
        addVinilo("Abbey Road", "The Beatles", "Rock", 1969, "Apple", "Reedición 🇬🇧",
                EstadoDisco.NEAR_MINT, new BigDecimal("45000"), 20, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Cantora", "Mercedes Sosa", "Folklore", 2009, "Sony", "Edición 🇦🇷",
                EstadoDisco.VG_PLUS, new BigDecimal("18000"), 10, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("The Blues", "B.B. King", "Blues", 1960, "Crown", "Edición 🇺🇸",
                EstadoDisco.VG, new BigDecimal("20000"), 10, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));
        addVinilo("Random Access Memories", "Daft Punk", "Electrónica", 2013, "Columbia", "Edición 🇪🇺",
                EstadoDisco.MINT, new BigDecimal("35000"), 15, EstadoVinilo.DISPONIBLE, null, now.minusDays(i++));

        // Casos para verificar la visibilidad (R-9): NO deben aparecer en el catalogo.
        addVinilo("Vendido Viejo", "Artista X", "Rock", 1975, null, null,
                EstadoDisco.VG, new BigDecimal("15000"), 5, EstadoVinilo.VENDIDO, now.minusDays(40), now.minusDays(i++));
        addVinilo("Pausado Test", "Artista Y", "Jazz", 1968, null, null,
                EstadoDisco.VG_PLUS, new BigDecimal("16000"), 5, EstadoVinilo.PAUSADO, null, now.minusDays(i++));

        log.info("[seed] {} vinilos creados (2 ocultos por R-9: vendido +30d y pausado)", i);
    }

    private void addVinilo(String titulo, String artista, String genero, int anio, String sello, String edicionPais,
                           EstadoDisco estadoDisco, BigDecimal precio, int descuentoCortePct,
                           EstadoVinilo estado, OffsetDateTime fechaVenta, OffsetDateTime fechaPublicacion) {
        Vinilo v = new Vinilo();
        v.setId(UUID.randomUUID());
        v.setTitulo(titulo);
        v.setArtista(artista);
        v.setGenero(generosPorNombre.get(genero));
        v.setAnio(anio);
        v.setSello(sello);
        v.setEdicionPais(edicionPais);
        v.setFormato(Formato.VINILO);
        v.setEstadoDisco(estadoDisco);
        v.setDescripcion("Disco de " + artista + " en muy buen estado. Ejemplo de datos de prueba (dev).");
        v.setPrecio(precio);
        v.setDescuentoCortePct(descuentoCortePct);
        v.setEstado(estado);
        v.setFechaVenta(fechaVenta);
        v.setFechaPublicacion(fechaPublicacion);
        v.setCreatedAt(OffsetDateTime.now());
        v.setUpdatedAt(OffsetDateTime.now());
        vinilos.save(v);

        for (int orden = 0; orden < 3; orden++) {
            FotoVinilo f = new FotoVinilo();
            f.setId(UUID.randomUUID());
            f.setVinilo(v);
            f.setUrl("https://picsum.photos/seed/vinilo" + (contadorFotos++) + "/600/600");
            f.setOrden(orden);
            f.setEsPortada(orden == 0);
            fotos.save(f);
        }
    }
}
