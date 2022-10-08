import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This thread is responsible for reading user's input and send it
 * to the server.
 * It runs in an infinite loop until the user types 'bye' to quit.
 *
 * @author www.codejava.net
 */
public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {

        Console console = System.console();

        String userName = console.readLine("\nEnter your name: ");
        client.setUserName(userName);
        writer.println(userName);

        String text;

        do {
            text = console.readLine("[" + userName + "]: ");


            if (text.startsWith("/upload")) {
                uploadFile(text);
            }
            else if (text.startsWith("/list")){
                listFilesInCurrentDirectory(text);
            }
            else {
                writer.println(text);
            }

        } while (!text.equals("bye"));

        try {
            socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }

    private void listFilesInCurrentDirectory(String command) {
        String[] commandParts = command.split(" ");
        String directoryPath = commandParts.length > 1 ? commandParts[1] : System.getProperty("user.dir");
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            System.out.println("Directory " + directoryPath + " does not exist!");
            return;
        }
        if (!directory.isDirectory()) {
            System.out.println(directoryPath + " is not a directory!");
            return;
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    private void uploadFile(String command) {
        String[] commandParts = command.split(" ");
        String filePath = commandParts[1];
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File" + filePath + " does not exist!");
            return;
        }

//         convert the whole file to bytes and then send it
        try {
            byte[] fileBytes = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileBytes);

            bufferedWriter.write("/upload " + file.getName() + " " + Arrays.toString(fileBytes));
            bufferedWriter.newLine();
            bufferedWriter.flush();
            fileInputStream.close();
            System.out.println("File " + file.getName() + " sent successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}