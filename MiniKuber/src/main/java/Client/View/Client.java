package Client.View;

import Client.View.Command.*;
import Model.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class Client {
    public Client(String host, int port) {
        Scanner scanner = new Scanner(System.in);
        try {
            Socket socket = new Socket(host, port);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("client");

            Thread resultThread = new Thread(() -> {
                try {
                    while (true) {
                        System.out.println(inputStream.readUTF());
                    }
                } catch (IOException ignored) {

                }
            });
            resultThread.setDaemon(true);
            resultThread.start();

            while (true) {
                String line = scanner.nextLine();
                TreeMap<String, ArrayList<String>> map = null;
                Request request = null;
                if ((map = CommandHandler.matches(Command.CREATE_TASK, line)) != null)
                    request = new Request(Command.CREATE_TASK, map);
                else if ((map = CommandHandler.matches(Command.DELETE_TASK, line)) != null)
                    request = new Request(Command.DELETE_TASK, map);
                else if ((map = CommandHandler.matches(Command.ENABLE_NODE, line)) != null)
                    request = new Request(Command.ENABLE_NODE, map);
                else if ((map = CommandHandler.matches(Command.DISABLE_NODE, line)) != null)
                    request = new Request(Command.DISABLE_NODE, map);
                else if ((map = CommandHandler.matches(Command.GET_NODES, line)) != null)
                    request = new Request(Command.GET_NODES, map);
                else if ((map = CommandHandler.matches(Command.GET_TASKS, line)) != null)
                    request = new Request(Command.GET_TASKS, map);
                else System.out.println("Invalid command");


                if (request != null) {
                    String data = request.toJson();
                    outputStream.writeUTF(data);
                }
            }
        } catch (IOException e) {
            System.out.printf("Connection to %s:%d failed\n", host, port);
        }
    }
}