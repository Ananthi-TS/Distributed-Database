package com.ddbms.sql;

import java.util.HashSet;
import java.util.Set;

import com.ddbms.main.Table;

public class Main {
	
	public static void main(String a[]) throws Exception {
		Table table=new Table("Persons","testDatabase");
		table.loadColumns();
		System.out.println(table.getColumns());
		table.loadTableValues();
		System.out.println(table.getTableValues());
		Set<String> set=new HashSet<>();
		set.add("PersonID");
		set.add("City");
		table.loadTableValues(set);
		System.out.println(table.getTableValues());
	}

}
