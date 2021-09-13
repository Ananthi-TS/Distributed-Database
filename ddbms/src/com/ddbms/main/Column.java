package com.ddbms.main;

public class Column {
	
	
	String columnName;
	String columnType;
	Object columnValue;
	int columnSize;
	boolean primaryKey;
	boolean foreignKey;
	boolean defaultFlag;
	boolean autoIncrement; 
	boolean notNullFlag;
	boolean uniqueFlag;
	String defaultValue;
	String foreignKeyTable;
	String foreignKeyColumn;
	
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	public boolean isDefaultFlag() {
		return defaultFlag;
	}
	public void setDefaultFlag(boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}
	public boolean isNotNullFlag() {
		return notNullFlag;
	}
	public void setNotNullFlag(boolean notNullFlag) {
		this.notNullFlag = notNullFlag;
	}
	public boolean isUniqueFlag() {
		return uniqueFlag;
	}
	public void setUniqueFlag(boolean uniqueFlag) {
		this.uniqueFlag = uniqueFlag;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getForeignKeyTable() {
		return foreignKeyTable;
	}
	public void setForeignKeyTable(String foreignKeyTable) {
		this.foreignKeyTable = foreignKeyTable;
	}
	public String getForeignKeyColumn() {
		return foreignKeyColumn;
	}
	public void setForeignKeyColumn(String foreignKeyColumn) {
		this.foreignKeyColumn = foreignKeyColumn;
	}
	public boolean isForeignKey() {
		return foreignKey;
	}
	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}
	public Object getColumnValue() { return columnValue;	}
	public void setColumnValue(Object columnValue) { this.columnValue = columnValue; }

	@Override
	public String toString() {
		return "Column [columnName=" + columnName + ", columnType=" + columnType + ", columnSize=" + columnSize
				+ ", primaryKey=" + primaryKey + ", foreignKey=" + foreignKey + ", defaultFlag=" + defaultFlag
				+ ", autoIncrement=" + autoIncrement + ", notNullFlag=" + notNullFlag + ", uniqueFlag=" + uniqueFlag
				+ ", defaultValue=" + defaultValue + ", foreignKeyTable=" + foreignKeyTable + ", foreignKeyColumn="
				+ foreignKeyColumn + "]";
	}
	
	

}
