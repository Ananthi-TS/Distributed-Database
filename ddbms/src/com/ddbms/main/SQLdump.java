package com.ddbms.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.Scanner;

import com.itextpdf.layout.element.Paragraph;

public class SQLdump {
	private static String localRootDirectory;
	private static String dumpLocation="SQL_DUMP";
	private static String databaseName;
	static{
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("db.properties"));
			localRootDirectory = properties.getProperty("local_root");

		} catch (IOException e) {

		}
	}

	public boolean createSQLDump(String query,String databasename,String tableName) {

		String filePath=localRootDirectory+"\\"+databasename+"\\"+tableName;
		File fileObject = new File(filePath);
		if(fileObject.exists()) {
			String tableDump=filePath+"\\dump.txt";
			File fileObj=new File(tableDump);
			try {
				if(fileObj.createNewFile()) {
					Files.write(Paths.get(tableDump), query.getBytes(), StandardOpenOption.APPEND);
					return true;
				}
			} catch (IOException e) {
				System.out.println("error while creating sql dump");
			}
		}
		return false;
	}

	public boolean generateSQLDump(String databaseName) throws Exception {

		String filePath=localRootDirectory+"\\"+databaseName;
		File dir = new File(filePath);
		if(dir.exists()) {
			SQLdump.databaseName=databaseName;

			File dumpFile = new File(dumpLocation+"\\"+databaseName+".sql");
			if(dumpFile.exists()) {
				new PrintWriter(dumpFile).close();
			}else {
				dumpFile.createNewFile();
			}

			showFiles(dir.listFiles());
			String location=new File(dumpLocation).getAbsolutePath()+"\\"+databaseName+".sql";
			System.out.println("SQL dump file created successfully at "+location);
			return true;
		}else {
			throw new Exception("Database doesn't exist");
		}

	}

	public  void showFiles(File[] files) throws Exception {
		for (File file : files) {
			if (file.isDirectory()) {
				showFiles(file.listFiles());
			} else {
				if(file.getName().equals("dump.txt")) {
					Scanner scannerObj = new Scanner(new File(file.getAbsolutePath()));
					String path=dumpLocation+"\\"+databaseName+".sql";
					while(scannerObj.hasNext()) {
						String line = scannerObj.nextLine()+";\n";
						Files.write(Paths.get(path), line.getBytes(), StandardOpenOption.APPEND);
					}
				}
			}
		}
	}
}
