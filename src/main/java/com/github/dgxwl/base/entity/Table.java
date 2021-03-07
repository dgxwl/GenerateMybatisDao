package com.github.dgxwl.base.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一张数据表的元数据
 * @author Administrator
 *
 */
public class Table {
	private String tableName;  //表名
	private List<Column> columns = new ArrayList<>();  //所有字段
	private Map<String, String> columnNameAndType = new HashMap<>();  //字段名和类型映射
	private List<PrimaryKey> primaryKeys = new ArrayList<>();  //主键名及其序列位置
	private Set<String> autoIncrementCols = new HashSet<>();
	private List<Table> slaves = new ArrayList<>();  //从表

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

	public String getTypeByColumnName(String columnName) {
		return columnNameAndType.get(columnName);
	}

	public void addColumnNameAndType(String columnName, String columnType) {
		this.columnNameAndType.put(columnName, columnType);
	}

	public List<PrimaryKey> getAllPrimaryKeys() {
		return primaryKeys;
	}

	public void addPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKeys.add(primaryKey);
	}

	public Set<String> getAllAutoIncrementCols() {
		return autoIncrementCols;
	}

	public void addAutoIncrementCols(String autoIncrementCol) {
		this.autoIncrementCols.add(autoIncrementCol);
	}

	public List<Table> getAllSlaves() {
		return slaves;
	}

	public void addSlave(Table slaves) {
		this.slaves.add(slaves);
	}

	@Override
	public String toString() {
		return "表名: " + tableName + ",\n\t字段: " + columns + ",\n\t主键: " + primaryKeys + "\n\t从表: " + slaves;
	}

}
