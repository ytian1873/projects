import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {

    public static void main(String... args) throws IOException {
        System.out.println("Server Start");
        ServerSocket serverSocket = new ServerSocket(Utils.SERVER_PORT);
        serverSocket.setReceiveBufferSize(Utils.MAX_BYTES_PER_BUFFER);

        try {
            System.out.println("Start to accept incoming connections");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Handler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
