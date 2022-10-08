import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println("\n" + response);
                receiveMsg();

                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }

    public void receiveMsg() {
        String receivedMsg;
        while (socket.isConnected()) {
            try {
                receivedMsg = reader.readLine();
                if (receivedMsg.split(" ")[1].equals("/upload")) {
                    downloadFile(receivedMsg);
                }else {
                    System.out.println(receivedMsg);
                }
            } catch (IOException e) {

            }
        }
    }

    private void downloadFile(String receivedMsg) {

        String[] commandParts = receivedMsg.split(" ");
        String fileName = commandParts[2];
        String fileBytes = String.join(" ", Arrays.copyOfRange(commandParts, 3, commandParts.length));
        String downloadPath = System.getProperty("user.dir") + File.separator + "download" + File.separator + fileName;
        System.out.println("Downloading " + downloadPath + "...");
// check if download folder exists and create if not
        File downloadFolder = new File(System.getProperty("user.dir") + File.separator + "download");
        if (!downloadFolder.exists()) downloadFolder.mkdir();

// convert filesBytes to bytes array and then save to file in download folder
        File file = new File(downloadPath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Could not create file " + downloadPath);
            return;
        }
        byte[] fileBytesArray = convertStringToByteArray(fileBytes);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(fileBytesArray);
            fileOutputStream.close();
            System.out.println("Downloaded " + downloadPath + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] convertStringToByteArray(String fileBytes) {
        String[] fileBytesStringArray = fileBytes.substring(1, fileBytes.length() - 1).split(", ");
        byte[] fileBytesArray = new byte[fileBytesStringArray.length];
        for (int i = 0; i < fileBytesStringArray.length; i++) {
            fileBytesArray[i] = Byte.parseByte(fileBytesStringArray[i]);
        }
        return fileBytesArray;
    }


}