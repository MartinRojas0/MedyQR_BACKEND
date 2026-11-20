# Backend deployment (Render)

The local integration work is on `feat/integracion-final`. Do not deploy it
until review and explicit authorization. When authorized, use the included
Render Blueprint (`render.yaml`). The Docker image runs Java 21 and
exposes `/api/health`, which checks the PostgreSQL connection with `SELECT 1`.
The supplied service URL is `https://mediqr-backend-tacn.onrender.com`; the
frontend origin to permit is `https://mediqrwebfrontend-qahy.vercel.app`. Neither
was accessed or changed during this local integration.

Configure these values as Render environment variables; do not commit them:

| Variable | Value |
| --- | --- |
| `DB_URL` | Neon JDBC URL, for example `jdbc:postgresql://<host>/<database>?sslmode=require` |
| `DB_USERNAME` | Database role from Neon |
| `DB_PASSWORD` | Database password from Neon |
| `JWT_SECRET` | Base64-encoded random secret made from at least 32 bytes |
| `MEDIQR_CORS_ALLOWED_ORIGINS` | Exact Vercel origin, e.g. `https://mediqrwebfrontend-qahy.vercel.app` |
| `MEDIQR_PUBLIC_APP_URL` | Public frontend origin used by QR links, e.g. `https://mediqrwebfrontend-qahy.vercel.app` |

Generate a signing secret locally in PowerShell without sharing it:

```powershell
$bytes = [byte[]]::new(32)
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Keep `spring.jpa.hibernate.ddl-auto=none` and
`spring.sql.init.mode=never`. The application does not execute `schema.sql` or
manual migrations at startup. Provision and validate the base schema separately;
review existing columns and constraints before applying any migration.
`/api/health` must return `{"status":"ok"}` before using the API.

On 2026-11-14, the public health endpoint returned HTTP 200 with the expected
CORS header for the Vercel origin. Registration, JWT login, session restore
after reload, and personal-profile update/read were also exercised using
synthetic, non-medical data. The profile authorization bean-name correction
(`@Service("accessControl")`) is included in the current branch; see the root
[`README.md`](README.md) for the diagnosed 500 cause and verified scope.

The integrated branch includes email-or-username login, patient-owned
appointments, emergency-contact persistence, secure QR issuance/public
resolution, and private document metadata/storage. In the Render Free
configuration, document routes and storage components are not registered
unless `MEDIQR_DOCUMENT_STORAGE_DIR` is set. Leave it unset: Free has no
persistent disk, and documents must not be stored on its ephemeral filesystem.
Other API features do not require this variable.

The document feature must remain disabled until durable private storage is
available. All three manual migrations still require review and explicit
authorization before application to the target database:

- [`username migration`](manual-migrations/20261119_add_username.sql)
- [`QR and emergency-contact migration`](manual-migrations/20261119_add_qr_emergency_contact.sql)
- [`document metadata migration`](manual-migrations/20261120_add_documentos.sql)

The migrations have not been run, and the current deployed Render service has
not been verified against these local changes. The integration was tested with
isolated H2 only; H2 compatibility mode does not establish PostgreSQL
compatibility. Back up the database, test the scripts on a disposable
PostgreSQL database matching the actual schema, and inspect the existing data
before requesting production authorization. The
included rollback scripts deliberately refuse to discard usernames, issued QR
tokens, saved emergency contacts, or non-empty document metadata. Render's
current free service has no persistent disk configured; do not enable document
routes there until a durable, private storage target is provided.

The patient API exposes registration, login, the authenticated patient's own
personal profile and clinical history, emergency-contact operations,
patient-owned appointment operations, QR management/public resolution, and
private document operations. QR public responses intentionally omit patient
identifiers, birth date, contact information, and address. Local API smoke
checks and isolated H2 tests passed; none were run against the deployed Neon
database.

After applying approved migrations and deploying, verify `/api/health`, CORS
from the exact Vercel origin, registration/login with synthetic accounts,
patient-owned profile/history/contact reads and writes, QR issue/revoke/public
resolution, and cross-patient denial. Never use Neon production for automated
integration tests.
