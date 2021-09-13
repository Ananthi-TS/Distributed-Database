import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            int port1 = Integer.parseInt(args[0]);
            int port2 = Integer.parseInt(args[1]);
            System.out.println("Server is running.");
            System.out.println("Waiting for instructions.");
            ServerSocket serverSocket = new ServerSocket(port1);
            Socket sSocket = serverSocket.accept();
            System.out.println("Client connected");
            DataOutputStream serverDataOutputStream = new DataOutputStream(sSocket.getOutputStream());
            DataInputStream serverDataInputStream = new DataInputStream(sSocket.getInputStream());
            String str = (String) serverDataInputStream.readUTF();
            System.out.println("Received "+str);
            ServerSocket clientSocket = new ServerSocket(port2);
            Socket cSocket = clientSocket.accept();
            System.out.println("Listener connected");
            DataOutputStream clientDataOutputStream = new DataOutputStream(cSocket.getOutputStream());
            DataInputStream clientDataInputStream = new DataInputStream(cSocket.getInputStream());
            ServerUtil serverUtil = new ServerUtil();

            while(true) {
                if(str.equals("EXIT")){
                    clientDataOutputStream.writeUTF("EXIT");
                    clientDataOutputStream.flush();
                    break;
                }
                switch (str){
                    case "UPDATE_GDD":
                        serverUtil.updateDistributedGdd(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                    case "INSERT":
                        serverUtil.insertOperation(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                    case "DROP":
                        serverUtil.dropOperation(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                    case "UPDATE":
                        serverUtil.updateOperation(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                    case "SELECT":
                        serverUtil.selectOperation(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                    default:
                        serverUtil.invalidOperation(serverDataOutputStream, serverDataInputStream, clientDataOutputStream, clientDataInputStream, str);
                        break;
                }
                str = (String) serverDataInputStream.readUTF();
                System.out.println("Received "+str);
            }

            clientSocket.close();
            serverSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
