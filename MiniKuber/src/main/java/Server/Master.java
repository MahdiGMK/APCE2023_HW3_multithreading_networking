package Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Master {
    Master(int port) {
        System.out.println("Starting Server ... ");
        Scheduler scheduler = new Scheduler();
        scheduler.setDaemon(true);
        scheduler.start();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket, scheduler);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Master(8080);
    }
}
