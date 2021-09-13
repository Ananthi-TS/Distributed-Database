package com.ddbms.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

public class ERD {

	private static String DELIMITER = "~~~";
	private static String localRootDirectory;
	private static String currentLocation ;
	private static String erdLocation="";
	private static  String database="";
	static Document doc = null;
	
	private void readProperties(){
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("db.properties"));
			localRootDirectory = properties.getProperty("local_root")+"\\"+database;
			File dir = new File("ERD");
			erdLocation=dir.getAbsolutePath();
			currentLocation = properties.getProperty("current_location");
			String file	= "ERD\\"+database+".pdf";
			new PdfWriter(file).close();
			PdfDocument pdfDoc= new PdfDocument(new PdfWriter(file));
			Document doc = new Document(pdfDoc);
			ERD.doc=doc;
		} catch (IOException e) {

		}
	}

	public boolean getERD(String database) throws Exception {
		ERD.database=database;
		readProperties();
		File dir = new File(localRootDirectory);
		if(dir.exists()) {
			createERD();
			System.out.println("ERD created succesfully at path:"+erdLocation+"\\"+database+".pdf");
			return true;
		}else {
			throw new Exception("Database doesn't exist");
		}
	}

	public  void createERD() throws Exception {
		File dir = new File(localRootDirectory);
		ERD.doc.add(new Paragraph("------------------------------------ENTITY RELATIONSHIP DIAGRAM------------------------------------\n"));
		ERD.doc.add(new Paragraph("\n"));
		ERD.doc.add(new Paragraph("Database - "+database));
		doc.add(new Paragraph("\n"));
		showFiles(dir.listFiles());

		doc.close();
	}

	public  void showFiles(File[] files) throws Exception {
		for (File file : files) {
			if (file.isDirectory()) {
				showFiles(file.listFiles());
			} else {
				if(file.getName().equals("metadata.txt")) {
					String parentDirName = file.getParent();
					parentDirName=parentDirName.substring(parentDirName.lastIndexOf("\\")+1,parentDirName.length());
					doc.add(new Paragraph("Table - "+parentDirName));
					updateERD(file.getAbsolutePath());
				}
			}
		}
	}

	private  void updateERD(String absolutePath) throws Exception {

		List<Column> list=new ArrayList<>();

		Scanner scannerObj = new Scanner(new File(absolutePath));
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
			list.add(columnObject);
		}


		writePDF(list);
	}

	private  void writePDF(List<Column> list) throws FileNotFoundException, IOException {


		Table table = new Table(7);
		table.addCell(new Cell().add(new Paragraph("Column Name")));
		table.addCell(new Cell().add(new Paragraph("Column Type")));
		table.addCell(new Cell().add(new Paragraph("Column size")));
		table.addCell(new Cell().add(new Paragraph("Primary key")));
		table.addCell(new Cell().add(new Paragraph("Foreign key")));
		table.addCell(new Cell().add(new Paragraph("Referenced table")));
		table.addCell(new Cell().add(new Paragraph("Referenced Column")));

		for(Column temp:list) {


			table.addCell(new Cell().add(new Paragraph(temp.getColumnName())));
			table.addCell(new Cell().add(new Paragraph(temp.getColumnType())));
			table.addCell(new Cell().add(new Paragraph(temp.getColumnSize()+"")));
			if(temp.isPrimaryKey()) {
				table.addCell(new Cell().add(new Paragraph("Yes")));
			}else {
				table.addCell(new Cell().add(new Paragraph("")));
			}

			if(temp.isForeignKey()) {
				table.addCell(new Cell().add(new Paragraph("Yes")));
				table.addCell(new Cell().add(new Paragraph(temp.getForeignKeyTable()+"")));
				table.addCell(new Cell().add(new Paragraph(temp.getForeignKeyColumn())));
			}
			else {

				table.addCell(new Cell().add(new Paragraph("")));
				table.addCell(new Cell().add(new Paragraph("")));
				table.addCell(new Cell().add(new Paragraph("")));
			}
		}
		doc.add(table);
		doc.add(new Paragraph("\n"));
	}

}
