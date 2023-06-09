package Model;

import Client.View.Command.Command;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Request {
    private final Command command;
    private final TreeMap<String, ArrayList<String>> data;

    public Request(Command command, TreeMap<String, ArrayList<String>> data) {
        this.command = command;
        this.data = data;
    }

    public static Request fromJson(String json) {
        return new Gson().fromJson(json, Request.class);
    }

    public Command getCommand() {
        return command;
    }

    public TreeMap<String, ArrayList<String>> getData() {
        return data;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
