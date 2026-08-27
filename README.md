# Biblioteca — API REST de Gestión de Biblioteca

Aplicación backend para la gestión de una biblioteca: usuarios, libros, ejemplares y préstamos. Desarrollada como prueba técnica para un perfil de **Desarrollador Full Stack Junior**.

El proyecto expone una **API REST** construida con **Java 17** y **Spring Boot**, persistiendo datos en **PostgreSQL** mediante **Spring Data JPA** y **Hibernate**. El despliegue con **Docker Compose** levanta PostgreSQL, el backend y el frontend (repositorio separado).

---

## Tabla de contenidos

- [Descripción del proyecto](#descripción-del-proyecto)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura](#arquitectura)
- [Modelo de dominio](#modelo-de-dominio)
- [Reglas de negocio](#reglas-de-negocio)
- [API REST](#api-rest)
- [Manejo de errores](#manejo-de-errores)
- [Base de datos](#base-de-datos)
- [Datos de prueba](#datos-de-prueba)
- [Docker](#docker)
- [Ejecución del proyecto](#ejecución-del-proyecto)
- [Frontend](#frontend)
- [Pruebas](#pruebas)
- [Decisiones técnicas](#decisiones-técnicas)
- [Mejoras futuras](#mejoras-futuras)

---

## Descripción del proyecto

El sistema resuelve la gestión operativa de una biblioteca: mantener el catálogo, controlar copias físicas y registrar préstamos y devoluciones con reglas de negocio.

Es una **API REST** que permite:

- Registrar y administrar **usuarios**.
- Gestionar **libros** del catálogo.
- Administrar **ejemplares** (copias físicas de un libro).
- Registrar **préstamos**, consultarlos y procesar **devoluciones**.

El frontend consume esta API y se encuentra en un repositorio aparte (`BibliotecaFrontend`).

---

## Tecnologías utilizadas

Versiones tomadas de `pom.xml`, del wrapper de Maven y de `Docker-compose.yml`.

| Tecnología | Versión / detalle |
|---|---|
| Java | 17 (`java.version` en `pom.xml`) |
| Spring Boot | 3.5.4 |
| Spring Web | `spring-boot-starter-web` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Hibernate | Gestionado por Spring Data JPA / Spring Boot 3.5.4 |
| Bean Validation | `spring-boot-starter-validation` |
| PostgreSQL | 16 (imagen `postgres:16`) |
| PostgreSQL Driver | Dependencia runtime en `pom.xml` |
| Maven | 3.9.16 (Maven Wrapper) |
| Docker | Imagen del backend (`Dockerfile`) |
| Docker Compose | PostgreSQL + backend + frontend |

---

## Arquitectura

El backend está organizado en capas:

```text
Controller
    ↓
Service
    ↓
DAO / Repository
    ↓
PostgreSQL
```

### Controller

Recibe peticiones HTTP, extrae parámetros y cuerpos JSON, delega en el servicio y devuelve `ResponseEntity` con el código HTTP correspondiente.

Controladores: `UsuarioController`, `LibroController`, `PrestamoController`.

La gestión de ejemplares está en `LibroController` (no hay un controlador propio).

### Service

Aplica las reglas de negocio, valida condiciones y coordina operaciones entre entidades y DAO.

Servicios: `UsuarioService`, `LibroService`, `EjemplarService`, `PrestamosService`.

### DAO / Repository

Interfaces que extienden `JpaRepository` (Spring Data JPA). Persisten y consultan entidades, con métodos derivados y consultas `@Query` cuando hace falta.

DAO: `UsuarioDAO`, `LibroDAO`, `EjemplarDAO`, `PrestamoDAO`.

Spring Data JPA y Hibernate traducen esas operaciones a SQL sobre PostgreSQL.

### Flujo de una petición

```text
Cliente (navegador / herramienta HTTP)
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
DAO / Repository
   │
   ▼
PostgreSQL
```

Si ocurre un error de negocio o un recurso no existe, `GlobalExceptionHandler` intercepta la excepción y responde con JSON.

---

## Modelo de dominio

Entidades en el paquete `Entitys`. Tablas: `usuarios`, `libros`, `ejemplares`, `prestamos`.

### Usuario

Persona registrada en la biblioteca.

| Campo | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `Long` | Identificador autogenerado |
| `nombre` | `String` | Nombre |
| `apellido` | `String` | Apellido |
| `email` | `String` | Correo electrónico |
| `fechaNacimiento` | `LocalDate` | Fecha de nacimiento |
| `prestamos` | `List<Prestamo>` | Préstamos del usuario (expuesto en JSON como `prestamo`) |

```text
Usuario 1 ─────── N Prestamo
```

### Libro

Título del catálogo.

| Campo | Tipo | Descripción |
|---|---|---|
| `idLibro` | `Long` | Identificador autogenerado |
| `titulo` | `String` | Título |
| `isbn` | `String` | ISBN |
| `edicion` | `String` | Edición |
| `autor` | `String` | Autor |
| `fechaPublicacion` | `LocalDate` | Fecha de publicación |
| `ejemplares` | `List<Ejemplar>` | Copias físicas (expuesto en JSON como `ejemplar`) |

```text
Libro 1 ─────── N Ejemplar
```

### Ejemplar

Copia física de un libro.

| Campo | Tipo | Descripción |
|---|---|---|
| `idEjemplar` | `Long` | Identificador autogenerado |
| `estado` | `Boolean` | `true` = disponible, `false` = no disponible |
| `libro` | `Libro` | Libro al que pertenece |
| `prestamos` | `List<Prestamo>` | Historial de préstamos del ejemplar |

```text
Ejemplar N ─────── 1 Libro
Ejemplar 1 ─────── N Prestamo
```

### Prestamo

Préstamo de un ejemplar a un usuario.

| Campo | Tipo | Descripción |
|---|---|---|
| `idPrestamo` | `Long` | Identificador autogenerado |
| `fechaPrestamo` | `LocalDate` | Fecha de registro del préstamo |
| `fechaLimite` | `LocalDate` | Fecha máxima esperada de devolución |
| `fechaDevolucion` | `LocalDate` | Fecha real de devolución (`null` si no se ha devuelto) |
| `estadoPrestamo` | `Enum` | `ACTIVO`, `DEVUELTO` o `VENCIDO` |
| `usuario` | `Usuario` | Usuario que recibe el préstamo |
| `ejemplar` | `Ejemplar` | Ejemplar prestado |

```text
Prestamo N ─────── 1 Usuario
Prestamo N ─────── 1 Ejemplar
```

**Estados:** `ACTIVO`, `DEVUELTO`, `VENCIDO`.

Al crear un préstamo, `fechaLimite` se calcula como `fechaPrestamo + 14 días` (constante `DIASPRESTAMO` en `PrestamosService`). `fechaDevolucion` queda en `null` mientras el préstamo no se devuelva.

---

## Reglas de negocio

Solo se documentan reglas implementadas en los servicios.

### Un usuario no puede tener más de un préstamo pendiente

Un usuario no puede registrar un préstamo nuevo si tiene otro con `fechaDevolucion IS NULL` (consulta `existsPrestamoPendiente`).

Eso incluye préstamos `ACTIVO` o `VENCIDO` aún no devueltos.

Mensaje: `"El Usuario tiene un prestamo pendiente para devolver"`.

### Un ejemplar con préstamo ACTIVO no puede prestarse

Antes de crear el préstamo se comprueba si el ejemplar ya tiene un préstamo en estado `ACTIVO`.

Mensaje: `"El Ejemplar tiene un prestamo ACTIVO"`.

Al registrar el préstamo el ejemplar pasa a `estado = false`. La consulta de ejemplares disponibles por ISBN filtra `estado = true`.

> La disponibilidad se valida por préstamo `ACTIVO`, no comparando el campo `estado` del ejemplar.

### Registro de préstamo

- El usuario debe existir.
- El ejemplar debe existir.
- `fechaPrestamo` = fecha actual.
- `fechaLimite` = `fechaPrestamo + 14 días`.
- `estadoPrestamo` = `ACTIVO`.
- `fechaDevolucion` = `null`.
- El ejemplar se marca no disponible (`estado = false`).

### Devolución

- Se permite devolver préstamos `ACTIVO` o `VENCIDO`.
- Si ya está `DEVUELTO`, se rechaza: `"El Prestamo ya fue DEVUELTO"`.
- Se asigna `fechaDevolucion` = fecha actual.
- `estadoPrestamo` pasa a `DEVUELTO`.
- El ejemplar vuelve a `estado = true`.

### Fecha límite y estado VENCIDO

El método privado `actualizarEstado` determina el estado así:

- Si `fechaDevolucion != null` → `DEVUELTO`
- Si la fecha actual es posterior a `fechaLimite` → `VENCIDO`
- En caso contrario → `ACTIVO`

Esa actualización se ejecuta y se persiste al **listar préstamos por usuario** (`GET /api/prestamos/usuario/{idUsuario}`).

`GET /api/prestamos/libro/{isbn}` **no** recalcula estados.

### Eliminación de libro

No se puede eliminar un libro si alguno de sus ejemplares tiene historial de préstamos (`existsByEjemplarLibroIdLibro`).

Mensaje: `"No se puede eliminar el libro porque tiene historial de préstamos asociados."`

Si no hay préstamos asociados, se eliminan primero los ejemplares del libro y después el libro.

> No existe una regla que impida borrar un libro solo por tener ejemplares no disponibles.

### Eliminación de usuario

No se puede eliminar un usuario si tiene historial de préstamos.

Mensaje: `"No se puede eliminar el usuario porque tiene historial de préstamos."`

### Otras validaciones

| Regla | Dónde | Qué hace |
|---|---|---|
| Email duplicado en creación | `UsuarioService.crear` | Si el email ya existe, lanza `ResourceNotFoundException` (`"Este Email ya existe"`) |
| Usuario existente | `PrestamosService` | El usuario debe existir para prestar |
| Ejemplar existente | `PrestamosService` | El ejemplar debe existir para prestar |
| Libro existente | `EjemplarService` | El libro debe existir para crear un ejemplar |
| Anotaciones `@NotNull` / `@Size` | Entidades | Están en el modelo; los controladores **no** usan `@Valid` |

---

## API REST

Base URL del backend: `http://localhost:8080` (o el puerto de `APP_PORT`).

Con Docker, el frontend también expone la API en `http://localhost:5173/api` (proxy de Nginx).

Los controladores usan `@Controller` y `ResponseEntity`.

---

### Usuarios

Base: `/api/usuarios`

| Método | Ruta | Parámetros | Body | Respuesta |
|---|---|---|---|---|
| `GET` | `/api/usuarios` | — | — | `200` lista de usuarios |
| `GET` | `/api/usuarios/{id}` | `id` (`Long`) | — | `200` usuario, o `404` si no existe |
| `POST` | `/api/usuarios` | — | JSON `Usuario` | `201` usuario creado |
| `PUT` | `/api/usuarios/{id}` | `id` (`Long`) | JSON `Usuario` | `200` usuario actualizado |
| `DELETE` | `/api/usuarios/{id}` | `id` (`Long`) | — | `204` sin cuerpo |

**Body de creación / actualización:**

```json
{
  "nombre": "Ana",
  "apellido": "García",
  "email": "ana@example.com",
  "fechaNacimiento": "1995-03-10"
}
```

---

### Libros

Base: `/api/libros`

| Método | Ruta | Parámetros | Body | Respuesta |
|---|---|---|---|---|
| `GET` | `/api/libros` | — | — | `200` lista de libros |
| `GET` | `/api/libros/{id}` | `id` (`Long`) | — | `200` libro, o `404` si no existe |
| `POST` | `/api/libros` | — | JSON `Libro` | `201` libro creado |
| `PUT` | `/api/libros/{id}` | `id` (`Long`) | JSON `Libro` | `200` libro actualizado |
| `DELETE` | `/api/libros/{id}` | `id` (`Long`) | — | `204` sin cuerpo |

**Body de creación / actualización:**

```json
{
  "titulo": "Clean Code",
  "isbn": "9780132350884",
  "edicion": "1ra",
  "autor": "Robert C. Martin",
  "fechaPublicacion": "2008-08-01"
}
```

---

### Ejemplares

Definidos en `LibroController`.

| Método | Ruta | Parámetros | Body | Respuesta |
|---|---|---|---|---|
| `GET` | `/api/libros/{isbn}/ejemplares` | `isbn` (`String`) | — | `200` ejemplares del libro |
| `GET` | `/api/libros/{isbn}/ejemplares/disponibles` | `isbn` (`String`) | — | `200` ejemplares con `estado = true` |
| `POST` | `/api/libros/{idLibro}/ejemplares` | `idLibro` (`Long`) | JSON `Ejemplar` | `201` ejemplar creado |

**Body de creación:**

```json
{
  "estado": true
}
```

---

### Préstamos

Base: `/api/prestamos`

| Método | Ruta | Parámetros | Body | Respuesta |
|---|---|---|---|---|
| `POST` | `/api/prestamos` | — | `PrestamoRequest` | `201` préstamo creado |
| `GET` | `/api/prestamos/usuario/{idUsuario}` | `idUsuario` (`Long`) | — | `200` préstamos del usuario (recalcula estados) |
| `GET` | `/api/prestamos/libro/{isbn}` | `isbn` (`String`) | — | `200` préstamos de los ejemplares del ISBN |
| `PUT` | `/api/prestamos/{idPrestamo}/devolver` | `idPrestamo` (`Long`) | — | `200` préstamo devuelto |

**Body de registro (`PrestamoRequest`):**

```json
{
  "idUsuario": 1,
  "idEjemplar": 1
}
```

**Respuesta típica de préstamo** (usuario y ejemplar no se serializan; ver [Decisiones técnicas](#decisiones-técnicas)):

```json
{
  "idPrestamo": 1,
  "fechaPrestamo": "2026-08-27",
  "fechaDevolucion": null,
  "fechaLimite": "2026-09-10",
  "estadoPrestamo": "ACTIVO"
}
```

No hay endpoint `GET /api/prestamos` (listado global).

---

## Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza las excepciones.

### `ResourceNotFoundException` → `404 Not Found`

Recurso inexistente (usuario, libro, ejemplar, préstamo). También se usa al crear un usuario con un email ya registrado.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado"
}
```

### `BusinessException` → `400 Bad Request`

Violación de regla de negocio (préstamo pendiente, ejemplar con préstamo activo, préstamo ya devuelto, eliminación con historial, etc.).

```json
{
  "status": 400,
  "error": "Business Rule",
  "message": "El Usuario tiene un prestamo pendiente para devolver"
}
```

### `DataIntegrityViolationException` → `409 Conflict`

Violación de integridad en base de datos (por ejemplo, borrar un registro con datos relacionados).

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "No se puede eliminar el registro porque tiene datos relacionados."
}
```

### Validaciones

Las entidades declaran `@NotNull` y `@Size`. Los controladores no reciben `@Valid` y el manejador global **no** trata `MethodArgumentNotValidException`.

Códigos HTTP usados por la API: `200`, `201`, `204`, `400`, `404`, `409`.

---

## Base de datos

- Motor: **PostgreSQL 16**.
- Base: `biblioteca_db` (definida en `Docker-compose.yml`).
- Esquema: Hibernate con `ddl-auto: update` (`application.yml`). Crea y actualiza tablas al arrancar.

Configuración por variables de entorno referenciadas en `application.yml`:

| Variable | Uso |
|---|---|
| `DB_URI` | URL JDBC |
| `DB_USER` | Usuario |
| `DB_PASSWORD` | Contraseña |
| `DB_DRIVER` | Driver JDBC |
| `DB_PORT` | Puerto publicado de PostgreSQL en Compose |
| `APP_PORT` | Puerto publicado de la API en Compose; puerto interno de Spring (`APP_PORT:8080` por defecto) |

Valores de `.env.example`:

```env
DB_URI=jdbc:postgresql://localhost:5432/biblioteca_db
DB_USER=postgres
DB_PASSWORD=postgres
DB_DRIVER=org.postgresql.Driver

DB_PORT=5432
APP_PORT=8080
```

- `localhost` en `DB_URI` sirve si el backend corre **en el host** y PostgreSQL en Docker.
- Si el backend corre **dentro de Compose**, `DB_URI` debe usar el servicio `postgres`: `jdbc:postgresql://postgres:5432/biblioteca_db`.

Copiar el ejemplo y ajustar:

```bash
cp .env.example .env
```

**No subir `.env` al repositorio.** Está en `.gitignore`.

---

## Datos de prueba

| Dato | Valor |
|---|---|
| Ubicación | `dump/biblioteca_db.dump` |
| Formato | Volcado personalizado de PostgreSQL (`PGDMP`, típico de `pg_dump -Fc`) |
| Propósito | Datos de prueba para la evaluación |

Docker Compose **no restaura este archivo al arrancar**. No hay script de init, el dump no se monta como volumen y `.dockerignore` excluye `dump/`.

Hibernate crea el esquema vacío (`ddl-auto: update`). El volumen `postgres_data` solo conserva lo que ya exista en esa instancia.

Si hace falta cargar el volcado (PostgreSQL ya en marcha, contenedor `biblioteca-postgres`):

```bash
docker cp dump/biblioteca_db.dump biblioteca-postgres:/tmp/biblioteca_db.dump
docker exec biblioteca-postgres pg_restore -U postgres -d biblioteca_db --no-owner /tmp/biblioteca_db.dump
```

Si las tablas ya existen, `pg_restore` puede informar de objetos duplicados. En ese caso valorar `--clean --if-exists` o un volumen limpio (`docker compose -f Docker-compose.yml down -v` elimina el volumen y los datos persistidos).

---

## Docker

Archivo: `Docker-compose.yml`. Tres servicios:

| Servicio | Contenedor | Rol |
|---|---|---|
| `postgres` | `biblioteca-postgres` | PostgreSQL 16, volumen `postgres_data` |
| `backend` | `biblioteca-backend` | API Spring Boot (JAR en `Dockerfile`) |
| `frontend` | `biblioteca-frontend` | React servido por Nginx; build desde `../BibliotecaFrontend` |

Comunicación:

```text
Navegador
   │  http://localhost:5173
   ▼
Frontend (Nginx :80)
   │  estáticos de React
   │  /api → proxy a http://backend:8080
   ▼
Backend (Spring Boot :8080)
   │  JDBC → postgres:5432
   ▼
PostgreSQL
```

Nginx (`BibliotecaFrontend/nginx.conf`) hace proxy de `/api/` al servicio `backend` en la red de Compose. El build del frontend recibe `VITE_API_URL=/api`.

Puertos publicados:

- Frontend: `5173` → `80`
- Backend: `${APP_PORT}` → `8080` (por defecto `8080`)
- PostgreSQL: `${DB_PORT}` → `5432` (por defecto `5432`)

El `Dockerfile` del backend copia `target/biblioteca-0.0.1-SNAPSHOT.jar`. Hay que **compilar el JAR en el host** antes de construir la imagen.

---

## Ejecución del proyecto

### Estructura de repositorios

Compose espera ambos repositorios como hermanos:

```text
Prueba-Tecnica/
├── Biblioteca/            ← este repositorio (API + Compose)
└── BibliotecaFrontend/    ← frontend React
```

El servicio `frontend` usa `context: ../BibliotecaFrontend`.

### Requisitos

- Java 17
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Docker y Docker Compose
- Repositorio `BibliotecaFrontend` en la ruta indicada

### Flujo recomendado para el evaluador

Desde `Biblioteca/`:

1. Copiar variables de entorno:

```bash
cp .env.example .env
```

2. En `.env`, dejar `DB_URI` apuntando al servicio Docker:

```env
DB_URI=jdbc:postgresql://postgres:5432/biblioteca_db
```

3. Compilar el backend:

```bash
./mvnw clean package -DskipTests
```

Windows (PowerShell):

```powershell
.\mvnw.cmd clean package -DskipTests
```

4. Levantar todo:

```bash
docker compose -f Docker-compose.yml up --build
```

5. Acceso:

| Recurso | URL |
|---|---|
| **Frontend** | **http://localhost:5173** |
| API (directa) | http://localhost:8080 |
| API vía Nginx | http://localhost:5173/api |

Detener:

```bash
docker compose -f Docker-compose.yml down
```

### Backend local (opcional)

PostgreSQL en Docker y API en el host. `DB_URI` con `localhost` (como en `.env.example`):

```bash
docker compose -f Docker-compose.yml up -d postgres
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`. El frontend en Docker seguirá llamando a `backend:8080`; para UI local, usar el modo desarrollo del repositorio frontend (`npm run dev`), que hace proxy de `/api` a `http://localhost:8080`.

---

## Frontend

Repositorio separado: `BibliotecaFrontend`.

| Tecnología | Uso |
|---|---|
| React | Interfaz |
| TypeScript | Tipado |
| Vite | Build y desarrollo |
| Axios | Cliente HTTP |
| Nginx | Estáticos y proxy `/api` en Docker |

Pantallas: dashboard, usuarios, libros (con ejemplares), préstamos y página 404.

Con Docker, el frontend consume la API mediante **`/api`** (mismo origen; Nginx reenvía al backend). No hace falta CORS hacia el puerto 8080 en ese modo.

Detalle de ejecución y desarrollo: README de `BibliotecaFrontend`.

---

## Pruebas

### Frontend

En `BibliotecaFrontend`:

```bash
npm run build
```

Compila TypeScript y genera `dist/`. El `Dockerfile` del frontend ejecuta el mismo build. No hay script de tests automatizados en `package.json`.

### Backend

Existe `BibliotecaApplicationTests` (`@SpringBootTest`): comprueba que el contexto de Spring arranca. **No hay tests unitarios ni de integración** de la lógica de negocio.

### Pruebas manuales recomendadas

- CRUD de usuarios y libros (incluido rechazo de email duplicado).
- Crear ejemplares y listar / listar disponibles por ISBN.
- Registrar préstamo y devolverlo; comprobar `estado` del ejemplar.
- Rechazo si el usuario tiene un préstamo pendiente.
- Rechazo si el ejemplar ya tiene un préstamo `ACTIVO`.
- Rechazo al devolver un préstamo ya `DEVUELTO`.
- Transición a `VENCIDO` al listar préstamos del usuario después de `fechaLimite`.
- No eliminar usuario o libro con historial de préstamos.
- Recorrer el frontend en http://localhost:5173 (usuarios, libros, préstamos).

---

## Decisiones técnicas

- **`PrestamoRequest`**: el alta de préstamo recibe `idUsuario` e `idEjemplar` en lugar de la entidad `Prestamo` completa. El resto de endpoints usan entidades JPA.
- **`@JsonIgnore`**: en `Prestamo` (usuario y ejemplar) y en `Ejemplar` (libro y préstamos) para evitar ciclos JSON. Las respuestas de préstamo no incluyen usuario ni ejemplar anidados.
- **Variables de entorno**: credenciales y URL JDBC fuera del código; `.env` ignorado por git.
- **Docker Compose**: un comando orquesta PostgreSQL, API y UI. El frontend se construye desde el repositorio hermano.
- **Repositorios separados**: API en `Biblioteca`, UI en `BibliotecaFrontend`.
- **`fechaLimite`**: distingue la fecha máxima de devolución de la fecha real (`fechaDevolucion`).
- **Dockerfile del backend**: imagen runtime (`eclipse-temurin:17-jre`) que espera el JAR ya empaquetado.

---

## Mejoras futuras

No implementado actualmente:

- DTOs de entrada y salida para todas las entidades.
- Recalcular estados `VENCIDO` al listar préstamos por ISBN (y/o con una tarea programada).
- Validar disponibilidad del ejemplar con el campo `estado`, no solo con préstamo `ACTIVO`.
- `@Valid` en controladores y manejo explícito de errores de validación.
- Email duplicado como regla de negocio (`400`) y/o restricción única en base de datos.
- Pruebas unitarias e de integración de la lógica de negocio.
- Dockerfile multi-stage del backend (compilar el JAR dentro de la imagen).
- Restauración automática del dump al iniciar PostgreSQL.
- Paginación en listados.
- Documentación OpenAPI / Swagger.
- Autenticación y autorización.

---

## Licencia

Proyecto desarrollado con fines educativos y de evaluación técnica.
