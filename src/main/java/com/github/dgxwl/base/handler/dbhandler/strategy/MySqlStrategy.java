package com.github.dgxwl.base.handler.dbhandler.strategy;

import com.github.dgxwl.base.entity.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlStrategy implements IDBStrategy {

    @Override
    public ResultSet getColumnMetaDataResultSet(Table table, Connection conn, DatabaseMetaData databaseMetaData) throws SQLException {
        String schema = conn.getMetaData().getUserName().toUpperCase();  //Oracle和DB2需要这个参数
        return databaseMetaData.getColumns(null, schema, table.getTableName(), "%");
    }

    @Override
    public void getAutoIncrementCols(Table table, Connection conn) throws SQLException {
        String tableName = table.getTableName();

        String sql = "SELECT COLUMN_NAME\n" +
                "FROM information_schema.columns\n" +
                "WHERE table_name = ? AND extra = 'auto_increment'";

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
        return "smallint";
    }

    @Override
    public String getDateTimeType() {
        return "datetime";
    }
}
