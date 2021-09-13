package com.ddbms.main;

import com.ddbms.sql.Drop;
import com.ddbms.sql.Insert;
import com.ddbms.sql.Select;
import com.ddbms.sql.Update;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class ListenerUtil {
    private static String DELIMITER = "~~~";
    private static String localRootDirectory;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {

        }
    }

    public boolean updateDistributedGdd(DataOutputStream dataOutputStream){
        try {
            String gddPath = localRootDirectory+"\\gdd\\metadata.txt";
            File fileObj = new File(gddPath);
            Scanner fileScanner = new Scanner(fileObj);
            dataOutputStream.writeUTF("BEGIN");
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                dataOutputStream.writeUTF(line);
                dataOutputStream.flush();
            }
            fileScanner.close();
            dataOutputStream.writeUTF("END");
            dataOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean insertOperation(DataOutputStream dataOutputStream,String query){
        try {
            Insert insert = new Insert();
            boolean result = insert.insertQuery(query);
            String response = insert.getMessage();
            dataOutputStream.writeUTF("BEGIN");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(response);
            dataOutputStream.flush();
            dataOutputStream.writeUTF("END");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean dropOperation(DataOutputStream dataOutputStream,String query){
        try {
            Drop drop = new Drop();
            boolean result = drop.dropQuery(query);
            String response = drop.getMessage();
            dataOutputStream.writeUTF("BEGIN");
            dataOutputStream.flush();
            dataOutputStream.writeBoolean(result);
            dataOutputStream.flush();
            dataOutputStream.writeUTF(drop.getDroppedTable());
            dataOutputStream.flush();
            dataOutputStream.writeUTF(response);
            dataOutputStream.flush();
            dataOutputStream.writeUTF("END");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean selectOperation(DataOutputStream dataOutputStream,String query) {
    	long start = System.nanoTime();
    	List<Map<String,Object>> tableValues=new ArrayList<>();
    	try {
			Select select=new Select();
			tableValues=select.selectProcess(query);
			dataOutputStream.writeUTF("BEGIN");
            dataOutputStream.flush();
            for(Map<String,Object> map:tableValues) {
            	dataOutputStream.writeUTF(map.toString());
                dataOutputStream.flush();
            }
            dataOutputStream.writeUTF(tableValues.size()+" rows returned");
            dataOutputStream.flush();
            long end = System.nanoTime();
			long execution = (end - start);
            dataOutputStream.writeUTF("Execution time : "+execution + " nanoseconds");
            dataOutputStream.flush();
            dataOutputStream.writeUTF("END");
            dataOutputStream.flush();
		} catch (Exception e) {
			
            try {
            	dataOutputStream.writeUTF(e.getMessage());
				dataOutputStream.flush();
            	dataOutputStream.writeUTF("END");
				dataOutputStream.flush();
			} catch (IOException e1) {
			}
			return false;
		}
    	
		return false;
    }
    public boolean updateOperation(DataOutputStream dataOutputStream, String updateQuery) {
        try {
            Update update = new Update();
            update.toUpdateTable(updateQuery);
            String response = update.getMessage();
            System.out.println(response);
            dataOutputStream.writeUTF("BEGIN");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(response);
            dataOutputStream.flush();
            dataOutputStream.writeUTF("END");
            dataOutputStream.flush();
        } catch (Exception e) {
            try {
                dataOutputStream.writeUTF("BEGIN");
                dataOutputStream.flush();
                dataOutputStream.writeUTF(e.getMessage());
                dataOutputStream.flush();
                dataOutputStream.writeUTF("END");
                dataOutputStream.flush();
            } catch (IOException e1) {
            }
            return false;
        }
        return true;
    }

}
