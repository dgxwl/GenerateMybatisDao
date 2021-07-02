package com.github.dgxwl.base.handler.db.strategy;

import com.github.dgxwl.base.entity.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgreSQLStrategy implements IDBStrategy {

    @Override
    public ResultSet getColumnMetaDataResultSet(Table table, Connection conn, DatabaseMetaData databaseMetaData) {
        try {
            String tableName = table.getTableName();

            String sql = "SELECT\n" +
                    "COLUMN_NAME,\n" +
                    "CASE WHEN sch.udt_name = 'int2' THEN 'SMALLINT'\n" +
                    "WHEN sch.udt_name = 'int4' THEN 'INTEGER'\n" +
                    "WHEN sch.udt_name = 'int8' THEN 'BIGINT'\n" +
                    "ELSE upper(sch.udt_name) END TYPE_NAME,\n" +
                    "COALESCE(sch.character_maximum_length,0) COLUMN_SIZE,\n" +
                    "COALESCE(sch.numeric_scale,0) DECIMAL_DIGITS,\n" +
                    "CASE WHEN sch.is_nullable = 'YES' THEN 1 ELSE 0 END NULLABLE,\n" +
                    "col_description(pa.attrelid, pa.attnum) REMARKS\n" +
                    "FROM information_schema.columns sch\n" +
                    "JOIN pg_class pc\n" +
                    "ON sch.table_name = pc.relname\n" +
                    "JOIN pg_attribute pa\n" +
                    "ON sch.column_name = pa.attname and pc.oid = pa.attrelid\n" +
                    "WHERE sch.table_schema='public' AND sch.table_name=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            return ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void getAutoIncrementCols(Table table, Connection conn) throws SQLException {
        String tableName = table.getTableName();

        String sql = "select COLUMN_NAME\n" +
                "FROM information_schema.columns\n" +
                "WHERE table_schema='public' AND TABLE_NAME = ? AND column_default LIKE 'nextval%'";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, tableName);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            table.addAutoIncrementCols(columnName);
        }
    }

    @Override
    public String getShortIntType() {
        return "int2";
    }

    @Override
    public String getDateTimeType() {
        return "timestamp(6)";
    }
}
