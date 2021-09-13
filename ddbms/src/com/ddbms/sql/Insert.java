package com.ddbms.sql;

import com.ddbms.main.Column;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Insert {
    private static final String DELIMITER = "~~~";
    private static final String TABLE_METADATA_FILE = "metadata.txt";
    private String message;
    private String eventMessage;
    private HashMap<String,ArrayList<String>> dbMetaData;
    private String tableMetadataPath;
    private static String localRootDirectory;
    private ArrayList<Object> pk;
    private ArrayList<Object> fk;
    private HashMap<String, Column> columns;
    private Column primaryKey = null;
    private Column foreignKey = null;

    private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {

        }
    }

    public Insert() {
        dbMetaData = new HashMap<>();
        columns = new HashMap<>();
        pk = new ArrayList<>();
        fk = new ArrayList<>();
        readProperties();
        String currentDIR = null;
        try {
            currentDIR = new File(".").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadDbMetadata(currentDIR+"\\db-1\\gdd\\metadata.txt");
    }

    private void loadDbMetadata(String dbMetaDataPath){
        try{
            File fileObj = new File(dbMetaDataPath);
            Scanner fileScanner = new Scanner(fileObj);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] metaData = line.split(DELIMITER);
                String database = metaData[0];
                String table = metaData[1];
                ArrayList<String> tables = new ArrayList<>();
                if(dbMetaData.containsKey(database)){
                    tables = dbMetaData.get(database);
                    if(tables.contains(table) == false){
                        tables.add(table);
                        dbMetaData.put(database, tables);
                    }
                }
                else{
                    tables.add(table);
                    dbMetaData.put(database,tables);
                }
            }
            fileScanner.close();
        } catch (Exception e) {
            System.out.println("An error occurred while reading Database MetaData.");
            setMessage("An error occurred while reading Database MetaData.");
            e.printStackTrace();
        }
    }

    private boolean loadTableMetaData(String database, String table){
        try {
            tableMetadataPath = localRootDirectory+"\\"+database+"\\"+table+"\\";
            File fileObj = new File(tableMetadataPath +TABLE_METADATA_FILE);
            Scanner fileScanner = new Scanner(fileObj);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Column col = new Column();
                String[] metaData = line.split(DELIMITER);
                for(int i=0; i< metaData.length ; i++){
                    String[] data = metaData[i].split("=");
                    switch (data[0]){
                        case "column_name":
                            col.setColumnName(data[1]);
                            break;
                        case "column_type":
                            col.setColumnType(data[1]);
                            break;
                        case "column_size":
                            col.setColumnSize(Integer.parseInt(data[1]));
                            break;
                        case "PK":
                            col.setPrimaryKey(true);
                            primaryKey = col;
                            break;
                        case "FK":
                            col.setForeignKey(true);
                            String[] dbTable = data[1].split("\\.");
                            col.setForeignKeyTable(dbTable[0]);
                            col.setForeignKeyColumn(dbTable[1]);
                            foreignKey = col;
                            break;
                        default:
                            System.out.println("Unknown table meta data property " + data[0]);
                            setMessage("Unknown table meta data property " + data[0]);
                            break;
                    }
                }
                columns.put(col.getColumnName(),col);
            }
            fileScanner.close();
            return true;
        } catch (Exception e) {
            System.out.println("An error occurred while reading Table MetaData.");
            setMessage("An error occurred while reading Table MetaData.");
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadPKs(String database, String table){
        try{
            String tablePath = localRootDirectory+"\\"+database+"\\"+table+"\\"+table+".txt";
            File tableFile = new File(tablePath);
            if(tableFile.exists()){
                Scanner fileScanner = new Scanner(tableFile);
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] data = line.split(DELIMITER);
                    for(String element: data){
                        String[] colValue = element.split("=");
                        String col = colValue[0];
                        String val = colValue[1];
                        if(col.equals(primaryKey.getColumnName())){
                            switch (primaryKey.getColumnType()){
                                case "int":
                                    pk.add(Integer.parseInt(val));
                                    break;
                                case "float":
                                    pk.add(Float.parseFloat(val));
                                    break;
                                default:
                                    pk.add(val);
                                    break;
                            } // switch
                        } // if - primary key
                    } // for - data
                }// while - line
            }// if - file exists
            return true;
        }catch (Exception e) {
            System.out.println("An error occurred while reading Table MetaData.");
            setMessage("An error occurred while reading Table MetaData.");
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadFKs(String database, String table){
        try{
            String tablePath = localRootDirectory+"\\"+database+"\\"+table+"\\"+table+".txt";
            File tableFile = new File(tablePath);
            if(tableFile.exists()){
                Scanner fileScanner = new Scanner(tableFile);
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] data = line.split(DELIMITER);
                    for(String element: data){
                        String[] colValue = element.split("=");
                        String col = colValue[0];
                        String val = colValue[1];
                        if(col.equals(foreignKey.getColumnName())){
                            switch (foreignKey.getColumnType()){
                                case "int":
                                    fk.add(Integer.parseInt(val));
                                    break;
                                case "float":
                                    fk.add(Float.parseFloat(val));
                                    break;
                                default:
                                    fk.add(val);
                                    break;
                            } // switch
                        } // if - primary key
                    } // for - data
                }// while - line
            }// if - file exists
            return true;
        }catch (Exception e) {
            System.out.println("An error occurred while reading Table MetaData.");
            setMessage("An error occurred while reading Table MetaData.");
            e.printStackTrace();
            return false;
        }
    }

    private boolean isLocked(String database, String table){
        try {
            String lockPath = localRootDirectory + "\\" + database + "\\" + table + "\\lock.txt";
            File lockFile = new File(lockPath);
            for (int i = 0; i < 50; i++) {
                if (lockFile.exists() == false) {
                    return false;
                }
                Thread.sleep(5000);
            }
            return true;
        }
        catch (Exception e){
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
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeLock(String database, String table){
        try {
            String lockPath = localRootDirectory + "\\" + database + "\\" + table + "\\lock.txt";
            File lockFile = new File(lockPath);
            if (lockFile.exists()) {
                lockFile.delete();
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertIntoFile(String database, String table, String[] insertColumns) {
        boolean loadPk = false;
        boolean loadFk = false;
        boolean pkColExists = false;
        boolean fkColExists = false;
        boolean isPKValuePresent;
        boolean isFKValuePresent;
        String line = "";
        if(primaryKey != null){
            loadPk = loadPKs(database, table);
            pkColExists = true;
        }
        if(foreignKey != null){
            loadFk = loadFKs(database, foreignKey.getForeignKeyTable());
            fkColExists = true;
        }

        String tablePath = localRootDirectory+"\\"+database+"\\"+table+"\\"+table+".txt";

        File tableFile = new File(tablePath);
        if(tableFile.exists() == false){
            try {
                tableFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Internal Error: Error creating empty file.");
                setMessage("Internal Error: Error creating empty file.");
                return false;
            }
        }
        boolean isLocked = isLocked(database, table);
        if(isLocked){
            System.out.println("Cannot perform operation, table already in use.");
            setMessage("Cannot perform operation, table already in use.");
            return false;
        }

        createLock(database, table);
        for (String col: insertColumns){
            String columnName = col;
            Column columnMetaData = columns.get(columnName);
            Object columnValue = columnMetaData.getColumnValue();
            if(pkColExists == true && columnName.equals(primaryKey.getColumnName())){
                isPKValuePresent = pk.contains(columnValue);
                if(isPKValuePresent == true){
                    System.out.println("Primary key constraint violation, cannot insert row");
                    setMessage("Primary key constraint violation, cannot insert row");
                    return false;
                }
                line+=columnName+"="+columnValue+DELIMITER;
            }
            else if(fkColExists == true && columnName.equals(foreignKey.getColumnName())){
                isFKValuePresent = fk.contains(columnValue);
                if(isFKValuePresent == false){
                    System.out.println("Foreign key constraint violation, cannot insert row");
                    setMessage("Foreign key constraint violation, cannot insert row");
                    return false;
                }
                line+=columnName+"="+columnValue+DELIMITER;
            }
            else{
                line+=columnName+"="+columnValue+DELIMITER;
            }
        }
        line = line.substring(0,(line.length()-DELIMITER.length()));
        line+="\n";
        try {
            Files.write(Paths.get(tablePath), line.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Internal Error: Error inserting into file.");
            setMessage("Internal Error: Error inserting into file.");
            return false;
        }
        System.out.println("Insert successful. Rows inserted: 1");
        setMessage("Insert successful. Rows inserted: 1");
        removeLock(database, table);
        return true;
    }

    private boolean validateAndMapColumnsToValues(String[] column, String[] value){
        for(String col: column){
            if(columns.containsKey(col) == false){
                System.out.println("Invalid column \""+col+"\".");
                setMessage("Invalid column \""+col+"\".");
                return false;
            }
        }
        if(primaryKey != null){
            if(Arrays.asList(column).contains(primaryKey.getColumnName()) == false){
                System.out.println("Cannot Insert with Primary key null.");
                setMessage("Cannot Insert with Primary key null.");
                return false;
            }
        }

        if(foreignKey != null){
            if(Arrays.asList(column).contains(foreignKey.getColumnName()) == false){
                System.out.println("Cannot Insert with Foreign key null.");
                setMessage("Cannot Insert with Foreign key null.");
                return false;
            }
        }

        for(int i = 0; i<column.length; i++){
            String columnName = column[i];
            String tempColumnValue = value[i];
            Column columnMetaData = columns.get(columnName);
            String columnType = columnMetaData.getColumnType();
            Object columnValue = null;
            switch (columnType.toLowerCase()){
                case "int":
                    try{
                        columnValue = Integer.parseInt(tempColumnValue);
                        columnMetaData.setColumnValue(columnValue);
                    }
                    catch (NumberFormatException e){
                        e.printStackTrace();
                        System.out.println("Value \""+tempColumnValue+"\" and Column \""+columnName+"\" of datatype \""+columnType+"\" do not match.");
                        setMessage("Value \""+tempColumnValue+"\" and Column \""+columnName+"\" of datatype \""+columnType+"\" do not match.");
                        return false;
                    }
                    break;
                case "float":
                    try{
                        columnValue = Float.parseFloat(tempColumnValue);
                        columnMetaData.setColumnValue(columnValue);
                    }
                    catch (NumberFormatException e){
                        e.printStackTrace();
                        System.out.println("Value \""+tempColumnValue+"\" and Column \""+columnName+"\" of datatype \""+columnType+"\" do not match.");
                        setMessage("Value \""+tempColumnValue+"\" and Column \""+columnName+"\" of datatype \""+columnType+"\" do not match.");
                        return false;
                    }
                    break;
                default:
                    int firstIndex = tempColumnValue.indexOf("'");
                    int lastIndex = tempColumnValue.lastIndexOf("'");
                    int strLength = tempColumnValue.length();
                    if(firstIndex >0 || lastIndex < (strLength -1)){
                        System.out.println("Value of type string must be enclosed in ''");
                        setMessage("Value of type string must be enclosed in ''");
                        return false;
                    }
                    if(strLength == 2){
                        if(columnName.equals(primaryKey.getColumnName())){
                            System.out.println("Primary Key cannot be an empty string");
                            setMessage("Primary Key cannot be an empty string");
                            return false;
                        }
                        if(column.equals(foreignKey.getColumnName())){
                            System.out.println("Foreign key cannot be an empty string");
                            setMessage("Foreign key cannot be an empty string");
                            return false;
                        }
                    }
                    columnValue = tempColumnValue.substring(1,strLength-1);
                    columnMetaData.setColumnValue(columnValue);
                    break;
            }
        }
        return true;
    }

    private boolean checkForDatabase(String database){
        if(dbMetaData.containsKey(database)){
            return true;
        }
        return false;
    }

    private boolean checkForTable(String database, String table){
        ArrayList<String> tables = dbMetaData.get(database);
        if(tables.contains(table)) {
            return true;
        }
        return false;
    }

    private boolean validateParenthesisString(String str){
        try {
            int colLeftParan = str.indexOf("(");
            int colRightParan = str.indexOf(")");

            if (colLeftParan == -1) {
                System.out.println("Missing \"(\" in query.");
                setMessage("Missing \"(\" in query.");
                return false;
            }

            if (colRightParan == -1) {
                System.out.println("Missing \")\" in query.");
                setMessage("Missing \")\" in query.");
                return false;
            }

            if (colLeftParan > 0) {
                System.out.println("Unknown character before \"(\" in query.");
                setMessage("Unknown character before \"(\" in query.");
                return false;
            }
            if (colRightParan < str.length() - 1) {
                System.out.println("Unknown character after \")\" in query.");
                setMessage("Unknown character after \")\" in query.");
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean insertQuery(String query) {
        query = query.replace(";","");
        String[] words = query.split(" ");
        try {
            if (words.length < 6) {
                System.out.println("Incomplete INSERT query.");
                setMessage("Incomplete INSERT query.");
                return false;
            }

            if (words[1].equalsIgnoreCase("into") == false) {
                System.out.println("Missing keyword \"INTO\" in query");
                setMessage("Missing keyword \"INTO\" in query");
                return false;
            }

            if (words[4].equalsIgnoreCase("values") == false) {
                System.out.println("Missing keyword \"VALUES\" in query");
                setMessage("Missing keyword \"VALUES\" in query");
                return false;
            }

            String databaseAndTable = words[2];
            int fullStopIndex = databaseAndTable.indexOf(".");

            if (fullStopIndex > 0) {
                String[] dbTable = words[2].split("\\.");
                if (dbTable.length < 2) {
                    System.out.println("Mention table name.");
                    setMessage("Mention table name.");
                    return false;
                } else {
                    String database = dbTable[0];
                    String table = dbTable[1];
                    String columns = words[3];
                    String values = words[5];

                    boolean isDatabasePresent = checkForDatabase(database);
                    if (isDatabasePresent) {
                        boolean isTablePresent = checkForTable(database, table);

                        if (isTablePresent) {
                            boolean isColumnStringValid = validateParenthesisString(columns);
                            boolean isValuesStringValid = validateParenthesisString(values);
                            boolean tableMetaDataLoaded = loadTableMetaData(database, table);
                            if (isValuesStringValid && isValuesStringValid && tableMetaDataLoaded) {
                                columns = columns.substring(1, columns.length() - 1);
                                values = values.substring(1, values.length() - 1);

                                String[] col = columns.split(",");
                                String[] val = values.split(",");

                                if (col.length != val.length) {
                                    System.out.println("Number of Columns and Values don't match.");
                                    setMessage("Number of Columns and Values don't match.");
                                    return false;
                                } else {
                                    boolean columnsAndValuesMapped = validateAndMapColumnsToValues(col, val);
                                    if (columnsAndValuesMapped) {
                                        boolean result = insertIntoFile(database, table, col);
                                        return result;
                                    }
                                    return columnsAndValuesMapped;
                                }
                            } else {
                                return isColumnStringValid || isValuesStringValid || tableMetaDataLoaded;
                            }
                        } else {
                            System.out.println("Invalid table name specified.");
                            setMessage("Invalid table name specified.");
                            return false;
                        }
                    } else {
                        System.out.println("Invalid database name specified.");
                        setMessage("Invalid database name specified.");
                        return false;
                    }
                }
            } else {
                System.out.println("Missing database name in query.");
                setMessage("Missing database name in query.");
                return false;
            }
        }
        catch (Exception e){
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

    public String getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }
    //    public static void main(String[] args) {
//        Insert i = new Insert();
//        i.insertQuery("INSERT INTO testDatabase.Persons (PersonID,FirstName) VALUES (,'sdf')");
//    }
}
