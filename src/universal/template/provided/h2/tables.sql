  SELECT tbs.table_schema || '.' || tbs.table_name table_name, 
         string_agg(column_name, ', ') columns
    FROM information_schema.tables tbs 
    JOIN information_schema.columns cls ON cls.table_name = tbs.table_name 
     AND cls.table_schema = tbs.table_schema
   WHERE lower(tbs.table_schema) not in ('information_schema', 'pg_catalog', 'performance_schema')
     AND LOWER(tbs.table_schema || '.' || tbs.table_name) LIKE '%' || ? || '%'
GROUP BY tbs.table_schema, tbs.table_name