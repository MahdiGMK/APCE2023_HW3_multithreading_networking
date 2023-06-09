package Worker;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Worker {
    private final Socket socket;
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;
    private final ArrayList<String> tasks = new ArrayList<>();
    private boolean enabled = true;

    Worker(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());
        System.out.print("input max load : ");
        int mxLoad = 0;
        while (true) {
            mxLoad = new Scanner(System.in).nextInt();
            if (mxLoad > 0) break;
            System.out.print("enter a valid load : ");
        }
        outputStream.writeUTF("worker");
        outputStream.writeInt(mxLoad);

        while (true) {
            String data = inputStream.readUTF();
            System.out.println(data);

            if (data.startsWith("create ")) {
                String taskName = data.substring(7);
                createTask(taskName);
            } else if (data.startsWith("delete ")) {
                String taskName = data.substring(7);
                deleteTask(taskName);
            } else switch (data) {
                case "disable":
                    enabled = false;
                    tasks.clear();
                    break;
                case "enable":
                    enabled = true;
                    break;
                case "alive":
                    outputStream.writeUTF("alive");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Worker("localhost", 8080);
        } catch (IOException e) {
            System.out.printf("Connection to %s:%d failed", "localhost", 8080);
        }
    }

    private void deleteTask(String taskName) {
        if (!enabled) return;
        if (!tasks.contains(taskName)) return;
        tasks.remove(taskName);
    }

    private void createTask(String taskName) {
        if (!enabled) return;
        if (tasks.contains(taskName)) return;
        tasks.add(taskName);
        return;
    }
}
