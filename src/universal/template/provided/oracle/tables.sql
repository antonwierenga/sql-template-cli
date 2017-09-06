  SELECT ats.owner || '.' || ats.table_name table_name, 
         LISTAGG(ats.column_name, ', ') WITHIN GROUP (ORDER BY column_id) columns
    FROM all_tab_columns ats 
   WHERE ats.owner NOT IN ('SYS', 'WMSYS', 'SYSTEM', 'DBSNMP', 'OUTLN')
     AND LOWER(ats.owner || '.' || ats.table_name) LIKE '%' || ? || '%'
GROUP BY ats.owner, ats.table_name