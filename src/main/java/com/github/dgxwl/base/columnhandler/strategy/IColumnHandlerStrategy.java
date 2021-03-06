package com.github.dgxwl.base.columnhandler.strategy;

import com.github.dgxwl.base.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IColumnHandlerStrategy {

    ResultSet getColumnMetaDataResultSet(Table table, Connection conn, DatabaseMetaData databaseMetaData) throws SQLException;

    void getAutoIncrementCols(Table table, Connection conn) throws SQLException;
}
