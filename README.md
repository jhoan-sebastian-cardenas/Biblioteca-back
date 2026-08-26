# Biblioteca — API REST de Gestión de Biblioteca

Aplicación backend para la gestión de una biblioteca: usuarios, libros, ejemplares y préstamos. Desarrollada como prueba técnica para un perfil de **Desarrollador Full Stack Junior**.

El proyecto expone una **API REST** construida con **Java 17** y **Spring Boot**, persistiendo datos en **PostgreSQL** mediante **Spring Data JPA** y **Hibernate**. El despliegue puede realizarse con **Docker Compose**, levantando tanto la base de datos como el backend.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura del backend](#arquitectura-del-backend)
- [Flujo de una petición](#flujo-de-una-petición)
- [Modelo de dominio](#modelo-de-dominio)
- [Decisión de diseño: fecha límite del préstamo](#decisión-de-diseño-fecha-límite-del-préstamo)
- [Reglas de negocio](#reglas-de-negocio)
- [Manejo de excepciones](#manejo-de-excepciones)
- [API REST](#api-rest)
- [Serialización JSON y relaciones bidireccionales](#serialización-json-y-relaciones-bidireccionales)
- [Base de datos](#base-de-datos)
- [Docker](#docker)
- [Datos de prueba](#datos-de-prueba)
- [Frontend](#frontend)
- [Pruebas](#pruebas)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Mejoras futuras](#mejoras-futuras)

---

## Descripción general

El sistema permite:

- Registrar y administrar **usuarios** de la biblioteca.
- Gestionar el **catálogo de libros**.
- Administrar **ejemplares** (copias físicas de un libro).
- Registrar **préstamos**, consultarlos y procesar **devoluciones**.
- Actualizar automáticamente el **estado del préstamo** según fechas y devoluciones.

Cada capa del backend tiene una responsabilidad clara: los controladores reciben peticiones HTTP, los servicios aplican reglas de negocio y los DAO coordinan la persistencia con la base de datos.

---

## Tecnologías utilizadas

### Backend

| Tecnología | Versión / detalle |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.4 |
| Spring Web | Incluido en `spring-boot-starter-web` |
| Spring Data JPA | Incluido en `spring-boot-starter-data-jpa` |
| Hibernate | Gestionado por Spring Boot JPA |
| Bean Validation | `spring-boot-starter-validation` |
| PostgreSQL Driver | Dependencia runtime en `pom.xml` |
| Maven | Gestión de dependencias y build |

### Base de datos

| Tecnología | Versión |
|---|---|
| PostgreSQL | 16 (imagen Docker) |

### Infraestructura

| Herramienta | Estado |
|---|---|
| Docker Compose | Implementado (PostgreSQL + backend) |
| Dockerfile | Implementado |

### Frontend

| Tecnología | Estado |
|---|---|
| React | Pendiente de implementación |

---

## Estructura del proyecto

```text
Biblioteca/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/biblioteca/
│   │   │       ├── BibliotecaApplication.java
│   │   │       ├── Controller/
│   │   │       │   ├── LibroController.java
│   │   │       │   ├── PrestamoController.java
│   │   │       │   └── UsuarioController.java
│   │   │       ├── DTO/
│   │   │       │   └── PrestamoRequest.java
│   │   │       ├── Entitys/
│   │   │       │   ├── Ejemplar.java
│   │   │       │   ├── Libro.java
│   │   │       │   ├── Prestamo.java
│   │   │       │   └── Usuario.java
│   │   │       ├── Exeptions/
│   │   │       │   ├── BusinessException.java
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       └── Model/
│   │   │           ├── DAO/
│   │   │           │   ├── EjemplarDAO.java
│   │   │           │   ├── LibroDAO.java
│   │   │           │   ├── PrestamoDAO.java
│   │   │           │   └── UsuarioDAO.java
│   │   │           └── Service/
│   │   │               ├── EjemplarService.java
│   │   │               ├── LibroService.java
│   │   │               ├── PrestamosService.java
│   │   │               └── UsuarioService.java
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│       └── java/
│           └── com/example/biblioteca/
│               └── BibliotecaApplicationTests.java
│
├── .env.example
├── .gitignore
├── Docker-compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## Arquitectura del backend

El backend sigue una arquitectura en capas:

### Controller

Responsabilidades:

- Recibir solicitudes HTTP.
- Recibir parámetros de ruta, query y cuerpos JSON.
- Delegar la lógica de negocio al **Service**.
- Devolver respuestas HTTP con los códigos de estado correspondientes.

Controladores actuales: `UsuarioController`, `LibroController`, `PrestamoController`.

### Service

Responsabilidades:

- Implementar reglas de negocio del dominio.
- Validar condiciones antes de persistir o modificar datos.
- Coordinar operaciones entre entidades y repositorios.

Servicios actuales: `UsuarioService`, `LibroService`, `EjemplarService`, `PrestamosService`.

### DAO (Repository)

Interfaces que extienden `JpaRepository` de Spring Data JPA.

Responsabilidades:

- Persistir y recuperar entidades.
- Ejecutar consultas derivadas (`findBy...`, `existsBy...`) y consultas personalizadas con `@Query`.

DAO actuales: `UsuarioDAO`, `LibroDAO`, `EjemplarDAO`, `PrestamoDAO`.

### Entitys

Representan el modelo persistente y las relaciones entre tablas de la base de datos.

Entidades actuales: `Usuario`, `Libro`, `Ejemplar`, `Prestamo`.

### DTO

Objetos de transferencia para desacoplar parcialmente el contrato de la API del modelo de persistencia.

DTO actual: `PrestamoRequest` (entrada para registrar préstamos).

### Exeptions

Manejo centralizado de errores de negocio y recursos no encontrados.

Clases actuales: `BusinessException`, `ResourceNotFoundException`, `GlobalExceptionHandler`.

---

## Flujo de una petición

```text
Cliente (Postman / Frontend)
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
Spring Data JPA
   │
   ▼
Hibernate
   │
   ▼
PostgreSQL
```

Cuando un cliente realiza una petición HTTP, el **Controller** la recibe y la delega al **Service** correspondiente. El servicio aplica las reglas de negocio necesarias y, si procede, invoca el **DAO** para leer o escribir en la base de datos. Spring Data JPA traduce esas operaciones a consultas SQL que Hibernate ejecuta sobre PostgreSQL.

Si ocurre un error de negocio o un recurso no existe, `GlobalExceptionHandler` intercepta la excepción y devuelve una respuesta JSON estructurada.

---

## Modelo de dominio

### Usuario

Representa a una persona registrada en la biblioteca.

| Campo | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `Long` | Identificador autogenerado |
| `nombre` | `String` | Nombre del usuario |
| `apellido` | `String` | Apellido del usuario |
| `email` | `String` | Correo electrónico (único en creación) |
| `fechaNacimiento` | `LocalDate` | Fecha de nacimiento |

**Relación:**

```text
Usuario 1 ─────── N Prestamo
```

---

### Libro

Representa un título del catálogo bibliográfico.

| Campo | Tipo | Descripción |
|---|---|---|
| `idLibro` | `Long` | Identificador autogenerado |
| `titulo` | `String` | Título del libro |
| `isbn` | `String` | ISBN del libro |
| `edicion` | `String` | Edición |
| `autor` | `String` | Autor |
| `fechaPublicacion` | `LocalDate` | Fecha de publicación |

**Relación:**

```text
Libro 1 ─────── N Ejemplar
```

Un mismo libro puede tener múltiples copias físicas (ejemplares).

**Ejemplo conceptual:**

```text
Libro:
  ISBN = 9781234567890
  Título = Clean Code

Ejemplares:
  Ejemplar 1 → estado = true  (disponible)
  Ejemplar 2 → estado = true  (disponible)
  Ejemplar 3 → estado = false (no disponible)
```

---

### Ejemplar

Representa una copia física de un libro.

| Campo | Tipo | Descripción |
|---|---|---|
| `idEjemplar` | `Long` | Identificador autogenerado |
| `estado` | `Boolean` | `true` = disponible, `false` = no disponible |
| `libro` | `Libro` | Libro al que pertenece el ejemplar |

**Relaciones:**

```text
Ejemplar N ─────── 1 Libro

Ejemplar 1 ─────── N Prestamo
```

---

### Prestamo

Representa el préstamo de un ejemplar a un usuario.

| Campo | Tipo | Descripción |
|---|---|---|
| `idPrestamo` | `Long` | Identificador autogenerado |
| `fechaPrestamo` | `LocalDate` | Fecha en que se registra el préstamo |
| `fechaLimite` | `LocalDate` | Fecha máxima esperada para devolver el ejemplar |
| `fechaDevolucion` | `LocalDate` | Fecha real de devolución (`null` mientras no se devuelva) |
| `estadoPrestamo` | `Enum` | `ACTIVO`, `DEVUELTO` o `VENCIDO` |
| `usuario` | `Usuario` | Usuario que realiza el préstamo |
| `ejemplar` | `Ejemplar` | Ejemplar prestado |

**Estados definidos:**

```text
ACTIVO
DEVUELTO
VENCIDO
```

**Relaciones:**

```text
Prestamo N ─────── 1 Usuario

Prestamo N ─────── 1 Ejemplar
```

---

## Decisión de diseño: fecha límite del préstamo

El enunciado original presenta una ambigüedad respecto al campo `fecha_devolucion`: puede interpretarse como la **fecha límite esperada** para devolver el ejemplar o como la **fecha real** en que el usuario lo devolvió.

Para resolver esa ambigüedad de forma consistente, se incorporó el campo `fechaLimite` con la siguiente semántica:

```text
fechaPrestamo
      │
      ▼
Inicio del préstamo


fechaLimite
      │
      ▼
Fecha máxima esperada para devolver el ejemplar


fechaDevolucion
      │
      ▼
Fecha real en que el usuario devuelve el ejemplar
```

Cuando un préstamo está **ACTIVO**, `fechaDevolucion` es `null`.

**Lógica de estados implementada:**

```text
                    ┌──────────────────┐
                    │ Préstamo creado  │
                    └────────┬─────────┘
                             │
                             ▼
                          ACTIVO
                             │
               ┌─────────────┴─────────────┐
               │                           │
               ▼                           ▼
       Se devuelve                 Supera fecha límite
               │                           │
               ▼                           ▼
           DEVUELTO                    VENCIDO
```

**Implementación actual:**

- Al registrar un préstamo, `fechaLimite` se calcula como `fechaPrestamo + 14 días` (constante `DIASPRESTAMO` en `PrestamosService`).
- El método privado `actualizarEstado` evalúa el estado según:
  - Si `fechaDevolucion != null` → `DEVUELTO`
  - Si la fecha actual es posterior a `fechaLimite` → `VENCIDO`
  - En caso contrario → `ACTIVO`
- Esta actualización se ejecuta al **listar préstamos por usuario** (`GET /api/prestamos/usuario/{idUsuario}`).

---

## Reglas de negocio

Las siguientes reglas están implementadas en los servicios del proyecto.

### Disponibilidad del ejemplar al registrar un préstamo

Antes de crear un préstamo, el sistema verifica que el ejemplar **no tenga un préstamo con estado `ACTIVO`**:

```text
Ejemplar sin préstamo ACTIVO
        │
        ▼
Puede crearse el préstamo
```

Si el ejemplar ya tiene un préstamo activo, se lanza `BusinessException` con el mensaje: `"El Ejemplar tiene un prestamo ACTIVO"`.

Adicionalmente:

- Al registrar el préstamo, el ejemplar pasa a `estado = false`.
- Al devolver el préstamo, el ejemplar vuelve a `estado = true`.
- La consulta de ejemplares disponibles por ISBN filtra por `estado = true`.

---

### Restricción de préstamos pendientes por usuario

Un usuario **no puede registrar un nuevo préstamo** mientras tenga un préstamo **pendiente de devolver** (`fechaDevolucion IS NULL`).

```text
Usuario 1
   │
   └── Préstamo pendiente (ACTIVO o VENCIDO)
```

Esta regla cubre tanto préstamos activos como vencidos que aún no han sido devueltos. Si el usuario tiene un préstamo pendiente, el sistema rechaza la operación con el mensaje: `"El Usuario tiene un prestamo pendiente para devolver"`.

---

### Registro de préstamo

Al registrar un préstamo:

- Se valida que el usuario exista.
- Se valida que el ejemplar exista.
- Se asigna `fechaPrestamo = fecha actual`.
- Se calcula `fechaLimite = fechaPrestamo + 14 días`.
- Se establece `estadoPrestamo = ACTIVO`.
- `fechaDevolucion` queda en `null`.
- El ejemplar se marca como no disponible (`estado = false`).

---

### Devolución de préstamo

Al devolver un préstamo:

- Se permite devolver préstamos en estado `ACTIVO` o `VENCIDO`.
- Si el préstamo ya fue devuelto (`DEVUELTO`), se rechaza con: `"El Prestamo ya fue DEVUELTO"`.
- Se asigna `fechaDevolucion = fecha actual`.
- Se actualiza `estadoPrestamo = DEVUELTO`.
- El ejemplar vuelve a estar disponible (`estado = true`).

---

### Actualización automática del estado

Al consultar los préstamos de un usuario, el sistema recalcula y persiste el estado de cada préstamo según las fechas actuales. Esto permite reflejar la transición a `VENCIDO` cuando se supera `fechaLimite`.

> **Nota:** la actualización automática de estados al listar préstamos por ISBN (`GET /api/prestamos/libro/{isbn}`) **no está implementada** todavía.

---

### Otras validaciones

| Regla | Ubicación | Descripción |
|---|---|---|
| Email único | `UsuarioService` | No se permite registrar dos usuarios con el mismo email |
| Usuario existente | `PrestamosService` | El usuario debe existir para crear un préstamo |
| Ejemplar existente | `PrestamosService` | El ejemplar debe existir para crear un préstamo |
| Libro existente | `EjemplarService` | El libro debe existir para crear un ejemplar |

---

## Manejo de excepciones

El proyecto implementa un manejador global de excepciones con `@RestControllerAdvice` en `GlobalExceptionHandler`.

### `ResourceNotFoundException` → `404 Not Found`

Se utiliza cuando un recurso no existe (usuario, libro, ejemplar, préstamo, etc.).

**Ejemplo de respuesta:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado"
}
```

### `BusinessException` → `400 Bad Request`

Se utiliza cuando se viola una regla de negocio (préstamo pendiente, ejemplar no disponible, préstamo ya devuelto, etc.).

**Ejemplo de respuesta:**

```json
{
  "status": 400,
  "error": "Business Rule",
  "message": "El Usuario tiene un prestamo pendiente para devolver"
}
```

---

## API REST

Base URL por defecto: `http://localhost:8080`

> Los controladores utilizan `@Controller` y devuelven `ResponseEntity`, lo que permite serializar las respuestas en JSON.

---

### Usuarios

Base path: `/api/usuarios`

#### `GET /api/usuarios`

Lista todos los usuarios registrados.

**Respuesta:** `200 OK`

```json
[
  {
    "idUsuario": 1,
    "nombre": "Ana",
    "apellido": "García",
    "email": "ana@example.com",
    "fechaNacimiento": "1995-03-10",
    "prestamo": null
  }
]
```

---

#### `GET /api/usuarios/{id}`

Obtiene un usuario por su identificador.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del usuario |

**Respuesta:** `200 OK`

---

#### `POST /api/usuarios`

Crea un nuevo usuario.

**Request body:**

```json
{
  "nombre": "Ana",
  "apellido": "García",
  "email": "ana@example.com",
  "fechaNacimiento": "1995-03-10"
}
```

**Respuesta:** `201 Created`

---

#### `PUT /api/usuarios/{id}`

Actualiza los datos de un usuario existente.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del usuario |

**Request body:** mismo formato que `POST`.

**Respuesta:** `200 OK`

---

#### `DELETE /api/usuarios/{id}`

Elimina un usuario por su identificador.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del usuario |

**Respuesta:** `204 No Content`

---

### Libros

Base path: `/api/libros`

#### `GET /api/libros`

Lista todos los libros del catálogo.

**Respuesta:** `200 OK`

```json
[
  {
    "idLibro": 1,
    "titulo": "Clean Code",
    "isbn": "9780132350884",
    "edicion": "1ra",
    "autor": "Robert C. Martin",
    "fechaPublicacion": "2008-08-01",
    "ejemplar": null
  }
]
```

---

#### `GET /api/libros/{id}`

Obtiene un libro por su identificador numérico.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del libro |

**Respuesta:** `200 OK`

---

#### `POST /api/libros`

Registra un nuevo libro en el catálogo.

**Request body:**

```json
{
  "titulo": "Clean Code",
  "isbn": "9780132350884",
  "edicion": "1ra",
  "autor": "Robert C. Martin",
  "fechaPublicacion": "2008-08-01"
}
```

**Respuesta:** `201 Created`

---

#### `PUT /api/libros/{id}`

Actualiza los datos de un libro existente.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del libro |

**Request body:** mismo formato que `POST`.

**Respuesta:** `200 OK`

---

#### `DELETE /api/libros/{id}`

Elimina un libro por su identificador.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | ID del libro |

**Respuesta:** `204 No Content`

---

### Ejemplares

La gestión de ejemplares está integrada en `LibroController`.

#### `GET /api/libros/{isbn}/ejemplares`

Lista todos los ejemplares asociados a un libro identificado por su ISBN.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `isbn` | `String` | ISBN del libro |

**Respuesta:** `200 OK`

```json
[
  {
    "idEjemplar": 1,
    "estado": true
  }
]
```

---

#### `GET /api/libros/{isbn}/ejemplares/disponibles`

Lista únicamente los ejemplares disponibles (`estado = true`) de un libro por ISBN.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `isbn` | `String` | ISBN del libro |

**Respuesta:** `200 OK`

---

#### `POST /api/libros/{idLibro}/ejemplares`

Crea un nuevo ejemplar asociado a un libro.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `idLibro` | `Long` | ID del libro |

**Request body:**

```json
{
  "estado": true
}
```

**Respuesta:** `201 Created`

---

### Préstamos

Base path: `/api/prestamos`

#### `POST /api/prestamos`

Registra un nuevo préstamo.

**Request body** (`PrestamoRequest`):

```json
{
  "idUsuario": 1,
  "idEjemplar": 1
}
```

**Respuesta:** `201 Created`

```json
{
  "idPrestamo": 1,
  "fechaPrestamo": "2026-08-26",
  "fechaDevolucion": null,
  "fechaLimite": "2026-09-09",
  "estadoPrestamo": "ACTIVO"
}
```

---

#### `GET /api/prestamos/usuario/{idUsuario}`

Consulta todos los préstamos de un usuario. Actualiza automáticamente el estado de cada préstamo antes de devolver la respuesta.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `Long` | ID del usuario |

**Respuesta:** `200 OK`

---

#### `GET /api/prestamos/libro/{isbn}`

Consulta todos los préstamos asociados a los ejemplares de un libro (por ISBN).

| Parámetro | Tipo | Descripción |
|---|---|---|
| `isbn` | `String` | ISBN del libro |

**Respuesta:** `200 OK`

---

#### `PUT /api/prestamos/{idPrestamo}/devolver`

Registra la devolución de un préstamo activo o vencido.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `idPrestamo` | `Long` | ID del préstamo |

**Respuesta:** `200 OK`

```json
{
  "idPrestamo": 1,
  "fechaPrestamo": "2026-08-26",
  "fechaDevolucion": "2026-09-02",
  "fechaLimite": "2026-09-09",
  "estadoPrestamo": "DEVUELTO"
}
```

---

## Serialización JSON y relaciones bidireccionales

Las entidades mantienen relaciones bidireccionales que podrían generar referencias circulares infinitas durante la serialización JSON:

```text
Usuario → Prestamos → Usuario → ...

Ejemplar → Prestamos → Ejemplar → ...
```

**Solución actual:** se utiliza `@JsonIgnore` en relaciones inversas de las entidades `Prestamo` y `Ejemplar` para evitar ciclos en las respuestas JSON.

**DTO parcial:** existe `PrestamoRequest` para la entrada de préstamos. El resto de endpoints siguen exponiendo entidades JPA directamente.

**Mejora futura recomendada:**

- Implementar DTOs completos de entrada y salida para todas las entidades.
- Separar el modelo de persistencia del contrato REST.

---

## Base de datos

### Motor

PostgreSQL 16.

### Configuración

La configuración se realiza mediante **variables de entorno**, referenciadas en `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URI}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: ${DB_DRIVER}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: ${APP_PORT:8080}
```

Hibernate crea y actualiza el esquema automáticamente (`ddl-auto: update`).

### Variables de entorno

Copiar el archivo de ejemplo y ajustar los valores según el entorno:

```bash
cp .env.example .env
```

**`.env.example`:**

```env
DB_URI=jdbc:postgresql://localhost:5432/biblioteca_db
DB_USER=postgres
DB_PASSWORD=postgres
DB_DRIVER=org.postgresql.Driver

DB_PORT=5432
APP_PORT=8080
```

| Variable | Descripción |
|---|---|
| `DB_URI` | URL JDBC de PostgreSQL |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `DB_DRIVER` | Driver JDBC (`org.postgresql.Driver`) |
| `DB_PORT` | Puerto expuesto de PostgreSQL en Docker |
| `APP_PORT` | Puerto expuesto de la API |

> **Nota para Docker:** cuando el backend se ejecuta dentro de Docker Compose, `DB_URI` debe apuntar al servicio `postgres` (por ejemplo: `jdbc:postgresql://postgres:5432/biblioteca_db`). Para ejecución local con PostgreSQL en Docker, usar `localhost`.

El archivo `.env` está incluido en `.gitignore` para evitar subir credenciales al repositorio.

---

## Docker

### Componentes

| Archivo | Descripción |
|---|---|
| `Dockerfile` | Imagen del backend basada en `eclipse-temurin:17-jre` |
| `Docker-compose.yml` | Orquestación de PostgreSQL y backend |

### Servicios

**PostgreSQL** — base de datos con volumen persistente.

**Backend** — aplicación Spring Boot empaquetada como JAR, dependiente de PostgreSQL.

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/biblioteca-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> El Dockerfile espera que el JAR ya esté compilado en `target/`. Es necesario ejecutar `mvnw package` antes de construir la imagen.

### Despliegue con Docker Compose

```bash
# 1. Configurar variables de entorno
cp .env.example .env

# 2. Compilar el backend
./mvnw clean package -DskipTests

# 3. Levantar servicios
docker compose up --build
```

En Windows (PowerShell):

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose up --build
```

Para detener los servicios:

```bash
docker compose down
```

La API quedará disponible en `http://localhost:8080` (o el puerto definido en `APP_PORT`).

---

## Datos de prueba

**Pendiente de implementación.**

La prueba técnica requiere incluir un archivo `.dump` con datos de prueba (por ejemplo, `database/biblioteca.dump`). Este archivo **todavía no está presente** en el repositorio.

Cuando se incorpore, se documentará aquí:

- Ubicación del archivo.
- Contenido del volcado.
- Comandos para restaurarlo en PostgreSQL.

---

## Frontend

**Pendiente de implementación.**

Se planea una aplicación **React** que consuma esta API REST e incluya:

- Gestión de usuarios.
- Gestión de libros.
- Consulta de ejemplares disponibles.
- Registro y consulta de préstamos.
- Devolución de préstamos.

Cuando el frontend esté disponible, se documentará aquí la tecnología utilizada (JavaScript/TypeScript, herramienta de build, etc.) y la variable de entorno para la URL del backend.

---

## Pruebas

### Pruebas automatizadas

El proyecto incluye únicamente una prueba de contexto de Spring Boot (`BibliotecaApplicationTests`), que verifica que el contexto de la aplicación carga correctamente. **No existen pruebas unitarias ni de integración** con JUnit/Mockito para la lógica de negocio.

### Pruebas manuales (Postman)

Los siguientes escenarios pueden validarse manualmente contra la API implementada:

**Usuarios**

- [ ] Crear usuario
- [ ] Listar usuarios
- [ ] Consultar usuario por ID
- [ ] Actualizar usuario
- [ ] Eliminar usuario
- [ ] Validar rechazo de email duplicado

**Libros**

- [ ] Crear libro
- [ ] Listar libros
- [ ] Consultar libro por ID
- [ ] Actualizar libro
- [ ] Eliminar libro

**Ejemplares**

- [ ] Crear ejemplar asociado a un libro
- [ ] Consultar ejemplares por ISBN
- [ ] Consultar ejemplares disponibles por ISBN

**Préstamos**

- [ ] Registrar préstamo
- [ ] Consultar préstamos por usuario
- [ ] Consultar préstamos por ISBN de libro
- [ ] Validar que un ejemplar no pueda tener múltiples préstamos activos
- [ ] Validar que un usuario no pueda registrar un préstamo con uno pendiente
- [ ] Validar transición a estado VENCIDO al superar `fechaLimite`
- [ ] Devolver un préstamo activo o vencido
- [ ] Validar respuestas de error estructuradas (`400`, `404`)

---

## Cómo ejecutar el proyecto

### Requisitos previos

- Java 17
- Maven (o usar el wrapper incluido: `mvnw` / `mvnw.cmd`)
- Docker y Docker Compose (opcional, recomendado)
- Postman u otra herramienta HTTP (para probar la API)

### Opción A — Docker Compose (recomendado)

```bash
cp .env.example .env
./mvnw clean package -DskipTests
docker compose up --build
```

Ajustar `DB_URI` en `.env` a `jdbc:postgresql://postgres:5432/biblioteca_db` para ejecución en contenedor.

### Opción B — Ejecución local

```bash
# 1. Configurar variables de entorno
cp .env.example .env

# 2. Levantar solo PostgreSQL
docker compose up -d postgres

# 3. Compilar y ejecutar la aplicación
./mvnw spring-boot:run
```

En Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.

### Verificación rápida

```bash
curl http://localhost:8080/api/usuarios
```

---

## Mejoras futuras

Funcionalidades y mejoras técnicas planificadas que **aún no están implementadas**:

- [ ] Archivo `.dump` con datos de prueba.
- [ ] Frontend en React consumiendo la API.
- [ ] DTOs completos para todas las entidades (entrada y salida).
- [ ] Actualización automática de estados al listar préstamos por ISBN.
- [ ] Pruebas unitarias con JUnit y Mockito.
- [ ] Paginación en endpoints de listado.
- [ ] Documentación OpenAPI / Swagger.
- [ ] Validaciones adicionales en capa de controlador (`@Valid`).
- [ ] Autenticación y autorización.
- [ ] Dockerfile multi-stage (compilar dentro de Docker sin depender de un JAR preconstruido).
- [ ] Tarea programada para actualizar estados `VENCIDO` sin depender de consultas del usuario.

---

## Licencia

Proyecto desarrollado con fines educativos y de evaluación técnica.
