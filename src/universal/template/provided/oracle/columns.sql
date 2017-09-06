  SELECT column_name name, 
         nullable,
         RTRIM(data_type) ||
         RTRIM(DECODE(data_type, 'DATE', NULL, 'LONG', NULL, 'NUMBER', DECODE(TO_CHAR(data_precision), NULL, NULL, '('), '(')) ||
         RTRIM(DECODE(data_type, 'DATE', NULL, 'CHAR', data_length, 'VARCHAR2', data_length, 'NUMBER', DECODE(TO_CHAR(data_precision), NULL, NULL, TO_CHAR(data_precision) || ',' || TO_CHAR(data_scale)), 'LONG', NULL,'******ERROR')) ||
         RTRIM(DECODE(data_type,'DATE',NULL,'LONG',NULL, 'NUMBER',DECODE(TO_CHAR(data_precision),NULL,NULL,')'), ')')) data_type
    FROM sys.all_tab_columns t
   WHERE LOWER(?) IN (LOWER(owner || '.' || table_name), LOWER(table_name))  