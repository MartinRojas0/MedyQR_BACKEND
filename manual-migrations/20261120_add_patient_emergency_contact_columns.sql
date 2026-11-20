-- MANUAL ONLY: back up the target PostgreSQL database and apply after approval.
-- Adds nullable profile fields; does not update or remove existing patient rows.
BEGIN;

LOCK TABLE public.pacientes IN SHARE ROW EXCLUSIVE MODE;

ALTER TABLE public.pacientes
    ADD COLUMN IF NOT EXISTS contacto_emergencia_nombre VARCHAR(120),
    ADD COLUMN IF NOT EXISTS contacto_emergencia_parentesco VARCHAR(80),
    ADD COLUMN IF NOT EXISTS contacto_emergencia_telefono VARCHAR(30);

COMMIT;
