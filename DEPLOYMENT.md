# Backend deployment (Render)

Deploy this repository from the `feat/integracion-backend` branch using the
included Render Blueprint (`render.yaml`). The Docker image runs Java 21 and
exposes `/api/health`, which checks the PostgreSQL connection with `SELECT 1`.
The current service is `https://mediqr-backend-tacn.onrender.com`; the frontend
origin to permit is `https://mediqrwebfrontend-qahy.vercel.app`.

Configure these values as Render environment variables; do not commit them:

| Variable | Value |
| --- | --- |
| `DB_URL` | Neon JDBC URL, for example `jdbc:postgresql://<host>/<database>?sslmode=require` |
| `DB_USERNAME` | Database role from Neon |
| `DB_PASSWORD` | Database password from Neon |
| `JWT_SECRET` | Base64-encoded random secret made from at least 32 bytes |
| `MEDIQR_CORS_ALLOWED_ORIGINS` | Exact Vercel origin, e.g. `https://mediqrwebfrontend-qahy.vercel.app` |

Generate a signing secret locally in PowerShell without sharing it:

```powershell
$bytes = [byte[]]::new(32)
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Keep `spring.jpa.hibernate.ddl-auto=none`. At startup, `schema.sql` uses
`CREATE TABLE IF NOT EXISTS` for the MVP tables `usuarios`, `pacientes`,
`historial_clinico`, and `registro_accesos`. It creates missing tables only; it
does not alter existing tables or delete data. Hibernate does not create,
update, or validate the broader Neon schema. Existing tables with incompatible
columns must be reviewed separately. `/api/health` must return
`{"status":"ok"}` before using the API.

On 2026-11-14, the public health endpoint returned HTTP 200 with the expected
CORS header for the Vercel origin. Registration, JWT login, session restore
after reload, and personal-profile update/read were also exercised using
synthetic, non-medical data. The profile authorization bean-name correction
(`@Service("accessControl")`) is included in the current branch; see the root
[`README.md`](README.md) for the diagnosed 500 cause and verified scope.

The patient MVP exposes registration, login, and the authenticated patient's
own personal profile. The history endpoint returns `404` until a row exists;
medical-history persistence was not included in the end-to-end verification.
Other medical modules remain outside this deployment scope; the existing
advanced patient-authorization workflows require a separate review before
those modules are opened to patients.
