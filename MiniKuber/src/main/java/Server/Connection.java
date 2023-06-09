package Server;

import Model.ClientType;
import Model.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Connection extends Thread {
    static int lastWorkerID;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Scheduler scheduler;
    private WorkerConnection workerConnection;

    public Connection(Socket socket, Scheduler scheduler) throws IOException {
        this.scheduler = scheduler;
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public synchronized void run() {
        super.run();
        ClientType clientType = ClientType.UNKNOWN;
        try {
            System.out.println("Connecting to " + socket.getInetAddress() + ":" + socket.getPort() + " ...");

            while ((clientType = getIntro()) == ClientType.UNKNOWN) ;

            if (clientType == ClientType.WORKER) handleWorker();
            else handleClient();

        } catch (Exception e) {
            System.out.println("Connection " + socket.getInetAddress() + ":" + socket.getPort() + " lost!");

            if (clientType == ClientType.UNKNOWN) return;

            if (clientType == ClientType.WORKER) {
                try {
                    removeWorker();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else removeClient();
        }
    }

    private void handleWorker() throws InterruptedException, IOException {
        int mxLoad = inputStream.readInt();
        workerConnection = new WorkerConnection("worker" + lastWorkerID++, mxLoad, socket, inputStream, outputStream);
        scheduler.addWorker(workerConnection);
        while (true) {
            Thread.sleep(1000);
            outputStream.writeUTF("alive");
            String status = inputStream.readUTF();
            if (!status.equals("alive")) break;
        }
    }

    private synchronized ClientType getIntro() throws IOException {
        String intro = inputStream.readUTF();
        switch (intro) {
            case "client":
                return ClientType.CLIENT;
            case "worker":
                return ClientType.WORKER;
        }
        return ClientType.UNKNOWN;
    }

    private synchronized void handleClient() throws IOException {
        while (true) {
            String data = inputStream.readUTF();
            Request request = Request.fromJson(data);
            String name = request.getData().containsKey("name") ? request.getData().get("name").get(0) : null;
            String node = request.getData().containsKey("node") ? request.getData().get("node").get(0) : null;
            switch (request.getCommand()) {
                case CREATE_TASK:
                    if (node == null) createTask(name);
                    else createTask(name, scheduler.getWorkerByName(node));
                    break;
                case DELETE_TASK:
                    deleteTask(name);
                    break;
                case ENABLE_NODE:
                    enableNode(scheduler.getWorkerByName(name));
                    break;
                case DISABLE_NODE:
                    disableNode(scheduler.getWorkerByName(name));
                    break;
                case GET_NODES:
                    getNodes();
                    break;
                case GET_TASKS:
                    getTasks();
                    break;
            }
        }
    }

    private synchronized void getTasks() throws IOException {
        outputStream.writeUTF(scheduler.getTasks());
    }

    private synchronized void getNodes() throws IOException {
        outputStream.writeUTF(scheduler.getNodes());
    }

    private synchronized void disableNode(WorkerConnection node) throws IOException {
        if (node == null) {
            outputStream.writeUTF("fail: invalid node");
            return;
        }
        scheduler.disableWorker(node);
        outputStream.writeUTF("success");
    }

    private synchronized void enableNode(WorkerConnection node) throws IOException {
        if (node == null) {
            outputStream.writeUTF("fail: invalid node");
            return;
        }
        scheduler.enableWorker(node);
        outputStream.writeUTF("success");
    }

    private synchronized void createTask(String taskName, WorkerConnection node) throws IOException {
        outputStream.writeUTF(scheduler.schedule(taskName, node));
    }

    private synchronized void createTask(String taskName) throws IOException {
        outputStream.writeUTF(scheduler.schedule(taskName));
    }

    private synchronized void deleteTask(String taskName) throws IOException {
        outputStream.writeUTF(scheduler.delete(taskName));
    }

    private synchronized void removeWorker() throws IOException {
        scheduler.removeWorker(workerConnection);
    }

    private synchronized void removeClient() {

    }
}
