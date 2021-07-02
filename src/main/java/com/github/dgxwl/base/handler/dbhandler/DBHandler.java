package com.github.dgxwl.base.handler.dbhandler;

import com.github.dgxwl.base.entity.Column;
import com.github.dgxwl.util.DBUtils;
import com.github.dgxwl.base.entity.Table;
import com.github.dgxwl.base.handler.dbhandler.strategy.IDBStrategy;
import com.github.dgxwl.base.handler.dbhandler.strategy.MySQLStrategy;
import com.github.dgxwl.base.handler.dbhandler.strategy.PostgreSQLStrategy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Properties;

public class DBHandler {

    private static Properties configs = DBUtils.getConfigs();
    private static String driverStr = configs.getProperty("jdbc.driver");

    public static void handleColumns(Table table, Connection conn, DatabaseMetaData databaseMetaData) {
        IDBStrategy strategy = getStrategyByDriver(driverStr);
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

    public static String getShortIntType() {
        IDBStrategy strategy = getStrategyByDriver(driverStr);
        if (strategy == null) {
            throw new IllegalStateException("未知数据库厂商");
        }
        return strategy.getShortIntType();
    }

    public static String getDateTimeType() {
        IDBStrategy strategy = getStrategyByDriver(driverStr);
        if (strategy == null) {
            throw new IllegalStateException("未知数据库厂商");
        }
        return strategy.getDateTimeType();
    }

    private static IDBStrategy getStrategyByDriver(String driverStr) {
        if (driverStr.contains("postgresql")) {
            return new PostgreSQLStrategy();
        } else if (driverStr.contains("mysql")) {
            return new MySQLStrategy();
        }
        return null;
    }
}
