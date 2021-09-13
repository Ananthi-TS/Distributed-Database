package com.ddbms.sql;

import com.ddbms.main.Column;
import com.ddbms.main.Table;
import org.apache.commons.lang3.StringUtils;
import com.ddbms.main.EventLogs;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Update {
    int count ;
    private static String localRootDirectory;
    Map<String, String> conditionsMap = new HashMap<>();
    Map<String, String> columnValuesMap = new HashMap<>();
    private String message;

    public Map<String, String> getConditionsMap() {
        return conditionsMap;
    }

    public void setConditionsMap(Map<String, String> conditionsMap) {
        this.conditionsMap = conditionsMap;
    }

    public Map<String, String> getColumnValuesMap() {
        return columnValuesMap;
    }

    public void setColumnValuesMap(Map<String, String> columnValuesMap) {
        this.columnValuesMap = columnValuesMap;
    }


    private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {

        }
    }
    
    public void toUpdateTable(String updateQuery) throws Exception {
        String table;
        String database;
        readProperties();
        Pattern conditionalPattern = Pattern.compile("update\\s(.*?)set\\s(.*?)where\\s(.*?)?;", Pattern.CASE_INSENSITIVE);

        String s = updateQuery;

        if (StringUtils.containsIgnoreCase(s, "where")) {
            Matcher conditionalMatcher = conditionalPattern.matcher(s);
            conditionalMatcher.find();
            String dbTable = conditionalMatcher.group(1);
            List<String> x= Arrays.asList(dbTable.trim().split("\\."));
            database = x.get(0);
            table = x.get(1);
            String colValues = conditionalMatcher.group(2);
            String conditions = conditionalMatcher.group(3);
            List<Map<String, Object>> tableValues1 = getTableValues(table,database ,colValues, conditions);


        }
        else {
            System.out.println("Cannot perform operation, Invalid syntax.");
            setMessage("Cannot perform operation, Invalid syntax.");
            throw new Exception("Invalid syntax");

        }
    }


    private List<Map<String, Object>>  getTableValues(String tableName, String database,String colValues, String conditions) throws Exception {
        boolean validColsandConditions =false;
        List<Map<String, Object>> tableValues;
        Map<String, String> conditionsMap1 = new HashMap<>();
        Map<String, String> columnValuesMap1 = new HashMap<>();

        String removeExceptTextAndNos = "[^0-9a-zA-Z]+";
        List<String> conditionLst = Arrays.asList(conditions.trim().split("and"));

        for (String temp : conditionLst) {
            String[] condtnArr = temp.split("=");
            conditionsMap1.put(condtnArr[0].trim(), condtnArr[1].trim().replaceAll(removeExceptTextAndNos, ""));
        }
        setConditionsMap(conditionsMap1);

        List<String> columnValuesLst = Arrays.asList(colValues.trim().split("and"));
        for (String temp : columnValuesLst) {
            String[] columnValuesArr = temp.split("=");
            columnValuesMap1.put(columnValuesArr[0].trim(), columnValuesArr[1].trim().replaceAll(removeExceptTextAndNos, ""));
        }
        setColumnValuesMap(columnValuesMap1);
        Table table = new Table(tableName.trim(), database);
        table.loadColumns();

        String columns = "";
        for (String temp : columnValuesLst) {
            String[] columnValuesArr = temp.split("=");
            columns = columns + columnValuesArr[0].trim() + ",";
        }

        columns = columns.trim().replaceAll("\\s+", "");
        Set<String> set = new LinkedHashSet<>(Arrays.asList(columns.split(",")));

        set.addAll(conditionsMap.keySet());


        Map<String, Column> map = table.getColumns();
        Set<String> mapKeys = map.keySet();
        if (mapKeys.containsAll(set)) {
            table.loadTableValues();
            tableValues = table.getTableValues();
            validColsandConditions = true;
            if (validColsandConditions){
                updateTable(tableValues,conditionsMap,columnValuesMap,tableName,database);
            }

        } else {
            System.out.println("Cannot perform operation, Invalid column.");
            setMessage("Cannot perform operation, Invalid column.");
            throw new Exception("Invalid column");
        }

        return tableValues;
    }



    private boolean updateTable(List<Map<String, Object>> existingColValuesMap, Map<String, String> conditionsMap , Map<String, String> columnValuesMap, String tableName, String database) throws IOException {
        String log = "";

        EventLogs eventLogs = new EventLogs();
        try {
            boolean isLocked = isLocked(database, tableName);
            if(isLocked){
                System.out.println("Cannot perform operation, table already in use.");
                setMessage("Cannot perform operation, table already in use.");
                return false;
            }

            createLock(database, tableName);
            List<Map<String, Object>> updatedMap = generateNewListOfMap(existingColValuesMap, conditionsMap, columnValuesMap);
            String filePath=localRootDirectory+"\\"+database+"\\"+tableName+"\\"+tableName+".txt";
            File originalFile = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(originalFile));

            File tempFile = new File(filePath);
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            new PrintWriter(originalFile).close();

            for (Map<String, Object> mapTemp : updatedMap) {
                String xyz = "";
                for (int i = 0; i < mapTemp.size(); i++) {
                    if (i < mapTemp.size() - 1) {
                        xyz += mapTemp.keySet().toArray()[i] + "=" + mapTemp.get(mapTemp.keySet().toArray()[i]) + "~~~";
                    } else {
                        xyz += mapTemp.keySet().toArray()[i] + "=" + mapTemp.get(mapTemp.keySet().toArray()[i]);
                    }
                }
                pw.println(xyz);
                pw.flush();
            }
            pw.close();
            br.close();
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd^HH:mm:ss").format(new Date());
            log+="DatabaseName:"+database.trim()+"~~~"+"TableName:"+tableName.trim()+"~~~"+"Event:Update"+"~~~"+"NoOfRowsUpdated:"+count+"~~~"+"DateAndTime:"+timeStamp;
            eventLogs.addEventLog(log);
            setMessage("Update successful.");
            System.out.println("Update successful.");
            removeLock(database, tableName);
            return true;

        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> generateNewListOfMap(List<Map<String, Object>> existingColValuesMap,Map<String, String> conditionsMap,Map<String, String> columnValuesMap){
        count = 0;
        boolean flag = false;
        for(Map<String,Object> mapTemp:existingColValuesMap) {
            for(Entry<String, String> entry:conditionsMap.entrySet()) {
                if ((mapTemp.get(entry.getKey()).equals(entry.getValue()))){
                    flag=true;
                }else {
                    flag=false;
                    break;
                }
            }
            if (flag) {
                for (Entry<String, String> entry : columnValuesMap.entrySet()) {
                    mapTemp.put(entry.getKey(),entry.getValue());
                    count+=1;
                }
            }
        }

        return existingColValuesMap;
    }
    private boolean isLocked(String database, String table){
        try {
            String lockPath = localRootDirectory + "\\" + database + "\\" + table + "\\lock.txt";
            File lockFile = new File(lockPath);
            for (int i = 0; i < 50; i++) {
                if (lockFile.exists()==false) {
                    return false;
                }
                Thread.sleep(5000);
            }
            return true;
        }
        catch (Exception e){
            System.out.println("Cannot perform operation, table already in use.");
            setMessage("Cannot perform operation, table already in use.");
            e.printStackTrace();
            return true;
        }
    }

    private boolean createLock(String database, String table){
        try {
            String lockPath = localRootDirectory + "\\" + database + "\\" + table + "\\lock.txt";
            File lockFile = new File(lockPath);
            if (lockFile.exists() == false) {
                lockFile.createNewFile();
            }
            return true;
        }
        catch (Exception e){
            System.out.println("lock is not created");
            setMessage("lock is not created");
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeLock(String database, String table){
        try {
            String lockPath = localRootDirectory + "\\" + database + "\\" + table + "\\lock.txt";
            File lockFile = new File(lockPath);
            if (lockFile.exists()== true) {
                lockFile.delete();
            }
            return true;
        }
        catch (Exception e){
            System.out.println("cannot remove the lock");
            setMessage("cannot remove the lock");
            e.printStackTrace();
            return false;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

