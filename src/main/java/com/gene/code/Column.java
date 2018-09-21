package com.gene.code;

/**
 * 一个字段的元数据
 * @author Administrator
 *
 */
public class Column {
	private String columnName;  //字段名
	private String type;  //字段类型
	private int dataSize;  //字段长度
	private int digits;  //字段小数点后位数(浮点类型)
	private int nullable;  //0非空, 1可空
	private String remarks;  //字段注释
	
	public Column() {
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getDataSize() {
		return dataSize;
	}

	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public int getDigits() {
		return digits;
	}

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public int getNullable() {
		return nullable;
	}

	public void setNullable(int nullable) {
		this.nullable = nullable;
	}
	
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Override
	public String toString() {
		return "|字段名: " + columnName + ", 类型: " + type + ", 长度: " + dataSize + ", 小数位: " + digits
				+ ", 非空约束: " + nullable + ", 注释: " + remarks + "|";
	}

}
