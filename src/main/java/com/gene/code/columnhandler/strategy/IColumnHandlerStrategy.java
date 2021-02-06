package com.gene.code.columnhandler.strategy;

import com.gene.code.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IColumnHandlerStrategy {

    ResultSet getColumnMetaDataResultSet(Table table, Connection conn, DatabaseMetaData databaseMetaData) throws SQLException;

    void getAutoIncrementCols(Table table, Connection conn) throws SQLException;
}
