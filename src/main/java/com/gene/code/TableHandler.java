package com.gene.code;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 连接数据库, 拿到所有数据表元数据
 * @author Administrator
 *
 */
public class TableHandler {

	private static List<Table> tables = new ArrayList<>();  //所有主表

	static {
		Connection conn = null;
		try {
			conn = DBUtils.getConnection();
			Properties config = DBUtils.getConfigs();

			Map<String, Table> tableNameMap = new HashMap<>();
			Map<String, String> slaveMap = new HashMap<>();

			String tablesStr = config.getProperty("tables");
			if (tablesStr == null) {
				tablesStr = "";
			}
			Set<String> tableNameSet = new HashSet<>(Arrays.asList(tablesStr.split("[\\s]*[,，][\\s]*")));
			tableNameSet.remove("");

			String slaveTableStr = config.getProperty("slave_tables");
			if (slaveTableStr == null) {
				slaveTableStr = "";
			}

			String[] pairs = slaveTableStr.split("[\\s]*[,，][\\s]*");
			for (String pair : pairs) {
				String[] masterAndSlave = pair.split(":");
				slaveMap.put(masterAndSlave[1], masterAndSlave[0]);
			}

			//获取数据库元数据
			DatabaseMetaData metaData = conn.getMetaData();

			String schema = conn.getMetaData().getUserName().toUpperCase();  //Oracle和DB2需要这个参数

			//"%"获取所有表
			ResultSet resultSet = metaData.getTables(null, "%", "%", new String[] {"TABLE"});
			while (resultSet.next()) {
				Table table = new Table();
				//获取表名
				String tableName = resultSet.getString("TABLE_NAME");
				//有指定表名时,跳过不需要的表
				if (!tableNameSet.contains(tableName) && !slaveMap.containsKey(tableName)) {
					continue;
				}
				table.setTableName(tableName);

				//获得该表字段的元数据
				ResultSet rs;
				boolean isPostgresql = isPostgresql(config);
				if (isPostgresql) {
					String sql = "SELECT\n" +
							"COLUMN_NAME,\n" +
							"CASE WHEN udt_name = 'int2' THEN 'SMALLINT'" +
							"WHEN udt_name = 'int4' THEN 'INTEGER'" +
							"WHEN udt_name = 'int8' THEN 'BIGINT'" +
							"ELSE upper(udt_name) END TYPE_NAME,\n" +
							"COALESCE(character_maximum_length,0) COLUMN_SIZE,\n" +
							"COALESCE(numeric_scale,0) DECIMAL_DIGITS,\n" +
							"CASE WHEN is_nullable = 'YES' THEN 1 ELSE 0 END NULLABLE,\n" +
							"'' REMARKS\n" +
							"FROM information_schema.columns \n" +
							"WHERE table_schema='public' AND table_name=?";
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setString(1, tableName);
					rs = ps.executeQuery();
				} else {
					rs = metaData.getColumns(null, schema, tableName, "%");
				}
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

				//获取主键元数据
				ResultSet pkSet = metaData.getPrimaryKeys(null, null, tableName);
				while (pkSet.next()) {
					PrimaryKey primaryKey = new PrimaryKey();

					String pkName = pkSet.getString("COLUMN_NAME");
					primaryKey.setPkName(pkName);
					int keySeq = pkSet.getInt("KEY_SEQ");
					primaryKey.setSeq(keySeq);
					String pkType = table.getTypeByColumnName(pkName);
					primaryKey.setPkType(pkType);

					table.addPrimaryKey(primaryKey);
				}

				tableNameMap.put(tableName, table);
			}

			for (Table table : tableNameMap.values()) {
				String tableName = table.getTableName();
				if (tableNameSet.contains(tableName)) {
					tables.add(table);
				}
				if (slaveMap.containsKey(tableName)) {
					tableNameMap.get(slaveMap.get(tableName)).addSlave(table);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtils.closeConnection(conn);
		}
	}

	/**
	 * 获得处理好的所有数据主表元数据
	 * @return tables 所有数据主表元数据
	 */
	public static List<Table> getTables() {
		return tables;
	}

	private static boolean isPostgresql(Properties prop) {
		String driverStr = prop.getProperty("jdbc.driver");
		return driverStr != null && driverStr.contains("postgresql");
	}
}
