# Prompt para Claude Code

Este archivo tiene el **prompt de arranque** (primera sesión, Milestone 0) y un **patrón reutilizable** para los milestones siguientes. Copiá y pegá el bloque que corresponda en Claude Code, lanzado desde la raíz del repo (`pelo-web/`).

---

## 1) Prompt de arranque — Milestone 0

```
Estás trabajando en el proyecto pelo-web. Antes de escribir código, leé estos archivos y no asumas nada que no esté en ellos:
- CLAUDE.md
- docs/plan-de-desarrollo.md (foco en Milestone 0)
- docs/documentacion-tecnica.md
- docs/spec-modulo-vinilos.md (secciones §4 entidades y §5 estados)

Trabajamos con Spec Driven Development: la spec de dominio es la fuente de verdad. Si algo falta o es ambiguo, preguntá en vez de asumir.

Tarea: implementar el Milestone 0 (setup del monorepo y esqueletos), con estos entregables:
1. Estructura del monorepo: backend/ (Spring Boot) y frontend/ (React + Vite + TypeScript).
2. Backend Spring Boot conectado a PostgreSQL con Flyway, y una migración inicial (V1__init.sql) que cree TODAS las entidades del §4 de la spec, con los enums del punto 3.2 de la documentación técnica. Todavía no hace falta exponer endpoints de negocio.
3. Frontend React con routing y un layout base con las tres áreas: catálogo, cuenta y panel del dueño (páginas vacías por ahora).
4. Perfiles dev/prod y manejo de secrets por variables de entorno (nada de credenciales en el repo).
5. Un health-check end-to-end: un endpoint simple en el backend consumido desde el frontend, para verificar que todo el circuito funciona.

Antes de codear:
- Preguntame las decisiones de setup pendientes: Maven o Gradle para el backend, y si querés Docker Compose para levantar PostgreSQL en local. Recomendá una opción para cada una.
- Presentame un plan corto (usá plan mode) con los pasos y los archivos que vas a crear. Esperá mi OK antes de implementar.

Al terminar el milestone:
- Marcá como hechas las tasks correspondientes en docs/historias-usuario.md.
- Agregá la entrada de release en docs/registro-releases.md (qué se hizo, la migración incluida y el "próximo paso" = Milestone 1).
- Verificá que se cumplen los criterios de salida del Milestone 0 antes de darlo por cerrado.
```

---

## 2) Patrón reutilizable — milestones siguientes (M1 en adelante)

Reemplazá `<N>` y el detalle según el plan.

```
Continuamos con pelo-web. Leé CLAUDE.md y docs/registro-releases.md para ubicar desde dónde seguimos.

Vamos con el Milestone <N> del docs/plan-de-desarrollo.md. Antes de codear, leé de docs/spec-modulo-vinilos.md las reglas y flujos involucrados, y las historias correspondientes en docs/historias-usuario.md.

Recordá los no-negociables de CLAUDE.md (pagos por webhook, pieza única con bloqueo, cuenta verificada, quién marca cada acción, reglas de cupón). Si algo de la spec es ambiguo, preguntá.

Presentame un plan corto (plan mode) con las historias que vas a cubrir y cómo, y esperá mi OK. Implementá contra los criterios de aceptación de cada historia.

Al terminar: marcá las tasks/historias como hechas, actualizá el registro de releases con el próximo paso, y confirmá que se cumplen los criterios de salida del milestone.
```

---

## Notas de uso
- Lanzá Claude Code desde la raíz `pelo-web/` para que cargue el `CLAUDE.md` automáticamente.
- El `CLAUDE.md` ya tiene el contexto permanente, así que los prompts pueden ser cortos: sirven para **enfocar la sesión** en un milestone, no para re-explicar el proyecto.
- Usá **plan mode** para revisar el enfoque antes de que escriba código.
- Si corregís a Claude Code dos veces sobre el mismo tema del proyecto, conviene anotar esa regla en `CLAUDE.md`.
```
