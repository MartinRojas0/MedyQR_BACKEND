-- MANUAL ONLY: do not copy this file into src/main/resources or run it at startup.
-- Review/authorize the migration and take a backup before applying it to any database.
BEGIN;

LOCK TABLE public.usuarios IN SHARE ROW EXCLUSIVE MODE;

ALTER TABLE public.usuarios
    ADD COLUMN IF NOT EXISTS username VARCHAR(30);

DO $migration$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.usuarios
        WHERE email IS NULL OR btrim(email) = ''
    ) THEN
        RAISE EXCEPTION 'Blank email addresses must be resolved before applying the migration';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.usuarios
        GROUP BY lower(btrim(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate normalized email addresses must be resolved before adding the unique index';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.usuarios
        WHERE username IS NOT NULL
          AND (username <> lower(btrim(username))
               OR username !~ '^[a-z0-9._-]{3,30}$')
    ) THEN
        RAISE EXCEPTION 'Existing usernames must be normalized and valid before applying the migration';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.usuarios
        WHERE username IS NOT NULL
        GROUP BY lower(btrim(username))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate normalized usernames must be resolved before adding the unique index';
    END IF;
END
$migration$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_email_normalized
    ON public.usuarios (lower(btrim(email)));

DO $migration$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_usuarios_username_format'
          AND conrelid = 'public.usuarios'::regclass
    ) THEN
        ALTER TABLE public.usuarios
            ADD CONSTRAINT ck_usuarios_username_format
            CHECK (username IS NULL OR username ~ '^[a-z0-9._-]{3,30}$');
    END IF;
END
$migration$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_username_lower
    ON public.usuarios (lower(username))
    WHERE username IS NOT NULL;

COMMIT;
