package com.ddbms.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ddbms.main.Column;
import com.ddbms.main.ERD;
import com.ddbms.main.SQLdump;
import com.ddbms.main.Table;


public class Create {
	
    private static String localRootDirectory;

    private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {
        }
    }
	
	public boolean createProcess(String query) throws Exception {
		
		if((query!=null) && !(query.trim().equals(""))) {
			String type="";
			try {
				 type=query.split("\\s+")[1];
			} catch (Exception e) {
				throw new Exception("Syntax error...Please check the sql query");
			}
			
			if(type.equalsIgnoreCase("table")) {
				
				return createTable(query);
				
			}else if(type.equalsIgnoreCase("database")) {
				
				return createDatabase(query);
				
			}else if(type.equalsIgnoreCase(("erd"))){
				String database="";
				try {
					database=query.trim().split("\\s+")[2].replaceAll(";", "");
				} catch (Exception e) {
					throw new Exception("Syntax error...Please check the sql query");
				}
				ERD erd=new ERD();
				return erd.getERD(database);
			}else if(type.equalsIgnoreCase("dump")) {
				String database="";
				try {
					database=query.trim().split("\\s+")[2].replaceAll(";", "");
				} catch (Exception e) {
					throw new Exception("Syntax error...Please check the sql query");
				}
				SQLdump dump=new SQLdump();
				return dump.generateSQLDump(database);
			}
		}
		
		return false;
	}
	
	private boolean createDatabase(String query) throws Exception {
		
		String tableinfo[]=query.trim().split("\\s+");
		if( (tableinfo!=null) & (tableinfo.length==3) ) {
			String database=tableinfo[2].replaceAll(";", "");
			readProperties();
			String path=localRootDirectory+"\\"+database;
			File fileDirectory=new File(path);
			if(fileDirectory.exists()) {
				throw new Exception("Database "+database+" already exist");
			}else {
				fileDirectory.mkdir();
				System.out.println("Database created successfully");
				return true;
			}
		}
		return false;
	}

	private boolean createTable(String query) throws Exception {
		query = query.replaceAll(";", "");
		if(query.indexOf("(")!=-1) {
			long charCount1 = query.chars().filter(ch -> ch == '(').count();
			long charCount2 = query.codePoints().filter(ch -> ch == ')').count();
			
			if(charCount1==charCount2) {
				String statement1=query.substring(0,query.indexOf("("));
				String statement2=query.substring(query.indexOf("("), query.lastIndexOf(")"));
				
				String[] tableArr=statement1.trim().split("\\s+");
				if((tableArr!=null) && (tableArr.length==3)) {
					
					String[] tableInfo=tableArr[2].split("\\.");
					
					if((tableInfo!=null) && (tableInfo.length==2)) {
						String databaseName=tableInfo[0];
						String tableName=tableInfo[1];		
						Table table=new Table(tableName,databaseName);
						statement2=statement2.replaceAll("[^a-zA-Z,0-9]", " ");
						List<String> columns=Arrays.asList(statement2.split(","));
						Map<String,Column> tableColumns=new HashMap<>();
						
						for(String column:columns) {
							
							//if(StringUtils.containsIgnoreCase(column, "PRIMARY KEY")) {
								if(column.contains("PRIMARY KEY")) {	
								String[] colTmp=column.trim().split("\\s+");
								if( ((null!=colTmp) && (colTmp.length==3) && (tableColumns.containsKey(colTmp[2])))){
									String columnName=colTmp[2];
									Column columnObj=tableColumns.get(columnName);
									columnObj.setPrimaryKey(true);
									tableColumns.put(columnName, columnObj);
								}else {
									throw new Exception("Syntax error");
								}
								
							}
							//else if(StringUtils.containsIgnoreCase(column, "FOREIGN KEY")) {
							else if(column.contains("FOREIGN KEY"))	{
								String[] colTmp=column.split("\\s+");
								if( ((null!=colTmp) && (colTmp.length==6) && (tableColumns.containsKey(colTmp[2])))){
									String columnName=colTmp[2];
									Column columnObj=tableColumns.get(columnName);
									columnObj.setForeignKey(true);
									columnObj.setForeignKeyTable(colTmp[4]);
									columnObj.setForeignKeyColumn(colTmp[5]);
									tableColumns.put(columnName, columnObj);
								}else {
									throw new Exception("Syntax error");
								}
								
							}else {
								Column columnObject=new Column();
								String[] colTmp=column.trim().split("\\s+");
								if(colTmp.length>=2) {
									String columnName=colTmp[0];
									String columnType=colTmp[1];
									columnObject.setColumnName(columnName);
									columnObject.setColumnType(columnType);
									if(colTmp.length==3) {
										columnObject.setColumnSize(Integer.parseInt(colTmp[2].trim()));
									}
									tableColumns.put(columnName, columnObject);
								}else {
									throw new Exception("Syntax error");
								}
							}
						}
						try {
							table.createTable(tableColumns);
							SQLdump dump=new SQLdump();
							dump.createSQLDump(query, databaseName, tableName);
							System.out.println("Table created successfully");
							return true;
						} catch (Exception e) {
							throw e;
						}
					}else {
						throw new Exception("Syntax error::::Missing database/table name");
					}
				}else {
					throw new Exception("Syntax error::::Please check your query");
				}
			}else {
				throw new Exception("Syntax error::::Please check your query");
			}
			
		}else {
			throw new Exception("Syntax error::::Please check your query");
		}
	}

	
}
