# MediQR Backend

API REST de MediQR, desarrollada con Java 21 y Spring Boot 4.1.1. La rama de
entrega es `feat/integracion-backend`; la base PostgreSQL de producción está
alojada en Neon y el servicio se publica en Render.

## Alcance verificado localmente

La rama `feat/integracion-final` integra APIs autenticadas de perfil, historial,
contacto de emergencia, QR, documentos privados y citas. Las cuentas nuevas se
registran como `PACIENTE`; las operaciones de personal de salud mantienen sus
controles de rol y autorización.

- `.\mvnw.cmd -B -ntp test`: 135 pruebas pasaron con H2.
- El backend arrancó con el perfil `demo`; `/api/health` respondió `{"status":"ok"}`.
- En H2 local se verificaron login por usuario/correo, persistencia de perfil e
  historial, aislamiento (lectura cruzada 403), QR/PNG/ficha pública mínima/
  revocación y subida/listado/descarga/eliminación de documentos.
- La prueba de integración de citas cubre registro, consulta, reprogramación,
  cancelación, disponibilidad, aislamiento y persistencia tras nuevo login.

No se ejecutaron migraciones, no hubo PostgreSQL desechable disponible y no se
verificaron ni modificaron Neon, Render o Vercel. H2 no sustituye una validación
de compatibilidad con PostgreSQL.

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

## Ejecución local de demostración

Requisitos: Java 21 o superior. El perfil `demo` usa H2 en archivo
(`data/mediqr-demo`) y almacenamiento local en `data/documents`; no requiere
PostgreSQL ni conecta a Neon. En PowerShell, desde la raíz de este repositorio:

```powershell
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:MEDIQR_DEMO_JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:SPRING_PROFILES_ACTIVE = 'demo'
$env:SERVER_PORT = '8081'
$env:MEDIQR_CORS_ALLOWED_ORIGINS = 'http://127.0.0.1:8810,http://localhost:8810'
.\mvnw.cmd -Plocal-demo spring-boot:run
```

La API local escucha en `http://127.0.0.1:8081`; verifica
`http://127.0.0.1:8081/api/health`. Para el frontend, compila con
`MEDIQR_API_URL=http://127.0.0.1:8081/api` y sirve `dist/` por HTTP en el
origen permitido. Los archivos H2 y documentos están bajo `data/`, ignorado
por Git; no compartas esos archivos.

## Variables de entorno

No guardes valores secretos en Git. Configura los secretos directamente en
Render o en el entorno local:

| Variable | Uso |
|---|---|
| `DB_URL` | URL JDBC PostgreSQL del perfil normal |
| `DB_USERNAME` | Usuario PostgreSQL del perfil normal |
| `DB_PASSWORD` | Contraseña PostgreSQL del perfil normal |
| `JWT_SECRET` | Secreto Base64 JWT del perfil normal |
| `MEDIQR_DEMO_JWT_SECRET` | Secreto Base64 JWT requerido por el perfil local `demo` |
| `MEDIQR_DOCUMENT_STORAGE_DIR` | Ruta local privada de documentos; obligatoria fuera del perfil demo |
| `MEDIQR_CORS_ALLOWED_ORIGINS` | Orígenes web permitidos, separados por coma |
| `PORT` / `SERVER_PORT` | Puerto Render / puerto local de Spring Boot |

Render despliega desde `feat/integracion-backend` con el `Dockerfile` y
`render.yaml`. La guía de despliegue y el formato de `JWT_SECRET` están en
[`DEPLOYMENT.md`](DEPLOYMENT.md).

## Pruebas y build

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

La suite automatizada usa H2 de pruebas aislada y no necesita PostgreSQL ni
credenciales de Neon:

```powershell
.\mvnw.cmd -B -ntp test
```

Este resultado no es una prueba de compatibilidad real con PostgreSQL.

## Pendientes antes de publicar

- Probar las migraciones manuales en una base PostgreSQL desechable y revisar su
  rollback; PostgreSQL, Docker y `psql` no estaban disponibles en el entorno.
- Configurar y probar almacenamiento privado persistente para documentos. La
  ruta local no es durable en Render y `render.yaml` no crea un disco.
- Revisar los cambios y obtener autorización antes de aplicar migraciones o
  desplegar. No se modificaron Neon, Render ni Vercel.
- La H2 de demostración no incluye personal de salud; la reserva real de citas
  necesita profesionales activos y horarios disponibles.
- Cambio/recuperación de contraseña y renovación de sesión no están implementados.
- Una cuenta nueva puede no tener fila de historial hasta que guarde su primer
  registro; el endpoint puede devolver `404` antes de eso.

El MVP no debe usarse con información médica real ni para decisiones clínicas.
