package com.gene.code.columnhandler;

import com.gene.code.Column;
import com.gene.code.Table;
import com.gene.code.columnhandler.strategy.IColumnHandlerStrategy;
import com.gene.code.columnhandler.strategy.MySqlStrategy;
import com.gene.code.columnhandler.strategy.PostgresqlStrategy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class ColumnHandler {

    public static void handleColumns(Table table, Connection conn, DatabaseMetaData databaseMetaData, String driverStr) {
        IColumnHandlerStrategy strategy = getStrategyByDriver(driverStr);
        if (strategy == null) {
            throw new IllegalStateException("未知数据库厂商");
        }

        try {
            ResultSet rs = strategy.getColumnMetaDataResultSet(table, conn, databaseMetaData);
            while (rs.next()) {
                Column column = new Column();

                String columnName = rs.getString("COLUMN_NAME");
                column.setColumnName(columnName);
                String type = rs.getString("TYPE_NAME");
                column.setType(type);
                int dataSize = rs.getInt("COLUMN_SIZE");
                column.setDataSize(dataSize);
                int digits = rs.getInt("DECIMAL_DIGITS");
                column.setDigits(digits);
                int nullable = rs.getInt("NULLABLE");
                column.setNullable(nullable);
                String remarks = rs.getString("REMARKS");
                column.setRemarks(remarks);

                table.addColumn(column);
                table.addColumnNameAndType(columnName, type);
            }

            strategy.getAutoIncrementCols(table, conn);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static IColumnHandlerStrategy getStrategyByDriver(String driverStr) {
        if (driverStr.contains("postgresql")) {
            return new PostgresqlStrategy();
        } else if (driverStr.contains("mysql")) {
            return new MySqlStrategy();
        }
        return null;
    }
}
