package com.ddbms.main;

import java.io.*;
import java.util.Properties;

public class EventLogs {
    private static String localRootDirectory;

    public EventLogs() {
        readProperties();
    }

    private void readProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            localRootDirectory=properties.getProperty("local_root");
        } catch (IOException e) {

        }
    }

    public void addEventLog(String log) throws IOException {
        File file = new File(localRootDirectory+"\\logs\\eventLogs.txt");
        FileWriter fileWriter = new FileWriter(file,true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        printWriter.println(log);
        printWriter.close();
        bufferedWriter.close();
        fileWriter.close();
    }
}
