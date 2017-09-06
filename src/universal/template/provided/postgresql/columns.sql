  SELECT column_name as name,
         is_nullable as nullable,
         data_type || CASE WHEN character_maximum_length IS NOT NULL THEN '(' || character_maximum_length || ')'
                           WHEN numeric_precision IS NOT NULL THEN '(' || numeric_precision || ',' || numeric_scale || ')'
                           ELSE NULL
                      END data_type
    FROM information_schema.columns
   WHERE LOWER(?) IN (LOWER(table_schema || '.' || table_name), LOWER(table_name))
ORDER BY ordinal_position