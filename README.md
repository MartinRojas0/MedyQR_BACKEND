# MediQR Backend

API REST de MediQR, desarrollada con Java 21 y Spring Boot 4.1.1. La rama de
entrega es `feat/integracion-backend`; la base PostgreSQL de producción está
alojada en Neon y el servicio se publica en Render.

## Alcance verificado

Comprobado el 2026-11-14 con una cuenta y datos sintéticos, sin información
médica real:

- `POST /api/usuarios`: registro y creación automática del perfil PACIENTE.
- `POST /api/auth/login` y `GET /api/auth/me`: login JWT y validación de sesión.
- `GET /api/pacientes/me` y `PUT /api/pacientes/{id}`: lectura y actualización
  del perfil propio; el cambio volvió a leerse desde el backend después de
  recargar la aplicación.
- `GET /api/health`: HTTP 200; ejecuta `SELECT 1` contra la base configurada.
- CORS permite el origen HTTPS de Vercel configurado en Render.

La suite automatizada cubre reglas unitarias y carga del contexto, pero no se
conecta a Neon. Las verificaciones de producción anteriores sí recorrieron el
frontend, Render y los datos del perfil mediante la API.

### Causa del HTTP 500 del perfil

Las expresiones de seguridad de los endpoints consultan el bean SpEL
`@accessControl`. La implementación estaba registrada con el nombre
predeterminado de clase, por lo que Spring no podía resolver ese nombre. El
commit `b7fa1d906c7123b7156674863cc4590796a49aea` corrigió el registro a
`@Service("accessControl")`. Esa corrección está incluida en la rama actual y
la actualización autenticada del perfil ya respondió correctamente en la
prueba de producción.

## Arquitectura

`Controller → Service → Repository → Model/JPA → PostgreSQL`

- `controller/`: endpoints REST y autorización por rol/propietario.
- `service/`: reglas de negocio y persistencia transaccional.
- `repository/`: consultas Spring Data JPA.
- `model/` y `dto/`: entidades y contratos HTTP.
- `security/`: autenticación JWT y usuario actual.
- `authorization/`: controles de acceso a pacientes y usuarios.
- `exception/`: respuestas HTTP de error.

El esquema no se administra con Hibernate (`spring.jpa.hibernate.ddl-auto=none`).
La inicialización SQL existente usa `CREATE TABLE IF NOT EXISTS`; este trabajo
no modificó el esquema ni los datos de Neon. No ejecutar cambios de esquema sin
aprobación.

## Ejecución local

Requisitos: Java 21 y PostgreSQL accesible. En PowerShell, configura variables
de desarrollo en la sesión actual (no uses valores de producción):

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/mediqr'
$env:DB_USERNAME = '<usuario-local>'
$env:DB_PASSWORD = '<password-local>'
$env:JWT_SECRET = '<secreto-local-base64-de-32-bytes-o-mas>'
$env:MEDIQR_CORS_ALLOWED_ORIGINS = 'http://localhost:8000'
.\mvnw.cmd spring-boot:run
```

La API local escucha en `http://localhost:8080`; verifica
`http://localhost:8080/api/health`.

## Variables de entorno

No guardes valores secretos en Git. Configura los secretos directamente en
Render o en el entorno local:

| Variable | Uso |
|---|---|
| `DB_URL` | URL JDBC PostgreSQL de Neon o de desarrollo |
| `DB_USERNAME` | Usuario PostgreSQL |
| `DB_PASSWORD` | Contraseña PostgreSQL |
| `JWT_SECRET` | Secreto Base64 para firmar tokens JWT |
| `MEDIQR_CORS_ALLOWED_ORIGINS` | Orígenes web permitidos, separados por coma |
| `PORT` | Puerto asignado por Render; localmente usa `8080` por defecto |

Render despliega desde `feat/integracion-backend` con el `Dockerfile` y
`render.yaml`. La guía de despliegue y el formato de `JWT_SECRET` están en
[`DEPLOYMENT.md`](DEPLOYMENT.md).

## Pruebas y build

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

La prueba `contextLoads` necesita un `DB_URL` con formato JDBC y un secreto JWT
de prueba. Ejecuta las pruebas con valores descartables y sin Neon:

```powershell
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:5432/mediqr_test'
$env:DB_USERNAME = 'test'
$env:DB_PASSWORD = 'test'
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:SPRING_SQL_INIT_MODE = 'never'
$env:SPRING_JPA_DATABASE_PLATFORM = 'org.hibernate.dialect.PostgreSQLDialect'
.\mvnw.cmd test
```

La suite automatizada no necesita datos ni credenciales de Neon. No conectes
las pruebas locales a la base de producción.

## Funcionalidades pendientes

- Recuperación y cambio de contraseña desde el portal.
- Persistencia de citas, documentos, medicamentos, alertas y notificaciones.
- Historial clínico automático: una cuenta nueva puede recibir `404` en
  `/api/historial-clinico/me` hasta que exista una fila.
- Revisión y pruebas de producción de módulos médicos avanzados y del QR.
- Renovación de sesión; el JWT actual tiene una expiración configurada.

El MVP no debe usarse con información médica real ni para decisiones clínicas.
