-- MANUAL ONLY. Roll back the application first and back up the database.
-- This refuses to remove any newly issued QR token or stored emergency contact.
BEGIN;

LOCK TABLE public.pacientes IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE public.qr_emergencia IN SHARE ROW EXCLUSIVE MODE;

DO $rollback$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.qr_emergencia
        WHERE token_hash IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Revoke and archive all issued secure QR tokens before rollback; no data was removed';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.pacientes
        WHERE contacto_emergencia_nombre IS NOT NULL
           OR contacto_emergencia_parentesco IS NOT NULL
           OR contacto_emergencia_telefono IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Export and clear saved emergency contacts before rollback; no data was removed';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.qr_emergencia
        WHERE token IS NULL
    ) THEN
        RAISE EXCEPTION 'Restore legacy QR token values from the backup before rollback; no data was removed';
    END IF;
END
$rollback$;

DROP INDEX IF EXISTS public.ux_qr_emergencia_active_patient;
DROP INDEX IF EXISTS public.ux_qr_emergencia_token_hash;
ALTER TABLE public.qr_emergencia
    ALTER COLUMN token SET NOT NULL;
ALTER TABLE public.qr_emergencia
    DROP CONSTRAINT IF EXISTS fk_qr_emergencia_paciente;
ALTER TABLE public.qr_emergencia
    DROP COLUMN IF EXISTS token_hash,
    DROP COLUMN IF EXISTS expires_at,
    DROP COLUMN IF EXISTS revoked_at;
ALTER TABLE public.pacientes
    DROP COLUMN IF EXISTS contacto_emergencia_nombre,
    DROP COLUMN IF EXISTS contacto_emergencia_parentesco,
    DROP COLUMN IF EXISTS contacto_emergencia_telefono;

COMMIT;
