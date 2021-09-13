import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ServerUtil {
    public boolean updateDistributedGdd(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            clientDataOutputStream.writeUTF(str);
            clientDataOutputStream.flush();
            String response = (String) clientDataInputStream.readUTF();
            System.out.println(response);
            if(response.equals("BEGIN")) {
                serverDataOutputStream.writeUTF(response);
                serverDataOutputStream.flush();
                while (true) {
                    response = (String) clientDataInputStream.readUTF();
                    System.out.println(response);
                    serverDataOutputStream.writeUTF(response);
                    serverDataOutputStream.flush();
                    if (response.equals("END")) {
                        System.out.println("GDD update complete.");
                        break;
                    }
                }
            }
            else{
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertOperation(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            clientDataOutputStream.writeUTF(str);
            clientDataOutputStream.flush();

            String query = (String) serverDataInputStream.readUTF();
            clientDataOutputStream.writeUTF(query);
            clientDataOutputStream.flush();

            String response = (String) clientDataInputStream.readUTF();
            System.out.println(response);

            if(response.equals("BEGIN")) {
                serverDataOutputStream.writeUTF(response);
                serverDataOutputStream.flush();
                while (true) {
                    response = (String) clientDataInputStream.readUTF();
                    System.out.println(response);
                    serverDataOutputStream.writeUTF(response);
                    serverDataOutputStream.flush();
                    if (response.equals("END")) {
                        System.out.println("INSERT operation complete");
                        break;
                    }
                }
            }
            else{
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOperation(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            clientDataOutputStream.writeUTF(str);
            clientDataOutputStream.flush();

            String query = (String) serverDataInputStream.readUTF();
            clientDataOutputStream.writeUTF(query);
            clientDataOutputStream.flush();

            String response = (String) clientDataInputStream.readUTF();
            System.out.println(response);

            if(response.equals("BEGIN")) {
                serverDataOutputStream.writeUTF(response);
                serverDataOutputStream.flush();
                while (true) {
                    response = (String) clientDataInputStream.readUTF();
                    System.out.println(response);
                    serverDataOutputStream.writeUTF(response);
                    serverDataOutputStream.flush();
                    if (response.equals("END")) {
                        System.out.println("UPDATE operation complete");
                        break;
                    }
                }
            }
            else{
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean selectOperation(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            clientDataOutputStream.writeUTF(str);
            clientDataOutputStream.flush();

            String query = (String) serverDataInputStream.readUTF();
            clientDataOutputStream.writeUTF(query);
            clientDataOutputStream.flush();

            String response = (String) clientDataInputStream.readUTF();
            System.out.println(response);
            if(response.equals("BEGIN")) {
                serverDataOutputStream.writeUTF(response);
                serverDataOutputStream.flush();
                while (true) {
                    response = (String) clientDataInputStream.readUTF();
                    System.out.println(response);
                    serverDataOutputStream.writeUTF(response);
                    serverDataOutputStream.flush();
                    if (response.equals("END")) {
                        System.out.println("INSERT operation complete");
                        break;
                    }
                }
            }
            else{
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean dropOperation(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            clientDataOutputStream.writeUTF(str);
            clientDataOutputStream.flush();

            String query = (String) serverDataInputStream.readUTF();
            clientDataOutputStream.writeUTF(query);
            clientDataOutputStream.flush();

            String response = (String) clientDataInputStream.readUTF();
            System.out.println(response);
            if(response.equals("BEGIN")) {
                serverDataOutputStream.writeUTF(response);
                serverDataOutputStream.flush();

                boolean result = clientDataInputStream.readBoolean();
                serverDataOutputStream.writeBoolean(result);
                serverDataOutputStream.flush();

                while (true) {
                    response = (String) clientDataInputStream.readUTF();
                    System.out.println(response);
                    serverDataOutputStream.writeUTF(response);
                    serverDataOutputStream.flush();
                    if (response.equals("END")) {
                        System.out.println("INSERT operation complete");
                        break;
                    }
                }
            }
            else{
                serverDataOutputStream.writeUTF("");
                serverDataOutputStream.flush();
                return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean invalidOperation(DataOutputStream serverDataOutputStream, DataInputStream serverDataInputStream, DataOutputStream clientDataOutputStream, DataInputStream clientDataInputStream, String str){
        try {
            serverDataOutputStream.writeUTF("BEGIN");
            serverDataOutputStream.flush();
            serverDataOutputStream.writeUTF("Invalid Request");
            serverDataOutputStream.flush();
            serverDataOutputStream.writeUTF("END");
            serverDataOutputStream.flush();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
