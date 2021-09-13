package com.ddbms.main;

import com.ddbms.sql.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class ClientUtil {
    private static String DELIMITER = "~~~";
    private static String localRootDirectory;
    private static String currentLocation ;
    private static ArrayList<String> localGDD;
    private static HashMap<String , String> localGDDHash;

    private void readProperties(){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory = properties.getProperty("local_root");
            currentLocation = properties.getProperty("current_location");
        } catch (IOException e) {

        }
    }

    private void readLocalGDD(){
        try {
            String currentDIR = new java.io.File(".").getCanonicalPath();
            String gddPath = currentDIR+"\\db-1\\gdd\\metadata.txt";
            File gddFileObj = new File(gddPath);
            Scanner fileScanner = null;
            fileScanner = new Scanner(gddFileObj);
            while (fileScanner.hasNext()){
                String line = fileScanner.nextLine();
                localGDD.add(line);
                String[] words = line.split(DELIMITER);
                localGDDHash.put(words[0]+DELIMITER+words[1], words[2]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLocation(String dbTable){
        String location = "";
        if(localGDDHash.containsKey(dbTable)){
            location = localGDDHash.get(dbTable);
        }
        return location;
    }

    public ClientUtil(){
        localGDD = new ArrayList<>();
        localGDDHash = new HashMap<>();
        readProperties();
    }

    public boolean updateDistributedGdd(DataInputStream dataInputStream){
        try {
            //String gddPath = localRootDirectory+"\\gdd\\metadata.txt";
            String currentDIR = new java.io.File(".").getCanonicalPath();
            String gddPath = currentDIR+"\\db-1\\gdd\\metadata.txt";
            File gddFileObj = new File(gddPath);
            if(gddFileObj.exists() == false){
                try {
                    System.out.println("gdd doesn't exist, creating a file.");
                    gddFileObj.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Internal Error: Error creating empty file.");
                    return false;
                }
            }

            readLocalGDD();

            String line = (String) dataInputStream.readUTF();
            if(line.equals("BEGIN")) {
                while (true) {
                    line = (String) dataInputStream.readUTF();
                    if(line.equals("END")){
                        break;
                    }
                    if(localGDD.contains(line) == false) {
                        localGDD.add(line);
                        String[] words = line.split(DELIMITER);
                        localGDDHash.put(words[0]+DELIMITER+words[1],words[2]);
                        line += "\n" ;
                        Files.write(Paths.get(gddPath), line.getBytes(), StandardOpenOption.APPEND);
                    }
                }
            }
            else{
                System.out.println("Did not get update from host.");
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFromDistributedGDD(String deletedTable){
        try {
            String gddPath = localRootDirectory + "\\gdd\\metadata.txt";
            String tempGddPath = localRootDirectory + "\\gdd\\tempmetadata.txt";

            File oldGdd = new File(gddPath);
            File newGdd = new File(tempGddPath);
            newGdd.createNewFile();
            Scanner fileScanner = new Scanner(oldGdd);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if(line.equals(deletedTable)){
                    localGDD.remove(deletedTable);
                    String[] dbtable = deletedTable.split(DELIMITER);
                    localGDDHash.remove(dbtable[0]+DELIMITER+dbtable[1]);
                    continue;
                }
                line+="\n";
                Files.write(Paths.get(tempGddPath), line.getBytes(), StandardOpenOption.APPEND);
            }
            fileScanner.close();
            oldGdd.delete();
            newGdd.renameTo(oldGdd);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void insertToGDD(String dbTable) {
    	try {
			String currentDIR = new java.io.File(".").getCanonicalPath();
			String gddPath = currentDIR+"\\db-1\\gdd\\metadata.txt";
			String fileLine=dbTable+DELIMITER+currentLocation+"\n";
			Files.write(Paths.get(gddPath), fileLine.getBytes(), StandardOpenOption.APPEND);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public boolean createTable(DataOutputStream dataOutputStream, DataInputStream dataInputStream,String query, String[] queryWords){
    	long start = System.nanoTime();
        String dbTable = queryWords[2];
        dbTable = dbTable.replace(".",DELIMITER);
            Create create=new Create();
            boolean flag=false;
            try {
				flag=create.createProcess(query);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
            if(flag) {
            	insertToGDD(dbTable);
            	localGDDHash.put(dbTable, currentLocation);
            	try {
					dataOutputStream.writeUTF("UPDATE_GDD");
					dataOutputStream.flush();
					updateDistributedGdd(dataInputStream);
					long end = System.nanoTime();
					long execution = (end - start);
					System.out.println("Execution time : "+execution + " nanoseconds");
				} catch (IOException e) {
					e.printStackTrace();
				}
            	
            }
        return true;
    }

    public boolean insertIntoTable(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String query, String[] queryWords){
        long start = System.nanoTime();
        try {
            String dbTable = queryWords[2];
            dbTable = dbTable.replace(".",DELIMITER);
            String location = getLocation(dbTable);
            if(location.equals("")){
                System.out.println("Table does not exists.");
                long end = System.nanoTime();
                long execution = (end - start);
                System.out.println("Execution time : "+execution + " nanoseconds");
                return false;
            }
            if(location.equals(currentLocation)){
                Insert insert = new Insert();
                insert.insertQuery(query);
            }
            else{
                dataOutputStream.writeUTF("INSERT");
                dataOutputStream.flush();
                dataOutputStream.writeUTF(query);
                dataOutputStream.flush();
                String line = (String) dataInputStream.readUTF();
                if(line.equals("BEGIN")) {
                    while (true) {
                        line = (String) dataInputStream.readUTF();
                        if(line.equals("END")){
                            break;
                        }
                        System.out.println(line);
                    }
                }
                else{
                    System.out.println("No response from host.");
                    long end = System.nanoTime();
                    long execution = (end - start);
                    System.out.println("Execution time : "+execution + " nanoseconds");
                    return false;
                }
            }
            long end = System.nanoTime();
            long execution = (end - start);
            System.out.println("Execution time : "+execution + " nanoseconds");
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            long end = System.nanoTime();
            long execution = (end - start);
            System.out.println("Execution time : "+execution + " nanoseconds");
            return false;
        }
    }

    public boolean updateInTable(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String query, String[] queryWords) throws Exception {
        long start = System.nanoTime();
        try {
            String dbTable = queryWords[1];
            dbTable = dbTable.replace(".", DELIMITER);
            String location = getLocation(dbTable);
            if (location.equals("")) {
                System.out.println("Table does not exists.");
                long end = System.nanoTime();
                long execution = (end - start);
                System.out.println("Execution time : "+execution + " nanoseconds");
                return false;
            }
            if (location.equals(currentLocation)) {
                Update update = new Update();
                update.toUpdateTable(query);
            } else {
                dataOutputStream.writeUTF("UPDATE");
                dataOutputStream.flush();
                dataOutputStream.writeUTF(query);
                dataOutputStream.flush();
                String line = (String) dataInputStream.readUTF();
                if (line.equals("BEGIN")) {
                    while (true) {
                        line = (String) dataInputStream.readUTF();
                        if (line.equals("END")) {
                            break;
                        }
                        System.out.println(line);
                    }
                } else {
                    System.out.println("No response from host.");
                    long end = System.nanoTime();
                    long execution = (end - start);
                    System.out.println("Execution time : "+execution + " nanoseconds");
                    return false;
                }
                long end = System.nanoTime();
                long execution = (end - start);
                System.out.println("Execution time : "+execution + " nanoseconds");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            long end = System.nanoTime();
            long execution = (end - start);
            System.out.println("Execution time : "+execution + " nanoseconds");
            return false;
        }
        return true;
    }

    public boolean dropTable(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String query, String[] queryWords){
        long start = System.nanoTime();
        try{
            String dbTable = queryWords[2];
            dbTable = dbTable.replace(";","");
            dbTable = dbTable.replace(".",DELIMITER);
            String location = getLocation(dbTable);
            if(location.equals("")){
                System.out.println("Table does not exists.");
                long end = System.nanoTime();
                long execution = (end - start);
                System.out.println("Execution time : "+execution + " nanoseconds");
                return false;
            }
            if(location.equals(currentLocation)){
                Drop drop = new Drop();
                drop.dropQuery(query);
            }
            else{
                dataOutputStream.writeUTF("DROP");
                dataOutputStream.flush();
                dataOutputStream.writeUTF(query);
                dataOutputStream.flush();
                String line = (String) dataInputStream.readUTF();
                if(line.equals("BEGIN")) {
                    boolean result = dataInputStream.readBoolean();
                    String deletedTable = dataInputStream.readUTF();
                    if(result){
                        deleteFromDistributedGDD(deletedTable);
                    }
                    while (true) {
                        line = (String) dataInputStream.readUTF();
                        if(line.equals("END")){
                            break;
                        }
                        System.out.println(line);
                    }
                }
                else{
                    System.out.println("No response from host.");
                    long end = System.nanoTime();
                    long execution = (end - start);
                    System.out.println("Execution time : "+execution + " nanoseconds");
                    return false;
                }
            }
            long end = System.nanoTime();
            long execution = (end - start);
            System.out.println("Execution time : "+execution + " nanoseconds");
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            long end = System.nanoTime();
            long execution = (end - start);
            System.out.println("Execution time : "+execution + " nanoseconds");
            return false;
        }
    }
    
    public boolean selectFromTable(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String query, String[] queryWords){
    	long start = System.nanoTime();
    	Select select=new Select();
        try {
			Map<String,String> conditionsMap=select.getTableInfo(query);
			if(conditionsMap.containsKey("table")) {
				List<Map<String,Object>> tableList=new ArrayList<>();
				String dbTable = conditionsMap.get("table");
		        dbTable = dbTable.replace(".",DELIMITER);
		        String location = getLocation(dbTable);
		        if(location.equals("")){
	                System.out.println("Table does not exists.");
	                return false;
	            }
		        if(location.equals(currentLocation)){
		        	tableList=select.selectProcess(query);
		        	for(Map<String,Object> map:tableList) {
		        		System.out.println(map);
		        	}
		        	System.out.println(tableList.size()+" rows returned");
		        	long end = System.nanoTime();
					long execution = (end - start);
					System.out.println("Execution time : "+execution + " nanoseconds");
	            }
	            else{
	            	dataOutputStream.writeUTF("SELECT");
	                dataOutputStream.flush();
	                dataOutputStream.writeUTF(query);
	                dataOutputStream.flush();
	                String line = (String) dataInputStream.readUTF();
	                if(line.equals("BEGIN")) {
	                    while (true) {
	                        line = (String) dataInputStream.readUTF();
	                        if(line.equals("END")){
	                            break;
	                        }
	                        System.out.println(line);
	                    }
	                }
	                else{
	                    System.out.println("No response from host.");
	                    return false;
	                }
	            }
			}else {
				System.out.println("Synatx error");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
        return true;
    }
}
