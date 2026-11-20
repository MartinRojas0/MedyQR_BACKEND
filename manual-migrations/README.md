# Manual database migrations

Scripts in this directory are outside Spring's classpath and never run
automatically. `schema.sql` does not apply or alter these changes. Do not run
any script against Neon or production without explicit authorization.
The default production profile has SQL initialization disabled; provision the
base schema separately and review these migrations before deployment.

## Pre-deployment sequence

1. Back up the target PostgreSQL database and test all three scripts on a
   disposable PostgreSQL database with the same schema.
2. Check for duplicate normalized email and username values and invalid
   non-empty usernames. `20261119_add_username.sql` repeats these checks while
   holding a table lock and aborts without applying partial changes.
3. Apply `20261119_add_username.sql`. Existing accounts retain a `NULL`
   username and continue to log in by email. New usernames are normalized to
   lowercase by the application and are unique case-insensitively.
4. Inspect existing `qr_emergencia` columns, patient references, and active
   duplicates. Apply `20261119_add_qr_emergency_contact.sql` only after review.
   It adds optional emergency-contact fields and the token hash/expiry/revocation
   columns. Existing raw UUID QR records are retained, but the new public API
   does not resolve them. New tokens are random 256-bit values; only their
   SHA-256 hashes are stored.
5. Before applying `20261120_add_documentos.sql`, verify that
   `public.pacientes` and `public.citas` exist with `BIGINT` primary keys.
   The migration stores metadata only; uploaded bytes remain in
   `app.documents.storage-dir`. Configure durable private storage before
   deploying the document feature. The current Render configuration does not
   declare a persistent disk, so its default local storage is not durable.
6. Deploy only after all required migrations succeed and document storage
   persistence has been configured and tested.

The document rollback refuses to drop non-empty metadata. Export and reconcile
metadata and file bytes first; the SQL rollback never deletes files from the
configured storage directory.

The username format is 3–30 ASCII lowercase letters, digits, periods,
underscores, or hyphens. The migration also adds normalized unique email
indexing to prevent case-variant duplicates. Existing blanks/duplicates must
be resolved manually; the script never selects which account to modify.

## Rollback

Roll back the application before the database. The rollback scripts are
manual and transactional. They stop if they would discard assigned usernames,
issued QR tokens, saved emergency contacts, or non-empty document metadata:

- `20261119_add_username_rollback.sql`
- `20261119_add_qr_emergency_contact_rollback.sql`
- `20261120_add_documentos_rollback.sql`

Back up and reconcile the relevant data before retrying a refused rollback.
The QR rollback keeps the `qr_emergencia` table and any legacy columns. Restore
legacy QR token values from the database backup if a migration was already
used to issue secure tokens.
