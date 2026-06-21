# Backend - Sistema de Inventario

API REST construida con Spring Boot para autenticación, gestión de categorías, productos, pedidos, usuarios, auditoría y control de stock del Sistema de Inventario.

## Tecnologías principales

- Java 21
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- Spring Validation
- Hibernate Validator
- JJWT 0.12.3
- springdoc-openapi 2.0.4
- H2 Database
- PostgreSQL driver
- Gradle 9.4.1
- JUnit 5 / Spring Boot Test

## Requisitos

- JDK 21
- Gradle Wrapper incluido en el proyecto

## Perfiles disponibles

### `dev`

Perfil por defecto.

- Base de datos H2 en memoria
- Swagger habilitado
- Consola H2 habilitada
- `ddl-auto=update`
- logging detallado

### `prod`

- Base de datos PostgreSQL
- Swagger deshabilitado
- Consola H2 deshabilitada
- `ddl-auto=validate`
- secretos y credenciales vía variables de entorno

## Cómo levantar el proyecto

1. Abrir una terminal dentro de `sistema-inventario`
2. Ejecutar:

```bash
./gradlew bootRun
```

En Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

La API quedará disponible en:

```text
http://localhost:8080
```

## Endpoints útiles en desarrollo

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

## Variables y configuración importante

Archivo base:

- `src/main/resources/application.properties`

Perfiles:

- `application-dev.properties`
- `application-prod.properties`

Propiedades relevantes:

- `server.port`
- `jwt.expiration`
- `jwt.refresh-expiration-days`
- `jwt.refresh-cleanup-cron`
- `JWT_SECRET`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Cómo abrirlo en IntelliJ IDEA

1. Abrir IntelliJ IDEA
2. Seleccionar `Open`
3. Elegir la carpeta `sistema-inventario`
4. Esperar a que importe el proyecto Gradle
5. Verificar que el SDK sea Java 21
6. Ejecutar la aplicación desde:
   - la clase principal de Spring Boot, o
   - la tarea Gradle `bootRun`

Recomendado:

- habilitar auto-import de Gradle
- usar el perfil `dev` para trabajo local

## Cómo abrirlo en Eclipse

1. Abrir Eclipse
2. Importar como proyecto Gradle:
   - `File > Import > Gradle > Existing Gradle Project`
3. Seleccionar la carpeta `sistema-inventario`
4. Confirmar que Eclipse use Java 21
5. Ejecutar como:
   - `Spring Boot App`, si tienes Spring Tools
   - o tarea Gradle `bootRun`

## Cómo abrirlo en VS Code

1. Abrir VS Code
2. Seleccionar `File > Open Folder`
3. Elegir la carpeta `sistema-inventario`
4. Instalar extensiones recomendadas:
   - Extension Pack for Java
   - Spring Boot Extension Pack
5. Verificar que VS Code detecte Java 21
6. Ejecutar:

```powershell
.\gradlew.bat bootRun
```

o iniciar la clase principal desde el panel de Run and Debug.

## Pruebas

Ejecutar:

```bash
./gradlew test
```

En Windows PowerShell:

```powershell
.\gradlew.bat test
```

## Estructura general

- `config`: seguridad, JWT, OpenAPI, auditoría
- `controller`: endpoints REST
- `dto`: contratos de entrada y salida
- `exception`: manejo global de errores
- `model`: entidades y enums
- `repository`: acceso a datos
- `service`: reglas de negocio
- `src/main/resources`: configuración y datos semilla
- `src/test`: pruebas unitarias e integración

## Notas

- El perfil por defecto es `dev`.
- En desarrollo se usa H2 en memoria, por lo que los datos no persisten al reiniciar.
- El proyecto incluye soporte para PostgreSQL pensando en producción.
