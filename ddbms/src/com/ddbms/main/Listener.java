package com.ddbms.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Listener {
    private static String host;
    private static int port;

    public Listener(){
        loadProperties();
    }

    private static void loadProperties(){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            host=properties.getProperty("host");
            port=Integer.parseInt(properties.getProperty("listener_port"));
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        try{
            boolean result = true;
            System.out.println("Listener is running.");
            System.out.println("Listening for instruction.");

            loadProperties();
            System.out.println(host);
            System.out.println(port);
            Socket socket=new Socket(host,port);
            ListenerUtil listenerUtil = new ListenerUtil();
            DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());

            while(true) {
                String str = (String) dataInputStream.readUTF();
                String query = "";
                System.out.println("Received "+str);
                if(str.equals("EXIT")){
                    break;
                }
                switch (str){
                    case "UPDATE_GDD":
                        result = listenerUtil.updateDistributedGdd(dataOutputStream);
                        break;
                    case "INSERT":
                        query = (String) dataInputStream.readUTF();
                        result = listenerUtil.insertOperation(dataOutputStream, query);
                        break;
                    case "DROP":
                        query = (String) dataInputStream.readUTF();
                        result = listenerUtil.dropOperation(dataOutputStream, query);
                        break;
                    case "SELECT":
                   	 String selectQuery = (String) dataInputStream.readUTF();
                   	 result = listenerUtil.selectOperation(dataOutputStream, selectQuery);
                   	 break;
                    case "UPDATE":
                        String updateQuery = (String) dataInputStream.readUTF();
                        result =  listenerUtil.updateOperation(dataOutputStream, updateQuery);
                        break;
                    default:
                        break;
                }
            }
            socket.close();
        }catch(Exception e){System.out.println(e);}
    }
}
