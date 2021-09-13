package com.ddbms.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ddbms.encryption.AesCipher;
import com.ddbms.main.Column;
import com.ddbms.main.Table;

public class Select {

	
	public List<Map<String,Object>> selectProcess(String query) throws Exception {
		List<Map<String,Object>> tableValues=new ArrayList<>();
		Map<String,String> tableInfo=new Select().getTableInfo(query);
		if(tableInfo.containsKey("conditions")) {
			String columns = tableInfo.get("columns");
	        String table = tableInfo.get("table");
	        String conditions = tableInfo.get("conditions");
			tableValues= new Select().getTableValues(table,columns,conditions);
		}else {
			String columns = tableInfo.get("columns");
	        String table = tableInfo.get("table");
			tableValues= new Select().getTableValues(table,columns);
		}
		
		return tableValues;	
		}

	public Map<String,String> getTableInfo(String query) throws Exception{
		Map<String,String> tableInfo=new HashMap<>();
		Pattern normalPattern = Pattern.compile("select\\s+(.*?)\\s*from\\s+(.*?);", Pattern.CASE_INSENSITIVE);
		Pattern conditionalPattern = Pattern.compile("select\\s+(.*?)\\s*from\\s+(.*?)\\s*where\\s+(.*?);", Pattern.CASE_INSENSITIVE);
		try {
			if( query.contains("where")) {
					//StringUtils.containsIgnoreCase(query, "where")) {
					Matcher conditionalMatcher = conditionalPattern.matcher(query);
					conditionalMatcher.find();
				    String columns = conditionalMatcher.group(1);
			        String table = conditionalMatcher.group(2);
			        String conditions = conditionalMatcher.group(3);
			        tableInfo.put("columns", columns);
			        tableInfo.put("table",table);
			        tableInfo.put("conditions", conditions);
			        
			}else {
			 		Matcher normalMatcher = normalPattern.matcher(query);
			 		normalMatcher.find();
					String columns = normalMatcher.group(1);
			        String table = normalMatcher.group(2).trim();
			        tableInfo.put("columns", columns);
			        tableInfo.put("table",table);
			}
		} catch (IllegalStateException e) {
			throw new Exception("Syntax error...check your sql query"); 
		}
		return tableInfo;
		
	}
	private  List<Map<String, Object>> getTableValues(String tableName, String columns) throws Exception  {
		String[] tableInfo=tableName.split("\\.");
		if((tableInfo!=null) && (tableInfo.length==2)){
			String databaseName=tableInfo[0];
			String tableStr=tableInfo[1];
				Table table=new Table(tableStr,databaseName);
				table.loadColumns();
				if(columns.trim().equals("*")) {
					table.loadTableValues();
					return table.getTableValues();
				}else {
					columns=columns.trim().replaceAll("\\s+", "");
					Set<String> set = new LinkedHashSet<>(Arrays.asList(columns.split(",")));
					Map<String,Column> map=table.getColumns();
					Set<String> mapKeys=map.keySet(); 
					if(mapKeys.containsAll(set)) {
						table.loadTableValues(set);
						return table.getTableValues();
					}else {
						throw new Exception("Invalid column");
					}
				}
		}else {
			throw new Exception("Syntax error....Missing database name/column name");
		}
	}

	private  List<Map<String, Object>> getTableValues(String tableName, String columns, String conditions) throws Exception {
		String[] tableInfo=tableName.split("\\.");
		if((tableInfo!=null) && (tableInfo.length==2)){
			String databaseName=tableInfo[0];
			String tableStr=tableInfo[1];
				Table table=new Table(tableStr,databaseName);
			String removeExceptTextAndNos="[^0-9a-zA-Z]+";
			List<String> conditionLst=Arrays.asList(conditions.trim().split("and"));
			Map<String,String> conditionsMap=new HashMap<>();
			for(String temp:conditionLst) {
				String[] condtnArr=temp.split("=");
				conditionsMap.put(condtnArr[0].trim(), condtnArr[1].trim().replaceAll(removeExceptTextAndNos, ""));
			}
			table.loadColumns();
			if(columns.trim().equals("*")) {
				table.loadTableValues();
				List<Map<String, Object>> tableValues=table.getTableValues();
				List<Map<String, Object>> tableReturnValues=new ArrayList<>();
				for(Map<String,Object> mapTemp:tableValues) {
					boolean validRow=checkIfConditionMatches(conditionsMap, mapTemp);
					if(validRow) {
						tableReturnValues.add(mapTemp);
					}
				}
				return tableReturnValues;
			}else {
				columns=columns.trim().replaceAll("\\s+", "");
				Set<String> set = new LinkedHashSet<>(Arrays.asList(columns.split(",")));
				set.addAll(conditionsMap.keySet());
				Map<String,Column> map=table.getColumns();
				Set<String> mapKeys=map.keySet();
				if(mapKeys.containsAll(set)) {
					table.loadTableValues(set);
					List<Map<String, Object>> tableValues=table.getTableValues();
					List<Map<String, Object>> tableReturnValues=new ArrayList<>();
					for(Map<String,Object> mapTemp:tableValues) {
						boolean validRow=checkIfConditionMatches(conditionsMap, mapTemp);
						if(validRow) {
							mapTemp.keySet().retainAll(Arrays.asList(columns.split(",")));
							tableReturnValues.add(mapTemp);
						}
					}
					return tableReturnValues;
				}else {
					throw new Exception("Invalid column");
				}
			}
		}else {
			throw new Exception("Syntax error....Missing database name/column name");
		}
	}

	private boolean checkIfConditionMatches(Map<String, String> conditionsMap, Map<String, Object> mapTemp) {
		 boolean flag=false;
		for(Entry<String, String> entry:conditionsMap.entrySet()) {
			if ((mapTemp.get(entry.getKey()).equals(entry.getValue()))){
				flag=true;
			}else {
				flag=false;
				break;
			}
		}
		return flag;
	}

}
