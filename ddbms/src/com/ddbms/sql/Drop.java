package com.ddbms.sql;

import com.ddbms.main.Column;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class Drop {
    private static final String DELIMITER = "~~~";
    private static final String TABLE_METADATA_FILE = "metadata.txt";
    private String message;
    private String droppedTable;
    private HashMap<String, ArrayList<String>> dbMetaData;
    private static String localRootDirectory;
    private static String currentLocation;
    private ArrayList<Object> pk;
    private ArrayList<Object> fk;
    private HashMap<String, Column> columns;
    private Column primaryKey = null;
    private Column foreignKey = null;

    public Drop(){
        dbMetaData = new HashMap<>();
        columns = new HashMap<>();
        pk = new ArrayList<>();
        fk = new ArrayList<>();
        readProperties();
        loadDbMetadata(localRootDirectory+"\\gdd\\metadata.txt");
    }

    private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory = properties.getProperty("local_root");
            currentLocation = properties.getProperty("current_location");
        } catch (IOException e) {

        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDroppedTable() {
        return droppedTable;
    }

    public void setDroppedTable(String droppedTable) {
        this.droppedTable = droppedTable;
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

    private boolean checkFKConstraint(String database, String table){
        try {
            String fkPath = localRootDirectory + "\\" + database + "\\" + table + "\\foreignkey.txt";
            File lockFile = new File(fkPath);
            if (lockFile.exists()) {
                return true;
            }
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    private boolean isLocked(String database, String table){
        try{
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

    private void updateGDD(String database, String table){
        try {
            String gddPath = localRootDirectory + "\\gdd\\metadata.txt";
            String tempGddPath = localRootDirectory + "\\gdd\\tempmetadata.txt";
            String deleteEntry = database+DELIMITER+table+DELIMITER+currentLocation;

            File oldGdd = new File(gddPath);
            File newGdd = new File(tempGddPath);
            newGdd.createNewFile();
            Scanner fileScanner = new Scanner(oldGdd);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if(line.equals(deleteEntry)){
                    continue;
                }
                line+="\n";
                Files.write(Paths.get(tempGddPath), line.getBytes(), StandardOpenOption.APPEND);
            }
            fileScanner.close();
            oldGdd.delete();
            newGdd.renameTo(oldGdd);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean dropTable(String database, String table){
        try {
            boolean isLocked = isLocked(database, table);
            if (isLocked) {
                System.out.println("Table is use, Try again later.");
                setMessage("Table is use, Try again later.");
                return false;
            }

            boolean hasFKConstraint = checkFKConstraint(database, table);
            if (hasFKConstraint) {
                System.out.println("Foreign Key constraint violation. Cannot drop table.");
                setMessage("Foreign Key constraint violation. Cannot drop table.");
                return false;
            }

            File dir = new File(localRootDirectory + "\\" + database + "\\" + table + "\\");
            for (File file : dir.listFiles()) {
                file.delete();
                continue;
            }
            dir.delete();
            updateGDD(database, table);
            System.out.println("Table dropped.");
            setMessage("Table dropped.");
            setDroppedTable(database+DELIMITER+table+DELIMITER+currentLocation);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Internal Error: Error in dropping table.");
            return false;
        }
    }

    public boolean dropQuery(String query) {
        query = query.replace(";","");
        String[] words = query.split(" ");
        try {
            if (words.length < 3) {
                System.out.println("Incomplete DROP query.");
                setMessage("Incomplete DROP query.");
                return false;
            }

            if (words[1].equalsIgnoreCase("table") == false) {
                System.out.println("Missing keyword \"TABLE\" in query");
                setMessage("Missing keyword \"TABLE\" in query");
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
                    boolean isDatabasePresent = checkForDatabase(database);
                    if (isDatabasePresent) {
                        boolean isTablePresent = checkForTable(database, table);

                        if (isTablePresent) {
                            boolean result = dropTable(database, table);
                            return result;
                        }
                        else {
                            System.out.println("Invalid table name specified.");
                            setMessage("Invalid table name specified.");
                            return false;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
