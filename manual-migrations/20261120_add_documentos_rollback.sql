-- MANUAL ONLY: this removes document metadata, not the files in the configured
-- storage directory. Back up files and metadata before running this rollback.
BEGIN;

DO $rollback$
BEGIN
    IF to_regclass('public.documentos') IS NOT NULL
       AND EXISTS (SELECT 1 FROM public.documentos) THEN
        RAISE EXCEPTION 'Export and reconcile document metadata before rollback; no records were removed';
    END IF;
END
$rollback$;

DROP TABLE IF EXISTS public.documentos;
COMMIT;
