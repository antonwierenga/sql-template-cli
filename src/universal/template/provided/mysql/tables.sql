  SELECT CONCAT(tbs.table_schema, '.', tbs.table_name) table_name, group_concat(column_name separator ', ') columns
    FROM information_schema.tables tbs 
    JOIN information_schema.columns cls ON cls.table_name = tbs.table_name 
     AND cls.table_schema = tbs.table_schema
   WHERE lower(tbs.table_schema) not in ('information_schema', 'sys', 'mysql', 'performance_schema')
     AND (LOWER(CONCAT(tbs.table_schema, '.', tbs.table_name)) LIKE CONCAT('%', ?, '%'))
GROUP BY tbs.table_schema, tbs.table_name