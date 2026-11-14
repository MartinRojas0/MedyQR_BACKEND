# Backend deployment (Render)

Deploy this repository from the `feat/integracion-backend` branch using the
included Render Blueprint (`render.yaml`). The Docker image runs Java 21 and
exposes `/api/health`, which checks the PostgreSQL connection with `SELECT 1`.

Configure these values as Render environment variables; do not commit them:

| Variable | Value |
| --- | --- |
| `DB_URL` | Neon JDBC URL, for example `jdbc:postgresql://<host>/<database>?sslmode=require` |
| `DB_USERNAME` | Database role from Neon |
| `DB_PASSWORD` | Database password from Neon |
| `JWT_SECRET` | Base64-encoded random secret made from at least 32 bytes |
| `MEDIQR_CORS_ALLOWED_ORIGINS` | Exact Vercel origin, e.g. `https://<deployment>.vercel.app` |

Generate a signing secret locally in PowerShell without sharing it:

```powershell
$bytes = [byte[]]::new(32)
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Keep `spring.jpa.hibernate.ddl-auto=none`. Hibernate must not create, update,
or validate the existing Neon schema during startup. The application does not
run migrations or alter Neon tables. `/api/health` must return
`{"status":"ok"}` before using the API.

The patient MVP exposes registration, login, and the authenticated patient's
own profile/history. Other medical modules remain outside this deployment
scope; the existing advanced patient-authorization workflows require a separate
review before those modules are opened to patients.
