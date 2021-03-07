package com.github.dgxwl.base.handler;

import com.github.dgxwl.util.DBUtils;
import com.github.dgxwl.base.handler.dbhandler.DBHandler;
import com.github.dgxwl.base.entity.PrimaryKey;
import com.github.dgxwl.base.entity.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
	private static List<String> entityNames = new ArrayList<>();  //记下含有下划线的表名转化后的实体名

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

			String oneToMany = config.getProperty("one_to_many");
			if (oneToMany == null) {
				oneToMany = "";
			}

			if (!oneToMany.equals("")) {
				String[] pairs = oneToMany.split("[\\s]*[,，][\\s]*");
				for (String pair : pairs) {
					String[] masterAndSlave = pair.split(":");
					slaveMap.put(masterAndSlave[1], masterAndSlave[0]);
				}
			}
			
			//获取数据库元数据
			DatabaseMetaData databaseMetaData = conn.getMetaData();

			//"%"获取所有表
			ResultSet resultSet = databaseMetaData.getTables(null, "%", "%", new String[] {"TABLE"});
			while (resultSet.next()) {
				Table table = new Table();
				//获取表名
				String tableName = resultSet.getString("TABLE_NAME");
				//有指定表名时,跳过不需要的表
				if (!tableNameSet.contains(tableName) && !slaveMap.containsKey(tableName)) {
					continue;
				}
				table.setTableName(tableName);
				if (tableName.contains("_")) {
					entityNames.add(tableName.substring(tableName.indexOf('_') + 1));
				}
				
				//获得该表字段的元数据
				DBHandler.handleColumns(table, conn, databaseMetaData);

				//获取主键元数据
				ResultSet pkSet = databaseMetaData.getPrimaryKeys(null, null, tableName);
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

	/**
	 * 获得处理好的(去掉前缀的)所有带下划线的表名
	 * @return
	 */
	public static List<String> getEntityNames() {
		return entityNames;
	}

}
