package Server;

import Worker.Worker;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Scheduler extends Thread {
    private final List<WorkerConnection> workers = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<String, WorkerConnection> taskWorker = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> pendingTasks = new LinkedBlockingQueue<>();
    private final List<String> pendingTasksVis = Collections.synchronizedList(new ArrayList<>());

    public List<WorkerConnection> getWorkers() {
        return workers;
    }

    public ConcurrentHashMap<String, WorkerConnection> getTaskWorker() {
        return taskWorker;
    }

    public synchronized List<String> getPendingTasksVis() {
        return pendingTasksVis;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
                if (!pendingTasks.isEmpty()) {
                    String task = pendingTasks.poll();
                    pendingTasksVis.remove(task);
                    schedule(task);
                }
            } catch (IOException | InterruptedException ignored) {
            }
        }
    }

    public synchronized WorkerConnection getWorkerByName(String name) {
        for (WorkerConnection workerConnection : workers)
            if (workerConnection.getName().equals(name)) return workerConnection;
        return null;
    }

    public synchronized void addWorker(WorkerConnection workerConnection) {
        if (!workers.contains(workerConnection))
            workers.add(workerConnection);
    }

    public synchronized void removeWorker(WorkerConnection workerConnection) throws IOException {
        workers.remove(workerConnection);
        for (String task : workerConnection.getTasks()) {
            taskWorker.remove(task);
            pendingTasksVis.add(task);
            pendingTasks.add(task);
        }
    }

    public synchronized void enableWorker(WorkerConnection workerConnection) throws IOException {
        workerConnection.enable();
    }

    public synchronized void disableWorker(WorkerConnection workerConnection) throws IOException {
        workerConnection.setEnabled(false);
        for (String task : workerConnection.getTasks()) {
            taskWorker.remove(task);
            pendingTasksVis.add(task);
            pendingTasks.add(task);
        }
        workerConnection.disable();
    }

    public synchronized String schedule(String task) throws IOException {
        if (taskWorker.containsKey(task) || pendingTasksVis.contains(task)) return "fail: task exists";

        Optional<WorkerConnection> workerConnection = workers.stream().min(WorkerConnection::compareTo);
        if (!workerConnection.isPresent() || !workerConnection.get().canLoad()) {
            taskWorker.remove(task);
            pendingTasksVis.add(task);
            pendingTasks.add(task);
        } else {
            return schedule(task, workerConnection.get());
        }
        return "success";
    }

    public synchronized String schedule(String task, WorkerConnection workerConnection) throws IOException {
        if (taskWorker.containsKey(task) || pendingTasksVis.contains(task)) return "fail: task exists";

        if (workerConnection == null) return "fail: invalid worker";

        String response = workerConnection.createTask(task);
        if (!response.equals("success")) return response;
        taskWorker.put(task, workerConnection);
        workers.remove(workerConnection);
        workers.add(workerConnection);
        return "success";
    }

    public synchronized String delete(String task) throws IOException {
        if (pendingTasksVis.contains(task)) {
            pendingTasksVis.remove(task);
            pendingTasks.remove(task);
            return "success";
        }
        if (!taskWorker.containsKey(task)) return "fail: task doesnt exist";

        WorkerConnection workerConnection = taskWorker.get(task);
        String response = workerConnection.deleteTask(task);
        if (!response.equals("success")) return response;
        taskWorker.remove(task);
        workers.remove(workerConnection);
        workers.add(workerConnection);
        return "success";
    }

    public synchronized String getTasks() {
        StringBuilder builder = new StringBuilder();
        for (String task : pendingTasksVis)
            builder.append(task).
                    append(" (Pending)\n");
        for (WorkerConnection node : workers) {
            for (String task : node.getTasks())
                builder.append(task).append(" (Running on [").append(node.getName()).append("] )\n");
        }
        return builder.toString();
    }

    public synchronized String getNodes() {
        StringBuilder builder = new StringBuilder();
        for (WorkerConnection node : workers) {
            builder.append(node.getName());
            if (node.isEnabled())
                builder.append(" (Running)").append("  ").
                        append(node.getTasks().size()).append("/").append(node
                                .getMxLoad()).append("\n");
            else builder.append(" (Sleeping)\n");
        }
        return builder.toString();
    }
}
