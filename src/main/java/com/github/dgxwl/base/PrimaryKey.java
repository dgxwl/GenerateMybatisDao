package com.github.dgxwl.base;

/**
 * 主键
 * @author Administrator
 *
 */
public class PrimaryKey {
	private String pkName;  //主键名
	private String pkType;  //主键类型
	private int seq;  //序列位置
	
	public PrimaryKey() {
	}

	public String getPkName() {
		return pkName;
	}

	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public String getPkType() {
		return pkType;
	}

	public void setPkType(String pkType) {
		this.pkType = pkType;
	}

	@Override
	public String toString() {
		return "|主键名: " + pkName + ", 位置: " + seq + "|";
	}
}
