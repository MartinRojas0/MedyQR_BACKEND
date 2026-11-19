# MediQR Backend

Sistema backend para la gestión de información médica de pacientes, citas, consultas y otros servicios relacionados con atención de salud.

## 1. Descripción del proyecto

MediQR es un sistema desarrollado para centralizar y gestionar información médica de pacientes mediante una API REST.

El proyecto busca facilitar la administración de información como pacientes, citas médicas, consultas, historial clínico, medicamentos, recordatorios y QR de emergencia.

Actualmente el backend se encuentra implementado y validado hasta el módulo de QR de Emergencia.

## 2. Objetivo

Desarrollar un backend organizado y escalable que permita gestionar información médica de manera estructurada, aplicando buenas prácticas de desarrollo, control de versiones y trabajo colaborativo mediante Git y GitHub.

## 3. Tecnologías utilizadas

* Java 22
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* Neon PostgreSQL
* Maven
* Git
* GitHub
* Visual Studio Code

## 4. Arquitectura

El proyecto utiliza una arquitectura por capas:

Controller → Service → Repository → Entity → PostgreSQL

### Controller

Recibe las solicitudes HTTP y expone los endpoints de la API.

### Service

Contiene las reglas de negocio y validaciones principales.

### Repository

Permite la comunicación con la base de datos mediante Spring Data JPA.

### Entity

Representa las tablas de la base de datos.

## 5. Estructura principal

src/
└── main/
└── java/
└── .../
├── controller/
├── service/
├── repository/
├── model/
└── exception/

También se utilizan clases DTO para validar y controlar la información recibida por los endpoints.

## 6. Base de datos

El proyecto utiliza PostgreSQL alojado en Neon.

La aplicación trabaja con:

spring.jpa.hibernate.ddl-auto=validate

Esto permite validar que la estructura de las entidades coincida con la estructura existente de la base de datos sin modificar automáticamente el esquema.

Las credenciales de la base de datos se manejan mediante variables de entorno y no se almacenan en el repositorio.

## 7. Funcionalidades implementadas

### Pacientes

Permite:

* Registrar pacientes.
* Consultar pacientes.
* Actualizar información permitida.
* Validar la existencia del usuario relacionado.
* Manejar errores de validación.

Endpoint principal:

/api/pacientes

### Citas

Permite:

* Registrar citas.
* Consultar citas.
* Actualizar citas.
* Consultar disponibilidad.
* Cambiar el estado de una cita.
* Validar conflictos de horario.

Endpoint principal:

/api/citas

### Consultas médicas

Permite gestionar:

* Motivo de consulta.
* Diagnóstico.
* Tratamiento.
* Observaciones.
* Fecha de consulta.
* Paciente y personal de salud.

Endpoint principal:

/api/consultas-medicas

### Historial clínico

Permite gestionar la información clínica principal del paciente.

Incluye:

* Tipo de sangre.
* Alergias.
* Enfermedades crónicas.
* Cirugías.
* Observaciones.

Endpoint principal:

/api/historial-clinico

### Medicamentos

Permite:

* Registrar medicamentos.
* Actualizar medicamentos.
* Consultar medicamentos.
* Consultar medicamentos por paciente.
* Activar o desactivar medicamentos.
* Validar fechas de inicio y fin.

Endpoint principal:

/api/medicamentos

### Recordatorios de medicamentos

Permite gestionar los horarios y mensajes relacionados con la toma de medicamentos.

Endpoint principal:

/api/recordatorios-medicamentos

### QR de emergencia

Permite gestionar el QR de emergencia asociado a un paciente.

Incluye configuraciones para controlar qué información puede mostrarse:

* DNI.
* Tipo de sangre.
* Alergias.
* Medicamentos.
* Enfermedades.
* Estado activo del QR.

Endpoint principal:

/api/qr-emergencia

## 8. Validaciones y manejo de errores

El backend cuenta con validaciones para evitar información incorrecta o incompleta.

También se manejan diferentes tipos de errores HTTP, entre ellos:

* 400 Bad Request
* 404 Not Found
* 409 Conflict
* 500 Internal Server Error

Se utiliza un manejo global de excepciones para mantener respuestas consistentes.

## 9. Pruebas

Se han desarrollado pruebas unitarias para validar las reglas principales de los servicios.

Las pruebas cubren funcionalidades relacionadas con:

* Pacientes.
* Citas.
* Consultas médicas.
* Historial clínico.
* Medicamentos.
* Recordatorios.
* QR de emergencia.

El proyecto ha sido compilado y probado durante las diferentes etapas de desarrollo.

## 10. Ejecución del proyecto

Para ejecutar el proyecto localmente:

1. Clonar el repositorio.

2. Configurar las variables de entorno necesarias para PostgreSQL.

3. Ejecutar:

./mvnw clean package -DskipTests

4. Iniciar la aplicación:

java -jar target/mediqr-backend-*.jar

La aplicación se ejecuta actualmente en el puerto 8085.

## 11. Control de versiones

El proyecto utiliza Git y GitHub para controlar las versiones del código.

Se utilizan commits descriptivos para registrar los cambios realizados.

Ejemplo:

docs: add project documentation

También se plantea utilizar ramas para desarrollar funcionalidades de manera independiente y posteriormente integrarlas mediante merge.

## 12. Trabajo remoto

El repositorio se encuentra alojado en GitHub.

Esto permite:

* Mantener una copia remota del proyecto.
* Registrar los cambios mediante commits.
* Trabajar con ramas.
* Integrar funcionalidades mediante merge.
* Revisar el historial de modificaciones.
* Facilitar el trabajo colaborativo.

## 13. Documentación

La documentación del proyecto se mantiene dentro del repositorio mediante este archivo README.md.

También se utilizarán evidencias de GitHub y de la terminal para demostrar:

* Commits.
* Creación y uso de ramas.
* Merge.
* Resolución de conflictos.
* Trabajo remoto.
* Historial del proyecto.

## 14. Estado actual

Actualmente el backend cuenta con las siguientes funcionalidades principales:

* Gestión de pacientes.
* Gestión de citas.
* Gestión de consultas médicas.
* Gestión del historial clínico.
* Gestión de medicamentos.
* Gestión de recordatorios.
* Gestión de QR de emergencia.
* Validaciones.
* Manejo global de errores.
* Pruebas unitarias.
* Persistencia en PostgreSQL mediante Neon.

## 15. Próximas funcionalidades

Entre las siguientes etapas del proyecto se consideran:

* Autorizaciones de acceso a información del paciente.
* Juntas médicas.
* Participantes de juntas médicas.
* Registro de accesos.
* Seguridad y autenticación.
* Panel para profesionales de salud.
* Portal para pacientes.
* Integración futura con servicios de mensajería e inteligencia artificial.

Estas funcionalidades serán implementadas progresivamente.

## 16. Repositorio

Repositorio oficial:

https://github.com/MartinRojas0/MedyQR_BACKEND.git
