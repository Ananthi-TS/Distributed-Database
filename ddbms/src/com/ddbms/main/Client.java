package com.ddbms.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private static String host;
    private static int port;

    public Client(){
        loadProperties();
    }

    private static void loadProperties(){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            host=properties.getProperty("host");
            port=Integer.parseInt(properties.getProperty("client_port"));
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        try{
            loadProperties();
            Socket socket=new Socket(host,port);
            DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            ClientUtil clientUtil = new ClientUtil();

            System.out.println("Client is running.");
            System.out.println("Attempting to update the GDD. Hope for the best.");

            System.out.println("Enter username: ");
            String username=scanner.nextLine();
            System.out.println("Enter password: ");

            String password=scanner.nextLine();
            User user=new User(username,password);

            if(user.vaildUser()) {
				System.out.println("Connecting to server..");
	            dataOutputStream.writeUTF("UPDATE_GDD");
	            dataOutputStream.flush();
	            clientUtil.updateDistributedGdd(dataInputStream);
				System.out.println("Connected");
	            String query = "";
	            while(true){
	                query = scanner.nextLine();
	                if(query.equalsIgnoreCase("exit")){
	                    System.out.println("Logging you out.");
	                    break;
	                }
	                String[] queryWords = query.split("\\s+");
	                String queryType = queryWords[0];
	                switch (queryType.toUpperCase()){
	                    case "CREATE":
	                        clientUtil.createTable(dataOutputStream, dataInputStream,query, queryWords);
	                        break;
	                    case "INSERT":
	                        clientUtil.insertIntoTable(dataOutputStream, dataInputStream, query, queryWords);
	                        break;
						case "UPDATE":
							clientUtil.updateInTable(dataOutputStream, dataInputStream, query,queryWords);
							break;
						case "DROP":
	                        clientUtil.dropTable(dataOutputStream, dataInputStream, query, queryWords);
	                        break;
	                    case "SELECT":
	                        clientUtil.selectFromTable(dataOutputStream, dataInputStream, query, queryWords);
	                        break;    
	                    default:
	                        System.out.println("Invalid query");
	                        break;
	                }
	
	            }
	            dataOutputStream.writeUTF("EXIT");
	            dataOutputStream.flush();
	            socket.close();
            } else {
            	System.out.println("Authentication failed");
            }
        }catch(Exception e){System.out.println(e);}
    }
}
