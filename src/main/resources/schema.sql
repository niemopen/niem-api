
CREATE EXTENSION IF NOT EXISTS unaccent;

-- slugify function:
-- 1. trim trailing and leading whitespaces from text
-- 2. remove accents (diacritic signs) from a given text
-- 3. lowercase unaccented text
-- 4. replace non-alphanumeric (excluding hyphen, underscore) with a hyphen
-- 5. trim leading and trailing hyphens
CREATE OR REPLACE FUNCTION public.slugify( value text ) RETURNS text
LANGUAGE plpgsql STRICT IMMUTABLE AS '
BEGIN
  RETURN trim(BOTH ''-'' FROM regexp_replace(lower(public.unaccent(trim(value))), ''[^a-z0-9\\-_\.]+'', ''-'', ''gi''));
END;
';
