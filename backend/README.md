# backend — vinilos (Spring Boot)

API REST del módulo de vinilos. Java 21 · Spring Boot 3.4 · PostgreSQL · Flyway.

## Requisitos
- JDK 21+ (se compila con target 21; funciona con JDK más nuevos).
- PostgreSQL corriendo (recomendado vía `docker compose up -d` desde la raíz).
- No hace falta instalar Maven: se usa el **wrapper** (`./mvnw` / `mvnw.cmd`).

## Configuración (perfiles y secrets)
- Perfiles Spring: `dev` (por defecto) y `prod`. Se elige con `SPRING_PROFILES_ACTIVE`.
- Los secrets (BD, y a futuro MP/SMTP/JWT) van por **variables de entorno**, nunca en el repo.
- En `dev` hay defaults locales que coinciden con el `docker-compose.yml` de la raíz
  (`vinilos/vinilos` sobre `localhost:5432/vinilos`).

Variables (ver `.env.example` en la raíz):
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `SPRING_PROFILES_ACTIVE`, `APP_CORS_ALLOWED_ORIGINS`,
`APP_FRONTEND_URL`, `JWT_SECRET` (obligatoria en prod), `APP_MAIL_MODE` (`log`/`smtp`), `APP_MAIL_FROM`,
`GOOGLE_CLIENT_ID` (opcional; activa el login con Google), y en prod `SMTP_*`.

### Cuentas de prueba (perfil dev)
El seeder (`DevDataSeeder`, solo en dev) crea datos de ejemplo si la base está vacía, incluyendo:
- `admin@pelo-web.local` / `admin1234` — rol **ADMIN**.
- `cliente@pelo-web.local` / `cliente1234` — rol **CLIENTE** (verificado).
El link de verificación de email, en dev, aparece en la consola del backend (no se envía mail real).

## Correr en local
```bash
# 1) Postgres (desde la raíz del repo)
docker compose up -d

# 2) Backend (desde backend/)
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```
Al arrancar, Flyway aplica `V1__init.sql` y Hibernate **valida** que el esquema coincida
con las entidades (`ddl-auto: validate`).

## Health-check
`GET http://localhost:8080/api/health` → `{ status, service, timestamp, database }`.
También `GET /actuator/health`.

## Estructura
```
src/main/java/com/peloweb/vinilos/
├── VinilosApplication.java
├── domain/            # entidades JPA (§4 de la spec) + enums
└── web/               # HealthController, config CORS
src/main/resources/
├── application.yml, application-dev.yml, application-prod.yml
└── db/migration/V1__init.sql
```
