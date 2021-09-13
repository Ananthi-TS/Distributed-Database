package com.ddbms.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class Table {
	
	private static final String DELIMITER="~~~";
	private static final String TABLE_METADATA_FILE = "metadata.txt";
	
	private static String localRootDirectory;
	String tableName;
	String databaseName;
	String databaseFragment;
	Map<String,Column> columns = null;
	List<Map<String,Object>> tableValues= null;
	
	   private void readProperties(){
	        try {
	            Properties properties = new Properties();
	            properties.load(new FileInputStream("db.properties"));
	            String currentDIR="";
				try {
					currentDIR = new File(".").getCanonicalPath();
				} catch (Exception e) {
					e.printStackTrace();
				}
	            
	            localRootDirectory = currentDIR+"\\"+properties.getProperty("local_root");
	        } catch (IOException e) {

	        }
	    }
	   
	
	public Table(String tableName,String databaseName) {
		this.tableName=tableName;
		this.databaseName=databaseName;
		columns = new HashMap<>();
		tableValues=new ArrayList<>();
		readProperties();
		
	}

	
	public boolean createTable(Map<String,Column> columns) throws Exception {
		
		String filePath=localRootDirectory+"\\"+databaseName+"\\"+tableName;
		File fileDBObject = new File(localRootDirectory+"\\"+databaseName);
		if(fileDBObject.exists()) {
			File fileObject = new File(filePath);
			if(fileObject.exists()) {
				throw new Exception("Table Already exists");
			}else {
				if(checkForeignKey(columns)) {
					fileObject.mkdir();
					String tableMetaData=filePath+"\\"+TABLE_METADATA_FILE;
					File fileObj=new File(tableMetaData);
					if(fileObj.createNewFile()) {
						for(Entry<String,Column> mapEntry:columns.entrySet()) {
							String fileLine="";
							Column columnObject=mapEntry.getValue();
							fileLine+="column_name="+columnObject.getColumnName()+DELIMITER;
							fileLine+="column_type="+columnObject.getColumnType()+DELIMITER;
							if(columnObject.getColumnSize()==0) {
								fileLine+="column_size="+300;
							}else {
								fileLine+="column_size="+columnObject.getColumnSize();
							}
							
							if(columnObject.isPrimaryKey()) {
								fileLine+=DELIMITER+"PK";
							}
							if(columnObject.isForeignKey()) {
								fileLine+=DELIMITER+"FK=";
								fileLine+=columnObject.getForeignKeyTable()+"."+columnObject.getForeignKeyColumn();
							}
							fileLine+="\n";
							Files.write(Paths.get(tableMetaData), fileLine.getBytes(), StandardOpenOption.APPEND);
						}
						updateForiegnKeyRefrences(columns);
					}	
				}else {
					throw new Exception("Invalid foreign key constriant");
				}
			}	
		}else {
			throw new Exception("Database does not exist");
		}
		return false;
	}
	
	private void updateForiegnKeyRefrences(Map<String, Column> columns) {
		for(Entry<String,Column> mapEntry:columns.entrySet()) {
			Column columnObject=mapEntry.getValue();
			if(columnObject.isForeignKey()) {
				String filePath=localRootDirectory+"\\"+databaseName+"\\"+columnObject.getForeignKeyTable()+"\\foreignkey.txt";
				File fileObject = new File(filePath);
				if(!(fileObject.exists())) {
					try {
						fileObject.createNewFile();
					} catch (IOException e) {
						
					}
				}
			}
		}
	}


	private boolean checkForeignKey(Map<String, Column> columns) {
		for(Entry<String,Column> mapEntry:columns.entrySet()) {
			Column columnObject=mapEntry.getValue();
			if(columnObject.isForeignKey()) {
				String filePath=localRootDirectory+"\\"+databaseName+"\\"+columnObject.getForeignKeyTable();
				File fileObject = new File(filePath);
				if(fileObject.exists()) {
					try {
						BufferedReader inFile=new BufferedReader( new FileReader(filePath+"\\"+TABLE_METADATA_FILE) );
						if(inFile!=null) {
							boolean fkExists=false;
							String line = inFile.readLine();
							while ((line!=null) && !(line.trim().equals(""))) {
								if(line.contains("PK")) {
									String lineValues[]=line.split(DELIMITER);
									for(String lineElements:lineValues) {
										if(lineElements.contains("column_name")) {
											String columnName=lineElements.split("=")[1];
											if(columnName.equals(columnObject.getForeignKeyColumn())) {
												fkExists=true;
											}else {
												return false;
											}
										}
									}
								}
								line = inFile.readLine();
							}
							inFile.close();
							if(!fkExists) { 
								return false;
							}
						} else {
							return false;
						}
					} catch (Exception e) { e.printStackTrace();
						return false;
					}
				}else {
					return false;
				}
			}
		}
		return true;
	}


	public void loadColumns() throws Exception {
		columns=new HashMap<>();
		String fileDBPath=localRootDirectory+"\\"+databaseName;
		File fileDBObject = new File(fileDBPath);
			if(fileDBObject.exists()) {
			String filePath=localRootDirectory+"\\"+databaseName+"\\"+tableName+"\\"+TABLE_METADATA_FILE;
			File fileObject = new File(filePath);
			if(fileObject.exists()) {
				//columns
				try {
					Scanner scannerObj = new Scanner(new File(filePath));
					while(scannerObj.hasNext()) {
							String line = scannerObj.nextLine();
							Column columnObject=new Column();
								String lineValues[]=line.split(DELIMITER);
								for(String lineElements:lineValues) {
									if(lineElements.contains("column_name")) {
										String columnName=lineElements.split("=")[1];
										columnObject.setColumnName(columnName);
									}
									else if(lineElements.contains("column_type")) {
										String columnType=lineElements.split("=")[1];
										columnObject.setColumnType(columnType);
									}
									else if(lineElements.contains("column_size")) {
										String columnSize=lineElements.split("=")[1];
										columnObject.setColumnSize(Integer.parseInt(columnSize));
									}
									else if(lineElements.contains("PK")) {
										columnObject.setPrimaryKey(true);
									}
									else if(lineElements.contains("FK")) {
										String secondLine=lineElements.split("=")[1];
										String foreignKeyValue[]=secondLine.split("\\.");
										columnObject.setForeignKey(true);
										columnObject.setForeignKeyTable(foreignKeyValue[0]);
										columnObject.setForeignKeyColumn(foreignKeyValue[1]);
									}
								}
								columns.put(columnObject.getColumnName(), columnObject);
					}
				} catch (Exception e) {
					throw new Exception("Table doesn't exists");
				}
				}else {
					throw new Exception("Table "+tableName+" doesn't exists" +filePath);
				}
			}else {
				throw new Exception("Database "+databaseName+" doesn't exists");
			}
	}	
	//List<HashMap<Object,Object>> tableValues= null;
	public void loadTableValues() throws Exception {
		tableValues=new ArrayList<>();
		if(columns.isEmpty()) {
			loadColumns();
		}else { 
			String fileDBPath=localRootDirectory+"\\"+databaseName;
			File fileDBObject = new File(fileDBPath);
			if(fileDBObject.exists()) {
				String filePath=localRootDirectory+"\\"+databaseName+"\\"+tableName+"\\"+tableName+".txt";
				File fileObject = new File(filePath);
				if(fileObject.exists()) {
					try {
						Scanner scannerObj = new Scanner(new File(filePath));
						while(scannerObj.hasNext()) {
								String line = scannerObj.nextLine();
								List<String> rows=Arrays.asList(line.split(DELIMITER));
								Map<String,Object> rowElements=new HashMap<String,Object>();
								for(String rowTemp:rows) {
									String[] columns=rowTemp.split("=");
									rowElements.put(columns[0], columns[1]);
								}
								for ( String key : columns.keySet() ) {
									if(! (rowElements.containsKey(key))) {
										rowElements.put(key, "NULL");
									}
								}
								tableValues.add(rowElements);
								
						}
					}catch(Exception e) {
						throw new Exception("Table "+tableName+" doesn't exist");
					}
				}	
			}else {
				throw new Exception("Database "+databaseName+" doesn't exist");
			}
		}		
	}
	
	
	public void loadTableValues(Set<String> columnNames) throws Exception {
		tableValues=new ArrayList<>();
		if(columns.isEmpty()) {
			loadColumns();
		}else {
			String filePath=localRootDirectory+"\\"+databaseName+"\\"+tableName+"\\"+tableName+".txt";
			File fileObject = new File(filePath);
			if(fileObject.exists()) {
				//columns
				try {
					Scanner scannerObj = new Scanner(new File(filePath));
					while(scannerObj.hasNext()) {
							String line = scannerObj.nextLine();
							List<String> rows=Arrays.asList(line.split(DELIMITER));
							Map<String,Object> rowElements=new HashMap<String,Object>();
							for(String rowTemp:rows) {
								String[] columns=rowTemp.split("=");
								if(columnNames.contains(columns[0])) {
									rowElements.put(columns[0], columns[1]);
								}
							}
							for(String givenColumn:columnNames) {
								if(! (rowElements.containsKey(givenColumn))) {
									rowElements.put(givenColumn, "NULL");
								}
							}
							tableValues.add(rowElements);
					}		
				}catch(Exception e) {
					
				}
			}	
		}
	}
	
	
	//getters and setters
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseFragment() {
		return databaseFragment;
	}

	public void setDatabaseFragment(String databaseFragment) {
		this.databaseFragment = databaseFragment;
	}

	public Map<String, Column> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Column> columns) {
		this.columns = columns;
	}

	public List<Map<String, Object>> getTableValues() {
		return tableValues;
	}

	public void setTableValues(List<Map<String, Object>> tableValues) {
		this.tableValues = tableValues;
	}

	@Override
	public String toString() {
		return "Table [tableName=" + tableName + ", databaseName=" + databaseName + ", databaseFragment="
				+ databaseFragment + ", columns=" + columns + ", tableValues=" + tableValues + "]";
	}
	
	

}
