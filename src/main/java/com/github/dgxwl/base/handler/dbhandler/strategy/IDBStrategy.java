package com.github.dgxwl.base.handler.dbhandler.strategy;

import com.github.dgxwl.base.entity.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IDBStrategy {

    ResultSet getColumnMetaDataResultSet(Table table, Connection conn, DatabaseMetaData databaseMetaData) throws SQLException;

    void getAutoIncrementCols(Table table, Connection conn) throws SQLException;

    String getShortIntType();

    String getDateTimeType();
}
