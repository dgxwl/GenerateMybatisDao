package com.github.dgxwl.util;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
	private static String driver;
	private static String url;
	private static String user;
	private static String password;
	
	private static int initialSize;
	private static int maxActive;
	
	private static BasicDataSource ds = null;

	private static Properties config = new Properties();
	static {
		try (InputStream in = DBUtils.class.getClassLoader().getResourceAsStream("base.properties")) {
			config.load(in);
			
			driver = config.getProperty("jdbc.driver");
			url = config.getProperty("jdbc.url");
			user = config.getProperty("jdbc.user");
			password = config.getProperty("jdbc.password");
			initialSize = Integer.parseInt(config.getProperty("initialSize"));
			maxActive = Integer.parseInt(config.getProperty("maxActive"));
			//创建BasicDataSource对象
			ds = new BasicDataSource();
			//设置6个属性
			ds.setDriverClassName(driver);
			ds.setUrl(url);
			ds.setUsername(user);
			ds.setPassword(password);
			ds.setInitialSize(initialSize);
			ds.setMaxActive(maxActive);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取配置项
	 */
	public static Properties getConfigs() {
		return config;
	}
	
	/**
	 * 获取连接池中connection对象
	 */
	public static Connection getConnection() {
		try {
			Connection conn = ds.getConnection();
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 归还连接池连接对象
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		if (conn == null) {
			return;
		}
		try {
			conn.setAutoCommit(true);
			//此处的close()是归还
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 事务回滚
	 * @param conn
	 */
	public static void rollback(Connection conn) {
		if (conn == null) {
			return;
		}
		try {
			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
