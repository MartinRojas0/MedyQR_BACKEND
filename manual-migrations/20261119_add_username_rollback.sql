-- MANUAL ONLY. Roll back the application first and take a database backup.
-- This refuses to drop usernames that have been assigned after migration.
BEGIN;

LOCK TABLE public.usuarios IN SHARE ROW EXCLUSIVE MODE;

DO $rollback$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.usuarios
        WHERE username IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Export or clear assigned usernames before rolling back; no username data was removed';
    END IF;
END
$rollback$;

DROP INDEX IF EXISTS public.ux_usuarios_username_lower;
DROP INDEX IF EXISTS public.ux_usuarios_email_normalized;
ALTER TABLE public.usuarios
    DROP CONSTRAINT IF EXISTS ck_usuarios_username_format;
ALTER TABLE public.usuarios
    DROP COLUMN IF EXISTS username;

COMMIT;
