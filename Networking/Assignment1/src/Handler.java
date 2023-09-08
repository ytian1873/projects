import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Handler implements Runnable {

    private Socket clientSocket;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
        System.out.println(String.format("Handle client %s", client));

        try {

            // These lines of the while loop valid for testing multiple scanner inputs, not valid for list of expressions
//
//            while (true) {
//                InputStream inStream = clientSocket.getInputStream();
//                OutputStream outStream = clientSocket.getOutputStream();
//
//                ArrayList<String> request = Utils.decodeMessage(inStream);
//                if (request == null || request.size() == 0) {
//                    break;
//                }
//                Utils.sendResults(outStream, request);
//            }


            // Following lines valid for testing a list of expressions, not valid to test multiple scanner inputs
            InputStream inStream = clientSocket.getInputStream();
            OutputStream outStream = clientSocket.getOutputStream();

            ArrayList<String> request = Utils.decodeMessage(inStream);
            Utils.sendResults(outStream, request);

            System.out.println("Client ended: " + client);
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
