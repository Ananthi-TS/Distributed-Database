package com.ddbms.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import com.ddbms.encryption.AesCipher;

public class User {
	
	String username="";
	String password="";
	private static String localRootDirectory;
	private static final String DELIMITER = "~~~";
	
	private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {

        }
    }
	
	public User(String username,String password){
		this.username=username;
		this.password=password;
		readProperties();
	}
	
	public boolean vaildUser() {
		
		String tablePath = localRootDirectory+"\\user.txt";
		
        try {
			File tableFile = new File(tablePath);
			if(tableFile.exists()){
			    Scanner fileScanner = new Scanner(tableFile);
			    while (fileScanner.hasNextLine()) {
			        String line = fileScanner.nextLine();
			        String[] data = line.split(DELIMITER);
			        if((data!=null) && (data.length==2)) {
			        	String user=data[0];
			        	String pwd=AesCipher.decrypt(data[1]);
			        	if( (username.endsWith(user)) && (password.equals(pwd)) ) {
			        		return true;
			        	}
			        }
			    } 
			}
		} catch (Exception e) {
			return false;
		}
		return false;
		
	}

}
