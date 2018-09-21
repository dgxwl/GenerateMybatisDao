package com.gene.code;

import java.util.ArrayList;
import java.util.List;

/**
 * 一张数据表的元数据
 * @author Administrator
 *
 */
public class Table {
	private String tableName;  //表名
	private List<Column> columns = new ArrayList<>();  //所有字段
	private List<PrimaryKey> primaryKeys = new ArrayList<>();  //主键名及其序列位置
	
	public Table() {
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<Column> getAllColumns() {
		return columns;
	}

	public void addColumn(Column column) {
		this.columns.add(column);
	}

	public List<PrimaryKey> getAllPrimaryKeys() {
		return primaryKeys;
	}

	public void addPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKeys.add(primaryKey);
	}

	@Override
	public String toString() {
		return "表名: " + tableName + ",\n\t字段: " + columns + ",\n\t主键: " + primaryKeys + "\n";
	}
	
}
