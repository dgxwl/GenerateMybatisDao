package com.gene.code;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 连接数据库, 拿到所有数据表元数据
 * @author Administrator
 *
 */
public class TableHandler {
	
	private static List<Table> tables = new ArrayList<>();
	
	static {
		Connection conn = null;
		try {
			conn = DBUtils.getConnection();
			//获取数据库元数据
			DatabaseMetaData metaData = conn.getMetaData();
			
			String schema = conn.getMetaData().getUserName().toUpperCase();  //Oracle和DB2需要这个参数
			
			//"%"获取所有表
			ResultSet resultSet = metaData.getTables(null, "%", "%", new String[] {"TABLE"});
			while (resultSet.next()) {
				Table table = new Table();
				//获取表名
				String tableName = resultSet.getString("TABLE_NAME");
				table.setTableName(tableName);
				
				//获得该表字段的元数据
				ResultSet rs = metaData.getColumns(null, schema, tableName, "%");
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
				}
				
				//获取主键元数据
				ResultSet pkSet = metaData.getPrimaryKeys(null, null, tableName);
				while (pkSet.next()) {
					PrimaryKey primaryKey = new PrimaryKey();
					
					String pkName = pkSet.getString("COLUMN_NAME");
					primaryKey.setPkName(pkName);
					int keySeq = pkSet.getInt("KEY_SEQ");
					primaryKey.setSeq(keySeq);
					
					table.addPrimaryKey(primaryKey);
				}
				
				tables.add(table);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtils.closeConnection(conn);
		}
	}
	
	/**
	 * 获得处理好的所有数据表元数据
	 * @return tables 所有数据表元数据
	 */
	public static List<Table> getTables() {
		return tables;
	}
}
