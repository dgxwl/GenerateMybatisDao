package com.gene.code;
 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
 * 
 * <p>Description: 获取数据库基本信息的工具类</p>
 * 
 * @author qxl
 * @date 2016年7月22日 下午1:00:34
 */
public class DbInfoUtil {
	
	/**
	 * 根据数据库的连接参数，获取指定表的基本信息：字段名、字段类型、字段注释
	 * @param driver 数据库连接驱动
	 * @param url 数据库连接url
	 * @param user	数据库登陆用户名
	 * @param pwd 数据库登陆密码
	 * @param table	表名
	 * @return Map集合
	 */
	public static List<Map<String, String>> getTableInfo(String table){
		List<Map<String, String>> result = new ArrayList<>();
		
		Connection conn = null;		
		DatabaseMetaData dbmd = null;
		
		try {
			conn = DBUtils.getConnection();
			
			dbmd = conn.getMetaData();
			ResultSet resultSet = dbmd.getTables(null, "%", table, new String[] {"TABLE"});
			
			while (resultSet.next()) {
		    	String tableName=resultSet.getString("TABLE_NAME");
		    	System.out.println(tableName);
		    	
		    	if(tableName.equals(table)){
		    		ResultSet rs = conn.getMetaData().getColumns(null, getSchema(conn),tableName.toUpperCase(), "%");
 
		    		while(rs.next()){
		    			System.out.println("字段名："+rs.getString("COLUMN_NAME")+"--字段注释："+rs.getString("REMARKS")+"--字段数据类型："+rs.getString("TYPE_NAME"));
		    			Map<String, String> map = new HashMap<>();
		    			String colName = rs.getString("COLUMN_NAME");
		    			map.put("code", colName);
		    			
		    			String remarks = rs.getString("REMARKS");
		    			if(remarks == null || remarks.equals("")){
		    				remarks = "无备注";
		    			}
		    			map.put("remark",remarks);
		    			
		    			String dbType = rs.getString("TYPE_NAME");
		    			map.put("dbType",dbType);
		    			
		    			map.put("valueType", changeDbType(dbType));
		    			result.add(map);
		    		}
		    	}
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	private static String changeDbType(String dbType) {
		dbType = dbType.toUpperCase();
		switch(dbType){
			case "VARCHAR":
			case "VARCHAR2":
			case "CHAR":
				return "1";
			case "NUMBER":
			case "DECIMAL":
				return "4";
			case "INT":
			case "SMALLINT":
			case "INTEGER":
				return "2";
			case "BIGINT":
				return "6";
			case "DATETIME":
			case "TIMESTAMP":
			case "DATE":
				return "7";
			default:
				return "1";
		}
	}
	
	//其他数据库不需要这个方法 oracle和db2需要
	private static String getSchema(Connection conn) throws Exception {
		String schema;
		schema = conn.getMetaData().getUserName();
		if ((schema == null) || (schema.length() == 0)) {
			throw new Exception("ORACLE数据库模式不允许为空");
		}
		return schema.toUpperCase().toString();
 
	}
 
	public static void main(String[] args) throws IOException {
		String table = "t_user";
		
		List<Map<String, String>> list = getTableInfo(table);
		System.out.println(list);
	}
	
}
