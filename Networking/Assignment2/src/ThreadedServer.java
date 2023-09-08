import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ThreadedServer {

  public static final int SERVER_PORT = 8080;

  public static void main(String... args) throws IOException, IOException {
    System.out.println("Threaded echo server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
    ArrayList<LocalDateTime> evalRecords = new ArrayList<>();
    ArrayList<LocalDateTime> gettimeRecords = new ArrayList<>();
    Queue<String> expressions = new LinkedList<>();
    try {
      System.out.println("Start to accept incoming connections");
      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(
            new Handler(clientSocket, evalRecords, gettimeRecords, expressions)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }
}
