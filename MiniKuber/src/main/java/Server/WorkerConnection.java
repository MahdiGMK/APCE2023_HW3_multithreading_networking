package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class WorkerConnection implements Comparable<WorkerConnection> {
    private final String name;
    private final int mxLoad;
    private final ArrayList<String> tasks = new ArrayList<>();
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private boolean enabled = true;

    public WorkerConnection(String name, int mxLoad, Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
        this.name = name;
        this.mxLoad = mxLoad;
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public boolean canLoad() {
        return enabled && tasks.size() < mxLoad;
    }

    public synchronized String createTask(String task) throws IOException {
        if (!enabled) return "fail: node is disabled";
        if (!canLoad()) return "fail: node cant add more load";
        if (tasks.contains(task)) return "fail: task exists";
        outputStream.writeUTF("create " + task);
        tasks.add(task);
        return "success";
    }

    public synchronized String deleteTask(String task) throws IOException {
        if (!enabled) return "fail: node is disabled";
        if (!task.contains(task)) return "fail: task doesnt exist";
        outputStream.writeUTF("delete " + task);
        tasks.remove(task);
        return "success";
    }

    public synchronized String enable() throws IOException {
        outputStream.writeUTF("enable");
        enabled = true;
        return "success";
    }

    public synchronized String disable() throws IOException {
        outputStream.writeUTF("disable");
        tasks.clear();
        enabled = false;
        return "success";
    }

    @Override
    public int compareTo(WorkerConnection workerConnection) {
        if (workerConnection.canLoad() ^ !canLoad())
            return Float.compare(1.0f * tasks.size() / mxLoad,
                    1.0f * workerConnection.tasks.size() / workerConnection.mxLoad);
        else
            return Boolean.compare(!canLoad(), !workerConnection.canLoad());
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public int getMxLoad() {
        return mxLoad;
    }
}
